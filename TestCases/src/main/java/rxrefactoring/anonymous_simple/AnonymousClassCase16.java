package rxrefactoring.anonymous_simple;

import java.util.List;

import javax.swing.*;

public class AnonymousClassCase16
{
	public void start()
	{
		new SwingWorker<String, Integer>()
		{
			@Override
			protected String doInBackground() throws Exception
			{
				for (int i = 0; i < 10; i++)
				{
					longRunningOperation();
					super.publish(i * 10);
				}
				return "DONE";
			}

			@Override
			protected void process(List<Integer> chunks)
			{
				for (Integer i : chunks)
				{
					System.out.println("Progress = " + i + "%");
				}
				super.process(chunks); // this line will be removed
			}

			@Override
			protected void done()
			{
				try
				{
					String result = super.get(); // "super.get()" will be replaced by "asyncResult"
					System.out.println("[Thread: " + Thread.currentThread().getName() + "] Result:" + result);
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
		Thread.sleep( 1000L );
		System.out.println( "[Thread: " + Thread.currentThread().getName() + "] Long running operation completed." );
	}
}