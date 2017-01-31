package winterbe2_CallablesAndFutures;

import rx.Observable;
import rx.exceptions.Exceptions;
import rx.schedulers.Schedulers;

import java.util.concurrent.*;

/**
 * Created: 20.10.16 creation date
 */
public class CallableExample
{
    public void main() throws ExecutionException, InterruptedException
    {
//        ExecutorService executor = Executors.newFixedThreadPool(1);

        Integer result = Observable
                .fromCallable(() ->
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
                })
                .subscribeOn(Schedulers.computation())
//                .subscribeOn(Schedulers.from(executor))
                .toBlocking().first(); // this would block the current thread line future.get() does

        System.out.print("result: " + result);
    }
}
