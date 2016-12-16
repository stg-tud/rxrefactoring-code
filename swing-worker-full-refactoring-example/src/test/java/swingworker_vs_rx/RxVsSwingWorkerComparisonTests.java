package swingworker_vs_rx;

import static org.junit.Assert.assertTrue;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.swing.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import rxswingworker.RxSwingWorkerAPI;
import rxswingworker.SwingWorkerSubscriber;
import swingworker_rx.RxSwingWorkerFactoryWithEmitter;
import swingworker_vs_rx.test_helpers.RxSwingWorkerWrapper;

/**
 * Description: Helper "Test" class to analyze the output
 * of the regular {@link SwingWorker} and the proposed
 * Rx Swing Worker<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/02/2016
 */
public class RxVsSwingWorkerComparisonTests
{
	private static final int DEFAULT_AMOUNT_OF_WORK = 3;
	private static final long TIME_FOR_WORK_UNIT = 2000L;
	private static RxSwingWorkerAPI<String> swingWorkerApiWrapper;

	/**
	 * Enable one of the statements below to define whether the tests
	 * should be run using the regular {@link SwingWorker} or
	 * the RxSwingWorker.<br>
	 * RxSwingWorker uses {@link SwingWorkerSubscriber} and
	 * {@link rxswingworker.SwingWorkerRxOnSubscribe}
	 */
	@Before
	public void initializeApiWrapper()
	{
//		swingWorkerApiWrapper = new SwingWorkerWrapper<String, Integer>( SwingWorkerFactory.createSwingWorker( DEFAULT_AMOUNT_OF_WORK ) );
		// swingWorkerApiWrapper = new RxSwingWorkerWrapper<String, Integer>(RxSwingWorkerFactoryWithOnSubscribe.createObserver(DEFAULT_AMOUNT_OF_WORK));
		swingWorkerApiWrapper = new RxSwingWorkerWrapper<String, Integer>( RxSwingWorkerFactoryWithEmitter.createObserver( DEFAULT_AMOUNT_OF_WORK ) );
	}

	@After
	public void delay() throws InterruptedException
	{
		Thread.sleep( DEFAULT_AMOUNT_OF_WORK * TIME_FOR_WORK_UNIT + 500L );
	}

	@Test
	public void testExecute()
	{
		printTestName( "testExecute" );

		swingWorkerApiWrapper.execute();
		swingWorkerApiWrapper.execute();
	}

	@Test
	public void testRun()
	{
		printTestName( "testRun" );

		swingWorkerApiWrapper.run();
	}

	@Test
	public void testCancelInterruptingAndIsCancelled() throws InterruptedException
	{
		printTestName( "testCancelInterruptingAndIsCancelled" );

		swingWorkerApiWrapper.execute();
		Thread.sleep( 2000L );

		swingWorkerApiWrapper.cancel( true );

		boolean cancelled = swingWorkerApiWrapper.isCancelled();
		assertTrue( cancelled );

		System.out.println( "isCancelled = " + cancelled );
	}

	@Test
	public void testCancelWithtoutInterruptingAndIsCancelled() throws InterruptedException
	{
		printTestName( "testCancelWithtoutInterruptingAndIsCancelled" );

		swingWorkerApiWrapper.execute();
		Thread.sleep( 2000L );

		swingWorkerApiWrapper.cancel( false );

		boolean cancelled = swingWorkerApiWrapper.isCancelled();
		System.out.println( "isCancelled = " + cancelled );
	}

	@Test
	public void testGetWithoutTimeout() throws InterruptedException
	{
		printTestName( "testGetWithoutTimeout" );

		swingWorkerApiWrapper.execute();
		Thread.sleep( 2000L );

		String asyncResult = swingWorkerApiWrapper.get();
		System.out.println( "Get = " + asyncResult );
	}

	@Test
	public void testGetWithTimeout() throws InterruptedException, TimeoutException
	{
		printTestName( "testGetWithTimeout" );

		swingWorkerApiWrapper.execute();
		Thread.sleep( 2000L );

		String asyncResult = swingWorkerApiWrapper.get( 2L, TimeUnit.SECONDS );
		System.out.println( "Get = " + asyncResult );

	}

	@Test
	public void testGetProgress() throws InterruptedException
	{
		printTestName( "testGetProgress" );

		swingWorkerApiWrapper.execute();

		for ( int i = 0; i < DEFAULT_AMOUNT_OF_WORK * 2 + 1; i++ )
		{
			Thread.sleep( 1000L );
			int progress = swingWorkerApiWrapper.getProgress();
			System.out.println( "Progress = " + progress );
		}
	}

	@Test
	public void testStatesAndDoneFlag() throws InterruptedException
	{
		printTestName( "testStatesAndDoneFlag" );

		System.out.println( "State before execute = " + swingWorkerApiWrapper.getState() );
		System.out.println( "isDone = " + swingWorkerApiWrapper.isDone() );
		swingWorkerApiWrapper.execute();

		Thread.sleep( 1000L );
		System.out.println( "State while executing = " + swingWorkerApiWrapper.getState() );
		System.out.println( "isDone = " + swingWorkerApiWrapper.isDone() );

		Thread.sleep( DEFAULT_AMOUNT_OF_WORK * TIME_FOR_WORK_UNIT + 500L );
		System.out.println( "State after done = " + swingWorkerApiWrapper.getState() );
		System.out.println( "isDone = " + swingWorkerApiWrapper.isDone() );
	}

	@Test
	public void testAddPropertyChangeListener()
	{
		printTestName( "testAddPropertyChangeListener" );

		PropertyChangeListener propertyChangeListener = getPropertyChangeListener();

		swingWorkerApiWrapper.addPropertyChangeListener( propertyChangeListener );
		swingWorkerApiWrapper.execute();
	}

	@Test
	public void testRemovePropertyChangeListener() throws InterruptedException
	{
		printTestName( "testRemovePropertyChangeListener" );

		PropertyChangeListener propertyChangeListener = getPropertyChangeListener();

		PropertyChangeSupport propertyChangeSupport = swingWorkerApiWrapper.getPropertyChangeSupport();
		System.out.println( "Adding Property Change Listener" );
		propertyChangeSupport.addPropertyChangeListener( propertyChangeListener );
		swingWorkerApiWrapper.execute();

		Thread.sleep( 4000L );
		swingWorkerApiWrapper.removePropertyChangeListener( propertyChangeListener );
		System.out.println( "Property Change Listener removed" );
	}

	private void printTestName( String testName )
	{
		System.out.println( "\n\n### Test: " + testName + " ###" );
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
					if ( SwingWorker.StateValue.STARTED == evt.getNewValue() )
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
}
