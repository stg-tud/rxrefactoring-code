package rxrefactoring;

import javax.swing.SwingWorker;

public class AnonymousClassCase2
{
	public void start()
	{
		// Anonymous class declaration of SwingWorker
		// class is not assigned to a variable
		// implementing doInBackground and done
		new SwingWorker<String, Integer>()
		{
			@Override
			protected String doInBackground() throws Exception
			{
				longRunningOperation();
				return "DONE";
			}

			@Override
			protected void done()
			{
				// Internally done is in charge of invoking doInBackground
				// after do in background is completed, then this code block is executed
				// try-catch block should be no longer there after refactoring
				try
				{
					String result = get(); // get() gets result from doInBackground
					System.out.println("[Thread: " + Thread.currentThread().getName() + "] Result:" + result);
				}
				catch ( Exception e )
				{
					// get() can throw an Exception
					System.err.println("Exception");
				}
			}
		}.execute();
	}

	private void longRunningOperation() throws InterruptedException
	{
		Thread.sleep( 2000L );
		System.out.println( "[Thread: " + Thread.currentThread().getName() + "] Long running operation completed." );
	}
}
