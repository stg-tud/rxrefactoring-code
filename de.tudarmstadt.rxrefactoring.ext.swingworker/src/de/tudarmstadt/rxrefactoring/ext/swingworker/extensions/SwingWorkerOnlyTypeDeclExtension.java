package de.tudarmstadt.rxrefactoring.ext.swingworker.extensions;

import org.eclipse.jdt.annotation.NonNull;

import de.tudarmstadt.rxrefactoring.core.IWorkerRef;
import de.tudarmstadt.rxrefactoring.core.IWorkerTree;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorScope;
import de.tudarmstadt.rxrefactoring.ext.swingworker.workers.RxCollector;
import de.tudarmstadt.rxrefactoring.ext.swingworker.workers.refactor.TypeDeclarationWorker;

public class SwingWorkerOnlyTypeDeclExtension extends SwingWorkerExtension {
		
	@Override
	public @NonNull String getName() {
		return "SwingWorker to Observable only Type Declarations";
	}
	
	@Override
	public boolean hasInteractiveRefactorScope() {
		return false;
	}
	
	@Override
	public boolean onlyScanOpenFile() {
		return true;
	}
	
	@Override
	public String getDescription() {
		return "Only Type Declaration";
	}
	
	@Override
	public RefactorScope getRefactorScope() {
		return RefactorScope.ONLY_ONE_OCCURENCE;
	}
	
	@Override
	public void addWorkersTo(@NonNull IWorkerTree workerTree) {
		setupFreemaker();
		IWorkerRef<Void, RxCollector> collector = workerTree.addWorker(new RxCollector(getRefactorScope()));

		workerTree.addWorker(collector, new TypeDeclarationWorker());

	}
	
	

}
