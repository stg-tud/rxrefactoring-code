package de.tudarmstadt.rxrefactoring.ext.template;

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNull;

import de.tudarmstadt.rxrefactoring.core.IRefactorExtension;
import de.tudarmstadt.rxrefactoring.core.IWorkerRef;
import de.tudarmstadt.rxrefactoring.core.IWorkerTree;
import de.tudarmstadt.rxrefactoring.core.analysis.impl.reachingdefinitions.UseDef;
import de.tudarmstadt.rxrefactoring.ext.template.workers.TemplateCollector;
import de.tudarmstadt.rxrefactoring.ext.template.workers.TemplatePreconditionWorker;
import de.tudarmstadt.rxrefactoring.ext.template.workers.TemplateUseDef;
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
		/**
		 * TODO 3 - Add workers that are needed for the refactoring to the worker tree. 
		 * Workers can be added specifying the location of their parent in the tree so 
		 * they will receive their output as input for the refactoring method.
		 * The workers are usually ordered in the following way:
		 * 
		 * 1. Precondition Analysis (if the refactoring should only take place under 
		 * certain conditions): Perform an AST analysis to gather the uses of each 
		 * instantiation. Collect instantiations of the constructs that are to be 
		 * refactored, such as subclasses of a certain class or its collections, and
		 * exclude those that should not be refactored because of how they are used.
		 * 
		 * 2. Collection: Gather the relevant constructs or information from each 
		 * compilation unit.
		 * 
		 * 3. Refactoring: Make changes to the gathered structures. Refactoring 
		 * workers use the output of the collector. There is usually one worker per 
		 * type of node.
		 */
		IWorkerRef<Void, Collection<UseDef>> analysis = workerTree.addWorker(new TemplateUseDef());	
		IWorkerRef<Collection<UseDef>, TemplatePreconditionWorker> preconditionWorker = workerTree.addWorker(analysis, new TemplatePreconditionWorker());	
		IWorkerRef<TemplatePreconditionWorker, TemplateCollector> collector = workerTree.addWorker(preconditionWorker, new TemplateCollector());	
		workerTree.addWorker(collector, new TemplateWorker());	
	}

	@Override
	public @NonNull String getPlugInId() {
		// TODO 4 - Change to the path to that of the current extension.
		return "de.tudarmstadt.rxrefactoring.ext.template";
	}

}
