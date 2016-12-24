package rxrefactoring;

import java.util.Arrays;
import java.util.List;

import javax.swing.*;

import de.tudarmstadt.stg.rx.swingworker.SWDto;
import de.tudarmstadt.stg.rx.swingworker.SWEmitter;
import de.tudarmstadt.stg.rx.swingworker.SWSubscriber;
import rx.Emitter;
import rx.Observable;

public class SimpleName
{
	public void doSomething()
	{
		Observable<SWDto<Object, Object>> rxObservable = Observable.fromEmitter(new SWEmitter<Object, Object>()
		{
			@Override
			protected Object doInBackground() throws Exception
			{
				return null;
			}
		}, Emitter.BackpressureMode.BUFFER );

		SWSubscriber<Object, Object> rxObserver = new SWSubscriber<Object, Object>( rxObservable ) {};
		
		doSomethingElse(rxObserver);
	}

	private void doSomethingElse(SWSubscriber rxObserver)
	{
		rxObserver.executeObservable();
	}
	
}