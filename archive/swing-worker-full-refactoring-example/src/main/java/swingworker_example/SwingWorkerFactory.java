package swingworker_example;

import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.*;

import utils.PrintUtils;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/02/2016
 */
public class SwingWorkerFactory
{
	public static SwingWorker<String, Integer> createSwingWorker( final int amountOfWork )
	{
		return new SwingWorker<String, Integer>()
		{
			private static final long TIME_FOR_WORK_UNIT = 2000L;

			@Override
			protected String doInBackground() throws Exception
			{
				PrintUtils.printMessage( "Entering doInBackground() method" );
				for ( int i = 0; i < amountOfWork * 2; i = i + 2 )
				{
					publish( i, i + 1 );
					Thread.sleep( TIME_FOR_WORK_UNIT );
				}
				PrintUtils.printMessage( "doInBackground() finished successfully" );
				return "Async Result";
			}

			@Override
			protected void process( List<Integer> chunks )
			{
				for ( Integer number : chunks )
				{
					PrintUtils.printMessage( "Processing " + number );
					setProgress( number * 100 / ( amountOfWork * 2 ) );
				}
			}

			@Override
			protected void done()
			{
				try
				{
					PrintUtils.printMessage( "Entering done() method" );
					String result = get();
					PrintUtils.printMessage( "doInBackground() result = " + result );
				}
				catch ( InterruptedException e )
				{
					PrintUtils.printMessage( "InterruptedException" );
				}
				catch ( ExecutionException e )
				{
					PrintUtils.printMessage( "ExecutionException" );
				}
			}
		};
	}
}
