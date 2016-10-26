package winterbe5_Locks;

import rx.Observable;
import winterbe4_Synchronized.ConcurrentUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

/**
 * Description:
 * Author: Grebiel Jose Ifill Brito
 * Created: 20.10.16 creation date
 */
public class ReentrantLockExample
{
    public void main() throws InterruptedException
    {
        new ReentrantLockExample.AuxClass().execute();
    }

    private static class AuxClass
    {
        ReentrantLock lock = new ReentrantLock();
        int count = 0;

        void increment()
        {
            lock.lock();
            try
            {
                count++;
            }
            finally
            {
                lock.unlock();
            }

            /*
            Interesting methods:
                lock.isLocked()
                lock.isHeldByCurrentThread()
                lock.tryLock()
             */
        }

        void execute() throws InterruptedException
        {
            ExecutorService executor = Executors.newFixedThreadPool(2);

            Observable.range(0, 10000)
                    .forEach(i -> executor.submit(this::increment));

            ConcurrentUtils.stop(executor);

            System.out.println(count);  // x < 10000
        }
    }
}
