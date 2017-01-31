package winterbe2_CallablesAndFutures;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created: 20.10.16 creation date
 */
public class InvokeAnyExample
{
    public static void main() throws ExecutionException, InterruptedException
    {
        ExecutorService executor = Executors.newWorkStealingPool();
        // Instead of using a fixed size thread-pool ForkJoinPools are created
        // for a given parallelism size which per default is the number of available
        // cores of the hosts CPU.

        List<Callable<String>> callables = Arrays.asList(
                callable("task1", 2),
                callable("task2", 1),
                callable("task3", 3));

        String result = executor.invokeAny(callables);
        System.out.println(result);
    }


    private static Callable<String> callable(String result, long sleepSeconds)
    {
        return () ->
        {
            TimeUnit.SECONDS.sleep(sleepSeconds);
            return result;
        };
    }
}
