package oracle7_GuardedBlocks;

import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created: 19.10.16 creation date
 */
public class ProducerConsumerExample
{
    public void main()
    {
        Drop drop = new Drop();

        CountDownLatch latch = new CountDownLatch(2);

        List<Runnable> tasks = new ArrayList<>();
        tasks.add(() -> new Producer(drop).run());
        tasks.add(() -> new Consumer(drop).run());

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
}
