package rxrefactoring;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.*;

public class SimpleName
{
	public void doSomething()
	{
		SwingWorker swingWorker = new SwingWorker(){

			@Override
			protected Object doInBackground() throws Exception 
			{
				return null;
			}
		};
		
		doSomethingElse(swingWorker);
		
		SwingWorker<String, Integer> swingWorker2 = new SwingWorker<String, Integer>(){

			@Override
			protected String doInBackground() throws Exception 
			{
				return null;
			}
		};
		
		doSomethingElseParameterized(swingWorker2);
		
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.submit(swingWorker);

		if ( swingWorker != null )
		{
			swingWorker.cancel( true );
		}
	}

	private void doSomethingElse(SwingWorker swingWorker) 
	{
		swingWorker.execute();
	}
	
	private void doSomethingElseParameterized(SwingWorker<String, Integer> swingWorker) 
	{
		swingWorker.execute();
	}
	
	
	
}