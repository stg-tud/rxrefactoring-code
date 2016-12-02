package complete_swingworker.helper_classes;

import rx.Observable;
import rx.Subscriber;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/02/2016
 */
public abstract class SwingWorkerRxObservable<ReturnType, ProcessType> implements Observable.OnSubscribe<SwingWorkerSubscriberDto<ReturnType, ProcessType>>
{
    private Subscriber<? super SwingWorkerSubscriberDto<ReturnType, ProcessType>> observer;
    private SwingWorkerSubscriberDto<ReturnType, ProcessType> dto;

    @Override
    public void call(Subscriber<? super SwingWorkerSubscriberDto<ReturnType, ProcessType>> observer)
    {
        this.observer = observer;
        try
        {
            if ( !this.observer.isUnsubscribed() )
            {
                this.dto = new SwingWorkerSubscriberDto<ReturnType, ProcessType>();
                this.observer.onStart();
                ReturnType asyncResult = doInBackground();
                this.observer.onNext( dto.setResult( asyncResult ) );
                this.observer.onCompleted();
            }
        }
        catch ( Exception throwable )
        {
            observer.onError(throwable);
        }
    }

    protected abstract ReturnType doInBackground() throws Exception;

    protected void publish(ProcessType ... chunks)
    {
        this.observer.onNext( this.dto.send( chunks ) );
    }
}
