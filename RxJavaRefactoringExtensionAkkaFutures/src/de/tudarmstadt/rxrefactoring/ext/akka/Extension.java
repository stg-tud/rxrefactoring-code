package de.tudarmstadt.rxrefactoring.ext.akka;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jdt.core.IJavaProject;

import com.google.common.collect.Sets;

import de.tudarmstadt.rxrefactoring.core.RefactoringExtension;
import de.tudarmstadt.rxrefactoring.core.collect.ASTCollector;
import de.tudarmstadt.rxrefactoring.core.utils.PluginUtils;
import de.tudarmstadt.rxrefactoring.core.workers.RefactorWorker;
import de.tudarmstadt.rxrefactoring.ext.akka.workers.AkkaWorker;

/**
 * Description: Implementation of API of the RxJavaRefactoringTool<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/21/2016
 */
public class Extension implements RefactoringExtension<ASTCollector> {
	
	
	public static final String PLUGIN_ID = "de.tudarmstadt.rxrefactoring.ext.akkafutures";

	private static final String COMMAND_ID = "rxRefactoring.commands.rxJavaRefactoringAkkaFutures";
	private static final String RESOURCES_DIR_NAME = "resources";
	private static final String COLLECTOR_NAME = "AkkaFutures";

	@Override
	public String getId() {
		return COMMAND_ID;
	}

	@Override
	public ASTCollector createCollector( IJavaProject project )	{
		return new ASTCollector(project, COLLECTOR_NAME, true);
	}


	@Override
	public Iterable<RefactorWorker> getRefactoringWorkers(ASTCollector collector) {
		return Sets.newHashSet(
				new AkkaWorker(collector));
	}

	@Override
	public Path getLibJars()	{
		String pluginDir = PluginUtils.getPluginDir( PLUGIN_ID );
		Path jarFilesPath = Paths.get( pluginDir, RESOURCES_DIR_NAME );
		return jarFilesPath;
	}	
}
