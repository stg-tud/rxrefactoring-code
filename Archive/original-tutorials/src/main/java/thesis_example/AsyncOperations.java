package thesis_example;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 01/25/2017
 */
public class AsyncOperations
{
    protected static int computeResult()
    {
        log(10);
        return 123;
    }

    protected static void log(int iterations)
    {
        for (int i = 0; i < iterations; i++)
        {
            try
            {
                Thread.sleep(1000L);
            }
            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName());
        }
    }

    protected static void performOperation()
    {
        log(5);
    }

    protected static void performOpAsync()
    {
        log(10);
    }
}
