package rx.refactoring.testingapp;

import java.util.concurrent.Callable;
import android.graphics.Bitmap;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class AnonymousClassCase2
{
	public void someMethod()
	{
		// Testing doInBackground only
		Observable
				.fromCallable( new Callable<Integer>()
				{
					@Override
					public Integer call() throws Exception
					{
						int a = 1;
						int b = 2;
						int c = a + b;
						return c;
					}
				} )
				.subscribeOn( Schedulers.computation() )
				.observeOn( AndroidSchedulers.mainThread() )
				.subscribe();
	}
}