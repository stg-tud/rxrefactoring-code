package codegenerators;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.core.dom.Block;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rxjavarefactoring.framework.constants.SchedulerType;
import rxjavarefactoring.framework.utils.SourceCodeValidator;

/**
 * Description: Builder to create Observables or Subscriptions as string<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/16/2016
 */
public final class RxObservableStringBuilder
{
	private static final String NEW_LINE = "\n";
	private static final String SPACE = " ";

	private StringBuilder rxObservable;
	private String type;
	private final boolean willBeCached;
	private boolean willBeSubscribed;

	private RxObservableStringBuilder( String type, Block doInBackground, SchedulerType observeOnScheduler )
	{
		validateDoInBackgroundBlock( doInBackground );
		rxObservable = new StringBuilder();
		addFromCallable( type, doInBackground, observeOnScheduler );
		this.willBeCached = false;
	}

	private RxObservableStringBuilder( String variableName, String type, Block doInBackground, SchedulerType observeOnScheduler )
	{
		validateDoInBackgroundBlock( doInBackground );
		rxObservable = new StringBuilder();
		rxObservable.append( "Observable<" );
		rxObservable.append( type );
		rxObservable.append( "> " );
		rxObservable.append( variableName );
		rxObservable.append( " = " );
		addFromCallable( type, doInBackground, observeOnScheduler );
		this.willBeCached = true;
	}

	/**
	 * Builds an {@link Observable} using
	 * {@link Observable#fromCallable(Callable)}. The {@link Observable}
	 * will be subscribed on
	 * {@link Schedulers#computation()}
	 *
	 * @param type
	 *            the type of the observable
	 * @param doInBackground
	 *            the code block passed to
	 *            {@link Observable#fromCallable(Callable)}
	 * @param observeOnScheduler
	 *            Scheduler used for
	 *            {@link Observable#observeOn(Scheduler)}
	 * @return The builder
	 */
	public static RxObservableStringBuilder newObservable(String type, Block doInBackground, SchedulerType observeOnScheduler )
	{
		return new RxObservableStringBuilder( type, doInBackground, observeOnScheduler );
	}

	/**
	 * Builds an {@link Observable} using
	 * {@link Observable#fromCallable(Callable)}. The {@link Observable}
	 * will be subscribed on {@link Schedulers#computation()}. The
	 * {@link Observable} will be assigned to a variable.
	 *
	 * @param variableName
	 *            variable used to cache the {@link Observable}
	 * @param type
	 *            the type of the observable
	 * @param doInBackground
	 *            the code block passed to
	 *            {@link Observable#fromCallable(Callable)}
	 * @param observeOnScheduler
	 *            scheduler used for {@link Observable#observeOn(Scheduler)}
	 * @return The builder
	 */
	public static RxObservableStringBuilder newObservable(String variableName, String type, Block doInBackground, SchedulerType observeOnScheduler )
	{
		return new RxObservableStringBuilder( variableName, type, doInBackground, observeOnScheduler );
	}

	/**
	 * Adds the {@link Observable#doOnSubscribe(Action0)} call.
	 *
	 * @param doOnSubscribeBlock
	 *            the code block to be used on subscribe. Nothing is added if
	 *            this parameter is null.
	 * @return The builder
	 */
	public RxObservableStringBuilder addDoOnSubscribe(Block doOnSubscribeBlock )
	{
		if ( doOnSubscribeBlock != null )
		{
			rxObservable.append( NEW_LINE );
			rxObservable.append( ".doOnSubscribe(new Action0() {" );
			rxObservable.append( NEW_LINE );
			rxObservable.append( "@Override public void call() " );
			rxObservable.append( NEW_LINE );
			rxObservable.append( doOnSubscribeBlock.toString() );
			rxObservable.append( "})" );

		}
		return this;
	}

	/**
	 * Adds the {@link Observable#doOnNext(Action1)} call. This method is
	 * used as an equivalent for doOnPostExecute(T result) for AsyncTasks.
	 * {@link Observable#doOnCompleted(Action0)} is not used because a result
	 * is needed
	 * 
	 * @param doOnNextBlock
	 *            the code block to be used on next. Nothing is added if this
	 *            parameter is null.
	 * @param resultVariableName
	 *            the name of the variable used. (For AsyncTasks it makes sense
	 *            to use the same from doOnPostExecute)<br>
	 *            for example: for "doOnPostExecute(Integer number)" use number
	 * @return The builder
	 */
	public RxObservableStringBuilder addDoOnNext(Block doOnNextBlock, String resultVariableName )
	{
		if ( doOnNextBlock != null )
		{
			rxObservable.append( NEW_LINE );
			rxObservable.append( ".doOnNext(new Action1<" );
			rxObservable.append( type );
			rxObservable.append( ">() {" );
			rxObservable.append( NEW_LINE );
			rxObservable.append( "@Override public void call(" );
			rxObservable.append( type );
			rxObservable.append( SPACE );
			rxObservable.append( resultVariableName );
			rxObservable.append( ") " );
			rxObservable.append( NEW_LINE );
			rxObservable.append( doOnNextBlock.toString() );
			rxObservable.append( "})" );
		}
		return this;
	}

	/**
	 * Adds the {@link Observable#timeout(long, TimeUnit)} and
	 * {@link Observable#onErrorReturn(Func1)} calls.
	 * The onErrorReturn returns null. Developers can modify this method after
	 * refactoring to return a default value
	 * 
	 * @param arguments
	 *            list of strings of size 2 for the arguments. The first
	 *            argument corresponds to the time and the second argument to
	 *            the time unit. For example: 3L, TimeUnit.SECONDS
	 * @param timeoutCatchBlock
	 * @return The builder
	 */
	public RxObservableStringBuilder addTimeout(List<String> arguments, Block timeoutCatchBlock )
	{
		if ( !arguments.isEmpty() && arguments.size() == 2 && timeoutCatchBlock != null )
		{
			rxObservable.append( NEW_LINE );
			rxObservable.append( ".timeout(" );
			rxObservable.append( arguments.get( 0 ) );
			rxObservable.append( ", " );
			rxObservable.append( arguments.get( 1 ) );
			rxObservable.append( ")" );
			// timeout requires to handle an error
			rxObservable.append( NEW_LINE );
			rxObservable.append( ".onErrorResumeNext(new Func1<Throwable, Observable<? extends " );
			rxObservable.append( type );
			rxObservable.append( ">>() {" );
			rxObservable.append( NEW_LINE );
			rxObservable.append( "@Override public Observable<? extends " );
			rxObservable.append( type );
			rxObservable.append( "> call (Throwable throwable ) {" );
			rxObservable.append( NEW_LINE );
			rxObservable.append( getStatements( timeoutCatchBlock ) );
			rxObservable.append( NEW_LINE );
			rxObservable.append( "return Observable.empty();}" );
			rxObservable.append( NEW_LINE );
			rxObservable.append( "})" );

		}
		return this;
	}

	/**
	 * Adds the {@link Observable#subscribe()}
	 * 
	 * @return The builder
	 */
	public RxObservableStringBuilder addSubscribe()
	{
		rxObservable.append( NEW_LINE );
		rxObservable.append( ".subscribe()" );
		willBeSubscribed = true;
		return this;
	}

	/**
	 * Builds the observable string based on the information collected until
	 * this method was invoked.
	 * 
	 * @return A String representing a statement.
	 */
	public String build()
	{
		rxObservable.append( ";" );
		String statement = rxObservable.toString();
		if ( willBeCached && willBeSubscribed )
		{
			// corrects the return type
			statement = statement
					.replace( "Observable<" + type + ">", "Subscription" );
		}
		SourceCodeValidator.validateStatement( statement );
		return statement;
	}

	/**
	 * Builds the observable as a result statement based on
	 * the information collected until this method was invoked.
	 * 
	 * @return A String representing the return statement
	 */
	public String buildReturnStatement()
	{
		return "return " + build();
	}

	// ### Private Methods ###

	private String getStatements( Block timeoutCatchBlock )
	{
		return timeoutCatchBlock.toString().replace( "{", "" ).replace( "}", "" );
	}

	private static void validateDoInBackgroundBlock( Block doInBackground )
	{
		if ( doInBackground == null )
		{
			throw new RuntimeException( "doInBackground cannot be null" );
		}
	}

	private void addFromCallable( String type, Block doInBackground, SchedulerType observeOnScheduler )
	{
		this.type = type;
		rxObservable.append( "Observable.fromCallable(new Callable<" );
		rxObservable.append( type );
		rxObservable.append( ">() {" );
		rxObservable.append( NEW_LINE );
		rxObservable.append( "@Override public " );
		rxObservable.append( type );
		rxObservable.append( " call() throws Exception" );
		rxObservable.append( NEW_LINE );
		rxObservable.append( doInBackground.toString() );
		rxObservable.append( "})" );
		rxObservable.append( NEW_LINE );
		rxObservable.append( ".subscribeOn(Schedulers.computation())" );
		rxObservable.append( NEW_LINE );
		rxObservable.append( ".observeOn(" );
		rxObservable.append( observeOnScheduler.getMainThread() );
		rxObservable.append( ")" );
	}
}
