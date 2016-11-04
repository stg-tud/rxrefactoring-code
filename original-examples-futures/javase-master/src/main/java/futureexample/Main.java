package futureexample;

import rx.Observable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {

	public static void main(String[] args) throws Exception {
		FutureContext futureContext = new FutureContext();
		
		ExecutorService executorService = Executors.newCachedThreadPool();
		for(int i = 0; i <= 9; i++) {
			Future<Integer> future = executorService.submit(new Worker(i * 10 + 1, i * 10 + 10));
			futureContext.addFuture(future);
		}
		executorService.shutdown();
		
		new Thread(new Consumer(futureContext)).start();
		System.out.println("Main thread finished.");


	}
	
}
