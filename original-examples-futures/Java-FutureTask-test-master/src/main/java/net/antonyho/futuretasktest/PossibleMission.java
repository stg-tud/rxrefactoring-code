package net.antonyho.futuretasktest;

import java.util.Random;
import java.util.concurrent.Callable;

public class PossibleMission implements Callable<Object> {
	
	private int threadNo;

	public PossibleMission(int threadNo) {
		super();
		this.threadNo = threadNo;
	}

	public int getThreadNo() {
		return threadNo;
	}

	public void setThreadNo(int threadNo) {
		this.threadNo = threadNo;
	}
	

	@Override
	public Object call() throws Exception {
		Thread.sleep(new Random().nextInt(20000));
		String hello = String.format("Hello from Marty McFly #%02d in 2015", this.threadNo);
		
		return hello;
	}

}
