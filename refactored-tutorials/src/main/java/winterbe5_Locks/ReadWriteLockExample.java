package winterbe5_Locks;

import rx.Observable;
import rx.schedulers.Schedulers;
import winterbe4_Synchronized.ConcurrentUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Description: The read-lock can be held simultaneously by multiple threads
 * as long as no threads hold the write-lock
 * Created: 20.10.16 creation date
 */
public class ReadWriteLockExample
{
    public void main()
    {
//        ExecutorService executor = Executors.newFixedThreadPool(2);
        Map<String, String> map = new HashMap<>();
        ReadWriteLock lock = new ReentrantReadWriteLock();

        Observable
                .create(subscriber ->
                {
                    lock.writeLock().lock();
                    ConcurrentUtils.sleep(1);
                    map.put("foo", "bar");
                    subscriber.onCompleted();
                })
                .doOnCompleted(() -> lock.writeLock().unlock())
                .subscribeOn(Schedulers.computation())
//                .subscribeOn(Schedulers.from(executor))
                .subscribe();

        Runnable readTask = () ->
        {
            lock.readLock().lock();
            System.out.println(map.get("foo"));
            ConcurrentUtils.sleep(1);
        };


        // consider using a method and loop to avoid code duplication
        Observable
                .create(subscriber ->
                {
                    readTask.run();
                    subscriber.onCompleted();
                })
                .subscribeOn(Schedulers.computation())
//                .subscribeOn(Schedulers.from(executor))
                .doOnCompleted(() -> lock.readLock().unlock())
                .subscribe();

        Observable
                .create(subscriber ->
                {
                    readTask.run();
                    subscriber.onCompleted();
                })
                .subscribeOn(Schedulers.computation())
//                .subscribeOn(Schedulers.from(executor))
                .doOnCompleted(() -> lock.readLock().unlock())
                .subscribe();

//        ConcurrentUtils.stop(executor);
    }
}
