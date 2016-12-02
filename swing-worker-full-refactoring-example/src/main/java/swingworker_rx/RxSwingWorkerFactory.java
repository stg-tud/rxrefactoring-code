package swingworker_rx;

import rx.Observable;
import rxswingworker.SwingWorkerRxOnSubscribe;
import rxswingworker.SwingWorkerSubscriber;
import rxswingworker.SwingWorkerSubscriberDto;
import utils.PrintUtils;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/02/2016
 */
public class RxSwingWorkerFactory
{
	private static final long TIME_FOR_WORK_UNIT = 2000L;

	public static SwingWorkerSubscriber<String, Integer> createObserver( final int amountOfWork )
	{
		Observable<SwingWorkerSubscriberDto<String, Integer>> observable = Observable.create( new SwingWorkerRxOnSubscribe<String, Integer>()
		{
			@Override
			protected String doInBackground() throws Exception
			{
				PrintUtils.printMessage( "Entering doInBackground() method" );
				for ( int i = 0; i < amountOfWork * 2; i = i + 2 )
				{
					Thread.sleep( TIME_FOR_WORK_UNIT );
					publish( i, i + 1 );
				}
				PrintUtils.printMessage( "doInBackground() finished successfully" );
				return "Async Result";
			}

		} );

		return new SwingWorkerSubscriber<String, Integer>( observable )
		{
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
			protected void done( String asyncResult )
			{
				PrintUtils.printMessage( "Entering done() method" );
				String result = asyncResult;
				PrintUtils.printMessage( "doInBackground() result = " + result );
			}

			@Override
			public void onError( Throwable throwable )
			{
				if ( throwable instanceof InterruptedException )
				{
					PrintUtils.printMessage( "InterruptedException" );
				}
				else if ( throwable instanceof ExecutionException )
				{
					PrintUtils.printMessage( "ExecutionException" );
				}
				throwable.printStackTrace();
			}
		};
	}
}
