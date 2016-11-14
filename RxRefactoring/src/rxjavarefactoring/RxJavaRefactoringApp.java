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
import rxjavarefactoring.framework.AbstractRxJavaRefactoringApp;
import rxjavarefactoring.framework.RxLogger;
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
	private Set<String> targetClasses;

	@Override
	protected String getDependenciesDirectoryName()
	{
		return DEPENDENCIES_DIRECTORY;
	}

	@Override
	public void refactorCompilationUnits( ICompilationUnit[] units )
	{
		icuVsNewSourceCodeMap = new HashMap<>();
		CuCollector asyncTasksCollector = new CuCollector();

		Observable
				.from( units )
				.filter( unit -> targetClasses != null && targetClasses.contains(unit.getElementName()))
				.doOnNext( unit -> processUnit( unit, asyncTasksCollector ) )
				.doOnCompleted( () -> refactorAsyncTasks( asyncTasksCollector ) )
				.doOnError( t -> RxLogger.error( this, "METHOD=refactorCompilationUnits", t ) )
				.subscribe();
	}

	public void refactorOnly( String... classNames )
	{
		targetClasses = new HashSet<>();
		targetClasses.addAll(Arrays.asList(classNames));
	}

	// ### Private Methods ###

	private void processUnit( ICompilationUnit iUnit, CuCollector collectors )
	{
		CompilationUnit cu = new RefactoringASTParser( AST.JLS8 ).parse( iUnit, true );
		// Collect relevant information from compilation units using the
		// visitors
		DeclarationVisitor declarationVisitor = new DeclarationVisitor( ClassDetails.ASYNC_TASK );
		UsagesVisitor usagesVisitor = new UsagesVisitor( ClassDetails.ASYNC_TASK );
		cu.accept( declarationVisitor );
		cu.accept( usagesVisitor );

		logFindings( cu, declarationVisitor, usagesVisitor );

		// Cache relevant information in an object that contains maps
		collectors.addSubclasses( iUnit, declarationVisitor.getSubclasses() );
		collectors.addAnonymClassDecl( iUnit, declarationVisitor.getAnonymousClasses() );
		collectors.addAnonymCachedClassDecl( iUnit, declarationVisitor.getAnonymousCachedClasses() );
		collectors.addRelevantUsages( iUnit, usagesVisitor.getUsages() );

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

	private void refactorAsyncTasks( CuCollector asyncTasksCollector )
	{
		RxLogger.info( this, "METHOD=refactorAsyncTasks - AsyncTasks Refactoring starting..." );
		AsyncTaskProcessor asyncTaskProcessor = new AsyncTaskProcessor( asyncTasksCollector );
		try
		{
			NullProgressMonitor iProgressMonitor = new NullProgressMonitor();
			asyncTaskProcessor.createChange( iProgressMonitor );
			Map<ICompilationUnit, String> icuVsNewCodeMap = asyncTaskProcessor.getICompilationUnitVsNewSourceCodeMap();
			icuVsNewSourceCodeMap.putAll( icuVsNewCodeMap );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}
}
