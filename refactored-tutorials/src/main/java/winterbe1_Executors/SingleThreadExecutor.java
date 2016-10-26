package winterbe1_Executors;

import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created: 20.10.16 creation date
 */
public class SingleThreadExecutor
{
    public void main()
    {
//        ExecutorService executor = Executors.newSingleThreadExecutor();

        Observable
                .create(subscriber ->
                {
                    String threadName = Thread.currentThread().getName();
                    System.out.println("Hello " + threadName);
                })
                .subscribeOn(Schedulers.computation())
//                .subscribeOn(Schedulers.from(executor))
                .subscribe();

    }
}
