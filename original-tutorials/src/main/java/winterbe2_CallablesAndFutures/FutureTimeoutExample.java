package winterbe2_CallablesAndFutures;

import java.util.concurrent.*;

/**
 * Created: 20.10.16 creation date
 */
public class FutureTimeoutExample
{
    public static void main() throws InterruptedException, ExecutionException, TimeoutException
    {
        ExecutorService executor = Executors.newFixedThreadPool(1);

        Future<Integer> future = executor.submit(() ->
        {
            try
            {
                TimeUnit.SECONDS.sleep(2);
                return 123;
            }
            catch ( InterruptedException e )
            {
                throw new IllegalStateException("task interrupted", e);
            }
        });

        // we expect an expection because the timeout is 1 second
        // but the task needs 2 seconds
        future.get(1, TimeUnit.SECONDS);
    }
}
