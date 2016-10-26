package winterbe2_CallablesAndFutures;

import rx.Observable;
import rx.schedulers.Schedulers;

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
//        ExecutorService executor = Executors.newWorkStealingPool();

        List<Callable<String>> callables = Arrays.asList(
                () -> "task1",
                () -> "task2",
                () -> "task3");

        Observable.from(callables)
                .map(task -> Observable.fromCallable(task)
                        .subscribeOn(Schedulers.computation())
//                        .subscribeOn(Schedulers.from(executor))
                        .toBlocking().single()) // future.get() also blocks
                .doOnNext(System.out::println)
                .subscribe();
    }
}
