package net.antonyho.futuretasktest;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class FutureWork extends FutureTask<Object> {
	
	private FutureCallback callback;

	public FutureWork(Callable<Object> callable) {
		super(callable);
	}
	
	public FutureCallback getCallback() {
		return callback;
	}

	public void setCallback(FutureCallback callback) {
		this.callback = callback;
	}

	@Override
	protected void done() {
		if (callback != null)
			callback.call();
	}

}
