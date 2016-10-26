package oracle9_ForkJoin;

import rx.schedulers.Schedulers;

import java.util.concurrent.CountDownLatch;

/**
 * Description:
 * Author: Grebiel Jose Ifill Brito
 * Created: 21.10.16 creation date
 */
public class FibonacciRx
{
    private long n;
    private long result[];

    public FibonacciRx(Long n, long[] result)
    {
        this.n = n;
        this.result = result;
    }

    public void compute()
    {
        if ( n < 30 )
        {
            result[ 0 ] = computeDirectly(n);
            return;
        }

        try
        {
            long result1[] = new long[ 1 ];
            long result2[] = new long[ 1 ];
            CountDownLatch latch = new CountDownLatch(2);
            Schedulers.newThread().createWorker().schedule(() -> getWork(result1, latch, n - 1));
            Schedulers.newThread().createWorker().schedule(() -> getWork(result2, latch, n - 2));
            latch.await();
            result[ 0 ] = result1[ 0 ] + result2[ 0 ];
        }
        catch ( InterruptedException e )
        {
            System.out.println("Task interrupted");
        }
    }

    private void getWork(long[] result, CountDownLatch latch, long n)
    {
        result[ 0 ] = computeDirectly(n);
        latch.countDown();
    }

    private long computeDirectly(long n)
    {
        if ( n == 0L )
        {
            return 0L;
        }
        else if ( n == 1L )
        {
            return 1L;
        }
        else
        {
            return computeDirectly(n - 1) + computeDirectly(n - 2);
        }
    }

    public long getResult()
    {
        return result[ 0 ];
    }
}
