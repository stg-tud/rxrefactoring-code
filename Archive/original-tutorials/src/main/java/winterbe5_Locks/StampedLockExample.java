package winterbe5_Locks;

import winterbe4_Synchronized.ConcurrentUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.StampedLock;

/**
 * Description: It can be held simultaneously by multiple threads
 * as long as no threads hold the write-lock
 * Created: 20.10.16 creation date
 */
public class StampedLockExample
{
    public void main()
    {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Map<String, String> map = new HashMap<>();
        StampedLock lock = new StampedLock();

        executor.submit(() ->
        {
            long stamp = lock.writeLock();
            try
            {
                ConcurrentUtils.sleep(1);
                map.put("foo", "bar");
            }
            finally
            {
                lock.unlockWrite(stamp);
            }
        });

        Runnable readTask = () ->
        {
            long stamp = lock.readLock();
            try
            {
                System.out.println(map.get("foo"));
                ConcurrentUtils.sleep(1);
            }
            finally
            {
                lock.unlockRead(stamp);
            }
        };

        executor.submit(readTask);
        executor.submit(readTask);

        ConcurrentUtils.stop(executor);
    }
}
