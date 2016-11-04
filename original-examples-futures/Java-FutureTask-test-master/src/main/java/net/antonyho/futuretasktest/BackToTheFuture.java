package net.antonyho.futuretasktest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

public class BackToTheFuture {

	public static void main(String[] args) throws InterruptedException {
		List<FutureTask> taskList = new ArrayList<FutureTask>();
		ExecutorService executor = Executors.newFixedThreadPool(5);
		//ExecutorService executor = Executors.newCachedThreadPool();
		long startMillis = System.currentTimeMillis();
		for (int i = 0; i < 20; ++i) {
			Callable callable = new PossibleMission(i);
			FutureWork futureTask = new FutureWork(callable);
			FutureCallback callback = new FutureCallback(futureTask) {
				@Override
				public void call() {
					System.out.println((String) this.getReturnedObject());
				}
			};
			futureTask.setCallback(callback);
			taskList.add(futureTask);
			executor.execute(futureTask);
		}

		executor.shutdown();
		executor.awaitTermination(10, TimeUnit.MINUTES);
		long endMillis = System.currentTimeMillis();
		System.out.printf("Elapsed time: %d ms\n", (endMillis - startMillis));
	}

}
