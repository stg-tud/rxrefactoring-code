package rxrefactoring.anonymous;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.swing.SwingWorker;

public class AnonymousClassCase10
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
				String result = null;
				try
				{
					result = get( 3L, TimeUnit.SECONDS );
				}
				catch ( InterruptedException | ExecutionException | TimeoutException e )
				{
					e.printStackTrace();
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
