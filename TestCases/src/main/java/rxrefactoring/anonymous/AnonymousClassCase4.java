package rxrefactoring.anonymous;

import java.util.concurrent.TimeUnit;

import javax.swing.SwingWorker;

public class AnonymousClassCase4
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
				try
				{
					// uses get(3L, TimeUnit.SECONDS) directly as a parameter
					System.out.println("[Thread: " + Thread.currentThread().getName() + "] Result:" + get(3L, TimeUnit.SECONDS));
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
		Thread.sleep( 4000L );
		System.out.println( "[Thread: " + Thread.currentThread().getName() + "] Long running operation completed." );
	}
}
