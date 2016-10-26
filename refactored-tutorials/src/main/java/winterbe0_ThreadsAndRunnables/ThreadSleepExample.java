package winterbe0_ThreadsAndRunnables;

import java.util.concurrent.TimeUnit;

/**
 * Created: 20.10.16 creation date
 */
public class ThreadSleepExample
{
    public void main()
    {
        Runnable runnable = () ->
        {
            try
            {
                String name = Thread.currentThread().getName();
                System.out.println("Foo " + name);
                TimeUnit.SECONDS.sleep(1); // Thread.sleep(1000)
                System.out.println("Bar " + name);
            }
            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();
    }
}
