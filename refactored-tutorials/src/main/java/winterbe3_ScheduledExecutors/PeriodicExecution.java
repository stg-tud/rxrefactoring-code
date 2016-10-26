package winterbe3_ScheduledExecutors;

import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created: 20.10.16 creation date
 */
public class PeriodicExecution
{
    public void main () throws InterruptedException
    {
//        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        Runnable task = () -> System.out.println("Scheduling: " + System.nanoTime());

        int initialDelay = 0;
        int period = 1;

        Subscription subscription = Observable.interval(initialDelay, period, TimeUnit.SECONDS)
                .doOnNext(x -> task.run())
//                .subscribeOn(Schedulers.from(executor))
                .subscribeOn(Schedulers.computation())
                .subscribe();

        Thread.sleep(3000L);
        subscription.unsubscribe();

//        executor.shutdown();
    }
}
