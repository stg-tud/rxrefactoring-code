package rxrefactoring.anonymous.complex;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class AnonymousComplexCase2
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
        private AtomicInteger iterationCounter;

        public Observable<String> getAsyncObservable()
        {
            final Subscriber<List<Integer>> rxUpdateSubscriber = getRxUpdateSubscriber();
            return Observable
                    .fromCallable(new Callable<String>()
                    {
                        @Override
                        public String call() throws Exception
                        {
                            iterationCounter = new AtomicInteger(0);
                            for ( int i = 0; i < 10; i++ )
                            {
                                iterationCounter.set(i);
                                longRunningOperation();
                                rxUpdateSubscriber.onNext(Arrays.asList(i * 10));
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

        private Subscriber<List<Integer>> getRxUpdateSubscriber()
        {
            return new Subscriber<List<Integer>>()
            {
                @Override
                public void onCompleted()
                {

                }

                @Override
                public void onError(Throwable throwable)
                {

                }

                @Override
                public void onNext(List<Integer> chunks)
                {
                    // Same code as in SwingWorker#process(List<Integer> chunks)
                    for ( Integer i : chunks )
                    {
                        System.out.println("Iteration = " + iterationCounter.get() + "; Progress = " + i + "%");
                    }
                }
            };
        }

        private void longRunningOperation() throws InterruptedException
        {
            Thread.sleep(SLEEP_TIME);
            System.out.println( "[Thread: " + Thread.currentThread().getName() + "] Long running operation completed." );
        }
    }
}