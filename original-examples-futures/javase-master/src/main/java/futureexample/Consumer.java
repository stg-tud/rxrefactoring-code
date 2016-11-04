package futureexample;

import java.util.concurrent.Future;

public class Consumer implements Runnable {
	private FutureContext futureContext;
	
	public Consumer(FutureContext futureContext) {
		this.futureContext = futureContext;
	}
	
	public void run() {
		try{
			Thread.sleep(3000);
			
			int sum = 0;
			for(Future<Integer> fu : futureContext.getFutureList()) {
				sum += fu.get();
			}
			System.out.println("Three seconds later, Consumer finished, sum=" + sum);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

}
