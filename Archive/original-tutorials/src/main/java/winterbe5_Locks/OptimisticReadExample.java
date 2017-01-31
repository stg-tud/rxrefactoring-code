package winterbe5_Locks;

import winterbe4_Synchronized.ConcurrentUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.StampedLock;

/**
 * Description:
 * Author: Grebiel Jose Ifill Brito
 * Created: 20.10.16 creation date
 */
public class OptimisticReadExample
{
    public void main()
    {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        StampedLock lock = new StampedLock();

        executor.submit(() ->
        {
            long stamp = lock.tryOptimisticRead();
            try
            {
                System.out.println("Optimistic Lock Valid: " + lock.validate(stamp)); // true
                ConcurrentUtils.sleep(1);
                System.out.println("Optimistic Lock Valid: " + lock.validate(stamp)); // false
                ConcurrentUtils.sleep(2);
                System.out.println("Optimistic Lock Valid: " + lock.validate(stamp)); // false:
                // Even when the write lock is released the optimistic read locks stays invalid


            }
            finally
            {
                lock.unlock(stamp);
            }
        });

        executor.submit(() ->
        {
            long stamp = lock.writeLock();
            try
            {
                System.out.println("Write Lock acquired");
                ConcurrentUtils.sleep(2);
            }
            finally
            {
                lock.unlock(stamp);
                System.out.println("Write done");
            }
        });

        ConcurrentUtils.stop(executor);
    }
}
