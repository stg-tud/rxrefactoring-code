package oracle6_Deadlock;

import rx.Observable;
import rx.schedulers.Schedulers;
import winterbe4_Synchronized.ConcurrentUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created: 19.10.16 creation date
 */
public class DeadLock
{
    public void main()
    {
        final Friend alphonse = new Friend("Alphonse");
        final Friend gaston = new Friend("Gaston");

        CountDownLatch latch = new CountDownLatch(2);

        List<Runnable> tasks = new ArrayList<>();
        tasks.add(() -> alphonse.bow(gaston));
        tasks.add(() -> gaston.bow(alphonse));

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

    class Friend
    {
        private final String name;

        public Friend(String name)
        {
            this.name = name;
        }

        public String getName()
        {
            return this.name;
        }

        public synchronized void bow(Friend bower)
        {
            ConcurrentUtils.message(bower.getName() + " bows");
            System.out.format("%s: %s" + "  has bowed to me!%n", this.name, bower.getName());
            bower.bowBack(this);
        }

        public synchronized void bowBack(Friend bower)
        {
            ConcurrentUtils.message(bower.getName() + " bows back");
            System.out.format("%s: %s" + " has bowed back to me!%n", this.name, bower.getName());
        }
    }
}
