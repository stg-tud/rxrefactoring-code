package de.tudarmstadt.rxrefactoring.ext.swingworker;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.JavaModelException;
import org.osgi.framework.Bundle;

import de.tudarmstadt.rxrefactoring.core.DependencyBetweenWorkerCheck;
import de.tudarmstadt.rxrefactoring.core.IRefactorExtension;
import de.tudarmstadt.rxrefactoring.core.IWorkerRef;
import de.tudarmstadt.rxrefactoring.core.IWorkerTree;
import de.tudarmstadt.rxrefactoring.core.internal.execution.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.internal.testing.MethodScanner;
import de.tudarmstadt.rxrefactoring.ext.swingworker.utils.DependencyBetweenWorkerCheckSwingWorker;
import de.tudarmstadt.rxrefactoring.ext.swingworker.utils.WorkerMapsUtils;
import de.tudarmstadt.rxrefactoring.ext.swingworker.workers.RxCollector;
import de.tudarmstadt.rxrefactoring.ext.swingworker.workers.refactor.AssignmentWorker;
import de.tudarmstadt.rxrefactoring.ext.swingworker.workers.refactor.ClassInstanceCreationWorker;
import de.tudarmstadt.rxrefactoring.ext.swingworker.workers.refactor.FieldDeclarationWorker;
import de.tudarmstadt.rxrefactoring.ext.swingworker.workers.refactor.MethodDeclarationWorker;
import de.tudarmstadt.rxrefactoring.ext.swingworker.workers.refactor.MethodInvocationWorker;
import de.tudarmstadt.rxrefactoring.ext.swingworker.workers.refactor.RelevantInvocationsWorker;
import de.tudarmstadt.rxrefactoring.ext.swingworker.workers.refactor.SimpleNameWorker;
import de.tudarmstadt.rxrefactoring.ext.swingworker.workers.refactor.SingleVariableDeclWorker;
import de.tudarmstadt.rxrefactoring.ext.swingworker.workers.refactor.TypeDeclarationWorker;
import de.tudarmstadt.rxrefactoring.ext.swingworker.workers.refactor.VariableDeclStatementWorker;
import de.tudarmstadt.rxrefactoring.ext.swingworker.workers.types.TypeOutput;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;

/**
 * Description: Extension class for SwingWorker<br>
 * Author: Camila Gonzalez<br>
 * Created: 18/01/2018
 */
public class SwingWorkerExtension implements IRefactorExtension {

	@Override
	public @NonNull String getName() {
		return "SwingWorker to Observable Test";
	}

	@Override
	public @NonNull String getDescription() {
		return "Refactor SwingWorker to Observable";
	}
	
	@Override
	public IPath getResourceDir() {
		return new Path("resources/");
	}
	
	@Override
	public ProjectUnits runDependencyBetweenWorkerCheck(ProjectUnits units, MethodScanner scanner) throws JavaModelException{
		DependencyCheckSwingWorker dependencyCheck = new DependencyCheckSwingWorker(units, scanner);
		return dependencyCheck.runDependendencyCheck();
	}

	@Override
	public void clearAllMaps() {
		WorkerMapsUtils.clearAllMaps();
	};

	@Override
	public IPath getDestinationDir() {
		return new Path("./libs/");
	}
	
	@Override
	public  boolean isRefactorScopeAvailable() {
		return true; 
	}

	@Override
	public void addWorkersTo(@NonNull IWorkerTree workerTree) {
		setupFreemaker();
		IWorkerRef<Void, RxCollector> collector = workerTree.addWorker(new RxCollector());

		IWorkerRef<RxCollector, TypeOutput> typeWorker = workerTree.addWorker(collector, new TypeDeclarationWorker());

		workerTree.addWorker(typeWorker, new AssignmentWorker());
		workerTree.addWorker(typeWorker, new FieldDeclarationWorker());
		workerTree.addWorker(typeWorker, new MethodInvocationWorker());
		workerTree.addWorker(typeWorker, new VariableDeclStatementWorker());
		workerTree.addWorker(typeWorker, new SimpleNameWorker());
		workerTree.addWorker(typeWorker, new SingleVariableDeclWorker());
		workerTree.addWorker(typeWorker, new ClassInstanceCreationWorker());
		workerTree.addWorker(typeWorker, new MethodDeclarationWorker());
		workerTree.addWorker(typeWorker, new RelevantInvocationsWorker());
	}

	@Override
	public @NonNull String getPlugInId() {
		return "de.tudarmstadt.rxrefactoring.ext.swingworker";
	}

	// TODO Check if freemaker was added correcly

	public static final String TEMPLATES_DIR_NAME = "templates";
	private static Configuration freemakerCfg;

	public static Configuration getFreemakerCfg() {
		return freemakerCfg;
	}

	protected void setupFreemaker() {
		Bundle bundle = Platform.getBundle(getPlugInId());
		URL pluginURL = null;
		try {
			pluginURL = FileLocator.resolve(bundle.getEntry("/"));
		} catch (IOException e) {
			throw new RuntimeException("Could not get installation directory of the plugin: " + getPlugInId());
		}
		String pluginDir = pluginURL.getPath().trim();
		if (Platform.getOS().compareTo(Platform.OS_WIN32) == 0)
			pluginDir = pluginDir.substring(1);
		String templatesDirName = Paths.get(pluginDir, TEMPLATES_DIR_NAME).toAbsolutePath().toString();
		freemakerCfg = new Configuration(Configuration.VERSION_2_3_25);
		freemakerCfg.setDefaultEncoding("UTF-8");
		freemakerCfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		freemakerCfg.setLogTemplateExceptions(false);
		try {
			freemakerCfg.setDirectoryForTemplateLoading(new File(templatesDirName));
		} catch (IOException e) {
			throw new RuntimeException("Exception in setDirectoryForTemplateLoading", e);
		}
	}

	@Override
	public @NonNull ExecutorService createExecutorService() {
		return //Executors.newFixedThreadPool(4);
			Executors.newSingleThreadExecutor();
	}
	
	public WorkerMapsUtils getWorkerMapsUtils() {
		return null;
		
	}
}
