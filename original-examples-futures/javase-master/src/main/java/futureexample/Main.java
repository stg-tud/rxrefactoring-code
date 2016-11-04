package futureexample;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {

	public static void main(String[] args) throws Exception {
		FutureContext futureContext = new FutureContext(); // specific implementation
		
		ExecutorService executorService = Executors.newCachedThreadPool();
		for(int i = 0; i <= 9; i++) {
			Future<Integer> future = executorService.submit(new Worker(i * 10 + 1, i * 10 + 10));
			futureContext.addFuture(future); // specific implementation (not a general pattern)
		}
		executorService.shutdown();
		
		new Thread(new Consumer(futureContext)).start();
		System.out.println("Main thread finished.");
	}
	
}