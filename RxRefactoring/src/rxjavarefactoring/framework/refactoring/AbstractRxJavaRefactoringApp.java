package rxjavarefactoring.framework.refactoring;

import java.io.File;
import java.util.*;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jdt.core.*;

import rx.Observable;
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
	private static final String PACKAGE_SEPARATOR = ".";
	protected Map<ICompilationUnit, String> originalCompilationUnitVsNewSourceCodeMap;
	protected Map<String, ICompilationUnit> compilationUnitsMap;

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

	protected abstract void refactorCompilationUnits( Map<String, ICompilationUnit> units );

	protected abstract String getDependenciesDirectoryName();

	// ### Private Methods ###

	private void refactorProject( IProject project )
	{
		IJavaProject javaProject = JavaCore.create( project );
		try
		{
			RxLogger.info( this, "METHOD=refactorProject : PROJECT ----> " + project.getName() );
			final String location = project.getLocation().toPortableString();
			updateClassPath( location + getDependenciesDirectoryName(), javaProject );
			compilationUnitsMap = getCompilationUnits( javaProject );
			refactorCompilationUnits( compilationUnitsMap );
		}
		catch ( JavaModelException e )
		{
			RxLogger.error( this, "Project: " + project.getName() + " could not be refactored.", e );
		}
	}

	private void updateClassPath( String location, IJavaProject javaProject ) throws JavaModelException
	{
		IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
		File libs = new File( location );
		if ( libs.isDirectory() && libs.exists() )
		{
			String[] allLibs = libs.list();
			List<IClasspathEntry> cps = new ArrayList<>();
			for ( String lib : allLibs )
			{
				String ap = libs.getAbsolutePath() + File.separator + lib;
				cps.add( JavaCore.newLibraryEntry( Path.fromOSString( ap ), null, null ) );
			}
			for ( IClasspathEntry oldEntry : oldEntries )
			{
				if ( oldEntry.getEntryKind() != IClasspathEntry.CPE_LIBRARY )
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
