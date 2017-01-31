package winterbe4_Synchronized;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created: 20.10.16 creation date
 */
public class ConcurrentUtils
{

    public static void stop(ExecutorService executor)
    {
        try
        {
            executor.shutdown();
            executor.awaitTermination(60, TimeUnit.SECONDS);
        }
        catch ( InterruptedException e )
        {
            System.err.println("termination interrupted");
        }
        finally
        {
            if ( !executor.isTerminated() )
            {
                System.err.println("killing non-finished tasks");
            }
            executor.shutdownNow();
        }
    }

    public static void sleep(int seconds)
    {
        try
        {
            TimeUnit.SECONDS.sleep(seconds);
        }
        catch ( InterruptedException e )
        {
            throw new IllegalStateException(e);
        }
    }

    public static void message(String message)
    {
        System.out.println("----> " + Thread.currentThread().getName() + ": " + message);
    }

}
