package de.tudarmstadt.rxrefactoring.ext.asynctask.workers;

import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.codegen.ASTNodeFactory;
import de.tudarmstadt.rxrefactoring.core.utils.ASTUtils;
import de.tudarmstadt.rxrefactoring.core.utils.Log;
import de.tudarmstadt.rxrefactoring.core.workers.AbstractWorker;
import de.tudarmstadt.rxrefactoring.core.workers.WorkerStatus;
import de.tudarmstadt.rxrefactoring.core.writers.UnitWriter;
import de.tudarmstadt.rxrefactoring.core.writers.UnitWriters;
import de.tudarmstadt.rxrefactoring.ext.asynctask.builders.ComplexObservableBuilder;
import de.tudarmstadt.rxrefactoring.ext.asynctask.builders.ObservableBuilder;
import de.tudarmstadt.rxrefactoring.ext.asynctask.builders.SubscriberHolder;
import de.tudarmstadt.rxrefactoring.ext.asynctask.collect.AsyncTaskCollector;
import de.tudarmstadt.rxrefactoring.ext.asynctask.domain.SchedulerType;
import de.tudarmstadt.rxrefactoring.ext.asynctask.utils.AsyncTaskASTUtils;
import de.tudarmstadt.rxrefactoring.ext.asynctask.utils.AsyncTaskWrapper;
import de.tudarmstadt.rxrefactoring.ext.asynctask.writers.UnitWriterExt;

/**
 * Author: Template<br>
 * Created: 01/18/2017
 */
public class AnonymAsyncTaskWorker extends AbstractWorker<AsyncTaskCollector> {
	private static final String EMPTY = "";
	private int NUMBER_OF_ANONYMOUS_TASKS = 0;

	private final String EXECUTE = "execute";
	private final String CANCEL = "cancel";
	private final String SUBSCRIBE = "subscribe";
	private final String UN_SUBSCRIBE = "unsubscribe";
	final String EXECUTE_ON_EXECUTOR = "executeOnExecutor";
	private String complexObservableclassName = "complexObservableclassName";
	private String subscription = "subscription";
	private String asyncMethodName = "asyncMethodName";

	public AnonymAsyncTaskWorker(AsyncTaskCollector collector) {
		super(collector);
	}

	@Override
	public WorkerStatus refactor() {

		// All anonymous class declarations
		Multimap<ICompilationUnit, AnonymousClassDeclaration> anonymousClasses = collector.getAnonymousClasses();

		int numCunits = collector.getNumberOfCompilationUnits();
		monitor.beginTask(getClass().getSimpleName(), numCunits);
		Log.info(getClass(), "METHOD=refactor - Number of compilation units: " + numCunits);

		// Iterate through all relevant compilation units
		for (ICompilationUnit unit : anonymousClasses.keySet()) {

			Collection<AnonymousClassDeclaration> declarations = anonymousClasses.get(unit);

			// Iterate through all anonymous AsyncTask declarations
			for (AnonymousClassDeclaration asyncTaskDeclaration : declarations) {

				Log.info(getClass(), "METHOD=refactor - Extract Information from AsyncTask: " + unit.getElementName());

				AsyncTaskWrapper asyncTask = new AsyncTaskWrapper(asyncTaskDeclaration);

				if (asyncTask.getDoInBackgroundBlock() == null)
					continue;

				AST ast = asyncTaskDeclaration.getAST();

				UnitWriterExt writer = UnitWriters.getOrElse(unit,
						() -> new UnitWriterExt(unit, ast, getClass().getSimpleName()));

				Log.info(getClass(), "METHOD=refactor - Updating imports: " + unit.getElementName());

				Statement referenceStatement = ASTUtils.findParent(asyncTaskDeclaration, Statement.class);

				addRxObservable(asyncTask, writer, unit, referenceStatement, asyncTaskDeclaration);

				if (asyncTask.hasField()) {
					if (!checkForimplecitExecute(collector.getRelevantUsages(), ast, writer, asyncTaskDeclaration,
							unit))
						writer.removeStatement(asyncTaskDeclaration);
					updateUsage(collector.getRelevantUsages(), ast, writer, asyncTaskDeclaration, unit);
				}

				updateImports(asyncTask, writer);
				Log.info(getClass(), "METHOD=refactor - Refactoring class: " + unit.getElementName());
				NUMBER_OF_ANONYMOUS_TASKS++;

				execution.addUnitWriter(writer);
			}
			monitor.worked(1);
		}

		Log.info(getClass(), "Number of Anonymous AsynckTasks =  " + NUMBER_OF_ANONYMOUS_TASKS);
		return WorkerStatus.OK;
	}

	/**
	 * This method checks if the instance creation statement has execute method
	 * implicit Ex: AsyncTask<Void,Void,Void> task= new AsyncTask().execute();
	 *
	 * In such cases this method reference is added to relevance usage list
	 *
	 * @param cuRelevantUsagesMap
	 * @param ast
	 * @param singleChangeWriter
	 * @param asyncTaskDeclaration
	 * @param unit
	 */
	private boolean checkForimplecitExecute(Multimap<ICompilationUnit, MethodInvocation> cuRelevantUsagesMap, AST ast,
			UnitWriterExt singleChangeWriter, AnonymousClassDeclaration asyncTaskDeclaration, ICompilationUnit unit) {
		String variableName = "getObservable";
		MethodInvocation implicitExecute = null;
		ASTNode execute = AsyncTaskASTUtils.findOuterParent(asyncTaskDeclaration, MethodInvocation.class);
		if (execute != null)
			implicitExecute = (MethodInvocation) execute;
		if (implicitExecute != null)
			if (implicitExecute.getName().toString().equals(EXECUTE)
					|| implicitExecute.getName().toString().equals(EXECUTE_ON_EXECUTOR)) {
				VariableDeclarationFragment var = ASTUtils.findParent(implicitExecute,
						VariableDeclarationFragment.class);
				// if (var != null) {
				// variableName = var.getName().toString();
				// } else {
				// variableName = ((Assignment)
				// ASTUtil.findParent(implicitExecute, Assignment.class))
				// .getLeftHandSide().toString();
				// }
				MethodInvocation me = ast.newMethodInvocation();
				me.setName(ast.newSimpleName(implicitExecute.getName().toString()));
				me.setExpression(ast.newSimpleName(variableName));
				Statement s = ASTUtils.findParent(asyncTaskDeclaration, Statement.class);
				replaceExecuteImplicit(me, unit, ast, false, s);
				return true;
			}
		return false;
	}

	private void updateUsage(Multimap<ICompilationUnit, MethodInvocation> cuRelevantUsagesMap, AST ast,
			UnitWriterExt SingleUnitExtensionWriter, AnonymousClassDeclaration asyncTaskDeclaration,
			ICompilationUnit icuOuter) {
		for (ICompilationUnit unit : cuRelevantUsagesMap.keySet()) {
			Log.info(getClass(), "METHOD=updateUsage - updating usage for class: " + icuOuter.getElementName() + " in "
					+ unit.getElementName());
			for (MethodInvocation methodInvoke : cuRelevantUsagesMap.get(unit)) {
				if (methodInvoke.getName().toString().equals(EXECUTE)
						|| methodInvoke.getName().toString().equals(EXECUTE_ON_EXECUTOR)) {

					if (icuOuter.getElementName().equals(unit.getElementName()))
						replaceCancel(cuRelevantUsagesMap, methodInvoke, icuOuter, ast);
					else {
						// String newName = ((TypeDeclaration)
						// ASTUtil.findParent(methodInvoke,
						// TypeDeclaration.class))
						// .getName().toString();
						// tyDec = (TypeDeclaration)
						// ASTUtil.findParent(methodInvoke,
						// TypeDeclaration.class);
						// astInvoke = tyDec.getAST();
						// if
						// (!SingleUnitExtensionWriterMap.keySet().contains(newName))
						// {
						// singleChangeWriterNew = new SingleUnitExtensionWriter(unit,
						// astInvoke,
						// getClass().getSimpleName());
						// SingleUnitExtensionWriterMap.put(newName, new
						// ComplexMap(singleChangeWriterNew, unit));
						// }
						// replaceCancel(cuRelevantUsagesMap, methodInvoke, unit,
						// astInvoke);
					}

				}
			}

		}

	}

	/**
	 * Replace asyncTask.cancel() method with Subscription.unsubscribe()
	 *
	 * @param cuRelevantUsagesMap
	 * @param methodInvoke
	 * @param unit
	 * @param astInvoke
	 */
	void replaceCancel(Multimap<ICompilationUnit, MethodInvocation> cuRelevantUsagesMap, MethodInvocation methodInvoke,
			ICompilationUnit unit, AST astInvoke) {
		boolean isCancelPresent = false;
		for (MethodInvocation methodReference : cuRelevantUsagesMap.get(unit)) {
			try {
				if (methodInvoke.getExpression().toString().equals(methodReference.getExpression().toString())) {
					if (methodReference.getName().toString().equals(CANCEL)) {
						isCancelPresent = true;
						Log.info(getClass(), "METHOD=replaceCancel - updating cancel invocation for class: "
								+ unit.getElementName());
						replaceExecute(methodInvoke, unit, astInvoke, true);
						updateCancelInvocation(unit, methodReference, astInvoke);

					}
				}
			} catch (Exception e) {

			}
		}
		if (!isCancelPresent) {

			replaceExecute(methodInvoke, unit, astInvoke, false);
		}
	}

	/**
	 * Add cancel method if AsyncTask.cacel() method was invoked
	 */
	private void updateCancelInvocation(ICompilationUnit unit, MethodInvocation methodInvoke, AST astInvoke) {

		UnitWriterExt singleChangeWriter = UnitWriters.getOrElse(unit,
				() -> new UnitWriterExt(unit, astInvoke, getClass().getSimpleName()));
		Log.info(getClass(),
				"METHOD=updateCancelInvocation - update Cancel Invocation for class: " + unit.getElementName());
		MethodInvocation unSubscribe = astInvoke.newMethodInvocation();
		unSubscribe
				.setExpression(astInvoke.newSimpleName(getVariableName(methodInvoke.getExpression()) + subscription));
		unSubscribe.setName(astInvoke.newSimpleName(UN_SUBSCRIBE));
		singleChangeWriter.replace(methodInvoke, unSubscribe);

	}

	/**
	 * Method to refactor method invocation statements with name execute EX: new
	 * Task().execute();
	 */
	void replaceExecute(MethodInvocation methodInvoke, ICompilationUnit unit, AST astInvoke, boolean withSubscription) {

		UnitWriterExt singleChangeWriter = UnitWriters.getOrElse(unit,
				() -> new UnitWriterExt(unit, astInvoke, getClass().getSimpleName()));

		AST methodAST = methodInvoke.getAST();
		MethodInvocation execute = (MethodInvocation) ASTNode.copySubtree(methodAST, methodInvoke);
		if (execute.getName().toString().equals(EXECUTE_ON_EXECUTOR)) {
			execute.arguments().clear();
		}
		execute.setExpression(AsyncTaskASTUtils.getinstannceCreationStatement(methodAST, complexObservableclassName));
		execute.setName(methodAST.newSimpleName(asyncMethodName));

		MethodInvocation invocation = methodAST.newMethodInvocation();

		invocation.setExpression(execute);
		invocation.setName(methodAST.newSimpleName(SUBSCRIBE));
		// ASTUtil.replaceInStatement(methodInvoke, invocation);
		if (!withSubscription) {
			singleChangeWriter.replace(methodInvoke, invocation);
		} else {
			createSubscriptionDeclaration(unit, methodInvoke);
			Assignment initSubscription = methodAST.newAssignment();

			initSubscription.setLeftHandSide(
					methodAST.newSimpleName(getVariableName(methodInvoke.getExpression()) + subscription));
			initSubscription.setRightHandSide(invocation);
			// ASTUtil.replaceInStatement(methodInvoke, initSubscription);
			singleChangeWriter.replace(methodInvoke, initSubscription);
		}
	}

	/**
	 * Method to refactor method invocation statements with name execute EX: new
	 * Task().execute();
	 */
	void replaceExecuteImplicit(MethodInvocation methodInvoke, ICompilationUnit unit, AST ast, boolean withSubscription,
			Statement referenceStatement) {

		UnitWriterExt singleChangeWriter = UnitWriters.getOrElse(unit,
				() -> new UnitWriterExt(unit, ast, getClass().getSimpleName()));

		AST methodAST = methodInvoke.getAST();
		MethodInvocation execute = (MethodInvocation) ASTNode.copySubtree(methodAST, methodInvoke);
		if (execute.getName().toString().equals(EXECUTE_ON_EXECUTOR)) {
			execute.arguments().clear();
		}
		execute.setExpression(AsyncTaskASTUtils.getinstannceCreationStatement(methodAST, complexObservableclassName));
		execute.setName(methodAST.newSimpleName(asyncMethodName));

		MethodInvocation invocation = methodAST.newMethodInvocation();

		invocation.setExpression(execute);
		invocation.setName(methodAST.newSimpleName(SUBSCRIBE));
		// ASTUtil.replaceInStatement(methodInvoke, invocation);
		Statement ss = ASTNodeFactory.createSingleStatementFromText(methodAST, invocation.toString());
		if (!withSubscription) {
			singleChangeWriter.replace(referenceStatement, ss);
		} else {
			createSubscriptionDeclaration(unit, methodInvoke);
			Assignment initSubscription = methodAST.newAssignment();

			initSubscription.setLeftHandSide(
					methodAST.newSimpleName(getVariableName(methodInvoke.getExpression()) + subscription));
			initSubscription.setRightHandSide(invocation);
			// ASTUtil.replaceInStatement(methodInvoke, initSubscription);
			singleChangeWriter.replace(methodInvoke, initSubscription);
		}
	}

	private String getVariableName(Expression e) {
		if (e instanceof FieldAccess)
			return ((FieldAccess) e).getName().toString();
		else
			return e.toString();
	}

	void createSubscriptionDeclaration(ICompilationUnit unit, MethodInvocation methodInvoke) {
		TypeDeclaration tyDec = ASTUtils.findParent(methodInvoke, TypeDeclaration.class);
		AST astInvoke = tyDec.getAST();

		UnitWriterExt singleChangeWriter = UnitWriters.getOrElse(unit,
				() -> new UnitWriterExt(unit, astInvoke, getClass().getSimpleName()));

		VariableDeclarationFragment variable = astInvoke.newVariableDeclarationFragment();
		variable.setName(astInvoke.newSimpleName(methodInvoke.getExpression().toString() + subscription));
		FieldDeclaration subscription = astInvoke.newFieldDeclaration(variable);
		subscription.setType(astInvoke.newSimpleType(astInvoke.newSimpleName("Subscription")));
		singleChangeWriter.addStatementToClass(subscription, tyDec);
		singleChangeWriter.addImport("rx.Subscription");
	}

	private void updateImports(AsyncTaskWrapper asyncTask, UnitWriterExt writer) {
		writer.addImport("rx.Observable");
		writer.addImport("rx.schedulers.Schedulers");
		writer.addImport("java.util.concurrent.Callable");
		writer.removeImport("android.os.AsyncTask");

		if (asyncTask.getOnPostExecuteBlock() != null) {
			writer.addImport("rx.functions.Action1");
			writer.removeImport("java.util.concurrent.ExecutionException");
			writer.removeImport("java.util.concurrent.TimeoutException");

		}

		if (asyncTask.getDoInBackgroundBlock() != null) {
			writer.addImport("rx.functions.Action1");
		}

		if (asyncTask.getOnProgressUpdateBlock() != null) {
			writer.addImport("rx.Subscriber");
			writer.addImport("java.util.Arrays");
		}

		if (asyncTask.getOnCancelled() != null) {
			writer.addImport("rx.functions.Action0");
		}
	}

	/**
	 * Remove super method invocation from AsyncTask methods
	 *
	 * @param asyncTask
	 */
	private void removeSuperInvocations(AsyncTaskWrapper asyncTask) {

		for (SuperMethodInvocation methodInvocation : asyncTask.getSuperClassMethodInvocation()) {
			Statement statement = ASTUtils.findParent(methodInvocation, Statement.class);
			statement.delete();
		}
	}

	private void addRxObservable(AsyncTaskWrapper asyncTask, UnitWriterExt writer, ICompilationUnit unit,
			Statement referenceStatement, AnonymousClassDeclaration anonymousCachedClassDecleration) {
		boolean complexRxObservableClassNeeded = asyncTask.hasField();
		boolean onProgressUpdateBlock = asyncTask.getOnProgressUpdateBlock() != null;
		removeSuperInvocations(asyncTask);

		SubscriberHolder subscriberHolder = null;
		if (onProgressUpdateBlock) {
			subscriberHolder = new SubscriberHolder(unit.getElementName(),
					asyncTask.getProgressType().toString() + "[]", asyncTask.getOnProgressUpdateBlock(),
					asyncTask.getProgressParameters());
		}

		AST ast = referenceStatement.getAST();
		if (!complexRxObservableClassNeeded && asyncTask.getIsVoid()) {
			String subscribedObservable = createObservable(asyncTask, writer, subscriberHolder).addSubscribe().build();

			if (onProgressUpdateBlock) {
				String newMethodString = subscriberHolder.getGetMethodDeclaration();
				MethodDeclaration newMethod = ASTNodeFactory.createMethodFromText(ast, newMethodString);
				writer.addMethodAfter(newMethod, anonymousCachedClassDecleration);

				String subscriberDecl = subscriberHolder.getSubscriberDeclaration();
				Statement getSubscriberStatement = ASTNodeFactory.createSingleStatementFromText(ast, subscriberDecl);
				writer.addStatementBefore(getSubscriberStatement, referenceStatement);
			}

			Statement newStatement = ASTNodeFactory.createSingleStatementFromText(ast, subscribedObservable);

			writer.replaceStatement(referenceStatement, newStatement);

		} else {

			List<FieldDeclaration> fieldDeclarations = asyncTask.getFieldDeclarations();
			String subscriberDecl = EMPTY;
			String subscriberGetRxUpdateMethod = EMPTY;

			if (onProgressUpdateBlock) {
				subscriberDecl = subscriberHolder.getSubscriberDeclaration();
				subscriberGetRxUpdateMethod = subscriberHolder.getGetMethodDeclaration();
			}

			String observableStatement = createObservable(asyncTask, writer, subscriberHolder).buildReturnStatement();
			String observableType = asyncTask.getReturnedType().toString();

			ComplexObservableBuilder complexObservable = ComplexObservableBuilder
					.newComplexRxObservable(unit.getElementName()).withFields(fieldDeclarations)
					.withGetAsyncObservable(observableType, subscriberDecl, observableStatement)
					.withMethod(subscriberGetRxUpdateMethod).withMethods(asyncTask.getAdditionalMethodDeclarations());

			String complexRxObservableClass = complexObservable.build();
			// initialize class name which will be used for replacing execute
			// method usage
			complexObservableclassName = complexObservable.getComplexObservableName();

			// initialize asyncmethodname to be used at execute
			asyncMethodName = complexObservable.getAsyncmethodName();

			// initialize asyncmethodname to be used at execute
			subscription = complexObservable.getAsynSubscription();

			TypeDeclaration complexRxObservableDecl = ASTNodeFactory.createTypeDeclarationFromText(ast,
					complexRxObservableClass);
			writer.addInnerClassAfter(complexRxObservableDecl, referenceStatement);
		}

	}

	private ObservableBuilder createObservable(AsyncTaskWrapper asyncTask, UnitWriter writer,
			SubscriberHolder subscriberHolder) {

		Block doInBackgroundBlock = asyncTask.getDoInBackgroundBlock();
		Block doOnCompletedBlock = asyncTask.getOnPostExecuteBlock();

		String type = (asyncTask.getReturnedType() == null ? "Void" : asyncTask.getReturnedType().toString());
		String postExecuteParameters = asyncTask.getPostExecuteParameters();

		if (type == null) {
			Log.error(getClass(), "NULL type for DoInBackground");
		}
		replacePublishInvocations(asyncTask, subscriberHolder);
		ASTUtils.removeUnnecessaryCatchClauses(doOnCompletedBlock);

		AsyncTaskASTUtils.removeMethodInvocations(doOnCompletedBlock);
		AsyncTaskASTUtils.replaceFieldsWithFullyQualifiedNameIn(doOnCompletedBlock, writer);

		ObservableBuilder complexObservable = ObservableBuilder
				.newObservable(type, doInBackgroundBlock, SchedulerType.JAVA_MAIN_THREAD)
				.addDoOnNext((doOnCompletedBlock == null ? "{}" : doOnCompletedBlock.toString()),
						postExecuteParameters == null ? "arg" : postExecuteParameters, type, true);

		Block preExec = asyncTask.getOnPreExecuteBlock();
		if (preExec != null) {
			complexObservable.addDoOnPreExecute(preExec);
		}

		Block onCancelled = asyncTask.getOnCancelled();
		if (onCancelled != null) {
			complexObservable.addDoOnCancelled(onCancelled);
		}

		return complexObservable;
	}

	private void replacePublishInvocations(AsyncTaskWrapper asyncTask, SubscriberHolder subscriberHolder) {
		if (!asyncTask.getPublishInvocations().isEmpty()) {
			for (MethodInvocation publishInvocation : asyncTask.getPublishInvocations()) {
				List<?> argumentList = publishInvocation.arguments();
				AST ast = publishInvocation.getAST();
				replacePublishInvocationsAux(subscriberHolder, publishInvocation, argumentList, ast);
			}
		}
	}

	private <T extends ASTNode> void replacePublishInvocationsAux(SubscriberHolder subscriberHolder,
			T publishInvocation, List<?> argumentList, AST ast) {
		String newInvocation = subscriberHolder.getOnNextInvocation(argumentList, subscriberHolder.getType());
		Statement newStatement = ASTNodeFactory.createSingleStatementFromText(ast, newInvocation);
		ASTUtils.replaceInStatement(publishInvocation, newStatement);
	}

	public int getNUMBER_OF_ANONYMOUS_TASKS() {
		return NUMBER_OF_ANONYMOUS_TASKS;
	}

}
