package oracle0_DefiningAndStartingAThread;

import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.concurrent.CountDownLatch;

/**
 * Created: 19.10.16 creation date
 */
public class HelloRunnable
{
    public void main()
    {
        // Latch necessary to not return before the new thread has completed
        // The number should correspond to the number of new threads in the method.
        CountDownLatch latch = new CountDownLatch(1);
        Observable.just(latch)
                .doOnNext(rxLatch -> new HelloRunnableClass().run())
                .doOnCompleted(() -> latch.countDown())
                .doOnUnsubscribe(() -> latch.countDown()) // in case of interrupt
                .subscribeOn(Schedulers.newThread())
                .subscribe();
        try
        {
            latch.await(); // equivalent to thread.join()
        }
        catch ( InterruptedException e )
        {
            System.out.println("Task interrupted");
        }
    }

    private class HelloRunnableClass
    {
        public void run()
        {
            try
            {
                Thread.sleep(2000L);
            }
            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }
            System.out.println("Hello from a thread!");
        }
    }
}
