package rxrefactoring;

import javax.swing.*;

import de.tudarmstadt.stg.rx.swingworker.SWDto;
import de.tudarmstadt.stg.rx.swingworker.SWEmitter;
import de.tudarmstadt.stg.rx.swingworker.SWSubscriber;
import rx.Emitter;

public class FieldDeclaration
{
	private SWSubscriber<String, Integer> rxObserver;

	private SWSubscriber<String, String> anotherRxObserver = new RxObserver();

	class RxObserver extends SWSubscriber<String, String>
	{
		RxObserver()
		{
			setObservable(getRxObservable());
		}

		private rx.Observable<SWDto<String, String>> getRxObservable()
		{
			return rx.Observable.fromEmitter( new SWEmitter<String, String>()
			{
				@Override
				protected String doInBackground() throws Exception
				{
					return "AsyncResult";
				}
			}, Emitter.BackpressureMode.BUFFER );
		}
	}

	private static SWSubscriber aThirdRxObserver;
}