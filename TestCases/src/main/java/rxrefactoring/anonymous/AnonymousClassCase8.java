package rxrefactoring.anonymous;

import java.util.concurrent.TimeUnit;

import javax.swing.SwingWorker;

public class AnonymousClassCase8
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
					String result = get(3L, TimeUnit.SECONDS);
					System.out.println("[Thread: " + Thread.currentThread().getName() + "] Result:" + result);
					Thread.sleep( 1000L );
				}
				catch ( Exception e )
				{
					e.printStackTrace();
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
