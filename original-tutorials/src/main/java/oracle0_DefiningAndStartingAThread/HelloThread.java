package oracle0_DefiningAndStartingAThread;

/**
 * Created: 19.10.16 creation date
 */
public class HelloThread
{
    public void main()
    {
        HelloThreadDecl helloThreadDecl = new HelloThreadDecl();
        helloThreadDecl.start();
        try
        {
            helloThreadDecl.join();
        }
        catch ( InterruptedException e )
        {
            System.out.println("Task interrupted");
        }
    }

    class HelloThreadDecl extends Thread
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
