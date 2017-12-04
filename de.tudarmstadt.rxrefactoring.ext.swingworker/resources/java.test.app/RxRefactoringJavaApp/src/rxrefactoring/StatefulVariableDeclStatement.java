package rxrefactoring;

import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.*;

public class StatefulVariableDeclStatement
{
	public void someMethod()
	{
		SwingWorker<String, Integer> swingWorker = new SwingWorker<String, Integer>()
		{
			private static final int AMOUNT_OF_WORK = 10;
			private static final long TIME_FOR_WORK_UNIT = 2000L;

			@Override
			protected String doInBackground() throws Exception
			{
				printInfo( "Entering doInBackground() method" );
				for ( int i = 0; i < AMOUNT_OF_WORK * 2; i = i + 2 )
				{
					publish( i, i + 1 );
					Thread.sleep( TIME_FOR_WORK_UNIT );
				}
				printInfo( "doInBackground() finished successfully" );
				return "Async Result";
			}

			@Override
			protected void process( List<Integer> chunks )
			{
				for ( Integer number : chunks )
				{
					printInfo( "Processing " + number );
					setProgress( number * 100 / ( AMOUNT_OF_WORK * 2 ) );
				}
			}

			@Override
			protected void done()
			{
				try
				{
					printInfo( "Entering done() method" );
					String result = get();
					printInfo( "doInBackground() result = " + result );
				}
				catch ( InterruptedException e )
				{
					printInfo( "InterruptedException" );
				}
				catch ( ExecutionException e )
				{
					printInfo( "ExecutionException" );
				}
			}

			private void printInfo( String message )
			{
				System.out.println( message );
			}
		};

		SwingWorker<String, Integer> swingWorker2 = swingWorker;
	}
}