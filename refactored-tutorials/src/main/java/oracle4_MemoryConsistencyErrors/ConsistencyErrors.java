package oracle4_MemoryConsistencyErrors;

import rx.Observable;
import rx.schedulers.Schedulers;
import winterbe4_Synchronized.ConcurrentUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created: 19.10.16 creation date
 */
public class ConsistencyErrors
{
    private static int counter = 0;

    public void main()
    {
        CountDownLatch latch = new CountDownLatch(2);

        List<Runnable> tasks = new ArrayList<>();
        tasks.add(() -> incrementCounter());
        tasks.add(() -> System.out.println(getCounter()));

        Observable.from(tasks)
                .map(task -> Observable.just(task)
                        .doOnNext(action -> action.run())
                        .doOnCompleted(() -> latch.countDown()) // countDown is thread safe
                        .doOnUnsubscribe(() -> latch.countDown()) // in case of interrupt
                        .subscribeOn(Schedulers.newThread())
                        .subscribe())
                .subscribe();

        try
        {
            latch.await();
        }
        catch ( InterruptedException e )
        {
            System.out.println("Task interrupted");
        }
    }

    // possible solution: used synchronized
    // important: the order of execution should not affect the result,
    // because we don't know which thread is going to start first

    public synchronized int getCounter()
    {
        ConcurrentUtils.message("getCounter");
        return counter;
    }

    public synchronized void incrementCounter()
    {
        ConcurrentUtils.message("incrementCounter");
        try
        {
            Thread.sleep(2000L);
        }
        catch ( InterruptedException e )
        {
            return;
        }
        counter++;
    }
}
