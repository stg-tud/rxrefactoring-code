package de.tudarmstadt.rxrefactoring.ext.asynctask.workers;

import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

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
 * Description: This worker is in charge of refactoring anonymous AsyncTasks
 * that are not assigned to a variable.<br>
 * Example: new AsyncTask(){...}.execute();<br>
 * Author: Grebiel Jose Ifill Brito, Ram<br>
 * Created: 11/12/2016
 */
// TODO: still in progress. execute(params) not yet considered!
public class CachedAnonymousTaskWorker extends AbstractWorker<AsyncTaskCollector> {
	private static final String EMPTY = "";
	private int NUMBER_OF_ASYNC_TASKS = 0;

	private final String EXECUTE = "execute";
	private final String CANCEL = "cancel";
	private final String SUBSCRIBE = "subscribe";
	private final String UN_SUBSCRIBE = "unsubscribe";
	final String EXECUTE_ON_EXECUTOR = "executeOnExecutor";
	private String complexObservableclassName = "complexObservableclassName";
	private String subscription = "subscription";
	private String asyncMethodName = "asyncMethodName";
	// private boolean isComplex = false;

	public CachedAnonymousTaskWorker(AsyncTaskCollector collector) {
		super(collector);
	}

	@Override
	public WorkerStatus refactor() {
		Multimap<ICompilationUnit, AnonymousClassDeclaration> cuAnonymousClassesMap = collector.getAnonymousCachedClasses();
		int numCunits = collector.getNumberOfCompilationUnits();
		monitor.beginTask(getClass().getSimpleName(), numCunits);
		Log.info(getClass(), "METHOD=refactor - Number of compilation units: " + numCunits);
		for (ICompilationUnit unit : cuAnonymousClassesMap.keySet()) {

			Collection<AnonymousClassDeclaration> declarations = cuAnonymousClassesMap.get(unit);
			for (AnonymousClassDeclaration asyncCachedTask : declarations) {

				ClassInstanceCreation classInstance = (ClassInstanceCreation) asyncCachedTask.getParent();
				AnonymousClassDeclaration asyncTaskDeclaration = classInstance.getAnonymousClassDeclaration();
				Log.info(getClass(), "METHOD=refactor - Extract Information from AsyncTask: " + unit.getElementName());

				AsyncTaskWrapper asyncTask = new AsyncTaskWrapper(classInstance, unit);

				if (asyncTask.getDoInBackgroundBlock() != null) {
					AST ast = asyncTaskDeclaration.getAST();
					UnitWriterExt writer = UnitWriters.getOrPut(unit,
							() -> new UnitWriterExt(unit, ast));

					Log.info(getClass(), "METHOD=refactor - Updating imports: " + unit.getElementName());
					Statement referenceStatement = (Statement) ASTUtils.findParent(asyncTaskDeclaration,
							Statement.class);
					addRxObservable(unit, writer, referenceStatement, asyncTask, asyncTaskDeclaration);
					if (asyncTask.hasAdditionalAccess()) {
						if (!checkForimplicitExecute(unit, writer, collector.getRelevantUsages(), ast,
								asyncTaskDeclaration))
							writer.removeStatement(asyncTaskDeclaration);
					}
					updateUsage(writer, collector.getRelevantUsages(), ast, asyncTaskDeclaration, unit);

					updateImports(writer, asyncTask);
					Log.info(getClass(), "METHOD=refactor - Refactoring class: " + unit.getElementName());
					NUMBER_OF_ASYNC_TASKS++;

					execution.addUnitWriter(writer);
				}
			}
			monitor.worked(1);
		}
		Log.info(getClass(), "Number of AsynckTasks Subclass=  " + NUMBER_OF_ASYNC_TASKS);
		return WorkerStatus.OK;
	}

	/**
	 * This method checks if the instance creation statement has execute method
	 * implicit Ex: AsyncTask<Void,Void,Void> task= new AsyncTask().execute();
	 * 
	 * In such cases this method reference is added to relevance usage list
	 * 
	 * @param unit
	 * @param writer
	 * @param cuRelevantUsagesMap
	 * @param ast
	 * @param asyncTaskDeclaration
	 */
	private boolean checkForimplicitExecute(ICompilationUnit unit, UnitWriterExt writer,
			Multimap<ICompilationUnit, MethodInvocation> cuRelevantUsagesMap, AST ast,
			AnonymousClassDeclaration asyncTaskDeclaration) {
		String variableName;
		MethodInvocation implicitExecute = null;
		ASTNode execute = AsyncTaskASTUtils.findOuterParent(asyncTaskDeclaration, MethodInvocation.class);
		if (execute != null)
			implicitExecute = (MethodInvocation) execute;
		if (implicitExecute != null)
			if (implicitExecute.getName().toString().equals(EXECUTE)
					|| implicitExecute.getName().toString().equals(EXECUTE_ON_EXECUTOR)) {
				VariableDeclarationFragment var = (VariableDeclarationFragment) ASTUtils.findParent(implicitExecute,
						VariableDeclarationFragment.class);
				if (var != null) {
					variableName = var.getName().toString();
				} else {
					variableName = ((Assignment) ASTUtils.findParent(implicitExecute, Assignment.class))
							.getLeftHandSide().toString();
				}
				MethodInvocation me = ast.newMethodInvocation();
				me.setName(ast.newSimpleName(implicitExecute.getName().toString()));
				me.setExpression(ast.newSimpleName(variableName));
				Statement s = (Statement) ASTUtils.findParent(asyncTaskDeclaration, Statement.class);
				replaceExecuteImplicit(writer, me, unit, ast, false, s);
				return true;
			}
		return false;
	}

	/**
	 * onPreExecute method will be invoked by getAsyncObservable block as a first
	 * statement.
	 * 
	 * @override has to be removed and super() should be removed from
	 *           onPreExecuteBlock
	 * @param asyncTask
	 * @param writer
	 * @param asyncTaskDeclaration
	 * @param ast
	 */
	// private void updateonPreExecute(AsyncTaskWrapper asyncTask,
	// UnitWriterExt writer, AnonymousClassDeclaration asyncTaskDeclaration, AST
	// ast)
	// {
	//
	// MethodDeclaration getAsyncMethod = ASTNodeFactory.createMethodFromText( ast,
	// "public onPreExecute(){" + asyncTask.getOnPreExecuteBlock().toString() + "}"
	// );
	// writer.replaceStatement( (MethodDeclaration)
	// asyncTask.getOnPreExecuteBlock().getParent(), getAsyncMethod );
	//
	// }

	private void updateUsage(UnitWriterExt writer, Multimap<ICompilationUnit, MethodInvocation> cuRelevantUsagesMap, AST ast,
			AnonymousClassDeclaration asyncTaskDeclaration,
			ICompilationUnit icuOuter) {
		for (ICompilationUnit unit : cuRelevantUsagesMap.keySet()) {
			// TypeDeclaration tyDec = ast.newTypeDeclaration();
			// UnitWriterExt newWriter = null;
			// AST astInvoke = ast;
			Log.info(getClass(), "METHOD=updateUsage - updating usage for class: " + icuOuter.getElementName() + " in "
					+ unit.getElementName());
			for (MethodInvocation methodInvoke : cuRelevantUsagesMap.get(unit)) {
				if (methodInvoke.getName().toString().equals(EXECUTE)
						|| methodInvoke.getName().toString().equals(EXECUTE_ON_EXECUTOR)) {

					if (icuOuter.getElementName().equals(unit.getElementName()))
						replaceCancel(writer, cuRelevantUsagesMap, methodInvoke, icuOuter, ast);
					else {
						// String newName = ((TypeDeclaration) ASTUtil.findParent(methodInvoke,
						// TypeDeclaration.class))
						// .getName().toString();
						// tyDec = (TypeDeclaration) ASTUtil.findParent(methodInvoke,
						// TypeDeclaration.class);
						// astInvoke = tyDec.getAST();
						// if (!SingleUnitExtensionWriterMap.keySet().contains(newName)) {
						// singleChangeWriterNew = new SingleUnitExtensionWriter(unit, astInvoke,
						// getClass().getSimpleName());
						// SingleUnitExtensionWriterMap.put(newName, new
						// ComplexMap(singleChangeWriterNew, unit));
						// }
						// replaceCancel(cuRelevantUsagesMap, methodInvoke, unit, astInvoke);
					}

				}
			}

		}

	}

	/**
	 * Replace asyncTask.cancel() method with Subscription.unsubscribe()
	 *
	 */
	void replaceCancel(UnitWriterExt writer, Multimap<ICompilationUnit, MethodInvocation> cuRelevantUsagesMap, MethodInvocation methodInvoke,
			ICompilationUnit unit, AST astInvoke) {
		boolean isCancelPresent = false;
		for (MethodInvocation methodReference : cuRelevantUsagesMap.get(unit)) {
			try {
				if (methodInvoke.getExpression().toString().equals(methodReference.getExpression().toString())) {
					if (methodReference.getName().toString().equals(CANCEL)) {
						isCancelPresent = true;
						Log.info(getClass(), "METHOD=replaceCancel - updating cancel invocation for class: "
								+ unit.getElementName());
						replaceExecute(writer, methodInvoke, unit, astInvoke, true);
						updateCancelInvocation(writer, unit, methodReference, astInvoke);

					}
				}
			} catch (Exception e) {

			}
		}
		if (!isCancelPresent) {

			replaceExecute(writer, methodInvoke, unit, astInvoke, false);
		}
	}

	/**
	 * Add cancel method if AsyncTask.cacel() method was invoked
	 */
	private void updateCancelInvocation(UnitWriterExt writer, ICompilationUnit unit, MethodInvocation methodInvoke, AST astInvoke) {

//		 = UnitWriters.getOrElse(unit,
//				() -> new UnitWriterExt(unit, astInvoke, getClass().getSimpleName()));
		Log.info(getClass(),
				"METHOD=updateCancelInvocation - update Cancel Invocation for class: " + unit.getElementName());
		MethodInvocation unSubscribe = astInvoke.newMethodInvocation();
		unSubscribe
				.setExpression(astInvoke.newSimpleName(getVariableName(methodInvoke.getExpression()) + subscription));
		unSubscribe.setName(astInvoke.newSimpleName(UN_SUBSCRIBE));
		writer.replace(methodInvoke, unSubscribe);

	}

	/**
	 * Method to refactor method invocation statements with name execute EX: new
	 * Task().execute();
	 */
	void replaceExecute(UnitWriterExt writer, MethodInvocation methodInvoke, ICompilationUnit unit, AST astInvoke, boolean withSubscription) {

//		UnitWriterExt singleChangeWriter = UnitWriters.getOrElse(unit,
//				() -> new UnitWriterExt(unit, astInvoke, getClass().getSimpleName()));

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
			writer.replace(methodInvoke, invocation);
		} else {
			createSubscriptionDeclaration(writer, unit, methodInvoke);
			Assignment initSubscription = methodAST.newAssignment();

			initSubscription.setLeftHandSide(
					methodAST.newSimpleName(getVariableName(methodInvoke.getExpression()) + subscription));
			initSubscription.setRightHandSide(invocation);
			// ASTUtil.replaceInStatement(methodInvoke, initSubscription);
			writer.replace(methodInvoke, initSubscription);
		}
	}

	/**
	 * Method to refactor method invocation statements with name execute EX: new
	 * Task().execute();
	 */
	void replaceExecuteImplicit(UnitWriterExt writer, MethodInvocation methodInvoke, ICompilationUnit unit, AST astInvoke,
			boolean withSubscription, Statement referenceStatement) {
//		UnitWriterExt singleChangeWriter = UnitWriters.getOrElse(unit,
//				() -> new UnitWriterExt(unit, astInvoke, getClass().getSimpleName()));

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
			writer.replace(referenceStatement, invocation);
		} else {
			createSubscriptionDeclaration(writer, unit, methodInvoke);
			Assignment initSubscription = methodAST.newAssignment();

			initSubscription.setLeftHandSide(
					methodAST.newSimpleName(getVariableName(methodInvoke.getExpression()) + subscription));
			initSubscription.setRightHandSide(invocation);
			writer.replace(methodInvoke, initSubscription);
		}
	}

	private String getVariableName(Expression e) {
		if (e instanceof FieldAccess)
			return ((FieldAccess) e).getName().toString();
		else
			return e.toString();
	}

	void createSubscriptionDeclaration(UnitWriterExt writer, ICompilationUnit unit, MethodInvocation methodInvoke) {
		TypeDeclaration tyDec = ASTUtils.findParent(methodInvoke, TypeDeclaration.class);
		AST astInvoke = tyDec.getAST();

//		UnitWriterExt writer = UnitWriters.getOrElse(unit,
//				() -> new UnitWriterExt(unit, astInvoke, getClass().getSimpleName()));

		VariableDeclarationFragment variable = astInvoke.newVariableDeclarationFragment();
		variable.setName(astInvoke.newSimpleName(methodInvoke.getExpression().toString() + subscription));
		FieldDeclaration subscription = astInvoke.newFieldDeclaration(variable);
		subscription.setType(astInvoke.newSimpleType(astInvoke.newSimpleName("Subscription")));
		writer.addStatementToClass(subscription, tyDec);
		writer.addImport("rx.Subscription");
	}

	private void updateImports(UnitWriterExt rewriter, AsyncTaskWrapper asyncTask) {
		rewriter.addImport("rx.Observable");
		rewriter.addImport("rx.schedulers.Schedulers");
		rewriter.addImport("java.util.concurrent.Callable");
		// rewriter.removeImport("android.os.AsyncTask");
		if (asyncTask.getOnPostExecuteBlock() != null) {
			rewriter.addImport("rx.functions.Action1");
			rewriter.removeImport("java.util.concurrent.ExecutionException");
			rewriter.removeImport("java.util.concurrent.TimeoutException");
		}
		if (asyncTask.getDoInBackgroundBlock() != null) {
			rewriter.addImport("rx.functions.Action1");
		}
		if (asyncTask.getOnProgressUpdateBlock() != null) {
			rewriter.addImport("rx.Subscriber");
			rewriter.addImport("java.util.Arrays");
		}
		if (asyncTask.getOnCancelled() != null) {
			rewriter.addImport("rx.functions.Action0");
		}
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

	private void addRxObservable(ICompilationUnit unit, UnitWriterExt rewriter, Statement referenceStatement,
			AsyncTaskWrapper asyncTask, AnonymousClassDeclaration anonymousCahcedClassDecleration) {
		boolean complexRxObservableClassNeeded = asyncTask.hasAdditionalAccess();
		boolean onProgressUpdateBlock = asyncTask.getOnProgressUpdateBlock() != null;
		removeSuperInvocations(asyncTask);

		SubscriberHolder subscriberHolder = null;
		if (onProgressUpdateBlock) {
			subscriberHolder = new SubscriberHolder(unit.getElementName(),
					asyncTask.getProgressParameter().getType().toString() + "[]", asyncTask.getOnProgressUpdateBlock(),
					asyncTask.getProgressParameter().toString());
		}

		AST ast = referenceStatement.getAST();
		if (!complexRxObservableClassNeeded && asyncTask.getIsVoid()) {
			String subscribedObservable = createObservable(asyncTask, subscriberHolder, rewriter).addSubscribe()
					.build();
			if (onProgressUpdateBlock) {
				String newMethodString = subscriberHolder.getGetMethodDeclaration();
				MethodDeclaration newMethod = ASTNodeFactory.createMethodFromText(ast, newMethodString);
				rewriter.addMethodAfter(newMethod, anonymousCahcedClassDecleration);

				String subscriberDecl = subscriberHolder.getSubscriberDeclaration();
				Statement getSubscriberStatement = ASTNodeFactory.createSingleStatementFromText(ast, subscriberDecl);
				rewriter.addStatementBefore(getSubscriberStatement, referenceStatement);
			}

			Statement newStatement = ASTNodeFactory.createSingleStatementFromText(ast, subscribedObservable);
			VariableDeclarationFragment m = ast.newVariableDeclarationFragment();
			if (referenceStatement instanceof VariableDeclarationStatement)
				m.setName(ast.newSimpleName(
						((VariableDeclarationFragment) ((VariableDeclarationStatement) referenceStatement).fragments()
								.get(0)).getName().toString() + subscription));
			if (referenceStatement instanceof ExpressionStatement)
				m.setName(ast.newSimpleName((((Assignment) ((ExpressionStatement) referenceStatement).getExpression())
						.getLeftHandSide().toString() + subscription)));
			m.setInitializer(
					(MethodInvocation) ASTNode.copySubtree(ast, ((ExpressionStatement) newStatement).getExpression()));
			VariableDeclarationStatement v = ast.newVariableDeclarationStatement(m);
			v.setType(ast.newSimpleType(ast.newSimpleName("Subscription")));

			rewriter.addStatementBefore(v, referenceStatement);

		} else {
			List<FieldDeclaration> fieldDeclarations = asyncTask.getFieldDeclarations();
			String subscriberDecl = EMPTY;
			String subscriberGetRxUpdateMethod = EMPTY;
			if (onProgressUpdateBlock) {
				subscriberDecl = subscriberHolder.getSubscriberDeclaration();
				subscriberGetRxUpdateMethod = subscriberHolder.getGetMethodDeclaration();
			}

			String observableStatement = createObservable(asyncTask, subscriberHolder, rewriter).buildReturnStatement();
			String observableType = asyncTask.getResultType().toString();
			ComplexObservableBuilder complexObservable = ComplexObservableBuilder
					.newComplexRxObservable(unit.getElementName())
					.withFields(fieldDeclarations)
					.withGetAsyncObservable(observableType, subscriberDecl, observableStatement)
					.withMethod(subscriberGetRxUpdateMethod)
					.withMethods(asyncTask.getAdditionalMethodDeclarations());

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
			rewriter.addInnerClassAfter(complexRxObservableDecl, referenceStatement);
		}

	}

	private ObservableBuilder createObservable(AsyncTaskWrapper asyncTask, SubscriberHolder subscriberHolder,
			UnitWriter writer) {
		Block doInBackgroundBlock = asyncTask.getDoInBackgroundBlock();
		Block doOnCompletedBlock = asyncTask.getOnPostExecuteBlock();
		Block onCancelled = asyncTask.getOnCancelled();

		String type = (asyncTask.getResultType() == null ? "Void" : asyncTask.getResultType().toString());
		String postExecuteParameters = asyncTask.getPostExecuteParameter().toString();

		if (type == null) {
			System.out.println("NULL type for Do In Background");
		}

		replacePublishInvocations(asyncTask, subscriberHolder);

		ASTUtils.removeUnnecessaryCatchClauses(doOnCompletedBlock);

		AsyncTaskASTUtils.replaceFieldsWithFullyQualifiedNameIn(doOnCompletedBlock, writer);

		ObservableBuilder complexObservable = ObservableBuilder
				.newObservable(asyncTask, writer, type, doInBackgroundBlock, SchedulerType.JAVA_MAIN_THREAD)
				.addDoOnNext((doOnCompletedBlock == null ? "{}" : doOnCompletedBlock.toString()),
						postExecuteParameters == null ? "arg" : postExecuteParameters, type, true);
		Block onPreExecute = asyncTask.getOnPreExecuteBlock();
		if (onPreExecute != null) {
			complexObservable.addDoOnPreExecute(onPreExecute);
		}

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

	public int getNUMBER_OF_ASYNC_TASKS() {
		return NUMBER_OF_ASYNC_TASKS;
	}
}
