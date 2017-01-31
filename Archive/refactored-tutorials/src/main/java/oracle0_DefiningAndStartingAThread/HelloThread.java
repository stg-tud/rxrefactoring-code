package oracle0_DefiningAndStartingAThread;

import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.concurrent.CountDownLatch;

/**
 * Created: 19.10.16 creation date
 */
public class HelloThread
{
    /**
     * Similar to {@link HelloRunnable}
     */
    public void main()
    {
        CountDownLatch latch = new CountDownLatch(1);
        Observable.just(latch)
                .doOnNext(rxLatch -> new HelloThreadDecl().run())
                .doOnCompleted(() -> latch.countDown())
                .doOnUnsubscribe(() -> latch.countDown()) // in case of interrupt
                .subscribeOn(Schedulers.newThread())
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

    class HelloThreadDecl
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
