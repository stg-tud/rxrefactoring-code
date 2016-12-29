package rxjavarefactoring.framework.refactoring;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jdt.core.*;

import rx.Observable;
import rxjavarefactoring.framework.api.RxJavaRefactoringExtension;
import rxjavarefactoring.framework.utils.RxLogger;

/**
 * Description: Abstract refactoring application. This class updates the
 * classpath given a directory name that contain the jar files and selects the
 * compilation units of all open Java Projects in the workspaces.<br>
 * This class contains code from the tool
 * <a href="http://refactoring.info/tools/asyncdroid/">AsyncDroid</a>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/11/2016
 */
public abstract class AbstractRxJavaRefactoringApp implements IApplication
{
	protected Map<ICompilationUnit, String> originalCompilationUnitVsNewSourceCodeMap;
	protected Map<String, ICompilationUnit> compilationUnitsMap;
	protected RxJavaRefactoringExtension extension;

	private static final String PACKAGE_SEPARATOR = ".";

	public AbstractRxJavaRefactoringApp()
	{
		originalCompilationUnitVsNewSourceCodeMap = new HashMap<>();
		compilationUnitsMap = new HashMap<>();
	}

	@Override
	public Object start( IApplicationContext iApplicationContext ) throws Exception
	{
		// TODO: display window showing the preconditions to produce compilable code:
		// dependencies in classpath or .jar files in "/all-deps"
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		Observable
				.from( workspaceRoot.getProjects() )
				.doOnSubscribe( () -> RxLogger.info( this, "Rx Refactoring Plugin Starting..." ) )
				.filter( this::isJavaProjectOpen )
				.doOnNext( this::refactorProject )
				.doOnCompleted( () -> RxLogger.info( this, "Rx Refactoring Plugin Done!" ) )
				.subscribe();

		return null;
	}

	@Override
	public void stop()
	{

	}

	/**
	 * @return Map containing the original compilation units and a string of the
	 *         new source code after refactoring
	 */
	public Map<ICompilationUnit, String> getOriginalCompilationUnitVsNewSourceCodeMap()
	{
		return originalCompilationUnitVsNewSourceCodeMap;
	}

	/**
	 * @return Map containing the binary name of a compilation unit and the
	 *         compilation unit object.
	 */
	public Map<String, ICompilationUnit> getCompilationUnitsMap()
	{
		return compilationUnitsMap;
	}

	/**
	 * refactor each compilation unit separately
	 * 
	 * @param units
	 *            compilation units
	 */
	protected abstract void refactorCompilationUnits( Map<String, ICompilationUnit> units );

	/**
	 * returns the directory name that contains all necessary dependencies to perform
	 * the refactorings. i.e: "all-deps"
	 * 
	 * @return directory name
	 */
	protected abstract String getDependenciesDirectoryName();

	// ### Private Methods ###

	private void refactorProject( IProject project )
	{
		IJavaProject javaProject = JavaCore.create( project );
		try
		{
			RxLogger.info( this, "METHOD=refactorProject : PROJECT ----> " + project.getName() );
			final String location = project.getLocation().toPortableString();
			addJarFiles( location );
			project.refreshLocal( IResource.DEPTH_INFINITE, null );
			String dependenciesDir = Paths.get( location, getDependenciesDirectoryName() ).toAbsolutePath().toString();
			updateClassPath( dependenciesDir, javaProject );
			compilationUnitsMap = getCompilationUnits( javaProject );
			refactorCompilationUnits( compilationUnitsMap );
		}
		catch ( JavaModelException e )
		{
			RxLogger.error( this, "Project: " + project.getName() + " could not be refactored.", e );
		}
		catch ( CoreException e )
		{
			RxLogger.error( this, "Project: " + project.getName() + " resources could not be refreshed.", e );
		}
	}

	private void addJarFiles( String location )
	{
		try
		{
			String jarFilesPath = this.extension.getJarFilesPath();
			if ( jarFilesPath == null )
			{
				return;
			}

			// copy jar files to DEPENDENCIES_DIRECTORY
			String destinationDirectory = Paths.get( location, getDependenciesDirectoryName() ).toAbsolutePath().toString();
			FileUtils.copyDirectory( new File( jarFilesPath ), new File( destinationDirectory ) );
		}
		catch ( Throwable throwable )
		{
			RxLogger.notifyExceptionInClient( throwable );
			return;
		}
	}

	private void updateClassPath( String location, IJavaProject javaProject ) throws JavaModelException
	{
		IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
		File libs = new File( location );
		if ( libs.isDirectory() && libs.exists() )
		{
			String[] allLibs = libs.list();
			List<String> sourceLibs = Observable.from( allLibs )
					.filter( lib -> lib.contains( "-sources.jar" ) )
					.map( lib -> lib.replace( "-sources", "" ) )
					.toList().toBlocking().single();

			Set<IClasspathEntry> cps = new HashSet<>();
			for ( String lib : allLibs )
			{
				if ( lib.contains( "-sources.jar" ) )
				{
					continue;
				}
				String ap = libs.getAbsolutePath() + File.separator + lib;
				IPath sourcesPath = null;
				if ( sourceLibs.contains( lib ) )
				{
					sourcesPath = Path.fromOSString( ap.replace( ".jar", "-sources.jar" ) );
				}
				cps.add( JavaCore.newLibraryEntry( Path.fromOSString( ap ), sourcesPath, null ) );
			}
			for ( IClasspathEntry oldEntry : oldEntries )
			{
				cps.add( oldEntry );
			}
			javaProject.setRawClasspath( cps.toArray( new IClasspathEntry[ 0 ] ), null );
		}
	}

	private Map<String, ICompilationUnit> getCompilationUnits( IJavaProject javaProject ) throws JavaModelException
	{
		IPackageFragmentRoot[] roots = javaProject.getAllPackageFragmentRoots();
		IPackageFragment[] packages = getPackageFragmentsInRoots( roots );
		return getCompilationUnitInPackages( packages );
	}

	private boolean isJavaProjectOpen( IProject iProject )
	{
		try
		{
			return iProject.isOpen() && iProject.hasNature( JavaCore.NATURE_ID );
		}
		catch ( CoreException e )
		{
			return false;
		}
	}

	private IPackageFragment[] getPackageFragmentsInRoots( IPackageFragmentRoot[] roots ) throws JavaModelException
	{
		ArrayList<IJavaElement> frags = new ArrayList<>();
		for ( IPackageFragmentRoot root : roots )
		{
			if ( root.getKind() == IPackageFragmentRoot.K_SOURCE )
			{
				IJavaElement[] rootFragments = root.getChildren();
				frags.addAll( Arrays.asList( rootFragments ) );
			}
		}
		IPackageFragment[] fragments = new IPackageFragment[ frags.size() ];
		frags.toArray( fragments );
		return fragments;
	}

	private Map<String, ICompilationUnit> getCompilationUnitInPackages( IPackageFragment[] packages ) throws JavaModelException
	{
		Map<String, ICompilationUnit> cUnitsMap = new HashMap<>();
		for ( IPackageFragment packageFragment : packages )
		{
			ICompilationUnit[] units = packageFragment.getCompilationUnits();
			for ( ICompilationUnit unit : units )
			{
				String binaryName = packageFragment.getElementName() + PACKAGE_SEPARATOR + unit.getElementName();
				binaryName = binaryName.substring( 0, binaryName.lastIndexOf( ".java" ) );
				cUnitsMap.put( binaryName, unit );
			}
		}
		return cUnitsMap;
	}
}
