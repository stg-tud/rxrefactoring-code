package rxrefactoring.anonymous.simple;

import java.util.concurrent.Callable;

import rx.Observable;
import rx.schedulers.Schedulers;

public class AnonymousClassCase1
{
    public void start()
    {
        Observable
                .fromCallable( new Callable<String>()
                {
                    @Override
                    public String call() throws Exception
                    {
                        // code to be execute in a background thread
                        longRunningOperation();
                        return "DONE";
                    }
                } )
                .subscribeOn( Schedulers.computation() ) // executes asynchronously
                .observeOn( Schedulers.immediate() )
                .subscribe();
    }

    private void longRunningOperation() throws InterruptedException
    {
        Thread.sleep( 2000L );
        System.out.println( "[Thread: " + Thread.currentThread().getName() + "] Long running operation completed." );
    }
}