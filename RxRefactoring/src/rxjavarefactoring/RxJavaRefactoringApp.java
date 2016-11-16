package rxjavarefactoring;

import java.util.*;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;

import rx.Observable;
import rxjavarefactoring.analyzers.DeclarationVisitor;
import rxjavarefactoring.analyzers.UsagesVisitor;
import rxjavarefactoring.domain.ClassDetails;
import rxjavarefactoring.framework.refactoring.AbstractCollector;
import rxjavarefactoring.framework.refactoring.AbstractProcessor;
import rxjavarefactoring.framework.refactoring.AbstractRxJavaRefactoringApp;
import rxjavarefactoring.framework.utils.RxLogger;
import rxjavarefactoring.processors.CuCollector;
import rxjavarefactoring.processors.asynctask.AsyncTaskProcessor;
import rxjavarefactoring.processors.swingworker.SwingWorkerProcessor;

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
	private static final String DEPENDENCIES_DIRECTORY = "/all-deps";
	private static final int ASYNCTASK_COLLECTOR_INDEX = 0;
	private static final int SWINGWORKER_COLLECTOR_INDEX = 1;
	private Set<String> targetClasses;
	private static boolean runningForTests = false;

	@Override
	protected String getDependenciesDirectoryName()
	{
		return DEPENDENCIES_DIRECTORY;
	}

	@Override
	public void refactorCompilationUnits( ICompilationUnit[] units )
	{
		originalCompilationUnitVsNewSourceCodeMap = new HashMap<>();

		RxLogger.info( this, "METHOD=refactorCompilationUnits - # units: " + units.length );
		List<AbstractCollector> collectors = createCollectors();

		Observable
				.from( units )
				// Filter using the boolean formula "runningForTest ->
				// validateName"
				.filter( unit -> !runningForTests || validateUnitName( unit ) )
				.doOnNext( unit -> processUnit( unit, collectors ) )
				.doOnCompleted( () -> refactorUnits( collectors ) )
				.doOnError( t -> RxLogger.error( this, "METHOD=refactorCompilationUnits", t ) )
				.subscribe();
	}

	public void refactorOnly( String... classNames )
	{
		targetClasses = new HashSet<>();
		targetClasses.addAll( Arrays.asList( classNames ) );
		runningForTests = true;
	}

	public static boolean isRunningForTests()
	{
		return runningForTests;
	}

	// ### Private Methods ###

	private List<AbstractCollector> createCollectors()
	{
		List<AbstractCollector> collectors = new ArrayList<>();
		AbstractCollector asyncTasksCollector = new CuCollector();
		AbstractCollector swingWorkerCollector = new CuCollector();

		collectors.add( asyncTasksCollector );
		collectors.add( swingWorkerCollector );
		return collectors;
	}

	private boolean validateUnitName( ICompilationUnit unit )
	{
		return targetClasses != null && targetClasses.contains( unit.getElementName() );
	}

	private void processUnit( ICompilationUnit iUnit, List<AbstractCollector> collectors )
	{
		// Collect relevant info from compilation units using the visitors
		if ( collectors.get( ASYNCTASK_COLLECTOR_INDEX ) instanceof CuCollector )
		{
			collectInfo( collectors, iUnit, ClassDetails.ASYNC_TASK, ASYNCTASK_COLLECTOR_INDEX );
		}

		if ( collectors.get( SWINGWORKER_COLLECTOR_INDEX ) instanceof CuCollector )
		{
			collectInfo( collectors, iUnit, ClassDetails.SWING_WORKER, SWINGWORKER_COLLECTOR_INDEX );
		}

	}

	private void collectInfo( List<AbstractCollector> collectors, ICompilationUnit cUnit, ClassDetails classDetails, int collectorIndex )
	{
		CompilationUnit cu = new RefactoringASTParser( AST.JLS8 ).parse( cUnit, true );
		DeclarationVisitor declarationVisitor = new DeclarationVisitor( classDetails );
		UsagesVisitor usagesVisitor = new UsagesVisitor( classDetails );
		cu.accept( declarationVisitor );
		cu.accept( usagesVisitor );

		logFindings( cu, declarationVisitor, usagesVisitor, classDetails );

		CuCollector collector = (CuCollector) collectors.get( collectorIndex );
		// Cache relevant information in an object that contains maps
		collector.addSubclasses( cUnit, declarationVisitor.getSubclasses() );
		collector.addAnonymClassDecl( cUnit, declarationVisitor.getAnonymousClasses() );
		collector.addAnonymCachedClassDecl( cUnit, declarationVisitor.getAnonymousCachedClasses() );
		collector.addRelevantUsages( cUnit, usagesVisitor.getUsages() );
	}

	private void logFindings( CompilationUnit cu, DeclarationVisitor declarationVisitor, UsagesVisitor usagesVisitor, ClassDetails classDetails )
	{
		String location = cu.getPackage().toString()
				.replaceAll( "package ", "" )
				.replaceAll( ";", "." + cu.getJavaElement().getElementName() )
				.replaceAll( "\n", "" )
				.replaceAll( "\\.java", "" );

		String className = classDetails.getBinaryName();
		if ( declarationVisitor.isTargetClassFound() )
		{
			RxLogger.info( this, "METHOD=processUnit - " + className + " found in class: " + location );
		}

		if ( usagesVisitor.isUsagesFound() )
		{
			RxLogger.info( this, "METHOD=processUnit - Method Invocation of " + className + " found in class: " + location );
		}
	}

	private void refactorUnits( List<AbstractCollector> collectors )
	{
		if ( collectors.get( ASYNCTASK_COLLECTOR_INDEX ) instanceof CuCollector )
		{
			RxLogger.info( this, "METHOD=refactorUnits - AsyncTasks Refactoring starting..." );
			CuCollector asyncTaskCollector = (CuCollector) collectors.get( ASYNCTASK_COLLECTOR_INDEX );
			AbstractProcessor processor = new AsyncTaskProcessor( asyncTaskCollector, "Convert AsyncTasks to RxObservable" );
			runProcessor( processor );
		}
		if ( collectors.get( SWINGWORKER_COLLECTOR_INDEX ) instanceof CuCollector )
		{
			RxLogger.info( this, "METHOD=refactorUnits - SwingWorker Refactoring starting..." );
			CuCollector swingWorkerCollector = (CuCollector) collectors.get( SWINGWORKER_COLLECTOR_INDEX );
			AbstractProcessor processor = new SwingWorkerProcessor( swingWorkerCollector, "Convert SwingWorkers to RxObservable" );
			runProcessor( processor );
		}
	}

	private void runProcessor( AbstractProcessor processor )
	{
		try
		{
			NullProgressMonitor progressMonitor = new NullProgressMonitor();
			processor.createChange( progressMonitor );
			Map<ICompilationUnit, String> icuVsNewCodeMap = processor.getICompilationUnitVsNewSourceCodeMap();
			originalCompilationUnitVsNewSourceCodeMap.putAll( icuVsNewCodeMap );
		}
		catch ( Exception e )
		{
			RxLogger.error( this, "METHOD=runProcessor, Processor:" + processor.getName() + " - FAILED", e );
		}
	}
}
