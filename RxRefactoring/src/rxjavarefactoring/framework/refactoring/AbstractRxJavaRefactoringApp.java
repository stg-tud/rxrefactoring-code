package rxjavarefactoring.framework.refactoring;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
	protected Map<ICompilationUnit, String> originalCompilationUnitVsNewSourceCodeMap;

	@Override
	public Object start( IApplicationContext iApplicationContext ) throws Exception
	{
		// TODO: display window showing the preconditions to produce compilable
		// code: dependencies in classpath or .jar files in "/all-deps"
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

	protected abstract void refactorCompilationUnits( ICompilationUnit[] units );

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
			ICompilationUnit[] units = getCompilationUnits( javaProject );
			refactorCompilationUnits( units );

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

	private ICompilationUnit[] getCompilationUnits( IJavaProject javaProject ) throws JavaModelException
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
				frags.addAll(Arrays.asList(rootFragments));
			}
		}
		IPackageFragment[] fragments = new IPackageFragment[ frags.size() ];
		frags.toArray( fragments );
		return fragments;
	}

	private ICompilationUnit[] getCompilationUnitInPackages( IPackageFragment[] packages ) throws JavaModelException
	{
		ArrayList<ICompilationUnit> frags = new ArrayList<>();
		for ( IPackageFragment packageFragment : packages )
		{
			ICompilationUnit[] units = packageFragment.getCompilationUnits();
			frags.addAll(Arrays.asList(units));
		}
		ICompilationUnit[] fragments = new ICompilationUnit[ frags.size() ];
		frags.toArray( fragments );
		return fragments;
	}
}
