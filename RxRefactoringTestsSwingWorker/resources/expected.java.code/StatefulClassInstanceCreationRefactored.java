package rxrefactoring;

import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.*;

import de.tudarmstadt.stg.rx.swingworker.SWChannel;
import de.tudarmstadt.stg.rx.swingworker.SWEmitter;
import de.tudarmstadt.stg.rx.swingworker.SWSubscriber;
import rx.Emitter;

public class StatefulClassInstanceCreation
{
	public void someMethod()
	{
		class RxObserver extends SWSubscriber<String, Integer>
		{
			private static final int AMOUNT_OF_WORK = 10;
			private static final long TIME_FOR_WORK_UNIT = 2000L;

			RxObserver()
			{
				setObservable( getRxObservable() );
			}

			private rx.Observable<SWChannel<String, Integer>> getRxObservable()
			{
				return rx.Observable.fromEmitter( new SWEmitter<String, Integer>()
				{
					@Override
					protected String doInBackground() throws Exception
					{
						RxObserver.this.printInfo( "Entering doInBackground() method" );
						for ( int i = 0; i < RxObserver.this.AMOUNT_OF_WORK * 2; i = i + 2 )
						{
							publish( i, i + 1 );
							Thread.sleep( RxObserver.this.TIME_FOR_WORK_UNIT );
						}
						RxObserver.this.printInfo( "doInBackground() finished successfully" );
						return "Async Result";
					}
				}, Emitter.BackpressureMode.BUFFER );
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
		}

		new RxObserver();

		class RxObserver1 extends SWSubscriber<String, Integer>
		{
			private int number;

			RxObserver1()
			{
				setObservable( getRxObservable() );
			}

			private rx.Observable<SWChannel<String, Integer>> getRxObservable()
			{
				return rx.Observable.fromEmitter( new SWEmitter<String, Integer>()
				{
					@Override
					protected String doInBackground() throws Exception
					{
						return null;
					}
				}, Emitter.BackpressureMode.BUFFER );
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
		}

		new RxObserver1().executeObservable();
	}
}