package complete_swingworker;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import complete_swingworker.wrapper_class.SwingWorkerSubscriber;
import complete_swingworker.wrapper_class.SwingWorkerSubscriberDto;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.exceptions.Exceptions;
import rx.schedulers.Schedulers;
import rx.schedulers.SwingScheduler;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/01/2016
 */
public class RxSwingWorkerExample
{
    private SwingWorkerSubscriber<String, Integer> observer;
    private Observable<SwingWorkerSubscriberDto<String, Integer>> observable;
    private Subscription subscription;

	public RxSwingWorkerExample( long timeout, int amountOfWork )
	{
		observer = new SwingWorkerSubscriber<String, Integer>()
		{
			@Override
			protected void process(List<Integer> chunks )
			{
				for ( Integer number : chunks )
				{
					printMessage( "Processing " + number );
					setProgress( number * 100 / ( amountOfWork * 2 ) );
				}
			}

			@Override
			protected void done( String asyncResult )
			{
				printMessage( "Entering done() method" );
				String result = asyncResult;
				printMessage( "doInBackground() result = " + result );
			}

			@Override
			public void onError( Throwable throwable )
			{
				Exceptions.propagate( throwable );
			}
		};

        observable = Observable.create(new Observable.OnSubscribe<SwingWorkerSubscriberDto<String, Integer>>()
        {
            @Override
            public void call(Subscriber<? super SwingWorkerSubscriberDto<String, Integer>> observer)
            {
                try
                {
                    if ( !observer.isUnsubscribed() )
                    {
                        SwingWorkerSubscriberDto<String, Integer> dto = new SwingWorkerSubscriberDto<>();
                        observer.onStart();
                        printMessage( "Entering doInBackground() method" );
                        for ( int i = 0; i < amountOfWork * 2; i = i + 2 )
                        {
                            Thread.sleep( 2000L );
                            observer.onNext( dto.send( i, i + 1 ) );
                        }
                        printMessage( "doInBackground() finished successfully" );
                        String asyncResult = "Async Result";
                        observer.onNext( dto.setResult( asyncResult ) );
                        observer.onCompleted();
                    }
                }
                catch ( Exception e )
                {
                    observer.onError(e);
                }

            }
        }).observeOn(SwingScheduler.getInstance())
                .timeout(timeout, TimeUnit.SECONDS);
    }

	private static void printMessage( String message )
	{
		System.out.println( "[" + Thread.currentThread().getName() + "]" + " - " + message );
	}

    // PROPERTIES: default "state", "progress"
    public void addPropertyChangeListenerToSwingWorker( PropertyChangeListener listener )
    {
        observer.addPropertyChangeListener( listener );
    }

    public void firePropertyChangeInSwingWorker( String propertyName, Object oldValue, Object newValue )
    {
        observer.firePropertyChange( propertyName, oldValue, newValue );
    }

    public PropertyChangeSupport getSwingWorkerPropertyChangeSupport()
    {
        return observer.getPropertyChangeSupport();
    }

    public void removePropertyChangeListerFromSwingWorker( PropertyChangeListener listener )
    {
        observer.removePropertyChangeListener( listener );
    }

    // WORKFLOW
    public boolean cancelSwingWorker( boolean mayInterruptIfRunning )
    {
        if ( this.subscription != null && mayInterruptIfRunning )
        {
            this.subscription.unsubscribe();
            return true;
        }
        else
        {
            return false;
        }
    }

    public void executeSwingWorker()
    {
        subscription = observable.subscribeOn(Schedulers.computation()).subscribe(observer);
    }

    public void runSwingWorker()
    {
        subscription = observable.subscribeOn(Schedulers.immediate()).subscribe(observer);
    }

    public int getSwingWorkerProgress()
    {
        return observer.getProgress();
    }

    public int getSwingWorkerState()
    {
        return observer.getState();
    }

    public boolean isSwingWorkerCancelled()
    {
        return observer.isCancelled();
    }

    public boolean isSwingWorkerDone()
    {
        return observer.isDone();
    }

    // GET RESULT
    public String getBlockingFromSwingWorker() throws ExecutionException, InterruptedException
    {
        return observable.toBlocking().single().getResult();
    }

    public String getBlockingFromSwingWorker( long timeout, TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException
    {
        return observable.timeout( timeout, unit ).toBlocking().single().getResult();
    }
}
