package rxrefactoring_expected.anonymous_simple;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class AnonymousClassCase16
{
	public void start()
	{
		// Same as case 12
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