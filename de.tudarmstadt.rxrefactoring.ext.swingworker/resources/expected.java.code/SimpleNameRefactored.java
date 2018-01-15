package rxrefactoring;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.*;

import de.tudarmstadt.stg.rx.swingworker.SWEmitter;
import de.tudarmstadt.stg.rx.swingworker.SWPackage;
import de.tudarmstadt.stg.rx.swingworker.SWSubscriber;
import rx.Emitter;

public class SimpleName
{
	public void doSomething()
	{
		rx.Observable<SWPackage<Object, Object>> rxObservable = rx.Observable.fromEmitter(new SWEmitter<Object, Object>()
		{
			@Override
			protected Object doInBackground() throws Exception
			{
				return null;
			}
		}, Emitter.BackpressureMode.BUFFER );

		SWSubscriber<Object, Object> rxObserver = new SWSubscriber<Object, Object>( rxObservable ) {};

		doSomethingElse(rxObserver);

		rx.Observable<SWPackage<String, Integer>> rxObservable1 = rx.Observable.fromEmitter(new SWEmitter<String, Integer>()
		{
			@Override
			protected String doInBackground() throws Exception
			{
				return null;
			}
		}, Emitter.BackpressureMode.BUFFER );

		SWSubscriber<String, Integer> rxObserver2 = new SWSubscriber<String, Integer>( rxObservable1 ) {};

		doSomethingElseParameterized(rxObserver2);

		ExecutorService executor = Executors.newSingleThreadExecutor();
		rxObserver.executeObservable();

		if ( rxObserver != null )
		{
			rxObserver.cancelObservable( true );
		}
	}

	private void doSomethingElse(SWSubscriber rxObserver)
	{
		rxObserver.executeObservable();
	}

	private void doSomethingElseParameterized(SWSubscriber<String, Integer> rxObserver)
	{
		rxObserver.executeObservable();
	}
	
}