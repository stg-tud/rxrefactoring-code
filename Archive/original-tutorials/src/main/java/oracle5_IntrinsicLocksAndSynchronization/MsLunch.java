package oracle5_IntrinsicLocksAndSynchronization;

/**
 * Created: 19.10.16 creation date
 */
public class MsLunch
{
    private long c1 = 0;
    private long c2 = 0;
    private Object lock1 = new Object();
    private Object lock2 = new Object();

    public void inc1()
    {
        synchronized ( lock1 )
        {
            c1++;
        }
    }

    public void inc2()
    {
        synchronized ( lock2 )
        {
            c2++;
        }
    }
}

//NOTE: Use this idiom with extreme care. You must be absolutely sure
// that it really is safe to interleave access of the affected fields.
