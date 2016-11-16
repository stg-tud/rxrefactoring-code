package rxrefactoring;

import java.util.concurrent.Callable;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class AnonymousClassCase3
{
	public void someMethod()
	{
		// Testing doInBackground only with empty onPostExecute declaration
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
				.doOnNext( new Action1<Integer>()
				{
					@Override
					public void call( Integer number )
					{
						// empty block
					}
				} )
				.subscribe();
	}
}