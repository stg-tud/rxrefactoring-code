package winterbe7_AtomicVariables;

import winterbe4_Synchronized.ConcurrentUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * Created: 20.10.16 creation date
 */
public class AtomicIntegerExample
{
    public void main()
    {
        AtomicInteger atomicInt = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        IntStream.range(0, 1000)
                .forEach(i ->
                {
                    Runnable task = () -> atomicInt.incrementAndGet(); // 1000
//                    Runnable task = () -> atomicInt.updateAndGet(n -> n + 2); // 2000
//                    Runnable task = () -> atomicInt.accumulateAndGet(i, (n, m) -> n + m); // 499500
                    executor.submit(task);
                });

        ConcurrentUtils.stop(executor);

        System.out.println(atomicInt.get());    // => 2000
    }
}
