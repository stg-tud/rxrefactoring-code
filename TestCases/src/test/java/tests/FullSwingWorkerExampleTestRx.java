package tests;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.swing.*;

import complete_swingworker.FullSwingWorkerExampleRx;
import org.junit.Test;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/30/2016
 */
public class FullSwingWorkerExampleTestRx
{

	public static final long TIMEOUT = 8L;
	public static final int AMOUT_OF_WORK = 3;

	@Test
	public void testExecute() throws InterruptedException
	{
		printTestName("testExecute");

		FullSwingWorkerExampleRx asyncWrapper = new FullSwingWorkerExampleRx( TIMEOUT, AMOUT_OF_WORK );
		asyncWrapper.executeSwingWorker();

		Thread.sleep( AMOUT_OF_WORK * 2000L + 500L );
	}

	@Test
	public void testRun() throws InterruptedException
	{
		printTestName("testRun");

		FullSwingWorkerExampleRx asyncWrapper = new FullSwingWorkerExampleRx( TIMEOUT, AMOUT_OF_WORK );
		asyncWrapper.runSwingWorker();

		Thread.sleep( AMOUT_OF_WORK * 2000L + 500L );
	}

	@Test
	public void testCancel() throws InterruptedException
	{
		printTestName("testCancel");

		FullSwingWorkerExampleRx asyncWrapper = new FullSwingWorkerExampleRx( TIMEOUT, AMOUT_OF_WORK );
		asyncWrapper.executeSwingWorker();

		Thread.sleep( 2000L );
		asyncWrapper.cancelSwingWorker( true );
		System.out.println( "isCancelled = " + asyncWrapper.isSwingWorkerCancelled() );

		Thread.sleep( AMOUT_OF_WORK * 2000L + 500L );
	}

	@Test
	public void testGetFromSwingWorker() throws InterruptedException, ExecutionException
	{
		printTestName("testGetFromSwingWorker");

		FullSwingWorkerExampleRx asyncWrapper = new FullSwingWorkerExampleRx( TIMEOUT, AMOUT_OF_WORK );
		asyncWrapper.executeSwingWorker();

		String asyncResult = asyncWrapper.getBlockingFromSwingWorker();
		System.out.println( "Get = " + asyncResult );

		Thread.sleep( AMOUT_OF_WORK * 2000L + 500L );

	}

	@Test( expected = RuntimeException.class )
	public void testGetWithTimeoutFromSwingWorker() throws InterruptedException, ExecutionException, TimeoutException
	{
		printTestName("testGetWithTimeoutFromSwingWorker");

		FullSwingWorkerExampleRx asyncWrapper = new FullSwingWorkerExampleRx( TIMEOUT, AMOUT_OF_WORK );
		asyncWrapper.executeSwingWorker();

		String asyncResult = asyncWrapper.getBlockingFromSwingWorker( 2L, TimeUnit.SECONDS );
		System.out.println( "Get = " + asyncResult );

		Thread.sleep( AMOUT_OF_WORK * 2000L + 500L );
	}

	@Test
	public void testGetProgress() throws InterruptedException
	{
		printTestName("testGetProgress");

		FullSwingWorkerExampleRx asyncWrapper = new FullSwingWorkerExampleRx( TIMEOUT, AMOUT_OF_WORK );
		asyncWrapper.executeSwingWorker();

		for ( int i = 0; i < 7; i++ )
		{
			Thread.sleep( 1000L );
			int progress = asyncWrapper.getSwingWorkerProgress();
			System.out.println( "Progress = " + progress );
		}

		Thread.sleep( AMOUT_OF_WORK * 2000L + 500L );
	}

	@Test
	public void testStatesAndDoneFlag() throws InterruptedException
	{
		printTestName("testStatesAndDoneFlag");

		FullSwingWorkerExampleRx asyncWrapper = new FullSwingWorkerExampleRx( TIMEOUT, AMOUT_OF_WORK );
		System.out.println( "State before execute = " + asyncWrapper.getSwingWorkerState() );
		System.out.println( "isDone = " + asyncWrapper.isSwingWorkerDone() );
		asyncWrapper.executeSwingWorker();

		Thread.sleep( 1000L );
		System.out.println( "State while executing = " + asyncWrapper.getSwingWorkerState() );
		System.out.println( "isDone = " + asyncWrapper.isSwingWorkerDone() );

		Thread.sleep( AMOUT_OF_WORK * 2000L + 500L );
		System.out.println( "State after done = " + asyncWrapper.getSwingWorkerState() );
		System.out.println( "isDone = " + asyncWrapper.isSwingWorkerDone() );
	}

	@Test
	public void testAddPropertyChangeListener() throws InterruptedException
	{
		printTestName("testAddPropertyChangeListener");

		PropertyChangeListener propertyChangeListener = getPropertyChangeListener();

		FullSwingWorkerExampleRx asyncWrapper = new FullSwingWorkerExampleRx( TIMEOUT, AMOUT_OF_WORK );
		asyncWrapper.addPropertyChangeListenerToSwingWorker( propertyChangeListener );
		asyncWrapper.executeSwingWorker();

		Thread.sleep( AMOUT_OF_WORK * 2000L + 500L );
	}

	@Test
	public void testRemovePropertyChangeListener() throws InterruptedException
	{
		printTestName("testRemovePropertyChangeListener");

		PropertyChangeListener propertyChangeListener = getPropertyChangeListener();

		FullSwingWorkerExampleRx asyncWrapper = new FullSwingWorkerExampleRx( TIMEOUT, AMOUT_OF_WORK );
		PropertyChangeSupport swingWorkerPropertyChangeSupport = asyncWrapper.getSwingWorkerPropertyChangeSupport();
		swingWorkerPropertyChangeSupport.addPropertyChangeListener( propertyChangeListener );
		asyncWrapper.executeSwingWorker();
		Thread.sleep( 4000L );
		asyncWrapper.removePropertyChangeListerFromSwingWorker( propertyChangeListener );

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
					if ( FullSwingWorkerExampleRx.StatefulRxObservable.STATE_STARTED == (int) evt.getNewValue()  )
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

