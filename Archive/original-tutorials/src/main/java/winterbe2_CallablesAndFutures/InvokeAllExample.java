package winterbe2_CallablesAndFutures;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created: 20.10.16 creation date
 */
public class InvokeAllExample
{
    public void main() throws InterruptedException
    {
        ExecutorService executor = Executors.newWorkStealingPool();

        List<Callable<String>> callables = Arrays.asList(
                () -> "task1",
                () -> "task2",
                () -> "task3");

        executor.invokeAll(callables)
                .stream()
                .map(future ->
                {
                    try
                    {
                        return future.get();
                    }
                    catch ( Exception e )
                    {
                        throw new IllegalStateException(e);
                    }
                })
                .forEach(System.out::println);
    }
}
