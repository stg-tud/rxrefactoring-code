package de.tudarmstadt.rxrefactoring.ext.asynctask;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.IJavaProject;

import com.google.common.collect.Sets;

import de.tudarmstadt.rxrefactoring.core.RefactoringExtension;
import de.tudarmstadt.rxrefactoring.core.utils.PluginUtils;
import de.tudarmstadt.rxrefactoring.core.workers.RefactorWorker;
import de.tudarmstadt.rxrefactoring.ext.asynctask.collect.AsyncTaskCollector;
import de.tudarmstadt.rxrefactoring.ext.asynctask.workers.AnonymAsyncTaskWorker;
import de.tudarmstadt.rxrefactoring.ext.asynctask.workers.CachedAnonymousTaskWorker;
import de.tudarmstadt.rxrefactoring.ext.asynctask.workers.NewWorker;
import de.tudarmstadt.rxrefactoring.ext.asynctask.workers.SubClassAsyncTaskWorker;

/**
 * Author: Template<br>
 * Created: 01/18/2017
 */
public class Extension implements RefactoringExtension<AsyncTaskCollector> {
	private static final String PLUGIN_ID = "de.tudarmstadt.rxrefactoring.ext.asynctask";

	private static final String COMMAND_ID = "rxRefactoring.commands.rxJavaRefactoringAsyncTask";

	private static final String RESOURCES_DIR_NAME = "resources";

	@Override
	public String getId() {
		return COMMAND_ID;
	}

	@Override
	public AsyncTaskCollector createCollector(IJavaProject project) {
		return new AsyncTaskCollector(project, "AsyncTask");
	}

	@Override
	public Iterable<RefactorWorker> getRefactoringWorkers(AsyncTaskCollector collector) {
//		Set<RefactorWorker> workers = new HashSet<>();
//		workers.add(new AnonymAsyncTaskWorker(collector));
//		//workers.add(new CachedAnonymousTaskWorker(collector));
//		workers.add(new SubClassAsyncTaskWorker(collector));
		return Sets.newHashSet(new NewWorker(collector));
	}

	@Override
	public Path getLibJars() {
		String pluginDir = PluginUtils.getPluginDir(PLUGIN_ID);
		Path jarFilesPath = Paths.get(pluginDir, RESOURCES_DIR_NAME).toAbsolutePath();
		return jarFilesPath;
	}

	public Path getLibPath() {
		return Paths.get("./app/libs");
	}

}
