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
import rxjavarefactoring.framework.refactoring.AbstractRxJavaRefactoringApp;
import rxjavarefactoring.framework.utils.RxLogger;
import rxjavarefactoring.processors.CuCollector;
import rxjavarefactoring.processors.asynctask.AsyncTaskProcessor;

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

		collectors.add( asyncTasksCollector );
		return collectors;
	}

	private boolean validateUnitName( ICompilationUnit unit )
	{
		return targetClasses != null && targetClasses.contains( unit.getElementName() );
	}

	private void processUnit( ICompilationUnit iUnit, List<AbstractCollector> collectors )
	{
		CompilationUnit cu = new RefactoringASTParser( AST.JLS8 ).parse( iUnit, true );
		// Collect relevant information from compilation units using the
		// visitors
		DeclarationVisitor declarationVisitor = new DeclarationVisitor( ClassDetails.ASYNC_TASK );
		UsagesVisitor usagesVisitor = new UsagesVisitor( ClassDetails.ASYNC_TASK );
		cu.accept( declarationVisitor );
		cu.accept( usagesVisitor );

		logFindings( cu, declarationVisitor, usagesVisitor );

		if ( collectors.get( ASYNCTASK_COLLECTOR_INDEX ) instanceof CuCollector )
		{
			CuCollector asyncTaskCollector = (CuCollector) collectors.get( ASYNCTASK_COLLECTOR_INDEX );
			// Cache relevant information in an object that contains maps
			asyncTaskCollector.addSubclasses( iUnit, declarationVisitor.getSubclasses() );
			asyncTaskCollector.addAnonymClassDecl( iUnit, declarationVisitor.getAnonymousClasses() );
			asyncTaskCollector.addAnonymCachedClassDecl( iUnit, declarationVisitor.getAnonymousCachedClasses() );
			asyncTaskCollector.addRelevantUsages( iUnit, usagesVisitor.getUsages() );
		}

	}

	private void logFindings( CompilationUnit cu, DeclarationVisitor declarationVisitor, UsagesVisitor usagesVisitor )
	{
		String location = cu.getPackage().toString()
				.replaceAll( "package ", "" )
				.replaceAll( ";", "." + cu.getJavaElement().getElementName() )
				.replaceAll( "\n", "" )
				.replaceAll( "\\.java", "" );
		if ( declarationVisitor.isTargetClassFound() )
		{
			RxLogger.info( this, "METHOD=processUnit - AsyncTask found in class: " + location );
		}

		if ( usagesVisitor.isUsagesFound() )
		{
			RxLogger.info( this, "METHOD=processUnit - Method Invocation of AsyncTask found in class: " + location );
		}
	}

	private void refactorUnits( List<AbstractCollector> collectors )
	{
		if ( collectors.get( ASYNCTASK_COLLECTOR_INDEX ) instanceof CuCollector )
		{
			RxLogger.info( this, "METHOD=refactorUnits - AsyncTasks Refactoring starting..." );
			CuCollector asyncTaskCollector = (CuCollector) collectors.get( ASYNCTASK_COLLECTOR_INDEX );
			refactorAsyncTasks( asyncTaskCollector );
		}
	}

	private void refactorAsyncTasks( CuCollector asyncTaskCollector )
	{
		AsyncTaskProcessor asyncTaskProcessor = new AsyncTaskProcessor( asyncTaskCollector, "Convert AsyncTasks To RxObservable" );
		try
		{
			NullProgressMonitor iProgressMonitor = new NullProgressMonitor();
			asyncTaskProcessor.createChange( iProgressMonitor );
			Map<ICompilationUnit, String> icuVsNewCodeMap = asyncTaskProcessor.getICompilationUnitVsNewSourceCodeMap();
			originalCompilationUnitVsNewSourceCodeMap.putAll( icuVsNewCodeMap );
		}
		catch ( Exception e )
		{
			RxLogger.error( this, "METHOD=refactorAsyncTasks - FAILED", e );
		}
	}
}
