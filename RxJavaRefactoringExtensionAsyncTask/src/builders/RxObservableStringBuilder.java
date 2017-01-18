package builders;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import domain.SchedulerType;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rxjavarefactoring.framework.utils.SourceCodeValidator;

/**
 * Description: Builder to create Observables or Subscriptions as text<br>
 * Author: Grebiel Jose Ifill Brito, Ram<br>
 * Created: 11/16/2016
 */
public class RxObservableStringBuilder {
	private static final String NEW_LINE = "\n";
	private static final String SPACE = " ";
	private static StringBuilder rxObservable;
	private static String type;
	private final boolean willBeCached;
	private boolean willBeSubscribed;

	private RxObservableStringBuilder(boolean willBeCached) {
		this.willBeCached = willBeCached;
	}

	/**
	 * Builds an {@link Observable} using
	 * {@link Observable#fromCallable(Callable)}. The {@link Observable}
	 * will be subscribed on {@link Schedulers#computation()}
	 * 
	 * @param type
	 *            the type of the observable
	 * @param doInBackground
	 *            the code block passed to
	 *            {@link Observable#fromCallable(Callable)}
	 * @param observeOnScheduler
	 *            Scheduler used for {@link Observable#observeOn(Scheduler)}
	 * @return The builder
	 */
	public static RxObservableStringBuilder newObservable(String type, Block doInBackground,
			SchedulerType observeOnScheduler) {
		validateDoInBackgroundBlock(doInBackground);
		rxObservable = new StringBuilder();
		addFromCallable(type, doInBackground, observeOnScheduler);
		return new RxObservableStringBuilder(false);
	}

	/**
	 * Builds an {@link Observable} using
	 * {@link Observable#from(Iterable)}. The {@link Observable} will be
	 * subscribed on {@link Schedulers#computation()}
	 * 
	 * @param type
	 *            the type of the observable
	 *            the code block passed to
	 *            {@link Observable#fromCallable(Callable)}
	 * @param observeOnScheduler
	 *            Scheduler used for {@link Observable#observeOn(Scheduler)}
	 * @return The builder
	 */
	public static RxObservableStringBuilder newObservable(String type, Expression expression,
			SchedulerType observeOnScheduler) {
		validateForExpressionk(expression);
		rxObservable = new StringBuilder();
		addFromIterable(type, expression, observeOnScheduler);
		return new RxObservableStringBuilder(false);
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
	public static RxObservableStringBuilder newObservable(String variableName, String type, Block doInBackground,
			SchedulerType observeOnScheduler) {
		validateDoInBackgroundBlock(doInBackground);
		rxObservable = new StringBuilder();
		rxObservable.append("Observable<");
		rxObservable.append(type);
		rxObservable.append("> ");
		rxObservable.append(variableName);
		rxObservable.append(" = ");
		addFromCallable(type, doInBackground, observeOnScheduler);
		return new RxObservableStringBuilder(true);
	}

	/**
	 * Adds the {@link Observable#doOnSubscribe(Action0)} call.
	 * 
	 * @param doOnSubscribeBlock
	 *            the code block to be used on subscribe. Nothing is added if
	 *            this parameter is null.
	 * @return The builder
	 */
	public RxObservableStringBuilder addDoOnSubscribe(Block doOnSubscribeBlock) {
		if (doOnSubscribeBlock != null) {
			rxObservable.append(NEW_LINE);
			rxObservable.append(".doOnSubscribe(new Action0() {");
			rxObservable.append(NEW_LINE);
			rxObservable.append("@Override public void call() ");
			rxObservable.append(NEW_LINE);
			rxObservable.append(doOnSubscribeBlock.toString());
			rxObservable.append("})");

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
	public RxObservableStringBuilder addDoOnNext(String doOnNextBlock, String resultVariableName,String type,boolean isFinal) {
		if (doOnNextBlock != null) {
			rxObservable.append(NEW_LINE);
			rxObservable.append(".");
			rxObservable.append("doOnNext");
			rxObservable.append("(new Action1<");
			rxObservable.append(type);
			rxObservable.append(">() {");
			rxObservable.append(NEW_LINE);
			rxObservable.append("@Override public void call(");
			rxObservable.append(isFinal?"final ":" ");
			rxObservable.append(type);
			rxObservable.append(SPACE);
			rxObservable.append(resultVariableName);
			rxObservable.append(") ");
			rxObservable.append(NEW_LINE);
			rxObservable.append(doOnNextBlock);
			rxObservable.append("})");
		}
		return this;
	}
	
	/**
	 * Adds the {@link Observable#doOnNext(Action1)} call. This method is
	 * used as an equivalent for doOnPostExecute(T result) for AsyncTasks.
	 * {@link Observable#doOnCompleted(Action0)} is not used because a result
	 * is needed
	 * 
	 * @param doPostExecuteBlock
	 *            the code block to be used on next. Nothing is added if this
	 *            parameter is null.
	 * @param resultVariableName
	 *            the name of the variable used. (For AsyncTasks it makes sense
	 *            to use the same from doOnPostExecute)<br>
	 *            for example: for "doOnPostExecute(Integer number)" use number
	 * @return The builder
	 */
	public RxObservableStringBuilder addDoOnCompleted(Block doPostExecuteBlock, String resultVariableName,String type) {
		if (doPostExecuteBlock != null) {
			rxObservable.append(NEW_LINE);
			rxObservable.append(".");
			rxObservable.append("doOnCompleted");
			rxObservable.append("(new Action0() {");
			rxObservable.append(NEW_LINE);
			rxObservable.append("@Override public void call()");
			rxObservable.append(NEW_LINE);
			rxObservable.append(doPostExecuteBlock.toString());
			rxObservable.append("})");
		}
		return this;
	}

	/**
	 * Adds the {@link Observable#doOnNext(Action1)} call. This method is
	 * used as an equivalent for doOnPostExecute(T result) for AsyncTasks.
	 * {@link Observable#doOnCompleted(Action0)} is not used because a result
	 * is needed
	 * 
	 * @param doOnCancelled
	 *            the code block to be used on next. Nothing is added if this
	 *            parameter is null.
	 * @return The builder
	 */
	public RxObservableStringBuilder addDoOnCancelled(Block doOnCancelled) {
		if (doOnCancelled != null) {
			rxObservable.append(NEW_LINE);
			rxObservable.append(".");
			rxObservable.append("doOnUnsubscribe");
			rxObservable.append("(new Action0() {");
			rxObservable.append(NEW_LINE);
			rxObservable.append("@Override public void call() ");
			rxObservable.append(NEW_LINE);
			rxObservable.append(doOnCancelled.toString());
			rxObservable.append("})");
		}
		return this;
	}
	
	/**
	 * Adds the {@link Observable#doOnNext(Action1)} call. This method is
	 * used as an equivalent for doOnPostExecute(T result) for AsyncTasks.
	 * {@link Observable#doOnCompleted(Action0)} is not used because a result
	 * is needed
	 * 
	 * @param onPreExecute
	 *            the code block to be used on next. Nothing is added if this
	 *            parameter is null.
	 * @return The builder
	 */
	public RxObservableStringBuilder addDoOnPreExecute(Block onPreExecute) {
		if (onPreExecute != null) {
			rxObservable.append(NEW_LINE);
			rxObservable.append(".");
			rxObservable.append("doOnSubscribe");
			rxObservable.append("(new Action0() {");
			rxObservable.append(NEW_LINE);
			rxObservable.append("@Override public void call() ");
			rxObservable.append(NEW_LINE);
			rxObservable.append(onPreExecute.toString());
			rxObservable.append("})");
		}
		return this;
	}
	
	
	/**
	 * Adds the {@link Observable#timeout(long, TimeUnit)} and
	 * {@link Observable#onErrorReturn(Func1)} calls. The onErrorReturn returns
	 * null. Developers can modify this method after refactoring to return a
	 * default value
	 * 
	 * @param arguments
	 *            list of strings of size 2 for the arguments. The first
	 *            argument corresponds to the time and the second argument to
	 *            the time unit. For example: 3L, TimeUnit.SECONDS
	 * @return The builder
	 */
	public RxObservableStringBuilder addTimeout(List<String> arguments) {
		if (!arguments.isEmpty() && arguments.size() == 2) {
			rxObservable.append(NEW_LINE);
			rxObservable.append(".timeout(");
			rxObservable.append(arguments.get(0));
			rxObservable.append(", ");
			rxObservable.append(arguments.get(1));
			rxObservable.append(")");
			// timeout requires to handle an error
			rxObservable.append(NEW_LINE);
			rxObservable.append(".onErrorReturn( new Func1<Throwable, ");
			rxObservable.append(type);
			rxObservable.append(">() {");
			rxObservable.append(NEW_LINE);
			rxObservable.append("@Override public ");
			rxObservable.append(type);
			rxObservable.append(" call (Throwable throwable ) { return null;}");
			rxObservable.append(NEW_LINE);
			rxObservable.append("})");

		}
		return this;
	}

	/**
	 * Adds the {@link Observable#subscribe()}
	 * 
	 * @return The builder
	 */
	public RxObservableStringBuilder addSubscribe() {
		rxObservable.append(NEW_LINE);
		rxObservable.append(".subscribe()");
		willBeSubscribed = true;
		return this;
	}

	/**
	 * Builds the observable string based on the information collected until
	 * this moment.
	 * 
	 * @return A String representing a statement.
	 */
	public String build() {
		rxObservable.append(";");
		String statement = rxObservable.toString();
		if (willBeCached && willBeSubscribed) {
			// corrects the return type
			statement = statement.replace("Observable<" + type + ">", "Subscription");
		}
		SourceCodeValidator.validateStatement(statement);
		return statement;
	}

	// ### Private Methods ###

	private static void validateDoInBackgroundBlock(Block doInBackground) {
		if (doInBackground == null) {
			throw new RuntimeException("doInBackground cannot be null");
		}
	}

	private static void validateForExpressionk(Expression expression) {
		if (expression == null) {
			throw new RuntimeException("expression in for loop cannot be null");
		}
	}

	private static void addFromCallable(String type, Block doInBackground, SchedulerType observeOnScheduler) {
		RxObservableStringBuilder.type = type;
		rxObservable.append("Observable.fromCallable(new Callable<");
		rxObservable.append(type);
		rxObservable.append(">() {");
		rxObservable.append(NEW_LINE);
		rxObservable.append("@Override public ");
		rxObservable.append(type);
		rxObservable.append(" call() throws Exception");
		rxObservable.append(NEW_LINE);
		rxObservable.append(doInBackground.toString());
		rxObservable.append("})");
		rxObservable.append(NEW_LINE);
		rxObservable.append(".subscribeOn(Schedulers.computation())");
		rxObservable.append(NEW_LINE);
		rxObservable.append(".observeOn(");
		rxObservable.append(observeOnScheduler.getMainThread());
		rxObservable.append(")");
	}

	private static void addFromIterable(String type, Expression expression, SchedulerType observeOnScheduler) {
		RxObservableStringBuilder.type = type;
		rxObservable.append("Observable.from(");
		rxObservable.append(expression.toString().replace(";", ""));
		rxObservable.append(")");
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
}
