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
import rxjavarefactoring.framework.api.RxJavaRefactoringExtension;
import rxjavarefactoring.framework.refactoring.AbstractRefactorWorker;
import rxjavarefactoring.framework.utils.PluginUtils;
import visitors.ExtCollector;
import visitors.ForEachVisitor;
import workers.ForEachWorker;

/**
 * Author: Template<br>
 * Created: 01/18/2017
 */
public class Extension implements RxJavaRefactoringExtension<ExtCollector>
{
	private static final String PLUGIN_ID = "de.tudarmstadt.stg.rxjava.refactoring.extension.forloops";

	private static final String COMMAND_ID = "rxRefactoring.commands.rxJavaRefactoringForLoops";

	private static final String RESOURCES_DIR_NAME = "resources";

	@Override
	public String getId()
	{
		return COMMAND_ID;
	}

	@Override
	public ExtCollector getASTNodesCollectorInstance( IProject project )
	{
		return new ExtCollector( project, "For Loops" );
	}

	@Override
	public void processUnit( ICompilationUnit unit, ExtCollector collector )
	{
		CompilationUnit cu = new RefactoringASTParser( AST.JLS8 ).parse( unit, true );

		// Collect information using visitor
		ForEachVisitor forEachVisitor = new ForEachVisitor();
		cu.accept(forEachVisitor);

		// put the collected information into the collector
		collector.addForBlock(unit, forEachVisitor.getForBlocks());
	}

	@Override
	public Set<AbstractRefactorWorker<ExtCollector>> getRefactoringWorkers( ExtCollector collector )
	{
		Set<AbstractRefactorWorker<ExtCollector>> workers = new HashSet<>();
		workers.add( new ForEachWorker( collector ) );
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
