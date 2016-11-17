package rxrefactoring;

import java.util.concurrent.TimeUnit;

import javax.swing.SwingWorker;

public class AnonymousClassCase5
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
					// The following get method should not be replace during refactoring
					String anotherGet = AnonymousClassCase5.this.get("another get method invocation");
					System.out.println("[Thread: " + Thread.currentThread().getName() + "] Result:" + result);
				}
				catch ( Exception e )
				{
					e.printStackTrace();
				}
			}
		}.execute();
	}

	private String get(String string)
	{
		return string.toUpperCase();
	}

	private void longRunningOperation() throws InterruptedException
	{
		Thread.sleep( 4000L );
		System.out.println( "[Thread: " + Thread.currentThread().getName() + "] Long running operation completed." );
	}
}
