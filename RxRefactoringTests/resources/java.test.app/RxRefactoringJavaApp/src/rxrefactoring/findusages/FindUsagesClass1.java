package rxrefactoring.findusages;

import java.util.List;

import javax.swing.SwingWorker;

public class FindUsagesClass1
{
	public void start()
	{
		SwingWorker<String, Integer> swingWorker = new SwingWorker<String, Integer>()
		{
			@Override
			protected String doInBackground() throws Exception
			{
				longRunningOperation();
				return "TEST";
			}

		};

		doSomething(swingWorker);
	}

	private void doSomething(SwingWorker<String, Integer> sameSwingWorker)
	{
		sameSwingWorker.execute();
		sameSwingWorker.cancel(true);
	}

	private void longRunningOperation() throws InterruptedException
	{
		Thread.sleep( 1000L );
		System.out.println( "[Thread: " + Thread.currentThread().getName() + "] Long running operation completed." );
	}
}