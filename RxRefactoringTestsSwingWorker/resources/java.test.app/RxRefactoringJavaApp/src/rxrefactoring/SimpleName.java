package rxrefactoring;

import java.util.Arrays;
import java.util.List;

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