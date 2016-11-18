package rxrefactoring.anonymous;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.swing.SwingWorker;

public class AnonymousClassCase7
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
				// Multiple catch blocks originated by get(long, TimeUnit)
				String result = null;
				try
				{
					result = get( 3L, TimeUnit.SECONDS );
				}
				catch ( InterruptedException e )
				{
					System.err.println("InterruptedException");
				}
				catch ( ExecutionException e )
				{
					System.err.println("ExecutionException");
				}
				catch ( TimeoutException e )
				{
					System.err.println("TimeoutException");
				}
				System.out.println( "[Thread: " + Thread.currentThread().getName() + "] Result:" + result );
			}
		}.execute();
	}

	private void longRunningOperation() throws InterruptedException
	{
		Thread.sleep( 4000L );
		System.out.println( "[Thread: " + Thread.currentThread().getName() + "] Long running operation completed." );
	}
}
