package tests;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import complete_swingworker.RxSwingWorkerExample;
import complete_swingworker.helper_classes.SwingWorkerSubscriber;
import org.junit.Test;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/30/2016
 */
public class RxSwingWorkerExampleTest
{

	public static final long TIMEOUT = 8L;
	public static final int AMOUT_OF_WORK = 3;

	RxSwingWorkerExample helperClass;

	private RxSwingWorkerExample getHelperClass() {return new RxSwingWorkerExample( TIMEOUT, AMOUT_OF_WORK );}

	@Test
	public void testExecute() throws InterruptedException
	{
		printTestName("testExecute");

		helperClass = getHelperClass();
		helperClass.executeSwingWorker();

		Thread.sleep( AMOUT_OF_WORK * 2000L + 500L );
	}

	@Test
	public void testRun() throws InterruptedException
	{
		printTestName("testRun");

		helperClass = getHelperClass();
		helperClass.runSwingWorker();

		Thread.sleep( AMOUT_OF_WORK * 2000L + 500L );
	}

	@Test
	public void testCancel() throws InterruptedException
	{
		printTestName("testCancel");

		helperClass = getHelperClass();
		helperClass.executeSwingWorker();

		Thread.sleep( 2000L );
		helperClass.cancelSwingWorker( true );
		System.out.println( "isCancelled = " + helperClass.isSwingWorkerCancelled() );

		Thread.sleep( AMOUT_OF_WORK * 2000L + 500L );
	}

	@Test
	public void testGetFromSwingWorker() throws InterruptedException, ExecutionException
	{
		printTestName("testGetFromSwingWorker");

		helperClass = getHelperClass();
		helperClass.executeSwingWorker();

		String asyncResult = helperClass.getBlockingFromSwingWorker();
		System.out.println( "Get = " + asyncResult );

		Thread.sleep( AMOUT_OF_WORK * 2000L + 500L );

	}

	@Test
	public void testGetWithTimeoutFromSwingWorker() throws InterruptedException, ExecutionException, TimeoutException
	{
		printTestName("testGetWithTimeoutFromSwingWorker");

		helperClass = getHelperClass();
		helperClass.executeSwingWorker();

		String asyncResult = helperClass.getBlockingFromSwingWorker( 2L, TimeUnit.SECONDS );
		System.out.println( "Get = " + asyncResult );

		Thread.sleep( AMOUT_OF_WORK * 2000L + 500L );
	}

	@Test
	public void testGetProgress() throws InterruptedException
	{
		printTestName("testGetProgress");

		helperClass = getHelperClass();
		helperClass.executeSwingWorker();

		for ( int i = 0; i < 7; i++ )
		{
			Thread.sleep( 1000L );
			int progress = helperClass.getSwingWorkerProgress();
			System.out.println( "Progress = " + progress );
		}

		Thread.sleep( AMOUT_OF_WORK * 2000L + 500L );
	}

	@Test
	public void testStatesAndDoneFlag() throws InterruptedException
	{
		printTestName("testStatesAndDoneFlag");

		helperClass = getHelperClass();
		System.out.println( "State before execute = " + helperClass.getSwingWorkerState() );
		System.out.println( "isDone = " + helperClass.isSwingWorkerDone() );
		helperClass.executeSwingWorker();

		Thread.sleep( 1000L );
		System.out.println( "State while executing = " + helperClass.getSwingWorkerState() );
		System.out.println( "isDone = " + helperClass.isSwingWorkerDone() );

		Thread.sleep( AMOUT_OF_WORK * 2000L + 500L );
		System.out.println( "State after done = " + helperClass.getSwingWorkerState() );
		System.out.println( "isDone = " + helperClass.isSwingWorkerDone() );
	}

	@Test
	public void testAddPropertyChangeListener() throws InterruptedException
	{
		printTestName("testAddPropertyChangeListener");

		PropertyChangeListener propertyChangeListener = getPropertyChangeListener();

		helperClass = getHelperClass();
		helperClass.addPropertyChangeListenerToSwingWorker( propertyChangeListener );
		helperClass.executeSwingWorker();

		Thread.sleep( AMOUT_OF_WORK * 2000L + 500L );
	}

	@Test
	public void testRemovePropertyChangeListener() throws InterruptedException
	{
		printTestName("testRemovePropertyChangeListener");

		PropertyChangeListener propertyChangeListener = getPropertyChangeListener();

		helperClass = getHelperClass();
		PropertyChangeSupport swingWorkerPropertyChangeSupport = helperClass.getSwingWorkerPropertyChangeSupport();
		swingWorkerPropertyChangeSupport.addPropertyChangeListener( propertyChangeListener );
		helperClass.executeSwingWorker();
		Thread.sleep( 4000L );
		helperClass.removePropertyChangeListerFromSwingWorker( propertyChangeListener );
		System.out.println("Progress Property removed");

		Thread.sleep( AMOUT_OF_WORK * 2000L + 500L );
	}

	private PropertyChangeListener getPropertyChangeListener()
	{
		return new PropertyChangeListener()
		{
			public static final String SEPARATOR = " - ";

			@Override
			public void propertyChange( PropertyChangeEvent evt )
			{
				if ( "state".equals( evt.getPropertyName() ) )
				{
					if ( SwingWorkerSubscriber.State.STARTED == evt.getNewValue()  )
					{
						printMessage( "Show progress bar" );
					}
					else
					{
						printMessage( "Hide progress bar" );
					}
				}
				if ( "progress".equals( evt.getPropertyName() ) )
				{
					printMessage( "Progress bar new value = " + evt.getNewValue() );
				}
			}

			private void printMessage( String message )
			{
				System.out.println( "[" + Thread.currentThread().getName() + "] (UI-Stub)" + SEPARATOR + message );
			}
		};
	}

	private void printTestName(String testName)
	{
		System.out.println("\n\n### Test: " + testName + " ###");
	}
}

