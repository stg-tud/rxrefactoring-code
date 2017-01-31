package oracle7_GuardedBlocks;

/**
 * Created: 19.10.16 creation date
 */
public class ProducerConsumerExample
{
    public void main()
    {
        Drop drop = new Drop();
        Thread t1 = new Thread(new Producer(drop));
        Thread t2 = new Thread(new Consumer(drop));
        t1.start();
        t2.start();

        try
        {
            t1.join();
            t2.join();
        }
        catch ( InterruptedException e )
        {
            System.out.println("Task interrupted");
        }
    }
}
