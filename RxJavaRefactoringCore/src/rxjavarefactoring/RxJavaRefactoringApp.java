package rxjavarefactoring;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;

import rx.Observable;
import rxjavarefactoring.framework.api.RxJavaRefactoringExtension;
import rxjavarefactoring.framework.refactoring.AbstractCollector;
import rxjavarefactoring.framework.refactoring.AbstractRefactoringProcessor;
import rxjavarefactoring.framework.refactoring.AbstractRxJavaRefactoringApp;
import rxjavarefactoring.framework.utils.RxLogger;
import rxjavarefactoring.processor.RefactoringProcessor;

/**
 * Description: Refactoring application. This class assumes that the Rx
 * dependencies are already in the classpath or that the .jar files are in
 * directory in root called "/all-deps".<br>
 * This class contains code from the tool
 * <a href="http://refactoring.info/tools/asyncdroid/">AsyncDroid</a>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/11/2016
 */
public class RxJavaRefactoringApp extends AbstractRxJavaRefactoringApp
{
	private static final String DEPENDENCIES_DIRECTORY = "all-deps";
	private Set<String> targetClasses;
	private String commandId;

	@Override
	protected String getDependenciesDirectoryName()
	{
		return DEPENDENCIES_DIRECTORY;
	}

	public void setExtension( RxJavaRefactoringExtension extension )
	{
		this.extension = extension;
	}

	public void setCommandId( String commandId )
	{
		this.commandId = commandId;
	}

	@Override
	public void refactorCompilationUnits( IProject project, Map<String, ICompilationUnit> units )
	{
		RxLogger.info( this, "METHOD=refactorCompilationUnits - # units: " + units.size() );
		AbstractCollector collector;
		try
		{
			collector = this.extension.getASTNodesCollectorInstance( project );
			if ( collector == null )
			{
				new IllegalArgumentException( "getASTNodesCollectorInstance must return not null" );
			}
		}
		catch ( Throwable throwable )
		{
			RxLogger.notifyExceptionInClient( throwable );
			return;
		}

		ProgressMonitorDialog dialog = new ProgressMonitorDialog( activeShell );
		try
		{
			dialog.run( true, false, new IRunnableWithProgress()
			{
				@Override
				public void run( IProgressMonitor monitor ) throws InvocationTargetException, InterruptedException
				{
					beginTask(monitor);
					Observable
							.from( units.values() )
							// Filter using the boolean formula "runningForTest -> validateName"
							.filter( unit -> !runningForTests || validateUnitName( unit ) )
							.doOnNext( unit -> {
								processUnitFromExtension( unit, RxJavaRefactoringApp.this.extension, collector );
								monitor.worked( 1 );
							} )
							.doOnCompleted( () -> refactorUnits( collector ) )
							.doOnError( t -> RxLogger.error( this, "METHOD=refactorCompilationUnits", t ) )
							.subscribe();
					monitor.done();
				}

				private void beginTask(IProgressMonitor monitor)
				{
					int numberOfFiles = units.values().size();
					String pluralForm = numberOfFiles == 1 ? "" : "s";
					String message = "Project: " + project.getName()+ ". Analyzing "
							+ numberOfFiles + " java file" + pluralForm + ".";
					monitor.beginTask( message, numberOfFiles );
				}
			} );
		}
		catch ( Exception e )
		{
			RxLogger.error( this, "METHOD=refactorCompilationUnits, Project:" + project.getName() + " - FAILED", e );
		}
	}

	public void refactorOnly( String... classNames )
	{
		this.targetClasses = new HashSet<>();
		this.targetClasses.addAll( Arrays.asList( classNames ) );
		runningForTests = true;
	}

	public static boolean isRunningForTests()
	{
		return runningForTests;
	}

	// ### Private Methods ###

	private boolean validateUnitName( ICompilationUnit unit )
	{
		return this.targetClasses != null && this.targetClasses.contains( unit.getElementName() );
	}

	private void refactorUnits( AbstractCollector collector )
	{
		RxLogger.info( this, "METHOD=refactorUnits - " + collector.getCollectorName() + " Refactoring starting..." );
		AbstractRefactoringProcessor processor = new RefactoringProcessor( extension, collector );
		runProcessor( processor );
	}

	private void runProcessor( AbstractRefactoringProcessor processor )
	{
		try
		{
			NullProgressMonitor progressMonitor = new NullProgressMonitor();
			processor.createChange( progressMonitor );
			Map<ICompilationUnit, String> icuVsNewCodeMap = processor.getICompilationUnitVsNewSourceCodeMap();
			this.originalCompilationUnitVsNewSourceCodeMap.putAll( icuVsNewCodeMap );
		}
		catch ( Exception e )
		{
			RxLogger.error( this, "METHOD=runProcessor, Processor:" + processor.getName() + " - FAILED", e );
		}
	}

	private void processUnitFromExtension( final ICompilationUnit unit,
			final RxJavaRefactoringExtension extension,
			final AbstractCollector collector )
	{
		ISafeRunnable runnable = new ISafeRunnable()
		{
			@Override
			public void handleException( Throwable throwable )
			{
				RxLogger.notifyExceptionInClient( throwable );
			}

			@Override
			public void run() throws Exception
			{
				if ( commandId.equals( extension.getId() ) )
				{
					extension.processUnit( unit, collector );
				}
			}
		};
		SafeRunner.run( runnable );
	}
}
