package rxrefactoring;

import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.*;

import de.tudarmstadt.stg.rx.swingworker.SWDto;
import de.tudarmstadt.stg.rx.swingworker.SWEmitter;
import de.tudarmstadt.stg.rx.swingworker.SWSubscriber;
import rx.Emitter;
import rx.Observable;

public class Assignment
{
	private static final int AMOUNT_OF_WORK = 10;
	private static final long TIME_FOR_WORK_UNIT = 2000L;

	private SWSubscriber<String, Integer> rxObserver;
	private SWSubscriber<String, Integer> anotherRxObserver;

	public void someMethod()
	{
		Observable<SWDto<String, Integer>> rxObservable = Observable.fromEmitter(new SWEmitter<String, Integer>()
		{
			@Override
			protected String doInBackground() throws Exception
			{
				System.out.println( "Entering doInBackground() method" );
				for ( int i = 0; i < AMOUNT_OF_WORK * 2; i = i + 2 )
				{
					publish( i, i + 1 );
					Thread.sleep( TIME_FOR_WORK_UNIT );
				}
				System.out.println( "doInBackground() finished successfully" );
				return "Async Result";
			}
		}, Emitter.BackpressureMode.BUFFER );

		rxObserver = new SWSubscriber<String, Integer>( rxObservable )
		{
			@Override
			protected void process( List<Integer> chunks )
			{
				for ( Integer number : chunks )
				{
					System.out.println( "Processing " + number );
					setProgress( number * 100 / ( AMOUNT_OF_WORK * 2 ) );
				}
			}

			@Override
			protected void done()
			{
				try
				{
					System.out.println("Entering done() method");
					String result = get();
					System.out.println("doInBackground() result = " + result);
				}
				catch ( InterruptedException e )
				{
					System.out.println("InterruptedException");
				}
				catch ( ExecutionException e )
				{
					System.out.println("ExecutionException");
				}
			}
		};

		anotherRxObserver = rxObserver;
	}
}