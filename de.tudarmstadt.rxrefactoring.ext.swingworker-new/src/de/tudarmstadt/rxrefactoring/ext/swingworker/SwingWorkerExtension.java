package de.tudarmstadt.rxrefactoring.ext.swingworker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.eclipse.jdt.annotation.NonNull;

import de.tudarmstadt.rxrefactoring.core.IRefactorExtension;
import de.tudarmstadt.rxrefactoring.core.IWorkerRef;
import de.tudarmstadt.rxrefactoring.core.IWorkerTree;
import de.tudarmstadt.rxrefactoring.ext.swingworker.workers.*;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;

/**
 * Description: Extension class for SwingWorker<br>
 * Author: Camila Gonzalez<br>
 * Created: 18/01/2018
 */
public class SwingWorkerExtension implements IRefactorExtension{

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
		workerTree.addWorker(collector, new AssignmentWorker());	
		workerTree.addWorker(collector, new FieldDeclarationWorker());	
		workerTree.addWorker(collector, new MethodInvocationWorker());	
		workerTree.addWorker(collector, new VariableDeclStatementWorker());	
		workerTree.addWorker(collector, new SimpleNameWorker());	
		workerTree.addWorker(collector, new SingleVariableDeclWorker());	
		workerTree.addWorker(collector, new ClassInstanceCreationWorker());	
		workerTree.addWorker(collector, new TypeDeclarationWorker());	
		workerTree.addWorker(collector, new MethodDeclarationWorker());	
	}

	@Override
	public @NonNull String getPlugInId() {
		return "de.tudarmstadt.rxrefactoring.ext.swingworker";
	}
	
	
	// TODO Check if freemaker was added correcly
	
	public static final String TEMPLATES_DIR_NAME = "templates";
	private static Configuration freemakerCfg;
	
	public static Configuration getFreemakerCfg()
	{
		return freemakerCfg;
	}

	private void setupFreemaker()
	{
		String pluginDir = getPlugInId();
		String templatesDirName = Paths.get( pluginDir, TEMPLATES_DIR_NAME ).toAbsolutePath().toString();
		freemakerCfg = new Configuration( Configuration.VERSION_2_3_25 );
		freemakerCfg.setDefaultEncoding( "UTF-8" );
		freemakerCfg.setTemplateExceptionHandler( TemplateExceptionHandler.RETHROW_HANDLER );
		freemakerCfg.setLogTemplateExceptions( false );
		try
		{
			freemakerCfg.setDirectoryForTemplateLoading( new File( templatesDirName ) );
		}
		catch ( IOException e )
		{
			throw new RuntimeException( "Exception in setDirectoryForTemplateLoading", e );
		}
	}

}
