package futureexample;

import java.util.concurrent.Callable;

public class Worker implements Callable<Integer> {
	private int start;
	private int end;
	
	public Worker(int start, int end) {
		this.start = start;
		this.end = end;
	}

	public Integer call() throws Exception {
		int sum = 0;
		for (int i = start; i <= end; i++) {
			sum += i;
		}
		System.out.println("Work[" + start + " , " + end + "], finished.");
		return sum;
	}
}
