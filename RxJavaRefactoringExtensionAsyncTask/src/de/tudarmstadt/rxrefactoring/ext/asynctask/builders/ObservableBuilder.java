//package de.tudarmstadt.rxrefactoring.ext.asynctask.builders;
//
//import java.util.List;
//import java.util.concurrent.Callable;
//import java.util.concurrent.TimeUnit;
//
//import org.eclipse.jdt.core.dom.AST;
//import org.eclipse.jdt.core.dom.ASTNode;
//import org.eclipse.jdt.core.dom.Block;
//
//import org.eclipse.jdt.core.dom.Expression;
//
//import de.tudarmstadt.rxrefactoring.core.writers.UnitWriter;
//import de.tudarmstadt.rxrefactoring.ext.asynctask.domain.SchedulerType;
//import de.tudarmstadt.rxrefactoring.ext.asynctask.utils.AsyncTaskWrapper;
//import rx.Observable;
//import rx.Scheduler;
//import rx.functions.Action0;
//import rx.functions.Action1;
//import rx.functions.Func1;
//import rx.schedulers.Schedulers;
//
///**
// * Description: Builder to create Observables or Subscriptions as string<br>
// * Author: Grebiel Jose Ifill Brito<br>
// * Created: 11/16/2016
// */
//@Deprecated
//public final class ObservableBuilder {
//	private static final String NEW_LINE = "\n";
//	private static final String SPACE = " ";
//
//	private StringBuilder rxObservable;
//	private String type;
//	private final boolean willBeCached;
//	private boolean willBeSubscribed;
//	
//	private final AsyncTaskWrapper asyncTask;
//	private final UnitWriter writer;
//
//	private ObservableBuilder(AsyncTaskWrapper asyncTask, UnitWriter writer, String type, Block doInBackground, SchedulerType observeOnScheduler) {
//		this.asyncTask = asyncTask;
//		this.writer = writer;
//		
//		validateDoInBackgroundBlock(doInBackground);
//		rxObservable = new StringBuilder();
//		addFromCallable(type, doInBackground, observeOnScheduler);
//		this.willBeCached = false;
//	}
//
//	private ObservableBuilder(AsyncTaskWrapper asyncTask, UnitWriter writer, String variableName, String type, Block doInBackground,
//			SchedulerType observeOnScheduler) {
//		this.asyncTask = asyncTask;
//		this.writer = writer;
//		
//		validateDoInBackgroundBlock(doInBackground);
//		rxObservable = new StringBuilder();
//		rxObservable.append("Observable<");
//		rxObservable.append(type);
//		rxObservable.append("> ");
//		rxObservable.append(variableName);
//		rxObservable.append(" = ");
//		addFromCallable(type, doInBackground, observeOnScheduler);
//		this.willBeCached = true;
//	}
//
//	public ObservableBuilder(AsyncTaskWrapper asyncTask, UnitWriter writer, String type, Expression expression, SchedulerType observeOnScheduler) {
//		this.asyncTask = asyncTask;
//		this.writer = writer;
//		
//		validateForExpressionk(expression);
//		rxObservable = new StringBuilder();
//		addFromIterable(type, expression, observeOnScheduler);
//		this.willBeCached = false;
//	}
//
//	/**
//	 * Builds an {@link rx.Observable} using
//	 * {@link rx.Observable#fromCallable(Callable)}. The {@link rx.Observable} will
//	 * be subscribed on {@link Schedulers#computation()}
//	 *
//	 * @param type
//	 *            the type of the observable
//	 * @param doInBackground
//	 *            the code block passed to
//	 *            {@link rx.Observable#fromCallable(Callable)}
//	 * @param observeOnScheduler
//	 *            Scheduler used for {@link rx.Observable#observeOn(Scheduler)}
//	 * @return The builder
//	 */
//	public static ObservableBuilder newObservable(AsyncTaskWrapper asyncTask, UnitWriter writer, String type, Block doInBackground, SchedulerType observeOnScheduler) {
//		return new ObservableBuilder(asyncTask, writer, type, doInBackground, observeOnScheduler);
//	}
//
//	/**
//	 * Builds an {@link rx.Observable} using
//	 * {@link rx.Observable#fromCallable(Callable)}. The {@link rx.Observable} will
//	 * be subscribed on {@link Schedulers#computation()}. The {@link rx.Observable}
//	 * will be assigned to a variable.
//	 *
//	 * @param variableName
//	 *            variable used to cache the {@link rx.Observable}
//	 * @param type
//	 *            the type of the observable
//	 * @param doInBackground
//	 *            the code block passed to
//	 *            {@link rx.Observable#fromCallable(Callable)}
//	 * @param observeOnScheduler
//	 *            scheduler used for {@link rx.Observable#observeOn(Scheduler)}
//	 * @return The builder
//	 */
//	public static ObservableBuilder newObservable(AsyncTaskWrapper asyncTask, UnitWriter writer, String variableName, String type, Block doInBackground,
//			SchedulerType observeOnScheduler) {
//		return new ObservableBuilder(asyncTask, writer, variableName, type, doInBackground, observeOnScheduler);
//	}
//
//	public static ObservableBuilder newObservable(AsyncTaskWrapper asyncTask, UnitWriter writer, String type, Expression expression,
//			SchedulerType observeOnScheduler) {
//		return new ObservableBuilder(asyncTask, writer, type, expression, observeOnScheduler);
//	}
//
//	/**
//	 * Adds the {@link Observable#doOnSubscribe(Action0)} call.
//	 *
//	 * @param doOnSubscribeBlock
//	 *            the code block to be used on subscribe. Nothing is added if this
//	 *            parameter is null.
//	 * @return The builder
//	 */
//	// public RxObservableStringBuilder addDoOnSubscribe(Block doOnSubscribeBlock) {
//	// if (doOnSubscribeBlock != null) {
//	// rxObservable.append(NEW_LINE);
//	// rxObservable.append(".doOnSubscribe(new Action0() {");
//	// rxObservable.append(NEW_LINE);
//	// rxObservable.append("@Override public void call() ");
//	// rxObservable.append(NEW_LINE);
//	// rxObservable.append(doOnSubscribeBlock.toString());
//	// rxObservable.append("})");
//	//
//	// }
//	// return this;
//	// }
//
//	/**
//	 * Adds the {@link Observable#doOnNext(Action1)} call. This method is used as an
//	 * equivalent for doOnPostExecute(T result) for AsyncTasks.
//	 * {@link Observable#doOnCompleted(Action0)} is not used because a result is
//	 * needed
//	 *
//	 * @param doOnNextBlock
//	 *            the code block to be used on next. Nothing is added if this
//	 *            parameter is null.
//	 * @param resultVariableName
//	 *            the name of the variable used. (For AsyncTasks it makes sense to
//	 *            use the same from doOnPostExecute)<br>
//	 *            for example: for "doOnPostExecute(Integer number)" use number
//	 * @return The builder
//	 */
//	public ObservableBuilder addDoOnNext(String doOnNextBlock, String resultVariableName, String type,
//			boolean isFinal) {
//		if (doOnNextBlock != null) {
//
//			rxObservable.append(NEW_LINE);
//			rxObservable.append(".");
//			rxObservable.append("doOnNext");
//			rxObservable.append("(new Action1<");
//			rxObservable.append(type);
//			rxObservable.append(">() {");
//			rxObservable.append(NEW_LINE);
//			rxObservable.append("@Override public void call(");
//			rxObservable.append(isFinal ? "final " : " ");
//			rxObservable.append(type);
//			rxObservable.append(SPACE);
//			rxObservable.append(resultVariableName);
//			rxObservable.append(") ");
//			rxObservable.append(NEW_LINE);
//			rxObservable.append(doOnNextBlock);
//			rxObservable.append("})");
//		}
//		return this;
//	}
//
//	/**
//	 * Adds the {@link Observable#doOnNext(Action1)} call. This method is used as an
//	 * equivalent for doOnPostExecute(T result) for AsyncTasks.
//	 * {@link Observable#doOnCompleted(Action0)} is not used because a result is
//	 * needed
//	 *
//	 * @param doPostExecuteBlock
//	 *            the code block to be used on next. Nothing is added if this
//	 *            parameter is null.
//	 * @param resultVariableName
//	 *            the name of the variable used. (For AsyncTasks it makes sense to
//	 *            use the same from doOnPostExecute)<br>
//	 *            for example: for "doOnPostExecute(Integer number)" use number
//	 * @return The builder
//	 */
//	public ObservableBuilder addDoOnCompleted(Block doPostExecuteBlock, String resultVariableName, String type) {
//		if (doPostExecuteBlock != null) {
//			rxObservable.append(NEW_LINE);
//			rxObservable.append(".");
//			rxObservable.append("doOnCompleted");
//			rxObservable.append("(new Action0() {");
//			rxObservable.append(NEW_LINE);
//			rxObservable.append("@Override public void call()");
//			rxObservable.append(NEW_LINE);
//			rxObservable.append(doPostExecuteBlock.toString());
//			rxObservable.append("})");
//		}
//		return this;
//	}
//
//	/**
//	 * Adds the {@link Observable#doOnNext(Action1)} call. This method is used as an
//	 * equivalent for doOnPostExecute(T result) for AsyncTasks.
//	 * {@link Observable#doOnCompleted(Action0)} is not used because a result is
//	 * needed
//	 *
//	 * @param doOnCancelled
//	 *            the code block to be used on next. Nothing is added if this
//	 *            parameter is null.
//	 * @return The builder
//	 */
//	public ObservableBuilder addDoOnCancelled(Block doOnCancelled) {
//		if (doOnCancelled != null) {
//			rxObservable.append(NEW_LINE);
//			rxObservable.append(".");
//			rxObservable.append("doOnUnsubscribe");
//			rxObservable.append("(new Action0() {");
//			rxObservable.append(NEW_LINE);
//			rxObservable.append("@Override public void call() ");
//			rxObservable.append(NEW_LINE);
//			rxObservable.append(doOnCancelled.toString());
//			rxObservable.append("})");
//		}
//		return this;
//	}
//
//	/**
//	 * Adds the {@link Observable#doOnNext(Action1)} call. This method is used as an
//	 * equivalent for doOnPostExecute(T result) for AsyncTasks.
//	 * {@link Observable#doOnCompleted(Action0)} is not used because a result is
//	 * needed
//	 *
//	 * @param onPreExecute
//	 *            the code block to be used on next. Nothing is added if this
//	 *            parameter is null.
//	 * @return The builder
//	 */
//	public ObservableBuilder addDoOnPreExecute(Block onPreExecute) {
//		if (onPreExecute != null) {
//			rxObservable.append(NEW_LINE);
//			rxObservable.append(".");
//			rxObservable.append("doOnSubscribe");
//			rxObservable.append("(new Action0() {");
//			rxObservable.append(NEW_LINE);
//			rxObservable.append("@Override public void call() ");
//			rxObservable.append(NEW_LINE);
//			rxObservable.append(onPreExecute.toString());
//			rxObservable.append("})");
//		}
//		return this;
//	}
//
//	/**
//	 * Adds the {@link Observable#timeout(long, TimeUnit)} and
//	 * {@link Observable#onErrorReturn(Func1)} calls. The onErrorReturn returns
//	 * null. Developers can modify this method after refactoring to return a default
//	 * value
//	 *
//	 * @param arguments
//	 *            list of strings of size 2 for the arguments. The first argument
//	 *            corresponds to the time and the second argument to the time unit.
//	 *            For example: 3L, TimeUnit.SECONDS
//	 * @return The builder
//	 */
//	public ObservableBuilder addTimeout(List<String> arguments) {
//		if (!arguments.isEmpty() && arguments.size() == 2) {
//			rxObservable.append(NEW_LINE);
//			rxObservable.append(".timeout(");
//			rxObservable.append(arguments.get(0));
//			rxObservable.append(", ");
//			rxObservable.append(arguments.get(1));
//			rxObservable.append(")");
//			// timeout requires to handle an error
//			rxObservable.append(NEW_LINE);
//			rxObservable.append(".onErrorReturn( new Func1<Throwable, ");
//			rxObservable.append(type);
//			rxObservable.append(">() {");
//			rxObservable.append(NEW_LINE);
//			rxObservable.append("@Override public ");
//			rxObservable.append(type);
//			rxObservable.append(" call (Throwable throwable ) { return null;}");
//			rxObservable.append(NEW_LINE);
//			rxObservable.append("})");
//
//		}
//		return this;
//	}
//
//	/**
//	 * Adds the {@link Observable#subscribe()}
//	 *
//	 * @return The builder
//	 */
//	public ObservableBuilder addSubscribe() {
//		rxObservable.append(NEW_LINE);
//		rxObservable.append(".subscribe()");
//		willBeSubscribed = true;
//		return this;
//	}
//
//	/**
//	 * Builds the observable string based on the information collected until this
//	 * method was invoked.
//	 *
//	 * @return A String representing a statement.
//	 */
//	public String build() {
//		rxObservable.append(";");
//		String statement = rxObservable.toString();
//		if (willBeCached && willBeSubscribed) {
//			// corrects the return type
//			statement = statement.replace("Observable<" + type + ">", "Subscription");
//		}
//		// SourceCodeValidator.validateStatement(statement);
//		return statement;
//	}
//
//	// ### Private Methods ###
//
//	private String getStatements(Block timeoutCatchBlock) {
//		return timeoutCatchBlock.toString().replace("{", "").replace("}", "");
//	}
//
//	private static void validateDoInBackgroundBlock(Block doInBackground) {
//		if (doInBackground == null) {
//			throw new RuntimeException("doInBackground cannot be null");
//		}
//	}
//
//	private static void validateForExpressionk(Expression expression) {
//		if (expression == null) {
//			throw new RuntimeException("expression in for loop cannot be null");
//		}
//	}
//
//	private void addFromCallable(String type, Block doInBackground, SchedulerType observeOnScheduler) {
//		this.type = type;
//		rxObservable.append("Observable.fromCallable(");
//		
////		FromCallableBuilder b = new FromCallableBuilder(asyncTask, writer);
////		ASTNode n = b.buildCallable();		
////		rxObservable.append(n.toString());
//		
//		rxObservable.append("new Callable<");
//		rxObservable.append(type);
//		rxObservable.append(">() {");
//		rxObservable.append(NEW_LINE);
//		rxObservable.append("@Override public ");
//		rxObservable.append(type);
//		rxObservable.append(" call() throws Exception");
//		rxObservable.append(NEW_LINE);
//		rxObservable.append(doInBackground.toString());
//		rxObservable.append("})");
//		
//		rxObservable.append(NEW_LINE);
//		rxObservable.append(".subscribeOn(Schedulers.computation())");
//		rxObservable.append(NEW_LINE);
//		rxObservable.append(".observeOn(");
//		rxObservable.append(observeOnScheduler.getMainThread());
//		rxObservable.append(")");
//	}
//
//	private void addFromIterable(String type, Expression expression, SchedulerType observeOnScheduler) {
//		this.type = type;
//		rxObservable.append("Observable.from(");
//		rxObservable.append(expression.toString().replace(";", ""));
//		rxObservable.append(")");
//	}
//
//	/**
//	 * Builds the observable as a result statement based on the information
//	 * collected until this method was invoked.
//	 *
//	 * @return A String representing the return statement
//	 */
//	public String buildReturnStatement() {
//		return "return " + build();
//	}
//}
