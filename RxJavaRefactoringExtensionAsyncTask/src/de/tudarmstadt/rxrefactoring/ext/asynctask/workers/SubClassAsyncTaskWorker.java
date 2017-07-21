/**
 * 
 */
package de.tudarmstadt.rxrefactoring.ext.asynctask.workers;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import de.tudarmstadt.rxrefactoring.core.codegen.ASTNodeFactory;
import de.tudarmstadt.rxrefactoring.core.utils.ASTUtils;
import de.tudarmstadt.rxrefactoring.core.utils.Log;
import de.tudarmstadt.rxrefactoring.core.workers.AbstractWorker;
import de.tudarmstadt.rxrefactoring.core.workers.WorkerStatus;
import de.tudarmstadt.rxrefactoring.core.writers.UnitWriter;
import de.tudarmstadt.rxrefactoring.core.writers.UnitWriters;
import de.tudarmstadt.rxrefactoring.ext.asynctask.builders.ObservableBuilder;
import de.tudarmstadt.rxrefactoring.ext.asynctask.builders.SubscriberBuilder;
import de.tudarmstadt.rxrefactoring.ext.asynctask.collect.AsyncTaskCollector;
import de.tudarmstadt.rxrefactoring.ext.asynctask.domain.SchedulerType;
import de.tudarmstadt.rxrefactoring.ext.asynctask.utils.AsyncTaskASTUtils;
import de.tudarmstadt.rxrefactoring.ext.asynctask.utils.AsyncTaskWrapper;
import de.tudarmstadt.rxrefactoring.ext.asynctask.writers.UnitWriterExt;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.*;

import com.google.common.collect.Multimap;

/**
 * Description: This worker is in charge of refactoring classes that extends
 * AsyncTasks that are assigned to a variable.<br>
 * Example: class Task extends AsyncTask<>{...}<br>
 * Author: Ram<br>
 * Created: 11/12/2016
 */
public class SubClassAsyncTaskWorker extends AbstractWorker<AsyncTaskCollector> {
	final String SUBSCRIPTION = "Subscription";
	final String EXECUTE = "execute";
	final String EXECUTE_ON_EXECUTOR = "executeOnExecutor";
	final String CANCEL = "cancel";
	final String ASYNC_METHOD_NAME = "getAsyncObservable";
	final String SUBSCRIBE = "subscribe";
	final String UN_SUBSCRIBE = "unsubscribe";
	private int NUMBER_OF_ASYNC_TASKS = 0;
	private int NUMBER_OF_ABSTRACT_TASKS = 0;

	/**
	 * @return the nUMBER_OF_ASYNC_TASKS
	 */
	public int getNUMBER_OF_ASYNC_TASKS() {
		return NUMBER_OF_ASYNC_TASKS;
	}

	/**
	 * @return the nUMBER_OF_ABSTRACT_TASKS
	 */
	public int getNUMBER_OF_ABSTRACT_TASKS() {
		return NUMBER_OF_ABSTRACT_TASKS;
	}

	public SubClassAsyncTaskWorker(AsyncTaskCollector collector) {
		super(collector);
	}

	@Override
	public WorkerStatus refactor() {
		NUMBER_OF_ASYNC_TASKS = 0;
		Multimap<ICompilationUnit, TypeDeclaration> cuAnonymousClassesMap = collector.getSubclasses();
		int numCunits = collector.getNumberOfCompilationUnits();
		monitor.beginTask(getClass().getSimpleName(), numCunits);
		Log.info(getClass(), "METHOD=refactor - Number of compilation units: " + numCunits);

		for (ICompilationUnit unit : cuAnonymousClassesMap.keySet()) {
			Collection<TypeDeclaration> declarations = cuAnonymousClassesMap.get(unit);
			for (TypeDeclaration asyncTaskDeclaration : declarations) {

				Log.info(getClass(), "METHOD=refactor - Extract Information from AsyncTask: " + unit.getElementName());
				AsyncTaskWrapper asyncTask = new AsyncTaskWrapper(asyncTaskDeclaration);

				if (asyncTask.getDoInBackgroundBlock() == null) {
					NUMBER_OF_ABSTRACT_TASKS++;
					continue;
				}

				AST ast = asyncTaskDeclaration.getAST();
				UnitWriterExt writer = UnitWriters.getOrElse(unit,
						() -> new UnitWriterExt(unit, ast));
				
				updateUsage(collector.getRelevantUsages(), ast, asyncTaskDeclaration, unit);
				writer.removeSuperClass(asyncTaskDeclaration);
				
				Log.info(getClass(), "METHOD=refactor - Updating imports: " + unit.getElementName());
				updateImports(writer, asyncTask);
				createLocalObservable(writer, asyncTaskDeclaration, ast, asyncTask);
				addProgressBlock(ast, unit, asyncTask, writer);
				
				Log.info(getClass(), "METHOD=refactor - Refactoring class: " + unit.getElementName());
				if (asyncTask.getOnPreExecuteBlock() != null)
					updateonPreExecute(asyncTaskDeclaration, writer, ast, asyncTask);

				NUMBER_OF_ASYNC_TASKS++;
				execution.addUnitWriter(writer);
			}

			monitor.worked(1);
		}
		Log.info(getClass(), "Number of AsynckTasks Subclass=  " + NUMBER_OF_ASYNC_TASKS);
		Log.info(getClass(), "Number of Abstract AsynckTasks Subclass=  " + NUMBER_OF_ABSTRACT_TASKS);

		return WorkerStatus.OK;
	}

	private void updateUsage(Multimap<ICompilationUnit, MethodInvocation> relevantUsages, AST ast,
			TypeDeclaration asyncTaskDeclaration, ICompilationUnit outerUnit) {
		for (ICompilationUnit unit : relevantUsages.keySet()) {
			TypeDeclaration tyDec = ast.newTypeDeclaration();

			Log.info(getClass(), "METHOD=updateUsage - updating usage for class: " + outerUnit.getElementName() + " in "
					+ unit.getElementName());
			for (MethodInvocation methodInvoke : relevantUsages.get(unit)) {
				if (methodInvoke.getName().toString().equals(EXECUTE)
						|| methodInvoke.getName().toString().equals(EXECUTE_ON_EXECUTOR)) {

					if (outerUnit.getElementName().equals(unit.getElementName())) {
						UnitWriterExt writer = UnitWriters.getOrElse(outerUnit, () -> new UnitWriterExt(outerUnit, ast));
						replaceCancel(writer, relevantUsages, methodInvoke, outerUnit, ast);
					} else {
						tyDec = ASTUtils.findParent(methodInvoke, TypeDeclaration.class);
						AST astInvoke = tyDec.getAST();
						UnitWriterExt writer = UnitWriters.getOrElse(unit, () -> new UnitWriterExt(unit, astInvoke));
						replaceCancel(writer, relevantUsages, methodInvoke, unit, astInvoke);
						execution.addUnitWriter(writer);
					}

				}
			}

		}

	}

	/**
	 * Replace asyncTask.cancel() method with Subscription.unsubscribe()
	 */
	void replaceCancel(UnitWriterExt writer, Multimap<ICompilationUnit, MethodInvocation> relevantUsages, MethodInvocation methodInvoke,
			ICompilationUnit unit, AST astInvoke) {
		
		
		
		boolean isCancelPresent = false;
		
		for (MethodInvocation methodReference : relevantUsages.get(unit)) {
			if (methodInvoke.getExpression().toString().equals(methodReference.getExpression().toString())) {
				if (methodReference.getName().toString().equals(CANCEL)) {
					isCancelPresent = true;
					Log.info(getClass(), "METHOD=replaceCancel - updating cancel invocation for class: "
							+ unit.getElementName());
					replaceExecuteWithSubscription(writer, methodInvoke, astInvoke);
					updateCancelInvocation(writer, methodReference, astInvoke);

				}
			}			
		}
		
		if (!isCancelPresent) {
			replaceExecute(writer, methodInvoke, unit, astInvoke);
		}
	}

	/**
	 * Method to refactor method invocation statements with name execute EX: new
	 * Task().execute();
	 */
	void replaceExecute(UnitWriterExt writer, MethodInvocation methodInvoke, ICompilationUnit unit, AST astInvoke) {
//		UnitWriterExt singleChangeWriter = UnitWriters.getOrElse(unit,
//				() -> new UnitWriterExt(unit, astInvoke, getClass().getSimpleName()));

		AST methodAST = methodInvoke.getAST();
		MethodInvocation execute = (MethodInvocation) ASTNode.copySubtree(methodAST, methodInvoke);
		if (execute.getName().toString().equals(EXECUTE_ON_EXECUTOR)) {
			execute.arguments().clear();
		}
		execute.setName(methodAST.newSimpleName(ASYNC_METHOD_NAME));

		MethodInvocation invocation = methodAST.newMethodInvocation();
		invocation.setExpression(execute);
		invocation.setName(methodAST.newSimpleName(SUBSCRIBE));
		// ASTUtil.replaceInStatement(methodInvoke, invocation);
		writer.replace(methodInvoke, invocation);
		// singleChangeWriter.replace(methodInvoke, invocation);
	}

	/**
	 * Method to refactor method invocation statements with name execute EX: new
	 * Task().execute();
	 */
	void replaceExecuteWithSubscription(UnitWriterExt writer, MethodInvocation methodInvoke, AST astInvoke) {
//		UnitWriterExt singleChangeWriter = UnitWriters.getOrElse(unit,
//				() -> new UnitWriterExt(unit, astInvoke, getClass().getSimpleName()));

		Log.info(getClass(), "METHOD=replaceExecuteWithSubscription - replace Execute With Subscription for class: "
				+ writer.getUnit().getElementName());
		
		createSubscriptionDeclaration(writer, methodInvoke);

		MethodInvocation execute = (MethodInvocation) ASTNode.copySubtree(astInvoke, methodInvoke);
		execute.setName(astInvoke.newSimpleName(ASYNC_METHOD_NAME));
		MethodInvocation invocation = astInvoke.newMethodInvocation();
		invocation.setExpression(execute);
		invocation.setName(astInvoke.newSimpleName(SUBSCRIBE));

		Assignment initSubscription = astInvoke.newAssignment();

		initSubscription
				.setLeftHandSide(astInvoke.newSimpleName(getVariableName(methodInvoke.getExpression()) + SUBSCRIPTION));
		initSubscription.setRightHandSide(invocation);
		writer.replace(methodInvoke, initSubscription);
	}

	void createSubscriptionDeclaration(UnitWriterExt writer, MethodInvocation methodInvoke) {

		
		TypeDeclaration tyDec = ASTUtils.findParent(methodInvoke, TypeDeclaration.class);
		AST astInvoke = tyDec.getAST();
//		UnitWriterExt singleChangeWriter = UnitWriters.getOrElse(unit,
//				() -> new UnitWriterExt(unit, astInvoke, getClass().getSimpleName()));
		VariableDeclarationFragment variable = astInvoke.newVariableDeclarationFragment();
		Expression e = methodInvoke.getExpression();

		variable.setName(astInvoke.newSimpleName(getVariableName(e) + SUBSCRIPTION));

		FieldDeclaration subscription = astInvoke.newFieldDeclaration(variable);
		subscription.setType(astInvoke.newSimpleType(astInvoke.newSimpleName(SUBSCRIPTION)));
		writer.addStatementToClass(subscription, tyDec);
		writer.addImport("rx.Subscription");
	}

	private String getVariableName(Expression e) {
		if (e instanceof FieldAccess)
			return ((FieldAccess) e).getName().toString();
		else
			return e.toString();
	}

	/**
	 * Add cancel method if AsyncTask.cacel() method was invoked
	 */
	private void updateCancelInvocation(UnitWriterExt writer, MethodInvocation methodInvoke, AST astInvoke) {

//		UnitWriterExt singleChangeWriter = UnitWriters.getOrElse(unit,
//				() -> new UnitWriterExt(unit, astInvoke, getClass().getSimpleName()));
		Log.info(getClass(),
				"METHOD=updateCancelInvocation - update Cancel Invocation for class: " + writer.getUnit().getElementName());
		MethodInvocation unSubscribe = astInvoke.newMethodInvocation();
		unSubscribe
				.setExpression(astInvoke.newSimpleName(getVariableName(methodInvoke.getExpression()) + SUBSCRIPTION));
		unSubscribe.setName(astInvoke.newSimpleName(UN_SUBSCRIBE));
		writer.replace(methodInvoke, unSubscribe);

	}

	private void updateImports(UnitWriterExt rewriter, AsyncTaskWrapper asyncTask) {
		rewriter.addImport("rx.Observable");
		rewriter.addImport("rx.schedulers.Schedulers");
		rewriter.addImport("java.util.concurrent.Callable");

		if (NUMBER_OF_ABSTRACT_TASKS == 0) {
			rewriter.removeImport("android.os.AsyncTask");
		}

		if (asyncTask.getOnPostExecuteBlock() != null) {
			rewriter.addImport("rx.functions.Action1");
			rewriter.removeImport("java.util.concurrent.ExecutionException");
			rewriter.removeImport("java.util.concurrent.TimeoutException");

		}

		if (asyncTask.getDoInBackgroundBlock() != null) {
			rewriter.addImport("rx.Subscriber");
			rewriter.addImport("java.util.Arrays");
			rewriter.addImport("rx.Subscription");
			rewriter.addImport("rx.functions.Action1");
		}

		if (asyncTask.getOnProgressUpdateBlock() != null) {
			rewriter.addImport("java.util.Arrays");
		}

		if (asyncTask.getOnCancelled() != null) {
			rewriter.addImport("rx.functions.Action0");
		}

		if (asyncTask.getOnPreExecuteBlock() != null) {
			rewriter.addImport("rx.functions.Action0");
		}
	}

	private void createLocalObservable(UnitWriterExt rewriter, TypeDeclaration taskObject, AST ast,
			AsyncTaskWrapper asyncTask) {
		replacePublishInvocations(asyncTask, rewriter);
		String observableStatement = createObservable(asyncTask, rewriter);
		MethodDeclaration getAsyncMethod = ASTNodeFactory.createMethodFromText(ast,
				"public Observable<" + asyncTask.getReturnedType() + "> getAsyncObservable( final "
						+ asyncTask.getParameters() + "){" + " return " + observableStatement + "}");
		rewriter.replaceStatement(asyncTask.getDoInBackgroundmethod(), getAsyncMethod);

		// Remove postExecute method
		if (asyncTask.getOnPostExecuteBlock() != null)
			rewriter.removeMethod(asyncTask.getOnPostExecuteBlock().getParent(), taskObject);
		// Remove updateProgress method
		if (asyncTask.getOnProgressUpdateBlock() != null)
			rewriter.removeMethod(asyncTask.getOnProgressUpdateBlock().getParent(), taskObject);
		// Remove onCancelled method
		if (asyncTask.getOnCancelled() != null)
			rewriter.removeMethod(asyncTask.getOnCancelled().getParent(), taskObject);
	}

	/**
	 * onPreExecute method will be invoked by getAsyncObservable block as a first
	 * statement.
	 * 
	 * @override has to be removed and super() should be removed from
	 *           onPreExecuteBlock
	 * @param parent
	 * @param singleChangeWriter
	 * @param ast
	 * @param asyncTask
	 */
	private void updateonPreExecute(TypeDeclaration parent, UnitWriterExt singleChangeWriter, AST ast,
			AsyncTaskWrapper asyncTask) {

		MethodDeclaration getAsyncMethod = ASTNodeFactory.createMethodFromText(ast,
				"public onPreExecute(){" + asyncTask.getOnPreExecuteBlock().toString() + "}");
		singleChangeWriter.replaceStatement((MethodDeclaration) asyncTask.getOnPreExecuteBlock().getParent(),
				getAsyncMethod);

	}

	private String createObservable(AsyncTaskWrapper asyncTask, UnitWriter writer) {

		Block doInBackgroundBlock = asyncTask.getDoInBackgroundBlock();
		Block onPostExecuteBlock = asyncTask.getOnPostExecuteBlock();
		Block doProgressUpdate = asyncTask.getOnProgressUpdateBlock();
		Block onCancelled = asyncTask.getOnCancelled();
		String type = asyncTask.getReturnedType().toString();
		String postExecuteParameters = asyncTask.getPostExecuteParameters();
		removeSuperInvocations(asyncTask);
		ObservableBuilder rxObservable = ObservableBuilder.newObservable(type, doInBackgroundBlock,
				SchedulerType.JAVA_MAIN_THREAD);
		if (doProgressUpdate != null) {

			if (onPostExecuteBlock != null) {
				AsyncTaskASTUtils.removeMethodInvocations(onPostExecuteBlock);
				AsyncTaskASTUtils.replaceFieldsWithFullyQualifiedNameIn(onPostExecuteBlock, writer);

				rxObservable.addDoOnNext(onPostExecuteBlock.toString(), postExecuteParameters,
						asyncTask.getPostExecuteType().toString(), true);
			}

		} else {
			if (onPostExecuteBlock != null) {
				AsyncTaskASTUtils.removeMethodInvocations(onPostExecuteBlock);
				AsyncTaskASTUtils.replaceFieldsWithFullyQualifiedNameIn(onPostExecuteBlock, writer);

				rxObservable.addDoOnNext(onPostExecuteBlock.toString(), postExecuteParameters, type, true);
			}
		}
		if (onCancelled != null) {
			rxObservable.addDoOnCancelled(onCancelled);
		}
		Block preExec = asyncTask.getOnPreExecuteBlock();
		if (preExec != null) {

			rxObservable.addDoOnPreExecute(preExec);
		}
		return rxObservable.build();
	}

	/**
	 * Remove super method invocation from AsyncTask methods
	 * 
	 * @param asyncTask
	 */
	private void removeSuperInvocations(AsyncTaskWrapper asyncTask) {
		for (SuperMethodInvocation methodInvocation : asyncTask.getSuperClassMethodInvocation()) {
			Statement statement = (Statement) ASTUtils.findParent(methodInvocation, Statement.class);
			statement.delete();
		}
	}

	/**
	 * If onUpdateProgressmethod exist method will add a new method to class
	 */
	private void addProgressBlock(AST ast, ICompilationUnit unit, AsyncTaskWrapper asyncTask,
			UnitWriterExt singleChangeWriter) {

		if (asyncTask.getOnProgressUpdateBlock() != null) {
			TypeDeclaration tyDec = (TypeDeclaration) ASTUtils
					.findParent(asyncTask.getOnProgressUpdateBlock().getParent(), TypeDeclaration.class);
			String newMethodString = createNewMethod(asyncTask);
			MethodDeclaration newMethod = ASTNodeFactory.createMethodFromText(ast, newMethodString);
			singleChangeWriter.addMethod(newMethod, tyDec);
		}

	}

	/**
	 * Method which will be created if onProgressUpdate method is present in
	 * asyncTask
	 * 
	 * @param asyncTask
	 * @return
	 */
	private String createNewMethod(AsyncTaskWrapper asyncTask) {
		Block doProgressUpdate = asyncTask.getOnProgressUpdateBlock();
		Type progressType = asyncTask.getProgressType();
		String progressParameters = asyncTask.getProgressParameters();
		String newSubscriber = SubscriberBuilder.newSubscriber(progressType.toString(), doProgressUpdate,
				progressParameters);
		return "private Subscriber<" + progressType.toString() + "[]> getRxUpdateSubscriber() { return " + newSubscriber
				+ "}";
	}

	/**
	 * Iterate on list of invocation of publishProgress
	 * 
	 * @param asynctaskVisitor
	 * @param rewriter
	 */
	private void replacePublishInvocations(AsyncTaskWrapper asynctaskVisitor, UnitWriterExt rewriter) {
		if (!asynctaskVisitor.getPublishInvocations().isEmpty()) {
			for (MethodInvocation publishInvocation : asynctaskVisitor.getPublishInvocations()) {
				List<?> argumentList = publishInvocation.arguments();
				AST ast = publishInvocation.getAST();
				replacePublishInvocationsAux(publishInvocation, argumentList, ast, rewriter, asynctaskVisitor);
			}
		}
	}

	/**
	 * Convert pubishProgress(parms) method to
	 * getRxUpdateSubscriber(parms).subscribe()
	 * 
	 * @param publishInvocation
	 * @param argumentList
	 * @param ast
	 */
	private <T extends ASTNode> void replacePublishInvocationsAux(T publishInvocation, List<?> argumentList, AST ast,
			UnitWriterExt rewriter, AsyncTaskWrapper asyncTask) {
		String value = argumentList.toString().replace("[", "").replace("]", "");
		String newInvocation = "getRxUpdateSubscriber().onNext((" + asyncTask.getProgressType() + "[])Arrays.asList("
				+ value + ").toArray())";
		Statement newStatement = ASTNodeFactory.createSingleStatementFromText(ast, newInvocation);

		System.out.println(publishInvocation + " -> " + newStatement);
		ASTUtils.replaceInStatement(publishInvocation, newStatement);
	}
}
