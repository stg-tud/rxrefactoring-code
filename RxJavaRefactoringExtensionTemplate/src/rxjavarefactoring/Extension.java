package rxjavarefactoring;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;

import rxjavarefactoring.framework.api.RxJavaRefactoringExtension;
import rxjavarefactoring.framework.refactoring.AbstractRefactorWorker;
import rxjavarefactoring.framework.utils.PluginUtils;
import rxjavarefactoring.processor.ASTNodesCollector;

public class Extension implements RxJavaRefactoringExtension<ASTNodesCollector>
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
	public ASTNodesCollector getASTNodesCollectorInstance()
	{
		return null;
	}

	@Override
	public void processUnit( ICompilationUnit unit, ASTNodesCollector collector )
	{

	}

	@Override
	public Set<AbstractRefactorWorker<ASTNodesCollector>> getRefactoringWorkers( ASTNodesCollector collector )
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
