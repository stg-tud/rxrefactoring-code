package rxrefactoring;

import javax.swing.SwingWorker;
import java.util.concurrent.TimeUnit;

public class AnonymousClassCase3
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
				// try-catch block should be gone after refactoring
				try
				{
					// In contrast to case 2, here a get with timeout is used
					String result = get(3L, TimeUnit.SECONDS);
					System.out.println("[Thread: " + Thread.currentThread().getName() + "] Result:" + result);
				}
				catch ( Exception e )
				{
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
