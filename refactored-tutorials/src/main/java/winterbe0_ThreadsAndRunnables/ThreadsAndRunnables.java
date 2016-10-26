package winterbe0_ThreadsAndRunnables;

/**
 * Created: 20.10.16 creation date
 */
public class ThreadsAndRunnables
{
    public void main()
    {
        Runnable task = () ->
        {
            String threadName = Thread.currentThread().getName();
            System.out.println("Hello " + threadName);
        };

        task.run();

        Thread thread = new Thread(task);
        thread.start();

        System.out.println("Done!");
    }
}
