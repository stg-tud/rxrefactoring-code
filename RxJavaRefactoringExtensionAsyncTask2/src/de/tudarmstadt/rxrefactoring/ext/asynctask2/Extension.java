package de.tudarmstadt.rxrefactoring.ext.asynctask2;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.IJavaProject;

import de.tudarmstadt.rxrefactoring.core.RxRefactoringExtension;
import de.tudarmstadt.rxrefactoring.core.collect.ASTCollector;
import de.tudarmstadt.rxrefactoring.core.utils.PluginUtils;
import de.tudarmstadt.rxrefactoring.core.workers.RxRefactorWorker;
import de.tudarmstadt.rxrefactoring.ext.asynctask2.collect.AsyncTaskCollector;
import de.tudarmstadt.rxrefactoring.ext.asynctask2.workers.AnonymAsyncTaskWorker;
import de.tudarmstadt.rxrefactoring.ext.asynctask2.workers.CachedAnonymousTaskWorker;
import de.tudarmstadt.rxrefactoring.ext.asynctask2.workers.SubClassAsyncTaskWorker;

/**
 * Author: Template<br>
 * Created: 01/18/2017
 */
public class Extension implements RxRefactoringExtension<ASTCollector>
{
	private static final String PLUGIN_ID = "de.tudarmstadt.rxrefactoring.ext.asynctask2";

	private static final String COMMAND_ID = "rxRefactoring.commands.rxJavaRefactoringAsyncTask2";

	private static final String RESOURCES_DIR_NAME = "resources";

	@Override
	public String getId() {
		return COMMAND_ID;
	}
	
	@Override
	public ASTCollector createCollector(IJavaProject project) {
		return new ASTCollector( project, "AsyncTask2", true);
	}

	@Override
	public Iterable<RxRefactorWorker> getRefactoringWorkers(ASTCollector collector )
	{
		Set<RxRefactorWorker> workers = new HashSet<>();
		workers.add( new AnonymAsyncTaskWorker( collector ) );
		workers.add( new CachedAnonymousTaskWorker( collector ) );
		workers.add( new SubClassAsyncTaskWorker( collector ) );
		return workers;
	}

	@Override
	public Path getLibJars()	{
		String pluginDir = PluginUtils.getPluginDir( PLUGIN_ID );
		Path jarFilesPath = Paths.get( pluginDir, RESOURCES_DIR_NAME ).toAbsolutePath();
		return jarFilesPath;
	}
	
	public Path getLibPath() {
		return Paths.get("./app/libs");
	}

	

}
