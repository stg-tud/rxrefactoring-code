package swingworker_rx;

import java.util.List;
import java.util.concurrent.ExecutionException;

import rx.Emitter;
import rx.Observable;
import rxswingworker.SWDto;
import rxswingworker.SWEmitter;
import rxswingworker.SWSubscriber;
import utils.PrintUtils;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/02/2016
 */
public class RxSwingWorkerFactoryFromEmitter2
{
	private static final long TIME_FOR_WORK_UNIT = 1L;

	public static SWSubscriber<String, Integer> createObserver( final int amountOfWork )
	{
		class RxObserver extends SWSubscriber<String, Integer>
		{
			RxObserver()
			{
				setObservable(getRxObservable());
			}

			Observable<SWDto<String, Integer>> getRxObservable()
			{
				return Observable.fromEmitter(new SWEmitter<String, Integer>()
                            {
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
                            }, Emitter.BackpressureMode.BUFFER );
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
			protected void done( String asyncResult )
			{
				PrintUtils.printMessage( "Entering done() method" );
				String result = asyncResult;
				PrintUtils.printMessage( "doInBackground() result = " + result );
			}
		}

		return new RxObserver();
	}
}