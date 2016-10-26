package winterbe1_Executors;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created: 20.10.16 creation date
 */
public class SingleThreadExecutor
{
    public void main()
    {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() ->
        {
            String threadName = Thread.currentThread().getName();
            System.out.println("Hello " + threadName);
        });

    }
}
