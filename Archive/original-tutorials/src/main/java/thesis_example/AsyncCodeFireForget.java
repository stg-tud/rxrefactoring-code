package thesis_example;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 01/25/2017
 */
public class AsyncCodeFireForget extends AsyncOperations
{
	public static void main( String[] args ) throws InterruptedException
	{
		Runnable runnable = () -> performOpAsync();

		Thread thread = new Thread( runnable );
		thread.start();

		performOperation();
		// performOpAsync and performOperation run concurrently
	}

	public static void rx()
	{
        Observable
                .fromCallable( () -> {
                    performOpAsync();
                    return null;
                } )
                .subscribeOn(Schedulers.computation())
//                .doOnCompleted( () -> doXYZ() )
                .subscribe();

        performOperation();
	}
}
