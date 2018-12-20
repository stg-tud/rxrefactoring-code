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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.utils.Log;

public class RandoopGenerator
{
    private static final String OMITMETHODS_FILE = "omitmethods.txt";
    private static final String CLASSLIST_FILE = "classlist.txt";
    private static final String RANDOOP_JAR = "randoop-all-4.1.0.jar";

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

        List<String> methodsToOmit = new ArrayList<>();
        Set<String> classesToTest = new HashSet<>();

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
                        String className = cu.getPackage().getName() + "." + type.getName().getFullyQualifiedName();
                        classesToTest.add(className);
                        Log.info(JavaVisitor.class, "Looking for changes in method " + className + "." + method.getName().getFullyQualifiedName() + "()");
                        List<ASTNode> nodes = JavaVisitor.visitMethodDeclaration(method);

                        boolean shouldOmit = true;
                        for(ASTNode node : nodes)
                        {
                            if(JavaVisitor.nodeHasChanges(node, unit))
                            {
                                shouldOmit = false;
                                break;
                            }
                        }

                        if(shouldOmit)
                        {
                            @SuppressWarnings("unchecked") List<SingleVariableDeclaration> objParams = method.parameters();
                            // @formatter:off
                            String params = objParams.stream().map(SingleVariableDeclaration::getType)
                                                              .map(t -> t.isParameterizedType() ? ((ParameterizedType)t).getType() : t)
                                                              .map(t -> (t.resolveBinding().getPackage() != null ? t.resolveBinding().getPackage().getName() + "." : "") + t.toString())
                                                              .collect(Collectors.joining(", "));
                            // @formatter:on

                            methodsToOmit.add(className + "." + method.getName().getFullyQualifiedName() + "(" + params + ")");
                        }
                    }
                }
            }
        }

        createOutput(methodsToOmit, classesToTest, projectPath, localPath);
    }

    private static void createOutput(List<String> methodsToOmit, Set<String> classesToTest, String projectPath, String localPath)
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
                out.write("#!/bin/bash");
                out.newLine();
                out.newLine();

                String pre_classpath = " -classpath \"pre/bin:" + RANDOOP_JAR + "\"";
                String main = " randoop.main.Main";
                String command = " gentests";
                String classlist = " --classlist=classlist.txt";
                String omitmethods_file = " --omitmethods-file=omitmethods.txt";
                String no_error_revealing_tests = " --no-error-revealing-tests=true";
                String pre_junit_output_dir = " --junit-output-dir=tests/pre";
                String time_limit = " --time-limit=10";

                // The actual command
                out.write("java");
                out.write(pre_classpath);
                out.write(main);
                out.write(command);
                out.write(classlist);
                out.write(omitmethods_file);
                out.write(no_error_revealing_tests);
                out.write(pre_junit_output_dir);
                out.write(time_limit);
                out.newLine();
                out.newLine();
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
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
