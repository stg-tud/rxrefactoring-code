package oracle3_SimpleThreadsExample;

import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created: 19.10.16 creation date
 */
public class SimpleThreads
{

    // Display a message, preceded by
    // the name of the current thread
    private void threadMessage(String message)
    {
        String threadName = Thread.currentThread().getName();
        System.out.format("%s: %s%n", threadName, message);
    }

    private class MessageLoop
    {
        public void run()
        {
            String importantInfo[] = {
                    "Mares eat oats",
                    "Does eat oats",
                    "Little lambs eat ivy",
                    "A kid will eat ivy too"
            };
            try
            {
                for ( int i = 0; i < importantInfo.length; i++ )
                {
                    // Pause for 4 seconds
                    Thread.sleep(4000);
                    // Print a message
                    threadMessage(importantInfo[ i ]);
                }
            }
            catch ( InterruptedException e )
            {
                threadMessage("I wasn't done!");
            }
        }
    }

    public void main(String ... args) throws InterruptedException
    {

        // Delay, in milliseconds before
        // we interrupt MessageLoop
        // thread (default one hour).
        long patience = 1000 * 60 * 60;

        // If command line argument
        // present, gives patience
        // in seconds.
        if ( args.length > 0 )
        {
            try
            {
                patience = Long.parseLong(args[ 0 ]) * 1000;
            }
            catch ( NumberFormatException e )
            {
                System.err.println("Argument must be an integer.");
                System.exit(1);
            }
        }

        threadMessage("Starting MessageLoop thread");
        long startTime = System.currentTimeMillis();

        CountDownLatch latch = new CountDownLatch(1);
        Subscription subscription = Observable.just(latch)
                .doOnNext(rxLatch -> new MessageLoop().run())
                .doOnCompleted(() -> latch.countDown())
                .doOnUnsubscribe(() -> latch.countDown()) // in case of interrupt
                .subscribeOn(Schedulers.newThread())
                .subscribe();

        threadMessage("Waiting for MessageLoop thread to finish");
        // loop until MessageLoop
        // thread exits
        while ( latch.getCount() != 0 ) // equivalent to t.isAlive()
        {
            threadMessage("Still waiting...");
            // Wait maximum of 1 second
            // for MessageLoop thread
            // to finish.
            latch.await(1, TimeUnit.SECONDS); // equivalent to t.wait(1000)
            if ( ((System.currentTimeMillis() - startTime) > patience) && latch.getCount() != 0 )
            {
                threadMessage("Tired of waiting!");
                subscription.unsubscribe(); // equivalent to t.interrupt()
                // Shouldn't be long now
                // -- wait indefinitely
                latch.await();
            }
        }
        threadMessage("Finally!");
    }
}
