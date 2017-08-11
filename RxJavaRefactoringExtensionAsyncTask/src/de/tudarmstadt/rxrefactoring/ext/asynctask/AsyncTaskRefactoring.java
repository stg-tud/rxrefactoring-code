package de.tudarmstadt.rxrefactoring.ext.asynctask;


import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import de.tudarmstadt.rxrefactoring.core.Refactoring;
import de.tudarmstadt.rxrefactoring.core.workers.WorkerTree;
import de.tudarmstadt.rxrefactoring.core.workers.WorkerTree.WorkerNode;
import de.tudarmstadt.rxrefactoring.ext.asynctask.workers.AnonymAsyncTaskWorker;
import de.tudarmstadt.rxrefactoring.ext.asynctask.workers.AsyncTaskCollector;
import de.tudarmstadt.rxrefactoring.ext.asynctask.workers.CachedAnonymousTaskWorker;
import de.tudarmstadt.rxrefactoring.ext.asynctask.workers.SubClassAsyncTaskWorker;

/**
 * Author: Template<br>
 * Created: 01/18/2017
 */
public class AsyncTaskRefactoring implements Refactoring {

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
		return new Path("./app/libs/");
	}

	@Override
	public String getDescription() {		
		return "Refactor AsyncTask to Observable";
	}

	@Override
	public void addWorkersTo(WorkerTree workerTree) {
		WorkerNode<Void,AsyncTaskCollector> a = 
				workerTree.addWorker(new AsyncTaskCollector());	
		
		workerTree.addWorker(a, new AnonymAsyncTaskWorker());
		workerTree.addWorker(a, new CachedAnonymousTaskWorker());
		workerTree.addWorker(a, new SubClassAsyncTaskWorker());
	}

	

}
