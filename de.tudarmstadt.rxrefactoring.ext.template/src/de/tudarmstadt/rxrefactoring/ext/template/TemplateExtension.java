package de.tudarmstadt.rxrefactoring.ext.template;

import org.eclipse.jdt.annotation.NonNull;

import de.tudarmstadt.rxrefactoring.core.IRefactorExtension;
import de.tudarmstadt.rxrefactoring.core.IWorkerRef;
import de.tudarmstadt.rxrefactoring.core.IWorkerTree;
import de.tudarmstadt.rxrefactoring.ext.template.workers.TemplateCollector;
import de.tudarmstadt.rxrefactoring.ext.template.workers.TemplateWorker;

public class TemplateExtension implements IRefactorExtension{

	@Override
	public @NonNull String getName() {
		// TODO 1 - Change the "TemplateN" placeholders by the name of the constructs 
		// that are to be refactored.
		return "Template1 [and Template2] to rx.Observable";
	}

	@Override
	public @NonNull String getDescription() {
		// TODO 2 - Describe the kind of refactoring that is done by this extension.
		return "Refactor Template1 [and Template2] to Observable[s]...";
	}

	@Override
	public void addWorkersTo(@NonNull IWorkerTree workerTree) {
		// TODO 3 - Add workers that are needed for the refactoring to the worker tree. 
		// The top-level worker is usually a collector that gathers relevant constructs 
		// or information from the compilation units. Other workers are added specifying 
		// the location of their parent (usually the collector) in the tree so they will 
		// receive their output as input for the refactoring method.
		IWorkerRef<Void, TemplateCollector> collector = workerTree.addWorker(new TemplateCollector());	
		workerTree.addWorker(collector, new TemplateWorker());	
	}

	@Override
	public @NonNull String getPlugInId() {
		// TODO 4 - Change to the path to that of the current extension.
		return "de.tudarmstadt.rxrefactoring.ext.template";
	}

}
