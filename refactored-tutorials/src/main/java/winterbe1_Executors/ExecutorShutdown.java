package winterbe1_Executors;

import rx.Observable;
import rx.exceptions.Exceptions;
import rx.schedulers.Schedulers;
import winterbe4_Synchronized.ConcurrentUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created: 20.10.16 creation date
 */
public class ExecutorShutdown
{
    public void main()
    {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        CountDownLatch latch = new CountDownLatch(1);

        Observable
                .create(subscriber ->
                {
                    String threadName = Thread.currentThread().getName();
                    System.out.println("Hello " + threadName);
                    subscriber.onCompleted(); // call on Completed
                })
                .subscribeOn(Schedulers.from(executor))
                .doOnCompleted(() -> latch.countDown())
                .subscribe();

        // the usage of subscribeOn(Schedulers.computation()) would not required
        // to shutdown the executor

        Observable
                .create(subscriber ->
                {
                    System.out.println("attempt to shutdown executor");
                    executor.shutdown();
                    try
                    {
                        executor.awaitTermination(5, TimeUnit.SECONDS);
                    }
                    catch ( InterruptedException e )
                    {
                        Exceptions.propagate(e);
                    }
                    subscriber.onCompleted(); // call on Completed
                })
                .doOnError(t -> System.err.println("tasks interrupted"))
                .doOnCompleted(() ->
                {
                    if ( !executor.isTerminated() )
                    {
                        System.err.println("cancel non-finished tasks");
                    }
                    executor.shutdownNow();
                    System.out.println("shutdown finished");
                }).subscribe();

        try
        {
            latch.await();
            ConcurrentUtils.message("Executor terminated? " + executor.isTerminated());
            ConcurrentUtils.message("Executor shutdown? " + executor.isShutdown());
        }
        catch ( InterruptedException e )
        {
            System.out.println("Task interrupted");
        }
    }
}