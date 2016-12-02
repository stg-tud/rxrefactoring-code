package rxswingworker;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/02/2016
 */
public interface RxSwingWorkerAPI<ReturnType>
{
	void addPropertyChangeListener( PropertyChangeListener listener );

	boolean cancel( boolean mayInterruptIfRunning );

	void execute();

	void firePropertyChange( String propertyName, Object oldValue, Object newValue );

	ReturnType get() throws InterruptedException;

	ReturnType get( long timeout, TimeUnit unit ) throws InterruptedException;

	int getProgress();

	PropertyChangeSupport getPropertyChangeSupport();

	SwingWorker.StateValue getState();

	boolean isCancelled();

	boolean isDone();

	void removePropertyChangeListener( PropertyChangeListener listener );

	void run();
}
