package rxrefactoring;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class AnonymousClassCase13
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
						return "DONE 1";
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

		// Here the variable and method name was made unique using a number
		final Subscriber<List<Integer>> rxUpdateSubscriber1 = getRxUpdateSubscriber1();
		Observable
				.fromCallable(new Callable<String>()
				{
					@Override
					public String call() throws Exception
					{
						for ( int i = 0; i < 10; i++ )
						{
							longRunningOperation();
							rxUpdateSubscriber1.onNext(Arrays.asList(i * 10));
						}
						return "DONE 2";
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

	private Subscriber<List<Integer>> getRxUpdateSubscriber1()
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
					System.out.println("Progress 2 = " + i + "%");
				}
			}
		};
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
					System.out.println("Progress 1 = " + i + "%");
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