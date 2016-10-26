package winterbe5_Locks;

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
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Map<String, String> map = new HashMap<>();
        ReadWriteLock lock = new ReentrantReadWriteLock();

        executor.submit(() ->
        {
            lock.writeLock().lock();
            try
            {
                ConcurrentUtils.sleep(1);
                map.put("foo", "bar");
            }
            finally
            {
                lock.writeLock().unlock();
            }
        });

        Runnable readTask = () ->
        {
            lock.readLock().lock();
            try
            {
                System.out.println(map.get("foo"));
                ConcurrentUtils.sleep(1);
            }
            finally
            {
                lock.readLock().unlock();
            }
        };

        executor.submit(readTask);
        executor.submit(readTask);

        ConcurrentUtils.stop(executor);
    }
}
