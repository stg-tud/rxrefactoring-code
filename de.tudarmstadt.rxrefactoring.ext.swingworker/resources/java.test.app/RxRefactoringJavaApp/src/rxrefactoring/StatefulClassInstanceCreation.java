package rxrefactoring;

import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.*;

public class StatefulClassInstanceCreation
{
	public void someMethod()
	{
		new SwingWorker<String, Integer>()
		{
			private static final int AMOUNT_OF_WORK = 10;
			private static final long TIME_FOR_WORK_UNIT = 2000L;

			@Override
			protected String doInBackground() throws Exception
			{
				this.printInfo( "Entering doInBackground() method" );
				for ( int i = 0; i < this.AMOUNT_OF_WORK * 2; i = i + 2 )
				{
					publish( i, i + 1 );
					Thread.sleep( this.TIME_FOR_WORK_UNIT );
				}
				this.printInfo( "doInBackground() finished successfully" );
				return "Async Result";
			}

			@Override
			protected void process( List<Integer> chunks )
			{
				for ( Integer number : chunks )
				{
					this.printInfo( "Processing " + number );
					setProgress( number * 100 / ( this.AMOUNT_OF_WORK * 2 ) );
				}
			}

			@Override
			protected void done()
			{
				try
				{
					this.printInfo( "Entering done() method" );
					String result = get();
					this.printInfo( "doInBackground() result = " + result );
				}
				catch ( InterruptedException e )
				{
					this.printInfo( "InterruptedException" );
				}
				catch ( ExecutionException e )
				{
					this.printInfo( "ExecutionException" );
				}
			}

			private void printInfo( String message )
			{
				System.out.println( message );
			}
		};

		new SwingWorker<String, Integer>()
		{
			private int number;

			@Override
			protected String doInBackground() throws Exception
			{
				return null;
			}

			private void doSomething()
			{
				new InnerClass().doSomethingInInnerClass();
			}

			class InnerClass
			{
				private int innerClassField;

				private void doSomethingInInnerClass()
				{
					System.out.println( "inner class" );
				}
			}
		}.execute();
	}
}