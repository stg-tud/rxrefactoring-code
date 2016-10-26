package oracle8_LockObjects;

import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created: 19.10.16 creation date
 */
public class Safelock
{
    public void main()
    {
        final Friend alphonse = new Friend("Alphonse");
        final Friend gaston = new Friend("Gaston");

        CountDownLatch latch = new CountDownLatch(2);

        List<Runnable> tasks = new ArrayList<>();
        tasks.add(() -> new BowLoop(alphonse, gaston).run());
        tasks.add(() -> new BowLoop(gaston, alphonse).run());

        Observable.from(tasks)
                .map(task -> Observable.just(task)
                        .doOnNext(action -> action.run())
                        .doOnCompleted(() -> latch.countDown()) // countDown is thread safe
                        .doOnUnsubscribe(() -> latch.countDown()) // in case of interrupt
                        .subscribeOn(Schedulers.newThread())
                        .timeout(1L, TimeUnit.SECONDS)
                        .onErrorResumeNext(Observable.empty()) // to ignore error due to timeout
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
        private final Lock lock = new ReentrantLock();

        public Friend(String name)
        {
            this.name = name;
        }

        public String getName()
        {
            return this.name;
        }

        public boolean impendingBow(Friend bower)
        {
            Boolean myLock = false;
            Boolean yourLock = false;
            try
            {
                myLock = lock.tryLock();
                yourLock = bower.lock.tryLock();
            }
            finally
            {
                if ( !(myLock && yourLock) )
                {
                    if ( myLock )
                    {
                        lock.unlock();
                    }
                    if ( yourLock )
                    {
                        bower.lock.unlock();
                    }
                }
            }
            return myLock && yourLock;
        }

        public void bow(Friend bower)
        {
            if ( impendingBow(bower) )
            {
                try
                {
                    System.out.format("%s: %s has bowed to me!%n", this.name, bower.getName());
                    bower.bowBack(this);
                }
                finally
                {
                    lock.unlock();
                    bower.lock.unlock();
                }
            }
            else
            {
                System.out.format("%s: %s started"
                                + " to bow to me, but saw that"
                                + " I was already bowing to"
                                + " him.%n",
                        this.name, bower.getName());
            }
        }

        public void bowBack(Friend bower)
        {
            System.out.format("%s: %s has" +
                            " bowed back to me!%n",
                    this.name, bower.getName());
        }
    }

    class BowLoop
    {
        private Friend bower;
        private Friend bowee;

        public BowLoop(Friend bower, Friend bowee)
        {
            this.bower = bower;
            this.bowee = bowee;
        }

        public void run()
        {
            Random random = new Random();
            for ( ; ; )
            {
                try
                {
                    Thread.sleep(random.nextInt(10));
                }
                catch ( InterruptedException e )
                {
                }
                bowee.bow(bower);
            }
        }
    }
}
