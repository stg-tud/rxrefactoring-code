package rxjavarefactoring.framework.constants;

import rx.Scheduler;

/**
 * Description: Enum to specify the scheduler to be used as parameter of
 * {@link rx.Observable#observeOn(Scheduler)}<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/16/2016
 */
public enum SchedulerType
{
	ANDROID_MAIN_THREAD( "AndroidSchedulers.mainThread()" ),
	JAVA_MAIN_THREAD( "Schedulers.immediate()" );

	private final String mainThread;

	SchedulerType( String mainThread )
	{
		this.mainThread = mainThread;
	}

	public String getMainThread()
	{
		return mainThread;
	}
}
