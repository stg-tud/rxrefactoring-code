package winterbe5_Locks;

import winterbe4_Synchronized.ConcurrentUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.StampedLock;

/**
 * Created: 20.10.16 creation date
 */
public class ConvertToWriteLockExample
{
    public void main() throws InterruptedException
    {
        new ConvertToWriteLockExample.AuxClass().execute();
    }

    private static class AuxClass
    {

        int count = 0;

        void execute() throws InterruptedException
        {
            ExecutorService executor = Executors.newFixedThreadPool(2);
            StampedLock lock = new StampedLock();

            executor.submit(() ->
            {
                long stamp = lock.readLock();
                try
                {
                    if ( count == 0 )
                    {
                        System.out.println(count);
                        stamp = lock.tryConvertToWriteLock(stamp);
                        if ( stamp == 0L )
                        {
                            System.out.println("Could not convert to write lock");
                            stamp = lock.writeLock();
                        }
                        count = 23;
                    }
                    System.out.println(count);
                }
                finally
                {
                    lock.unlock(stamp);
                }
            });

            ConcurrentUtils.stop(executor);
        }
    }

}
