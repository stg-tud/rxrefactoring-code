package de.tudarmstadt.rxrefactoring.ext.asynctask;


import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import de.tudarmstadt.rxrefactoring.core.IWorkerRef;
import de.tudarmstadt.rxrefactoring.core.IWorkerTree;
import de.tudarmstadt.rxrefactoring.core.RefactorExtension;
import de.tudarmstadt.rxrefactoring.ext.asynctask.workers.AnonymAsyncTaskWorker;
import de.tudarmstadt.rxrefactoring.ext.asynctask.workers.AsyncTaskCollector;
import de.tudarmstadt.rxrefactoring.ext.asynctask.workers.SubClassAsyncTaskWorker;

/**
 * Author: Template<br>
 * Created: 01/18/2017
 */
public class AsyncTaskRefactoring implements RefactorExtension {

	@Override
	public String getPlugInId() {		
		return "de.tudarmstadt.rxrefactoring.ext.asynctask";
	}
	
	@Override
	public IPath getResourceDir() {
		return new Path("./resources/");
	}

	@Override
	public IPath getDestinationDir() {
		return new Path("./libs/");
	}

	@Override
	public String getDescription() {		
		return "Refactor AsyncTask to Observable";
	}

	@Override
	public void addWorkersTo(IWorkerTree workerTree) {
		IWorkerRef<Void,AsyncTaskCollector> a = 
				workerTree.addWorker(new AsyncTaskCollector());	
		
		workerTree.addWorker(a, new AnonymAsyncTaskWorker());
//		workerTree.addWorker(a, new CachedAnonymousTaskWorker());
		workerTree.addWorker(a, new SubClassAsyncTaskWorker());
	}

	@Override
	public String getName() {
		return "AsyncTask to RxJava";
	}

	

}
