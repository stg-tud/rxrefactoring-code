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
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import de.tudarmstadt.rxrefactoring.core.utils.Log;
import org.eclipse.jdt.core.dom.*;

/**
 * This class is responsible for setting up the test environment (e.g. creating
 * temporary folders, copying binaries over...).
 * 
 * @author Nikolas Hanstein, Maximilian Kirschner
 */
public class RandoopGenerator {
	// Constants
	private static final String OMITMETHODS_FILE = "omitmethods.txt";
	private static final String CLASSLIST_FILE = "classlist.txt";

	private static final String SHEBANG = "#!/bin/bash";
	// @formatter:off
	private static final String[] COLORS = new String[] { "C_BLUE='\\033[1;34m'", "C_GREEN='\\033[1;32m'",
			"C_RED='\\033[1;31m'", "C_YELLOW='\\033[1;33m'", "C_NC='\\033[0m'" };
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


	
	/**
	 * The randoop-gen temp directory. This is where the compiled binaries will be
	 * copied and where tests will be generated and run.
	 */
	private final File tempDir;
	
	private File getPreDir() {
		return new File(tempDir, "pre");
	}
	
	private File getPostDir() {
		return new File(tempDir, "post");
	}
	
	private File getLibDir() {
		return new File(tempDir, "libs");
	}
		
	public RandoopGenerator() {		
		/**
		 * Sets {@link #tempDir} to a directory in /tmp, based on the current time and
		 * date. This should only be called once - calling it multiple times will fail
		 * if the calls occur in the same second.
		 */
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");
		String time = sdf.format(java.util.Date.from(Instant.now()));
		tempDir = new File("/tmp/randoop-gen-" + time);
		tempDir.mkdirs();
		
		getPreDir().mkdirs();
		getPostDir().mkdirs();
		getLibDir().mkdirs();
	}
		

	/**
	 * Returns the randoop-gen temp directory. This is where the compiled binaries
	 * will be copied and where tests will be generated and run.
	 * 
	 * @return The randoop-gen temp directory.
	 */
	public File getTempDir() {
		return tempDir;
	}

	public void copyProjectBinariesToPre(IProject project) {
		copyBinaries(project, getPreDir());
	}
	
	public void copyProjectBinariesToPost(IProject project) {
		copyBinaries(project, getPostDir());
	}
	
	
	private void copyLibraries(IProject project, File destination) {
		String projectPath = project.getLocation().toOSString();
		IJavaProject jProj = JavaCore.create(project);
		
//		IClasspathEntry[] cp = jProj.getRawClasspath();
		
		
//		org.eclipse.m2e.MAVEN2_CLASSPATH_CONTAINER
		
	}
	
	/**
	 * Builds the specified project and then copies binaries generated from it to
	 * the specified destination. This method takes build systems with multiple
	 * output locations (e.g. Maven) and other complexities into account.
	 * 
	 * @param project The project to build and retrieve binaries from.
	 * @param destination    The destination to copy the binaries to.
	 */
	private void copyBinaries(IProject project, File destination) {
		try {
			// Rebuild the project to make sure the binaries we'll copy are
			// up-to-date
			project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
		} catch (CoreException e) {
			Log.error(RandoopGenerator.class, "Failed to rebuild project.", e);
		}

		Set<String> outputLocations = new HashSet<>();
		String projectPath = project.getLocation().toOSString();
		IJavaProject jProj = JavaCore.create(project);
		try {
			IClasspathEntry[] cp = jProj.getRawClasspath();
			for (IClasspathEntry entry : cp) {
				
				
//				if (entry instanceof IClasspathContainer) {
//					IClasspathContainer container = (IClasspathContainer) entry;
//					IClasspathEntry[] entries = container.getClasspathEntries();
//					System.out.println("oof");
//				}
				
				
				if (entry.getContentKind() == IPackageFragmentRoot.K_SOURCE) {
					IPath outputPath = entry.getOutputLocation();
					if (outputPath != null) {
						// getOutputLocation() prepends the name of the project
						// again, so remove it
						String outputLoc = outputPath.removeFirstSegments(1).toOSString();
						if (!outputLoc.trim().isEmpty()) // Poor man's isBlank()
						{
							outputLocations.add(outputLoc);
						}
					}
				}
			}

			// Check the default output as well
			IPath defaultOutput = jProj.getOutputLocation();
			if (defaultOutput != null) {
				// getOutputLocation() prepends the name of the project
				// again, so remove it
				String outputLoc = defaultOutput.removeFirstSegments(1).toOSString();
				if (!outputLoc.trim().isEmpty()) // Poor man's isBlank()
				{
					outputLocations.add(outputLoc);
				}
			}
		} catch (JavaModelException e) {
			Log.error(RandoopGenerator.class, "Failed to retrieve list of project packages.", e);
		}

		// Copy the binaries over
		try {
			for (String outputLoc : outputLocations) {
				Files.walkFileTree(
						Paths.get(projectPath, outputLoc),
						new CopyVisitor(destination.toPath())
				);
			}
		} catch (IOException e) {
			Log.error(RandoopGenerator.class, "Failed to copy binaries into temp directory.", e);
		}
	}

	/**
	 * Generates the output files ({@code randoop.sh}, {@code classlist.txt} and
	 * {@code omitmethods.txt}) inside the temp directory.
	 * 
	 * @param testClasses The classes containing the methods that should be
	 *                      tested.
	 * @param ommittedMethods The methods that should NOT be tested.
	 */
	public void writeFiles(Set<TypeDeclaration> testClasses, Set<MethodDeclaration> ommittedMethods) {

		// Create a shell file for running randoop
		File randoopSh = new File(tempDir, "randoop.sh");
		try {
			randoopSh.createNewFile();
			try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(randoopSh),
					StandardCharsets.UTF_8.newEncoder()); BufferedWriter out = new BufferedWriter(writer)) {

				// Shebang
				out.write(SHEBANG);
				out.newLine();
				out.newLine();

				for (String color : COLORS) {
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
				out.write("mkdir tests/bin");
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
		} catch (IOException e) {
			Log.error(RandoopGenerator.class, "Failed to create randoop.sh", e);
		}

		// Flag randoop.sh as executable
		try {
			Path randoopShPath = Paths.get(randoopSh.getAbsolutePath());
			Set<PosixFilePermission> perms = Files.getPosixFilePermissions(randoopShPath);
			perms.add(PosixFilePermission.OWNER_EXECUTE);
			perms.add(PosixFilePermission.GROUP_EXECUTE);
			perms.add(PosixFilePermission.OTHERS_EXECUTE);
			Files.setPosixFilePermissions(randoopShPath, perms);
		} catch (IOException e) {
			Log.error(RandoopGenerator.class, "Failed to make randoop.sh executable", e);
		}

		// Create a file of classes to test
		File classListFile = new File(tempDir, CLASSLIST_FILE);
		try {
			classListFile.createNewFile();
			try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(classListFile),
					StandardCharsets.UTF_8.newEncoder()); BufferedWriter out = new BufferedWriter(writer)) {
				for (TypeDeclaration cls : testClasses) {
					out.write(convertClassSignature(cls));
					out.newLine();
				}
			}
		} catch (IOException e) {
			Log.error(RandoopGenerator.class, "Failed to write " + classListFile.getAbsolutePath(), e);
		}

		// Create a file of methods to omit
		File omitMethodsFile = new File(tempDir, OMITMETHODS_FILE);
		try {
			omitMethodsFile.createNewFile();

			try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(omitMethodsFile),
					StandardCharsets.UTF_8.newEncoder()); BufferedWriter out = new BufferedWriter(writer)) {

				for (MethodDeclaration mthd : ommittedMethods) {
					out.write(convertMethodSignature(mthd));
					out.newLine();
				}

				//Add .getClass to ommitted methods
				out.write("java\\.lang\\.Object\\.getClass\\(");
				out.newLine();
			}



		} catch (IOException e) {
			Log.error(RandoopGenerator.class, "Failed to write " + omitMethodsFile.getAbsolutePath(), e);
		}

		Log.info(RandoopGenerator.class, "Randoop configuration written to " + tempDir.getAbsolutePath());
	}

	/**
	 * Converts the specified set of signatures into a set of regex strings suitable
	 * for passing to randoop's omitmethods parameter. This is accomplished by first
	 * truncating the signature to the first parantheses and then escaping the dots
	 * and parantheses inside the signature (since those have special meaning as
	 * regex characters).
	 * 
	 * @param signatures The set of signatures to process.
	 * @return A set of regexes suitable for passing to randoop.
	 */
	public static Set<String> convertToRegexFormat(Set<String> signatures) {
		Set<String> ret = new HashSet<>();
		for (String sig : signatures) {
			// We keep the last '(', because otherwise multiple unrelated
			// methods may be matched by the regex (e.g. testA and testAB)
			String temp = sig.substring(0, sig.indexOf('(') + 1);
		}
		return ret;
	}

	private static String convertClassSignature(TypeDeclaration type) {
		return type.getName().getFullyQualifiedName();//.resolveBinding().getBinaryName();
	}

	private static String convertMethodSignature(MethodDeclaration method) {

		ITypeBinding classBinding;
		ASTNode parent = method.getParent();
		if (parent instanceof AnnotationTypeDeclaration) {
			classBinding = ((AnnotationTypeDeclaration) parent).resolveBinding();
		} else if (parent instanceof AnonymousClassDeclaration) {
			classBinding = ((AnonymousClassDeclaration) parent).resolveBinding();
		} else if (parent instanceof EnumDeclaration) {
			classBinding = ((EnumDeclaration) parent).resolveBinding();
		} else if (parent instanceof TypeDeclaration) {
			classBinding = ((TypeDeclaration) parent).resolveBinding();
		} else {
			throw new RuntimeException("Found naughty method declaration hiding somewhere it doesn't belong ("
				+ parent.getClass().getName() + ").");
		}

		String className = classBinding.getBinaryName();
		String methodName = method.resolveBinding().getName();


		String fullyQualifiedName = className + "." + methodName;
		fullyQualifiedName = fullyQualifiedName.replace(".", "\\.");
		fullyQualifiedName += "\\(";

		return fullyQualifiedName;
	}


	/**
	 * A small FileVisitor used to recusively copy directories over.
	 * 
	 * @author Nikolas Hanstein, Maximilian Kirschner
	 */
	private static class CopyVisitor extends SimpleFileVisitor<Path> {
		/** The path to which the files should be copied. */
		private final Path target;
		/** The path from which the files should be copied. */
		private Path source;

		/**
		 * Constructs a new CopyVisitor with the specified target.
		 * 
		 * @param target The path to which the files should be copied.
		 */
		public CopyVisitor(Path target) {
			this.target = target;
			this.source = null;
		}

		@Override
		public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
			if (this.source == null) {
				this.source = dir;
			} else {
				Files.createDirectories(this.target.resolve(this.source.relativize(dir)));
			}
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
			Files.copy(file, this.target.resolve(this.source.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
			return FileVisitResult.CONTINUE;
		}
	}
}
