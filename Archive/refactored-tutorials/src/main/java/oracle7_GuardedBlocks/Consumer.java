package oracle7_GuardedBlocks;

import winterbe4_Synchronized.ConcurrentUtils;

import java.util.Random;

/**
 * Created: 19.10.16 creation date
 */
public class Consumer
{
    private Drop drop;

    public Consumer(Drop drop)
    {
        this.drop = drop;
    }

    public void run()
    {
        Random random = new Random();
        for ( String message = drop.take(); !message.equals("DONE"); message = drop.take() )
        {
            ConcurrentUtils.message("MESSAGE RECEIVED: " + message);
            try
            {
                Thread.sleep(random.nextInt(5000));
            }
            catch ( InterruptedException e )
            {
            }
        }
    }
}
