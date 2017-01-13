package rxrefactoring;

import javax.swing.*;

import de.tudarmstadt.stg.rx.swingworker.SWChannel;
import de.tudarmstadt.stg.rx.swingworker.SWEmitter;
import de.tudarmstadt.stg.rx.swingworker.SWSubscriber;
import rx.Emitter;

public class MethodInvocationSubclass
{
	
	public void main()
	{
		new MySwingWorker().executeObservable();
	}
	
	class MySwingWorker extends SWSubscriber
	{
		MySwingWorker() {
			setObservable(getRxObservable());
		}

		private rx.Observable<SWChannel<Object, Object>> getRxObservable() {
			return rx.Observable.fromEmitter(new SWEmitter<Object, Object>() {
				@Override
				protected Object doInBackground() throws Exception {
					return null;
				}
			}, Emitter.BackpressureMode.BUFFER);
		}
		
	}
}