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
	}

	private void doSomethingElse(SwingWorker swingWorker) 
	{
		swingWorker.execute();
	}
	
}