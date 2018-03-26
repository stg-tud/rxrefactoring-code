package de.tudarmstadt.rxrefactoring.ext.swingworker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.annotation.NonNull;

import de.tudarmstadt.rxrefactoring.core.IRefactorExtension;
import de.tudarmstadt.rxrefactoring.core.IWorkerRef;
import de.tudarmstadt.rxrefactoring.core.IWorkerTree;
import de.tudarmstadt.rxrefactoring.ext.swingworker.workers.*;
import de.tudarmstadt.rxrefactoring.ext.swingworker.workers.refactor.AssignmentWorker;
import de.tudarmstadt.rxrefactoring.ext.swingworker.workers.refactor.ClassInstanceCreationWorker;
import de.tudarmstadt.rxrefactoring.ext.swingworker.workers.refactor.FieldDeclarationWorker;
import de.tudarmstadt.rxrefactoring.ext.swingworker.workers.refactor.MethodDeclarationWorker;
import de.tudarmstadt.rxrefactoring.ext.swingworker.workers.refactor.MethodInvocationWorker;
import de.tudarmstadt.rxrefactoring.ext.swingworker.workers.refactor.RelevantInvocationsWorker;
import de.tudarmstadt.rxrefactoring.ext.swingworker.workers.refactor.SimpleNameWorker;
import de.tudarmstadt.rxrefactoring.ext.swingworker.workers.refactor.SingleVariableDeclWorker;
import de.tudarmstadt.rxrefactoring.ext.swingworker.workers.refactor.VariableDeclStatementWorker;
import de.tudarmstadt.rxrefactoring.ext.swingworker.workers.types.TypeDeclarationWorker;
import de.tudarmstadt.rxrefactoring.ext.swingworker.workers.types.TypeOutput;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;

import java.net.URL;
import org.osgi.framework.Bundle;

/**
 * Description: Extension class for SwingWorker<br>
 * Author: Camila Gonzalez<br>
 * Created: 18/01/2018
 */
public class SwingWorkerExtension implements IRefactorExtension {

	@Override
	public @NonNull String getName() {
		return "SwingWorker to rx.Observable";
	}

	@Override
	public @NonNull String getDescription() {
		return "Refactor SwingWorker to Observable";
	}

	@Override
	public void addWorkersTo(@NonNull IWorkerTree workerTree) {
		setupFreemaker();
		IWorkerRef<Void, RxCollector> collector = workerTree.addWorker(new RxCollector());
		
		IWorkerRef<RxCollector, TypeOutput> typeWorker = workerTree.addWorker(collector, new TypeDeclarationWorker());
		
		
		workerTree.addWorker(typeWorker, new AssignmentWorker());
		workerTree.addWorker(typeWorker, new FieldDeclarationWorker());
		//workerTree.addWorker(collector, new MethodInvocationWorker());
		workerTree.addWorker(typeWorker, new VariableDeclStatementWorker());
//		workerTree.addWorker(collector, new SimpleNameWorker());
//		workerTree.addWorker(collector, new SingleVariableDeclWorker());
		workerTree.addWorker(typeWorker, new ClassInstanceCreationWorker());
//		workerTree.addWorker(collector, new TypeDeclarationWorker());
//		workerTree.addWorker(collector, new MethodDeclarationWorker());
//		workerTree.addWorker(collector, new RelevantInvocationsWorker());
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

	private void setupFreemaker() {
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
		return Executors.newSingleThreadExecutor();
	}

}
