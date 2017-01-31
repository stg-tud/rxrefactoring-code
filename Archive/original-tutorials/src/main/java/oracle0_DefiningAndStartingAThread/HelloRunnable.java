package oracle0_DefiningAndStartingAThread;

/**
 * Created: 19.10.16 creation date
 */
public class HelloRunnable
{
    public void main()
    {
        Thread thread = new Thread(new HelloRunnableClass());
        thread.start();
        try
        {
            thread.join();
        }
        catch ( InterruptedException e )
        {
            System.out.println("Task interrupted");
        }
    }

    private class HelloRunnableClass implements Runnable
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
