package winterbe4_Synchronized;

import rx.Observable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Created: 20.10.16 creation date
 */
public class SynchronizedExample
{
    public void main() throws InterruptedException
    {
        new AuxClass().execute();
    }

    private static class AuxClass
    {

        int count = 0;

        // synchronized avoids data races. In this example it can be used in 2 places
        /* synchronized */ void increment()
        {
            synchronized ( this )
            {
                count = count + 1;
            }
        }

        void execute() throws InterruptedException
        {
            ExecutorService executor = Executors.newFixedThreadPool(2);

            Observable.range(0, 1000)
                    .forEach(i -> executor.submit(this::increment));

            ConcurrentUtils.stop(executor);

            System.out.println(count);  // x < 10000
        }
    }

}
