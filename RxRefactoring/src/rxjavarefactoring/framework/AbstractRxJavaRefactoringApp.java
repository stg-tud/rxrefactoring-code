package rxjavarefactoring.framework;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jdt.core.*;

import rx.Observable;

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
	@Override
	public Object start( IApplicationContext iApplicationContext ) throws Exception
	{
		// TODO: display window showing the preconditions to produce compilable
		// code: dependencies in classpath or .jar files in "/all-deps"

		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		Observable
				.from( workspaceRoot.getProjects() )
				.doOnSubscribe( () -> RxLogger.info( this, "Rx Refactoring Started..." ) )
				.filter( project -> isJavaProjectOpen( project ) )
				.doOnNext( project -> refactorProject( project ) )
				.doOnCompleted( () -> RxLogger.info( this, "Refactoring Done!" ) )
				.subscribe();

		return null;
	}

	@Override
	public void stop()
	{

	}

	protected abstract String getDependenciesDirectoryName();

	protected abstract void refactorCompilationUnits( ICompilationUnit[] units );

	// ### Private Methods ###

	private void refactorProject( IProject project )
	{
		IJavaProject javaProject = JavaCore.create( project );
		try
		{
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
		IClasspathEntry[] oldEntry = javaProject.getRawClasspath();
		File libs = new File( location );
		if ( libs.isDirectory() && libs.exists() )
		{
			String[] allLibs = libs.list();
			List<IClasspathEntry> cps = new ArrayList<>();
			for ( int i = 0; i < allLibs.length; i++ )
			{
				String ap = libs.getAbsolutePath() + File.separator + allLibs[ i ];
				cps.add( JavaCore.newLibraryEntry( Path.fromOSString( ap ), null, null ) );
			}
			for ( int i = 0; i < oldEntry.length; i++ )
			{
				if ( oldEntry[ i ].getEntryKind() != IClasspathEntry.CPE_LIBRARY )
					cps.add( oldEntry[ i ] );
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

	public IPackageFragment[] getPackageFragmentsInRoots( IPackageFragmentRoot[] roots ) throws JavaModelException
	{
		ArrayList<IJavaElement> frags = new ArrayList<>();
		for ( int i = 0; i < roots.length; i++ )
		{
			if ( roots[ i ].getKind() == IPackageFragmentRoot.K_SOURCE )
			{
				IPackageFragmentRoot root = roots[ i ];
				IJavaElement[] rootFragments = root.getChildren();
				for ( int j = 0; j < rootFragments.length; j++ )
				{
					frags.add( rootFragments[ j ] );
				}
			}
		}
		IPackageFragment[] fragments = new IPackageFragment[ frags.size() ];
		frags.toArray( fragments );
		return fragments;
	}

	public ICompilationUnit[] getCompilationUnitInPackages( IPackageFragment[] packages ) throws JavaModelException
	{
		ArrayList<ICompilationUnit> frags = new ArrayList<>();
		for ( int i = 0; i < packages.length; i++ )
		{
			IPackageFragment p = packages[ i ];
			ICompilationUnit[] units = p.getCompilationUnits();
			for ( int j = 0; j < units.length; j++ )
			{
				frags.add( units[ j ] );
			}
		}
		ICompilationUnit[] fragments = new ICompilationUnit[ frags.size() ];
		frags.toArray( fragments );
		return fragments;
	}
}
