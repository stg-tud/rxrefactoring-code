package net.antonyho.futuretasktest;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public abstract class FutureCallback {
	private FutureTask futureTask;
	
	public FutureCallback(FutureTask futureTask) {
		this.futureTask = futureTask;
	}

	public FutureTask getFutureTask() {
		return futureTask;
	}

	public void setFutureTask(FutureTask futureTask) {
		this.futureTask = futureTask;
	}

	public Object getReturnedObject() {
		try {
			return this.futureTask.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public abstract void call();

}
