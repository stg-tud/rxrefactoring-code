package oracle9_ForkJoin;

import java.util.concurrent.RecursiveAction;

/**
 * Created: 20.10.16 creation date
 */
public class Fibonacci extends RecursiveAction
{
    private long n;
    private long result[];

    public Fibonacci(Long n, long[] result)
    {
        this.n = n;
        this.result = result;
    }

    @Override
    protected void compute()
    {
        if ( n < 30 )
        {
            result[ 0 ] = computeDirectly(n);
            return;
        }

        long result1[] = new long[ 1 ];
        long result2[] = new long[ 1 ];
        Fibonacci t1 = new Fibonacci(n - 1, result1);
        Fibonacci t2 = new Fibonacci(n - 2, result2);
        invokeAll(t1, t2); // returns when all tasks have completed
        result[ 0 ] = result1[ 0 ] + result2[ 0 ];

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
