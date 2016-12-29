package rxrefactoring.anonymous.complex;

import java.util.List;

import javax.swing.SwingWorker;

public class AnonymousComplexCase1
{

	public void start()
	{
		// SwingWorker has a private field and a private method
		// A helper class is needed to refactor this case
		new SwingWorker<String, Integer>()
		{
			private static final long SLEEP_TIME = 1000L;

			@Override
			protected String doInBackground() throws Exception
			{
				for (int i = 0; i < 10; i++)
				{
					longRunningOperation();
				}
				return "DONE";
			}

			@Override
			protected void done()
			{
				try
				{
					String result = get();
					System.out.println("[Thread: " + Thread.currentThread().getName() + "] Result:" + result);
				}
				catch ( Exception e )
				{
					System.err.println("Exception");
				}
			}

			private void longRunningOperation() throws InterruptedException
			{
				Thread.sleep(SLEEP_TIME);
				System.out.println( "[Thread: " + Thread.currentThread().getName() + "] Long running operation completed." );
			}
		}.execute();
	}
}