package rxrefactoring.anonymous.simple;

import javax.swing.SwingWorker;

public class AnonymousClassCase15
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
					// using super.get() instead of get(); the keyword "super" must be removed
					System.out.println("[Thread: " + Thread.currentThread().getName() + "] Result:" + super.get());
				}
				catch ( Exception e )
				{
					System.err.println("Exception");
				}
				super.done(); // this line will be removed
			}
		}.execute();
	}

	private void longRunningOperation() throws InterruptedException
	{
		Thread.sleep( 2000L );
		System.out.println( "[Thread: " + Thread.currentThread().getName() + "] Long running operation completed." );
	}
}
