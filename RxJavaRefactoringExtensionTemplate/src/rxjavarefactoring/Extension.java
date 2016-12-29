package rxjavarefactoring;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;

import rxjavarefactoring.framework.api.RxJavaRefactoringExtension;
import rxjavarefactoring.framework.refactoring.AbstractRefactorWorker;
import rxjavarefactoring.framework.utils.PluginUtils;
import rxjavarefactoring.processor.StubCollector;

public class Extension implements RxJavaRefactoringExtension<StubCollector>
{
	// The PLUGIN_ID must match the Bundle-SymbolicName from MANIFEST.MF
	private static final String PLUGIN_ID = "de.tudarmstadt.stg.rxjava.refactoring.extension.[ID]";

	// The COMMAND_ID must match the one given in plugin.xml
	private static final String COMMAND_ID = "rxRefactoring.commands.rxJavaRefactoring[COMMAND_ID]";

	private static final String RESOURCES_DIR_NAME = "resources";

	@Override
	public String getId()
	{
		return COMMAND_ID;
	}

	@Override
	public StubCollector getASTNodesCollectorInstance( IProject project )
	{
		return null;
	}

	@Override
	public void processUnit( ICompilationUnit unit, StubCollector collector )
	{

	}

	@Override
	public Set<AbstractRefactorWorker<StubCollector>> getRefactoringWorkers( StubCollector collector )
	{
		return null;
	}

	@Override
	public String getJarFilesPath()
	{
		String pluginDir = PluginUtils.getPluginDir( PLUGIN_ID );
		Path jarFilsPath = Paths.get( pluginDir, RESOURCES_DIR_NAME ).toAbsolutePath();
		return jarFilsPath.toString();
	}

}
