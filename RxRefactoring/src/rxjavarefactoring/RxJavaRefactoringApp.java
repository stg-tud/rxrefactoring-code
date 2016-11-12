package rxjavarefactoring;

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

	@Override
	protected String getDependenciesDirectoryName()
	{
		return DEPENDENCIES_DIRECTORY;
	}

	@Override
	protected void refactorCompilationUnits( ICompilationUnit[] units )
	{
		CuCollector asyncTasksCollector = new CuCollector();

		Observable
				.from( units )
				.doOnNext( unit -> processUnit( unit, asyncTasksCollector ) )
				.doOnCompleted( () -> refactorAsyncTasks( asyncTasksCollector ) )
				.subscribe();
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
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}
}
