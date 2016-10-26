package winterbe2_CallablesAndFutures;

import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.concurrent.*;

/**
 * Created: 20.10.16 creation date
 */
public class FutureTimeoutExample
{
    public static void main() throws InterruptedException, ExecutionException, TimeoutException
    {
//        ExecutorService executor = Executors.newFixedThreadPool(1);

        Integer result = Observable
                .fromCallable(() ->
                {
                    try
                    {
                        TimeUnit.SECONDS.sleep(1);
                        return 123;
                    }
                    catch ( InterruptedException e )
                    {
                        throw new IllegalStateException("task interrupted", e);
                    }
                })
                .timeout(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.computation())
//                .subscribeOn(Schedulers.from(executor))
                .toBlocking().first(); // this would block the current thread line future.get() does


        // we expect an exception because the timeout is 1 second
        // but the task needs 2 seconds
        System.out.print("result: " + result);
    }
}
