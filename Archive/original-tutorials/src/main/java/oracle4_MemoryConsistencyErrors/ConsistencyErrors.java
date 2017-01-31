package oracle4_MemoryConsistencyErrors;

import java.util.Arrays;

/**
 * Created: 19.10.16 creation date
 */
public class ConsistencyErrors
{
    private static int counter = 0;

    public void main()
    {
        Thread t1 = new Thread(() -> incrementCounter());
        Thread t2 = new Thread(() -> System.out.println(getCounter()));

        // Consistency problems if t1 slower than t2
        Arrays.asList(t1, t2).forEach(thread -> thread.start());

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

    // possible solution: used synchronized
    // important: the order of execution should not affect the result,
    // because we don't know which thread is going to start first

    public synchronized int getCounter()
    {
        return counter;
    }

    public synchronized void incrementCounter()
    {
        try
        {
            Thread.sleep(2000L);
        }
        catch ( InterruptedException e )
        {
            return;
        }
        counter++;
    }
}
