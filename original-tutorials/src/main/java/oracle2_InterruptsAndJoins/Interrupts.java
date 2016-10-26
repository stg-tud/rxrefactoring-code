package oracle2_InterruptsAndJoins;

/**
 * Created: 19.10.16 creation date
 */
public class Interrupts
{
    public static void main(String args[])
    {
        String importantInfo[] = {
                "Mares eat oats",
                "Does eat oats",
                "Little lambs eat ivy",
                "A kid will eat ivy too"
        };

        for ( int i = 0; i < importantInfo.length; i++ )
        {
            //Pause for 4 seconds
            try
            {
                Thread.sleep(4000);
                // if no method that can throw InterruptedException is invoked,
                // then we have to program ourselves a condition that checks
                // if (Thread.interrupted()) {...}
            }
            catch ( InterruptedException e )
            {
                // We've been interrupted: no more messages
                return;
            }
            //Print a message
            System.out.println(importantInfo[ i ]);
        }
    }

    // NOTES:
    // Thread.interrupted() clears the interrupt status
    // Thread.isInterrupted() does not change the interrupt flag
    // t.join() allows one thread to wait for the completion of another
}
