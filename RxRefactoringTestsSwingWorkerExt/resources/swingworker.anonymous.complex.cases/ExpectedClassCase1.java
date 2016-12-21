package rxrefactoring.anonymous.complex;

import java.util.List;
import java.util.concurrent.Callable;

import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class AnonymousComplexCase1
{

    public void start()
    {
        // helper class created ComplexRxObservable
        new ComplexRxObservable().getAsyncObservable().subscribe();
    }

    // This class contatins the fields and methods of the original SwingWorker
    // The class returns an observable that can be subscribed
    private class ComplexRxObservable
    {
        private static final long SLEEP_TIME = 1000L;

        public Observable<String> getAsyncObservable()
        {
            return Observable
                    .fromCallable(new Callable<String>()
                    {
                        @Override
                        public String call() throws Exception
                        {
                            for ( int i = 0; i < 10; i++ )
                            {
                                longRunningOperation();
                            }
                            return "DONE";
                        }
                    }).subscribeOn(Schedulers.computation())
                    .observeOn(Schedulers.immediate())
                    .doOnNext(new Action1<String>()
                    {
                        @Override
                        public void call(String asyncResult)
                        {
                            String result = asyncResult;
                            System.out.println("[Thread: " + Thread.currentThread().getName() + "] Result:" + result);
                        }
                    });
        }

        private void longRunningOperation() throws InterruptedException
        {
            Thread.sleep(SLEEP_TIME);
            System.out.println( "[Thread: " + Thread.currentThread().getName() + "] Long running operation completed." );
        }
    }
}