package rxrefactoring.anonymous.simple;

import javax.swing.SwingWorker;

public class AnonymousClassCase1
{
	public void start()
	{
		// Anonymous class declaration of SwingWorker
		// class is not assigned to a variable
		// only implementing doInBackground
		new SwingWorker<String, Integer>()
		{
			@Override
			protected String doInBackground() throws Exception
			{
				longRunningOperation();
				return "DONE";
			}

		}.execute();
	}

	private void longRunningOperation() throws InterruptedException
	{
		Thread.sleep( 2000L );
		System.out.println( "[Thread: " + Thread.currentThread().getName() + "] Long running operation completed." );
	}
}