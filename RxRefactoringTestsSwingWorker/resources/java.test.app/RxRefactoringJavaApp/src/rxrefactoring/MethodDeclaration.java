package rxrefactoring;

import javax.swing.*;

import de.tudarmstadt.stg.rx.swingworker.SWChannel;
import de.tudarmstadt.stg.rx.swingworker.SWEmitter;
import de.tudarmstadt.stg.rx.swingworker.SWSubscriber;
import rx.Emitter;

public class MethodDeclaration
{
	public void doSomething()
	{
		getSwingWorker().executeObservable();
	}

	private SWSubscriber<String, Integer> getSwingWorker()
	{
		rx.Observable<SWChannel<String, Integer>> rxObservable = rx.Observable
				.fromEmitter(new SWEmitter<String, Integer>() {
					@Override
					protected String doInBackground() throws Exception {
						return null;
					}
				}, Emitter.BackpressureMode.BUFFER);
		
		return new SWSubscriber<String, Integer>(rxObservable) {
		};
		
	};
}