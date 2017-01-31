package winterbe2_CallablesAndFutures;

import java.util.concurrent.*;

/**
 * Created: 20.10.16 creation date
 */
public class CallableExample
{
    public void main() throws ExecutionException, InterruptedException
    {
        Callable<Integer> task = () ->
        {
            try
            {
                TimeUnit.SECONDS.sleep(1);
                return 123;
            }
            catch ( InterruptedException e )
            {
                throw new IllegalStateException("task interrupted", e);
            }
        };

        ExecutorService executor = Executors.newFixedThreadPool(1);
        Future<Integer> future = executor.submit(task);

        System.out.println("future done? " + future.isDone());

//        executor.shutdownNow(); This would throw an exception because the future value hasn't been read
        Integer result = future.get();
        executor.shutdownNow();

        System.out.println("future done? " + future.isDone());
        System.out.print("result: " + result);
    }
}
