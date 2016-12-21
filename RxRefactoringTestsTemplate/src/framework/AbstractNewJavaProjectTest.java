package framework;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.junit.After;
import org.junit.Before;

/**
 * Description: Abstract test class to create a project before each test and
 * delete it after the test is done<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/13/2016
 */
public abstract class AbstractNewJavaProjectTest extends AbstractTest
{
	private static final String JAVA_EXTENSION = ".java";
	private static final String PACKAGE_DECLARATION_START = "package ";
	private static final String PACKAGE_DECLARATION_END = ";\n";
	private static final String NEW_LINE = "\n";
	private static final String SRC_FOLDER_NAME = "src";
	private static final String TEST_PROJECT_NAME = "Test Project";

	private IProject project;
	private IJavaProject javaProject;
	private IFolder sourceFolder;

	@Before
	public void setupProject() throws CoreException
	{
		project = createProject();
		javaProject = createJavaProject( project );
		sourceFolder = createSourceFolder( project, javaProject );
	}

	@After
	public void deleteProject() throws CoreException
	{
		project.delete( true, true, null );
	}

	/**
	 * Create a list of compilation units according to the information contained
	 * in {@link TestFilesDto}
	 * The java test files cannot have a package declaration. That declaration
	 * will be added by this method using the data from the {@link TestFilesDto}
	 * 
	 * @param inputTestFiles
	 *            data transfer object contain the relevant information. Class
	 *            names are not supposed to have the extension ".java"
	 * @return a list containing {@link ICompilationUnit}s
	 * @throws CoreException
	 *             core exception
	 * @throws IOException
	 *             io exception
	 */
	protected List<ICompilationUnit> createCompilationUnits( TestFilesDto inputTestFiles ) throws CoreException, IOException
	{
		List<ICompilationUnit> compilationUnits = new ArrayList<>();

		IPackageFragmentRoot packageFragmentRoot = javaProject.getPackageFragmentRoot( sourceFolder );
		String packageName = inputTestFiles.getPackageName();
		IPackageFragment pack = packageFragmentRoot.createPackageFragment( packageName, false, null );

		for ( String testClass : inputTestFiles.getClassNames() )
		{
			String className = testClass + JAVA_EXTENSION;
			String sourceCode = getSourceCode( inputTestFiles.getDirectoryName(), className );

			StringBuilder sb = new StringBuilder();
			sb.append( PACKAGE_DECLARATION_START );
			sb.append( pack.getElementName() );
			sb.append( PACKAGE_DECLARATION_END );
			sb.append( NEW_LINE );
			sb.append( sourceCode );

			ICompilationUnit icu = pack.createCompilationUnit( className, sb.toString(), true, null );
			compilationUnits.add( icu );
		}
		return compilationUnits;
	}

	// ### Private Methods ###

	private IFolder createSourceFolder( IProject project, IJavaProject javaProject ) throws CoreException
	{
		IFolder sourceFolder = project.getFolder( SRC_FOLDER_NAME );
		sourceFolder.create( false, true, null );

		IPackageFragmentRoot packageFragmentRoot = javaProject.getPackageFragmentRoot( sourceFolder );
		IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
		IClasspathEntry[] newEntries = new IClasspathEntry[ oldEntries.length + 1 ];
		System.arraycopy( oldEntries, 0, newEntries, 0, oldEntries.length );
		newEntries[ oldEntries.length ] = JavaCore.newSourceEntry( packageFragmentRoot.getPath() );
		javaProject.setRawClasspath( newEntries, null );
		return sourceFolder;
	}

	private IJavaProject createJavaProject( IProject project ) throws CoreException
	{
		IProjectDescription description = project.getDescription();
		description.setNatureIds( new String[] { JavaCore.NATURE_ID } );
		project.setDescription( description, null );

		IJavaProject javaProject = JavaCore.create( project );

		List<IClasspathEntry> entries = new ArrayList<>();
		IVMInstall vmInstall = JavaRuntime.getDefaultVMInstall();
		LibraryLocation[] locations = JavaRuntime.getLibraryLocations( vmInstall );
		for ( LibraryLocation element : locations )
		{
			entries.add( JavaCore.newLibraryEntry( element.getSystemLibraryPath(), null, null ) );
		}

		javaProject.setRawClasspath( entries.toArray( new IClasspathEntry[ entries.size() ] ), null );
		return javaProject;
	}

	private IProject createProject() throws CoreException
	{
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject( TEST_PROJECT_NAME );
		project.create( null );
		project.open( null );
		return project;
	}
}
