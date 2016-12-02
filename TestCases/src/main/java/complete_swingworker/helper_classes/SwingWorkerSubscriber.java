package complete_swingworker.helper_classes;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.schedulers.SwingScheduler;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/01/2016
 */
public abstract class SwingWorkerSubscriber<ResultType, ProcessType> extends Subscriber<SwingWorkerSubscriberDto<ResultType, ProcessType>>
{

    public enum State
    {
        PENDING, STARTED, DONE
    }

    private Observable<SwingWorkerSubscriberDto<ResultType, ProcessType>> observable;
    private Subscription subscription;
    private ResultType asyncResult;
    private PropertyChangeSupport propertyChangeSupport;
    private AtomicInteger progress;
    private AtomicBoolean cancelled;
    private AtomicBoolean done;
    private State currentState;

    public SwingWorkerSubscriber(Observable<SwingWorkerSubscriberDto<ResultType, ProcessType>> observable)
    {
        this.observable = observable;
        this.propertyChangeSupport = new PropertyChangeSupport(this);
        this.progress = new AtomicInteger(0);
        this.cancelled = new AtomicBoolean(false);
        this.done = new AtomicBoolean();
        this.currentState = State.PENDING;
    }

    protected abstract void done(ResultType asyncResult);

    protected abstract void process(List<ProcessType> dto);

    @Override
    public void onStart()
    {
        setState(State.STARTED);
    }

    @Override
    public void onNext(SwingWorkerSubscriberDto<ResultType, ProcessType> dto)
    {
        asyncResult = dto.getResult();
        List<ProcessType> processedChunks = dto.getChunks();
        process(processedChunks);
        dto.removeChunks(processedChunks);
    }

    @Override
    public void onCompleted()
    {
        done(asyncResult);
        setState(State.DONE);
    }

//    protected abstract void handleError(Throwable throwable);

    public void execute()
    {
        if ( !isSubscribed() )
        {
            Scheduler scheduler = Schedulers.computation();
            subscribeObservable(scheduler);
        }
    }

    public void run()
    {
        if ( !isSubscribed() )
        {
            Scheduler scheduler = Schedulers.immediate();
            subscribeObservable(scheduler);
        }
    }

    private boolean isSubscribed()
    {
        return subscription != null && !subscription.isUnsubscribed();
    }

    private void subscribeObservable(Scheduler scheduler)
    {
        this.subscription = this.observable
                .observeOn(SwingScheduler.getInstance())
                .subscribeOn(scheduler)
                .subscribe(this);
    }

    public ResultType get()
    {
        this.observable.toBlocking().subscribe(this);
        return asyncResult;
    }

    public ResultType get(long timeout, TimeUnit unit) throws TimeoutException
    {
        this.observable
                .timeout(timeout, unit)
                .doOnError(new Action1<Throwable>()
                {
                    @Override
                    public void call(Throwable throwable)
                    {
                        SwingWorkerSubscriber.this.unsubscribe();
                        SwingWorkerSubscriber.this.onError(throwable);
                    }
                })
                .toBlocking()
                .subscribe(this);

        return asyncResult;
    }

    public State getState()
    {
        return this.currentState;
    }

    public boolean isDone()
    {
        return this.done.get();
    }

    public void setDone(boolean done)
    {
        this.done.set(done);
    }

    public boolean isCancelled()
    {
        return this.cancelled.get();
    }

    public boolean cancel(boolean mayInterruptIfRunning)
    {
        if ( !this.isUnsubscribed() && mayInterruptIfRunning )
        {
            this.unsubscribe();
            this.cancelled.set(true);
            return true;
        }
        else
        {
            return false;
        }
    }

    public int getProgress()
    {
        return this.progress.get();
    }

    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        synchronized ( this )
        {
            this.propertyChangeSupport.addPropertyChangeListener(listener);
        }
    }

    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        synchronized ( this )
        {
            this.propertyChangeSupport.removePropertyChangeListener(listener);
        }
    }

    public void firePropertyChange(String propertyName, Object oldValue, Object newValue)
    {
        synchronized ( this )
        {
            this.propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
        }
    }

    public PropertyChangeSupport getPropertyChangeSupport()
    {
        return propertyChangeSupport;
    }

    public void setProgress(int progress)
    {
        if ( progress < 0 || progress > 100 )
        {
            throw new IllegalArgumentException("the value should be from 0 to 100");
        }
        if ( this.progress.equals(progress) )
        {
            return;
        }
        synchronized ( this )
        {
            int oldProgress = this.progress.get();
            propertyChangeSupport.firePropertyChange("progress", oldProgress, progress);
            this.progress.set(progress);
        }
    }

    public void setState(State state)
    {
        synchronized ( this )
        {
            State oldState = this.currentState;
            propertyChangeSupport.firePropertyChange("state", oldState, state);
            this.currentState = state;
        }
    }
}
