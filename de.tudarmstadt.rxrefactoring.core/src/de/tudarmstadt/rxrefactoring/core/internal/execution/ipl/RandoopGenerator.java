package de.tudarmstadt.rxrefactoring.core.internal.execution.ipl;

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
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import de.tudarmstadt.rxrefactoring.core.utils.Log;

/**
 * This class is responsible for setting up the test environment (e.g. creating
 * temporary folders, copying binaries over...).
 * @author Nikolas Hanstein, Maximilian Kirschner
 */
public class RandoopGenerator
{
    private static final String OMITMETHODS_FILE = "omitmethods.txt";
    private static final String CLASSLIST_FILE = "classlist.txt";

    private static final String SHEBANG = "#!/bin/bash";
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
    private static final String ECHO_RUN_RANDOOP = "echo -e \"${C_BLUE}==> ${C_YELLOW}Running randoop on pre-refactoring binaries${C_NC}\"";
    private static final String COMMAND_RUN_RANDOOP = "java -classpath \"pre:libs/*\" randoop.main.Main gentests --classlist=classlist.txt --omitmethods-file=omitmethods.txt --no-error-revealing-tests=true --junit-output-dir=tests/src --time-limit=10";
    private static final String IF_CHECK_RETURN_CODE = "if [ ! $? -eq 0 ]";
    private static final String THEN = "then";
    private static final String EXIT_ERROR = "    exit 1";
    private static final String FI = "fi";
    private static final String ECHO_JAVAC = "echo -e \"${C_BLUE}==> ${C_YELLOW}Compiling generated tests with pre-refactoring binaries${C_NC}\"";
    private static final String COMMAND_JAVAC = "javac -cp \"pre:libs/*\" -d \"tests/bin\" tests/src/RegressionTest*.java";
    private static final String ECHO_JUNIT_POST = "echo -e \"${C_BLUE}==> ${C_YELLOW}Running tests on post-refactoring binaries${C_NC}\"";
    private static final String COMMAND_JUNIT_POST = "java -cp \"tests/bin:post:libs/*\" org.junit.runner.JUnitCore RegressionTest";
    private static final String IF_CHECK_SAVED_RESULTS = "if [ ! $? -eq 0 ]";
    private static final String ECHO_PRINT_ERROR = "    echo -e \"${C_BLUE}==> ${C_RED}Error: One or more tests failed! This refactoring may not be safe.${C_NC}\"";
    private static final String ELSE = "else";
    private static final String ECHO_ALL_OK = "    echo -e \"${C_BLUE}==> ${C_GREEN}All tests ran OK. This refactoring is probably safe.${C_NC}\"";

    private static File tempDir;

    public static File mkTempDir()
    {
        String time = Calendar.getInstance().getTime().toString().replace(' ', '_').replace(':', '_');
        File randoopTemp = new File("/tmp/randoop-gen-" + time);
        tempDir = randoopTemp;
        randoopTemp.mkdirs();
        new File(randoopTemp, "libs").mkdirs();
        new File(randoopTemp, "pre").mkdirs();
        new File(randoopTemp, "post").mkdirs();
        return randoopTemp;
    }

    public static File getTempDir()
    {
        return tempDir;
    }

    public static void copyBinaries(IProject project, File dest)
    {
        // TODO Copy libs over (randoop, junit, hamcrest, reactivex, reactive-streams)
        try
        {
            // Rebuild the project to make sure the binaries we'll copy are
            // up-to-date
            project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        }
        catch(CoreException e)
        {
            Log.error(RandoopGenerator.class, "Failed to rebuild project.", e);
        }

        Set<String> outputLocations = new HashSet<>();
        String projectPath = project.getLocation().toOSString();
        IJavaProject jProj = JavaCore.create(project);
        try
        {
            IClasspathEntry[] cp = jProj.getRawClasspath();
            for(IClasspathEntry entry : cp)
            {
                if(entry.getContentKind() == IPackageFragmentRoot.K_SOURCE)
                {
                    IPath outputPath = entry.getOutputLocation();
                    if(outputPath != null)
                    {
                        // getOutputLocation() prepends the name of the project
                        // again, so remove it
                        String outputLoc = outputPath.removeFirstSegments(1).toOSString();
                        if(!outputLoc.trim().isEmpty()) // Poor man's isBlank()
                        {
                            outputLocations.add(outputLoc);
                        }
                    }
                }
            }
        }
        catch(JavaModelException e)
        {
            Log.error(RandoopGenerator.class, "Failed to retrieve list of project packages.", e);
        }

        // Copy the binaries over
        try
        {
            for(String outputLoc : outputLocations)
            {
                Files.walkFileTree(Paths.get(projectPath, outputLoc), new CopyVisitor(Paths.get(dest.getAbsolutePath())));
            }
        }
        catch(IOException e)
        {
            Log.error(RandoopGenerator.class, "Failed to copy binaries into temp directory.", e);
        }
    }

    public static void createOutput(Set<String> classesToTest, Set<String> methodsToOmit)
    {
        // Create a shell file for running randoop
        File randoopSh = new File(tempDir, "randoop.sh");
        try
        {
            randoopSh.createNewFile();
            try(OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(randoopSh), StandardCharsets.UTF_8.newEncoder()); BufferedWriter out = new BufferedWriter(writer))
            {
                out.write(SHEBANG);
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

                out.write(ECHO_JUNIT_POST);
                out.newLine();
                out.write(COMMAND_JUNIT_POST);
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
        File classListFile = new File(tempDir, CLASSLIST_FILE);
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
            Log.error(RandoopGenerator.class, "Failed to write " + classListFile.getAbsolutePath(), e);
        }

        // Create a file of methods to omit
        File omitMethodsFile = new File(tempDir, OMITMETHODS_FILE);
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

        Log.info(RandoopGenerator.class, "Randoop configuration written to " + tempDir.getAbsolutePath());
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
            Files.copy(file, this.target.resolve(this.source.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
            return FileVisitResult.CONTINUE;
        }
    }
}
