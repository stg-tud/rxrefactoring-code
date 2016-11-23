package rxrefactoring.anonymous_complex;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.SwingWorker;

public class AnonymousComplexCase2
{

	public void start()
	{
		new SwingWorker<String, Integer>()
		{
			private static final long SLEEP_TIME = 1000L;
			private AtomicInteger iterationCounter;

			@Override
			protected String doInBackground() throws Exception
			{
				iterationCounter = new AtomicInteger(0);
				for (int i = 0; i < 10; i++)
				{
					iterationCounter.set(i);
					longRunningOperation();
					publish(i * 10); // can be used to update the progress
				}
				return "DONE";
			}

			@Override
			protected void process(List<Integer> chunks)
			{
				for (Integer i : chunks)
				{
					System.out.println("Iteration = " + iterationCounter.get() + "; Progress = " + i + "%");
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

			private void longRunningOperation() throws InterruptedException
			{
				Thread.sleep(SLEEP_TIME);
				System.out.println( "[Thread: " + Thread.currentThread().getName() + "] Long running operation completed." );
			}
		}.execute();
	}
}