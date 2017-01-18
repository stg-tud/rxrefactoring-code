package rxjavarefactoring;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;

import domain.ClassDetails;
import rxjavarefactoring.framework.api.RxJavaRefactoringExtension;
import rxjavarefactoring.framework.refactoring.AbstractRefactorWorker;
import rxjavarefactoring.framework.utils.PluginUtils;
import visitors.DeclarationVisitor;
import visitors.CuCollector;
import visitors.UsagesVisitor;
import workers.AnonymAsyncTaskWorker;
import workers.CachedAnonymousTaskWorker;
import workers.SubClassAsyncTaskWorker;

/**
 * Author: Template<br>
 * Created: 01/18/2017
 */
public class Extension implements RxJavaRefactoringExtension<CuCollector>
{
	private static final String PLUGIN_ID = "de.tudarmstadt.stg.rxjava.refactoring.extension.asynctask";

	private static final String COMMAND_ID = "rxRefactoring.commands.rxJavaRefactoringAsyncTask";

	private static final String RESOURCES_DIR_NAME = "resources";

	@Override
	public String getId()
	{
		return COMMAND_ID;
	}

	@Override
	public CuCollector getASTNodesCollectorInstance(IProject project )
	{
		return new CuCollector( project, "AsyncTask" );
	}

	@Override
	public void processUnit( ICompilationUnit unit, CuCollector collector )
	{
		CompilationUnit cu = new RefactoringASTParser( AST.JLS8 ).parse( unit, true );

		DeclarationVisitor declarationVisitor = new DeclarationVisitor( ClassDetails.ASYNC_TASK );
		UsagesVisitor usagesVisitor = new UsagesVisitor( ClassDetails.ASYNC_TASK );
		cu.accept( declarationVisitor );
		cu.accept( usagesVisitor );

		// Cache relevant information in an object that contains maps
		collector.addSubclasses( unit, declarationVisitor.getSubclasses() );
		collector.addAnonymClassDecl( unit, declarationVisitor.getAnonymousClasses() );
		collector.addAnonymCachedClassDecl( unit, declarationVisitor.getAnonymousCachedClasses() );
		collector.addRelevantUsages( unit, usagesVisitor.getUsages() );
	}

	@Override
	public Set<AbstractRefactorWorker<CuCollector>> getRefactoringWorkers(CuCollector collector )
	{
		Set<AbstractRefactorWorker<CuCollector>> workers = new HashSet<>();
		workers.add( new AnonymAsyncTaskWorker( collector ) );
		workers.add( new CachedAnonymousTaskWorker( collector ) );
		workers.add( new SubClassAsyncTaskWorker( collector ) );
		return workers;
	}

	@Override
	public String getJarFilesPath()
	{
		String pluginDir = PluginUtils.getPluginDir( PLUGIN_ID );
		Path jarFilsPath = Paths.get( pluginDir, RESOURCES_DIR_NAME ).toAbsolutePath();
		return jarFilsPath.toString();
	}

}
