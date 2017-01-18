package rxjavarefactoring;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;

import rxjavarefactoring.framework.api.RxJavaRefactoringExtension;
import rxjavarefactoring.framework.refactoring.AbstractRefactorWorker;
import rxjavarefactoring.framework.utils.PluginUtils;
import visitors.ExtCollector;
import workers.FirstWorker;

/**
 * Author: Template<br>
 * Created: 01/18/2017
 */
public class Extension implements RxJavaRefactoringExtension<ExtCollector>
{
	// TODO 1 - refactor place holder [ID]: The PLUGIN_ID must match the Bundle-SymbolicName from MANIFEST.MF
	private static final String PLUGIN_ID = "de.tudarmstadt.stg.rxjava.refactoring.extension.[ID]";

	// TODO 2 - refactor placeholder [COMMAND_ID]: The COMMAND_ID must match the one given in plugin.xml
	private static final String COMMAND_ID = "rxRefactoring.commands.rxJavaRefactoring[COMMAND_ID]";

	private static final String RESOURCES_DIR_NAME = "resources";

	@Override
	public String getId()
	{
		return COMMAND_ID;
	}

	@Override
	public ExtCollector getASTNodesCollectorInstance( IProject project )
	{
		// TODO 3 - change "extension name" for more suitable name
		return new ExtCollector( project, "extension name" );
	}

	@Override
	public void processUnit( ICompilationUnit unit, ExtCollector collector )
	{
		// TODO 6 - collect the information from each unit in the collector.
		// I.e: use a class that extends ASTVisitor to iterate through the unit
		// and save the relevant in the collector.
	}

	@Override
	public Set<AbstractRefactorWorker<ExtCollector>> getRefactoringWorkers( ExtCollector collector )
	{
		Set<AbstractRefactorWorker<ExtCollector>> workers = new HashSet<>();
		// TODO 7 - implement workers and add them to the workers set
		workers.add( new FirstWorker( collector ) );
		return workers;
	}

	@Override
	public String getJarFilesPath()
	{
		// TODO 0 - add required jar files to the resources folder
		String pluginDir = PluginUtils.getPluginDir( PLUGIN_ID );
		Path jarFilsPath = Paths.get( pluginDir, RESOURCES_DIR_NAME ).toAbsolutePath();
		return jarFilsPath.toString();
	}

}
