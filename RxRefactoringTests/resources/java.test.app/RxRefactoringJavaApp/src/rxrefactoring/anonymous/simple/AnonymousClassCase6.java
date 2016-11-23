package rxrefactoring.anonymous.simple;

import java.util.concurrent.TimeUnit;

import javax.swing.SwingWorker;

public class AnonymousClassCase6
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
					// the following get() method matching the parameter types of SwingWorker#get() should
					// not be replaced during refactoring
					String anotherGet = AnonymousClassCase6.this.get();
					System.out.println("[Thread: " + Thread.currentThread().getName() + "] Result:" + result);
				}
				catch ( Exception e )
				{
					System.err.println("Exception");
				}
			}
		}.execute();
	}

	private String get()
	{
		return "some string";
	}

	private void longRunningOperation() throws InterruptedException
	{
		Thread.sleep( 4000L );
		System.out.println( "[Thread: " + Thread.currentThread().getName() + "] Long running operation completed." );
	}
}