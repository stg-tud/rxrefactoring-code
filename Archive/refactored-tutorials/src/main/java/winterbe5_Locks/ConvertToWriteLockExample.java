package winterbe5_Locks;

import rx.Observable;
import rx.schedulers.Schedulers;
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
//            ExecutorService executor = Executors.newFixedThreadPool(2);
            StampedLock lock = new StampedLock();

            final long[] stamp = new long[ 1 ];
            Observable
                    .create(subscriber ->
                    {
                        stamp[ 0 ] = lock.readLock();
                        if ( count == 0 )
                        {
                            System.out.println(count);
                            stamp[ 0 ] = lock.tryConvertToWriteLock(stamp[ 0 ]);
                            if ( stamp[ 0 ] == 0L )
                            {
                                System.out.println("Could not convert to write lock");
                                stamp[ 0 ] = lock.writeLock();
                            }
                            count = 23;
                        }
                        System.out.println(count);
                        subscriber.onCompleted();
                    })
                    .subscribeOn(Schedulers.computation())
                    .doOnCompleted(() -> lock.unlock(stamp[ 0 ]))
                    .subscribe();

//            ConcurrentUtils.stop(executor);
        }
    }

}
