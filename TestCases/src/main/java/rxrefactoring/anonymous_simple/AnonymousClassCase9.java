package rxrefactoring.anonymous_simple;

import java.util.concurrent.TimeUnit;

import javax.swing.*;

public class AnonymousClassCase9
{
	public void start()
	{
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
				// Only the first try-catch block should be removed
				try
				{
					String result = get(3L, TimeUnit.SECONDS);
					System.out.println("[Thread: " + Thread.currentThread().getName() + "] Result:" + result);
				}
				catch ( Exception e )
				{
					System.err.println("Exception");
				}
				try
				{
					Thread.sleep( 1000L );
				}
				catch ( InterruptedException e )
				{
					System.err.println("InterruptedException");
				}
			}
		}.execute();
	}

	private void longRunningOperation() throws InterruptedException
	{
		Thread.sleep( 4000L );
		System.out.println( "[Thread: " + Thread.currentThread().getName() + "] Long running operation completed." );
	}
}