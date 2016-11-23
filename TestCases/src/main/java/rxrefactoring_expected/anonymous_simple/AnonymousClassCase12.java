package rxrefactoring_expected.anonymous_simple;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class AnonymousClassCase12
{
	public void start()
	{
		// Subscriber needed to send progress during the execution.
		// See MethodDeclaration getRxUpdateSubscriber()
		final Subscriber<List<Integer>> rxUpdateSubscriber = getRxUpdateSubscriber();
		Observable
				.fromCallable(new Callable<String>()
				{
					@Override
					public String call() throws Exception
					{
						for ( int i = 0; i < 10; i++ )
						{
							longRunningOperation();
							// The call on next is in charge of sending the progress
							// Arrays.asList(x, y, ... ) is used because the original signarute
							// works chunks (List data type)
							rxUpdateSubscriber.onNext(Arrays.asList(i * 10));
						}
						return "DONE";
					}
				}).subscribeOn(Schedulers.computation())
				.observeOn(Schedulers.immediate())
				.doOnNext(new Action1<String>()
				{
					@Override
					public void call(String asyncResult)
					{
						String result = asyncResult;
						System.out.println("[Thread: " + Thread.currentThread().getName() + "] Result:" + result);
					}
				})
				.subscribe();
	}

	// Implements the action that action that was performed after SwingWorker#publish(x, y ...)
	private Subscriber<List<Integer>> getRxUpdateSubscriber()
	{
		return new Subscriber<List<Integer>>()
		{
			@Override
			public void onCompleted()
			{

			}

			@Override
			public void onError(Throwable throwable)
			{

			}

			@Override
			public void onNext(List<Integer> chunks)
			{
				// Same code as in SwingWorker#process(List<Integer> chunks)
				for ( Integer i : chunks )
				{
					System.out.println("Progress = " + i + "%");
				}
			}
		};
	}

	private void longRunningOperation() throws InterruptedException
	{
		Thread.sleep(1000L);
		System.out.println("[Thread: " + Thread.currentThread().getName() + "] Long running operation completed.");
	}
}