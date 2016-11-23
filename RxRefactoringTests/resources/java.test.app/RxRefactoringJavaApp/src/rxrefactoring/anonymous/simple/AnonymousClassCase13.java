package rxrefactoring.anonymous.simple;

import java.util.List;

import javax.swing.SwingWorker;

public class AnonymousClassCase13
{
	public void start()
	{
		// Anonymous class declaration of SwingWorker
		// class is not assigned to a variable
		// implementing doInBackground, done and process
		new SwingWorker<String, Integer>()
		{
			@Override
			protected String doInBackground() throws Exception
			{
				for (int i = 0; i < 10; i++)
				{
					longRunningOperation();
					publish(i * 10);
				}
				return "DONE 1";
			}

			@Override
			protected void process(List<Integer> chunks)
			{
				for (Integer i : chunks)
				{
					System.out.println("Progress 1 = " + i + "%");
				}
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
		}.execute();

		new SwingWorker<String, Integer>()
		{
			@Override
			protected String doInBackground() throws Exception
			{
				for (int i = 0; i < 10; i++)
				{
					longRunningOperation();
					publish(i * 10); // can be used to update the progress
				}
				return "DONE 2";
			}

			@Override
			protected void process(List<Integer> chunks)
			{
				for (Integer i : chunks)
				{
					System.out.println("Progress 2 = " + i + "%");
				}
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
		}.execute();
	}

	private void longRunningOperation() throws InterruptedException
	{
		Thread.sleep( 1000L );
		System.out.println( "[Thread: " + Thread.currentThread().getName() + "] Long running operation completed." );
	}
}