package de.tudarmstadt.rxrefactoring.core.internal.execution;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodReference;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.utils.Log;

public class RandoopGenerator
{
    private static final String OMITMETHODS_FILE = "omitmethods.txt";
    private static final String CLASSLIST_FILE = "classlist.txt";

    private static final String HASHBANG = "#!/bin/bash";
    // @formatter:off
    private static final String[] COLORS = new String[]
    {
        "C_BLUE='\\033[1;34m'",
        "C_GREEN='\\033[1;32m'",
        "C_RED='\\033[1;31m'",
        "C_YELLOW='\\033[1;33m'",
        "C_NC='\\033[0m'"
    };
    // @formatter:on
    private static final String ECHO_RUN_RANDOOP = "echo -e \"${C_BLUE}==> ${C_YELLOW}Running randoop on pre-refactoring source code${C_NC}\"";
    private static final String COMMAND_RUN_RANDOOP = "java -classpath \"pre/bin:libs/*\" randoop.main.Main gentests --classlist=classlist.txt --omitmethods-file=omitmethods.txt --no-error-revealing-tests=true --junit-output-dir=tests/src --time-limit=10";
    private static final String IF_CHECK_RETURN_CODE = "if [ ! $? -eq 0 ]";
    private static final String THEN = "then";
    private static final String EXIT_ERROR = "    exit 1";
    private static final String FI = "fi";
    private static final String ECHO_JAVAC = "echo -e \"${C_BLUE}==> ${C_YELLOW}Compiling pre-refactoring source code${C_NC}\"";
    private static final String COMMAND_JAVAC = "javac -cp \"pre/bin:libs/*\" -d \"tests/bin\" tests/src/RegressionTest*.java";
    private static final String ECHO_JUNIT_PRE = "echo -e \"${C_BLUE}==> ${C_YELLOW}Running tests on pre-refactoring binaries${C_NC}\"";
    private static final String COMMAND_JUNIT_PRE = "java -cp \"tests/bin:pre/bin:libs/*\" org.junit.runner.JUnitCore RegressionTest";
    private static final String COMMAND_SAVE_RESULT_1 = "res1=$?";
    private static final String ECHO_JUNIT_POST = "echo -e \"${C_BLUE}==> ${C_YELLOW}Running tests on post-refactoring binaries${C_NC}\"";
    private static final String COMMAND_JUNIT_POST = "java -cp \"tests/bin:post/bin:libs/*\" org.junit.runner.JUnitCore RegressionTest";
    private static final String COMMAND_SAVE_RESULT_2 = "res2=$?";
    private static final String IF_CHECK_SAVED_RESULTS = "if [ ! $res1 -eq 0 ] || [ ! $res2 -eq 0 ]";
    private static final String ECHO_PRINT_ERROR = "    echo -e \"${C_BLUE}==> ${C_RED}Error: One or more tests failed! This refactoring may not be safe.${C_NC}\"";
    private static final String ELSE = "else";
    private static final String ECHO_ALL_OK = "    echo -e \"${C_BLUE}==> ${C_GREEN}All tests ran OK. This refactoring is probably safe.${C_NC}\"";

    public static void runRandoopGenerator(ProjectUnits units, IProject project)
    {
        IPackageFragmentRoot[] pkgs = new IPackageFragmentRoot[0];
        try
        {
            pkgs = JavaCore.create(project).getAllPackageFragmentRoots();
        }
        catch(JavaModelException e)
        {
            e.printStackTrace();
        }

        String projectPath = "";
        String localPath = "";
        if(pkgs.length >= 1)
        {
            projectPath = project.getLocation().toOSString();
            localPath = pkgs[0].getResource().getFullPath().removeFirstSegments(1).toOSString();
        }

        // Find all methods that will be refactored
        Set<String> changingMethods = new HashSet<>();
        for(IRewriteCompilationUnit unit : units)
        {
            if(unit.hasChanges())
            {
                CompilationUnit cu = (CompilationUnit)unit.getRoot();
                for(Object objType : cu.types())
                {
                    TypeDeclaration type = (TypeDeclaration)objType;
                    for(MethodDeclaration method : type.getMethods())
                    {
                        String className = type.resolveBinding().getBinaryName();
                        String methodName = method.getName().getFullyQualifiedName();
                        Log.info(JavaVisitor.class, "Looking for changes in method " + className + "." + methodName + "()");
                        List<ASTNode> nodes = new JavaVisitor(node -> nodeHasChanges(node, unit)).visitMethodDeclaration(method);

                        if(!nodes.isEmpty())
                        {
                            @SuppressWarnings("unchecked") List<SingleVariableDeclaration> objParams = method.parameters();
                            String params = objParams.stream().map(SingleVariableDeclaration::resolveBinding)
                                    .map(IVariableBinding::getType)
                                    .map(ITypeBinding::getBinaryName)
                                    .collect(Collectors.joining(", "));
                            changingMethods.add(buildSignature(className, methodName, params, method.getReturnType2().resolveBinding().getBinaryName()));
                        }
                    }
                }
            }
        }

        // Find all methods that call methods in changingMethods
        Set<String> callingMethods = new HashSet<>();
        JavaVisitor visitor = new JavaVisitor(node -> changingMethods.contains(buildSignatureFromNode(node)));
        for(IRewriteCompilationUnit unit : units)
        {
            CompilationUnit cu = (CompilationUnit)unit.getRoot();
            List<ASTNode> nodes = visitor.visitCompilationUnit(cu);
            callingMethods.addAll(nodes.stream()
                                       .map(RandoopGenerator::buildSignatureFromNode)
                                       .collect(Collectors.toList()));
        }

        // TODO Execute the refactoring now
        // TODO Once the refactoring is done, hook and check if we can still
        // find methods with signatures matching those in callingMethods
    }

    private static void createOutput(Set<String> methodsToOmit, Set<String> classesToTest, String projectPath, String localPath)
    {
        String time = Calendar.getInstance().getTime().toString().replace(' ', '_').replace(':', '_');
        File randoopTemp = new File(determineTempPath() + File.separator + "randoop-gen-" + time);
        randoopTemp.mkdirs();

        File preDir = new File(randoopTemp, "pre");
        File postDir = new File(randoopTemp, "post");

        // @formatter:off
        try
        {
            if(preDir.isDirectory())
            {
                Files.walk(Paths.get(preDir.getAbsolutePath()))
                     .sorted(Comparator.reverseOrder())
                     .map(Path::toFile)
                     .forEach(File::delete);
            }
            if(postDir.isDirectory())
            {
                Files.walk(Paths.get(postDir.getAbsolutePath()))
                     .sorted(Comparator.reverseOrder())
                     .map(Path::toFile)
                     .forEach(File::delete);
            }
        }
        catch(IOException e)
        {
            Log.error(JavaVisitor.class, "Failed to delete existing randoop temp directory.", e);
        }
        // @formatter:on

        preDir.mkdirs();
        postDir.mkdirs();

        // TODO We need the binaries, not the source code
        // Copy the source code over
        try
        {
            Files.walkFileTree(Paths.get(projectPath, localPath), new CopyVisitor(Paths.get(preDir.getAbsolutePath())));
        }
        catch(IOException e)
        {
            Log.error(JavaVisitor.class, "Failed to copy source folder into temp directory.", e);
        }

        // Create a shell file for running randoop
        File randoopSh = new File(randoopTemp, "randoop.sh");
        try
        {
            randoopSh.createNewFile();
            try(OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(randoopSh), StandardCharsets.UTF_8.newEncoder()); BufferedWriter out = new BufferedWriter(writer))
            {
                out.write(HASHBANG);
                out.newLine();
                out.newLine();

                for(String color : COLORS)
                {
                    out.write(color);
                    out.newLine();
                }
                out.newLine();

                out.write(ECHO_RUN_RANDOOP);
                out.newLine();
                out.write(COMMAND_RUN_RANDOOP);
                out.newLine();
                out.write(IF_CHECK_RETURN_CODE);
                out.newLine();
                out.write(THEN);
                out.newLine();
                out.write(EXIT_ERROR);
                out.newLine();
                out.write(FI);
                out.newLine();
                out.newLine();

                out.write(ECHO_JAVAC);
                out.newLine();
                out.write(COMMAND_JAVAC);
                out.newLine();
                out.write(IF_CHECK_RETURN_CODE);
                out.newLine();
                out.write(THEN);
                out.newLine();
                out.write(EXIT_ERROR);
                out.newLine();
                out.write(FI);
                out.newLine();
                out.newLine();

                out.write(ECHO_JUNIT_PRE);
                out.newLine();
                out.write(COMMAND_JUNIT_PRE);
                out.newLine();
                out.write(COMMAND_SAVE_RESULT_1);
                out.newLine();
                out.newLine();

                out.write(ECHO_JUNIT_POST);
                out.newLine();
                out.write(COMMAND_JUNIT_POST);
                out.newLine();
                out.write(COMMAND_SAVE_RESULT_2);
                out.newLine();
                out.newLine();

                out.write(IF_CHECK_SAVED_RESULTS);
                out.newLine();
                out.write(THEN);
                out.newLine();
                out.write(ECHO_PRINT_ERROR);
                out.newLine();
                out.write(ELSE);
                out.newLine();
                out.write(ECHO_ALL_OK);
                out.newLine();
                out.write(FI);
                out.newLine();
                out.newLine();
            }
        }
        catch(IOException e)
        {
            Log.error(RandoopGenerator.class, "Failed to create randoop.sh", e);
        }

        // Flag randoop.sh as executable
        try
        {
            Path randoopShPath = Paths.get(randoopSh.getAbsolutePath());
            Set<PosixFilePermission> perms = Files.getPosixFilePermissions(randoopShPath);
            perms.add(PosixFilePermission.OWNER_EXECUTE);
            perms.add(PosixFilePermission.GROUP_EXECUTE);
            perms.add(PosixFilePermission.OTHERS_EXECUTE);
            Files.setPosixFilePermissions(randoopShPath, perms);
        }
        catch(IOException e)
        {
            Log.error(RandoopGenerator.class, "Failed to make randoop.sh executable", e);
        }

        // Create a file of classes to test
        File classListFile = new File(randoopTemp, CLASSLIST_FILE);
        try
        {
            classListFile.createNewFile();
            try(OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(classListFile), StandardCharsets.UTF_8.newEncoder()); BufferedWriter out = new BufferedWriter(writer))
            {
                for(String line : classesToTest)
                {
                    out.write(line);
                    out.newLine();
                }
            }
        }
        catch(IOException e)
        {
            Log.error(JavaVisitor.class, "Failed to write " + classListFile.getAbsolutePath(), e);
        }

        // Create a file of methods to omit
        File omitMethodsFile = new File(randoopTemp, OMITMETHODS_FILE);
        try
        {
            omitMethodsFile.createNewFile();
            try(OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(omitMethodsFile), StandardCharsets.UTF_8.newEncoder()); BufferedWriter out = new BufferedWriter(writer))
            {
                for(String line : methodsToOmit)
                {
                    out.write(line);
                    out.newLine();
                }
            }
        }
        catch(IOException e)
        {
            Log.error(RandoopGenerator.class, "Failed to write " + omitMethodsFile.getAbsolutePath(), e);
        }

        // TODO Copy libs over (randoop, junit, hamcrest, reactivex, reactive-streams)

        Log.info(RandoopGenerator.class, "Randoop configuration written to " + randoopTemp.getAbsolutePath());
    }

    private static String determineTempPath()
    {
        if(System.getProperty("os.name").startsWith("Windows"))
        {
            return "%USERPROFILE%\\AppData\\Local\\Temp";
        }

        // Mac, Linux, other Unixes
        // Sorry to all the other ones that don't use this path :(
        return "/tmp";
    }

    // Basically a fixed version of IRewriteCompilationUnit.getRewrittenNode
    @SuppressWarnings("unchecked")
    private static boolean nodeHasChanges(ASTNode node, IRewriteCompilationUnit root)
    {
        ASTRewrite rewriter = root.writer();
        StructuralPropertyDescriptor spd = node.getLocationInParent();
        if(spd.isChildListProperty())
        {
            ListRewrite lw = rewriter.getListRewrite(node.getParent(), (ChildListPropertyDescriptor)node.getLocationInParent());
            List<Object> rewritten = lw.getRewrittenList();
            List<Object> original = lw.getOriginalList();

            for(int i = 0; i < original.size(); i++)
            {
                if(Objects.equals(original.get(i), node))
                {
                    try
                    {
                        return rewritten.get(i) != null && rewritten.get(i) != original.get(i);
                    }
                    catch(IndexOutOfBoundsException e)
                    {
                        return false;
                    }
                }
            }
        }
        else
        {
            if(rewriter.get(node.getParent(), node.getLocationInParent()) != node)
            {
                return true;
            }
        }
        return false;
    }

    private static String buildSignatureFromNode(ASTNode node)
    {
        IMethodBinding binding;
        if(node instanceof MethodInvocation)
        {
            MethodInvocation invocation = (MethodInvocation)node;
            binding = invocation.resolveMethodBinding().getMethodDeclaration();
        }
        else if(node instanceof MethodReference)
        {
            MethodReference reference = (MethodReference)node;
            binding = reference.resolveMethodBinding().getMethodDeclaration();
        }
        else
        {
            return null;
        }
        String className = binding.getDeclaringClass().getBinaryName();
        String methodName = binding.getName();
        String params = Arrays.stream(binding.getParameterTypes()).map(t -> t.getBinaryName()).collect(Collectors.joining(", "));
        String returnName = binding.getReturnType().getBinaryName();
        return buildSignature(className, methodName, params, returnName);
    }

    private static String buildSignature(String className, String methodName, String params, String returnName)
    {
        return className + "." + methodName + "(" + params + ") -> " + returnName;
    }

    private static class CopyVisitor extends SimpleFileVisitor<Path>
    {
        private final Path target;
        private Path source;

        public CopyVisitor(Path target)
        {
            this.target = target;
            this.source = null;
        }

        @Override
        public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException
        {
            if(this.source == null)
            {
                this.source = dir;
            }
            else
            {
                Files.createDirectories(this.target.resolve(this.source.relativize(dir)));
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException
        {
            Files.copy(file, this.target.resolve(this.source.relativize(file)));
            return FileVisitResult.CONTINUE;
        }
    }
}
