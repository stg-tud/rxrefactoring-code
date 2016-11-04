package futureexample;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

public class FutureContext {
	
	private List<Future<Integer>> futureList = new ArrayList<Future<Integer>>();

	public List<Future<Integer>> getFutureList() {
		return futureList;
	}

	public void addFuture(Future<Integer> future) {
		this.futureList.add(future);
	}
	
}
