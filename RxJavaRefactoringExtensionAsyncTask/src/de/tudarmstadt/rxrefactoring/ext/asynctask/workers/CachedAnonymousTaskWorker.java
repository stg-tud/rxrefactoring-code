package de.tudarmstadt.rxrefactoring.ext.asynctask.workers;

import java.util.Collection;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.logging.Log;
import de.tudarmstadt.rxrefactoring.core.parser.BundledCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.parser.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.utils.ASTUtils;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.workers.IWorker;
import de.tudarmstadt.rxrefactoring.ext.asynctask.builders.AnonymousClassBuilder;
import de.tudarmstadt.rxrefactoring.ext.asynctask.builders.InnerClassBuilder;
import de.tudarmstadt.rxrefactoring.ext.asynctask.builders.SubscriberBuilder;
import de.tudarmstadt.rxrefactoring.ext.asynctask.utils.AsyncTaskASTUtils;
import de.tudarmstadt.rxrefactoring.ext.asynctask.utils.AsyncTaskWrapper;

/**
 * Description: This worker is in charge of refactoring anonymous AsyncTasks
 * that are not assigned to a variable.<br>
 * Example: new AsyncTask(){...}.execute();<br>
 * Author: Grebiel Jose Ifill Brito, Ram<br>
 * Created: 11/12/2016
 */
// TODO: still in progress. execute(params) not yet considered!
public class CachedAnonymousTaskWorker implements IWorker<AsyncTaskCollector, Void>, WorkerEnvironment {

	private final String EXECUTE = "execute";
	private final String CANCEL = "cancel";
	private final String SUBSCRIBE = "subscribe";
	private final String UN_SUBSCRIBE = "unsubscribe";
	
	final String EXECUTE_ON_EXECUTOR = "executeOnExecutor";
	
	private String complexObservableclassName = "complexObservableclassName";
	private String subscription = "subscription";
	private String asyncMethodName = "asyncMethodName";
	// private boolean isComplex = false;

	@Override
	public Void refactor(ProjectUnits units, AsyncTaskCollector collector, WorkerSummary summary) throws Exception {
		
		
		Multimap<BundledCompilationUnit, AnonymousClassDeclaration> cuAnonymousClassesMap = collector.getAnonymousCachedClasses();
		
		for (BundledCompilationUnit unit : cuAnonymousClassesMap.keySet()) {

			Collection<AnonymousClassDeclaration> declarations = cuAnonymousClassesMap.get(unit);
			for (AnonymousClassDeclaration asyncCachedTask : declarations) {

				ClassInstanceCreation classInstance = (ClassInstanceCreation) asyncCachedTask.getParent();
				AnonymousClassDeclaration asyncTaskDeclaration = classInstance.getAnonymousClassDeclaration();

				AsyncTaskWrapper asyncTask = new AsyncTaskWrapper(classInstance, unit);

				if (asyncTask.getDoInBackgroundBlock() != null) {
					AST ast = asyncTaskDeclaration.getAST();
					

					Statement referenceStatement = (Statement) ASTUtils.findParent(asyncTaskDeclaration,
							Statement.class);
					addRxObservable(asyncTask, referenceStatement);
					if (asyncTask.hasAdditionalAccess()) {
						if (!checkForimplicitExecute(unit, collector.getRelevantUsages(), ast,
								asyncTaskDeclaration))
							unit.remove(asyncTaskDeclaration);
					}
					updateUsage(collector.getRelevantUsages(), ast, asyncTaskDeclaration, unit);

					updateImports(asyncTask);
					
				}
			}
		}
		return null;
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
	private boolean checkForimplicitExecute(BundledCompilationUnit unit,
			Multimap<BundledCompilationUnit, MethodInvocation> cuRelevantUsagesMap, AST ast,
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
				replaceExecuteImplicit(unit, me, false, s);
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

	private void updateUsage(Multimap<BundledCompilationUnit, MethodInvocation> cuRelevantUsagesMap, AST ast,
			AnonymousClassDeclaration asyncTaskDeclaration,
			BundledCompilationUnit icuOuter) {
		for (BundledCompilationUnit unit : cuRelevantUsagesMap.keySet()) {
			// TypeDeclaration tyDec = ast.newTypeDeclaration();
			// UnitWriterExt newWriter = null;
			// AST astInvoke = ast;
			Log.info(getClass(), "METHOD=updateUsage - updating usage for class: " + icuOuter.getElementName() + " in "
					+ unit.getElementName());
			for (MethodInvocation methodInvoke : cuRelevantUsagesMap.get(unit)) {
				if (methodInvoke.getName().toString().equals(EXECUTE)
						|| methodInvoke.getName().toString().equals(EXECUTE_ON_EXECUTOR)) {

					if (icuOuter.getElementName().equals(unit.getElementName()))
						replaceCancel(cuRelevantUsagesMap, methodInvoke, icuOuter, ast);
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
	void replaceCancel(Multimap<BundledCompilationUnit, MethodInvocation> cuRelevantUsagesMap, MethodInvocation methodInvoke,
			BundledCompilationUnit unit, AST astInvoke) {
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
	private void updateCancelInvocation(BundledCompilationUnit unit, MethodInvocation methodInvoke, AST astInvoke) {

		MethodInvocation unSubscribe = astInvoke.newMethodInvocation();
		unSubscribe
				.setExpression(astInvoke.newSimpleName(getVariableName(methodInvoke.getExpression()) + subscription));
		unSubscribe.setName(astInvoke.newSimpleName(UN_SUBSCRIBE));
		unit.replace(methodInvoke, unSubscribe);

	}

	/**
	 * Method to refactor method invocation statements with name execute EX: new
	 * Task().execute();
	 */
	void replaceExecute(MethodInvocation methodInvoke, BundledCompilationUnit unit, AST astInvoke, boolean withSubscription) {

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
		
		Log.info(getClass(), "UNITNAME: " + unit.getElementName());
		
		if (!withSubscription) {
			unit.replace(methodInvoke, invocation);
		} else {
			createSubscriptionDeclaration(unit, methodInvoke);
			Assignment initSubscription = methodAST.newAssignment();

			initSubscription.setLeftHandSide(
					methodAST.newSimpleName(getVariableName(methodInvoke.getExpression()) + subscription));
			initSubscription.setRightHandSide(invocation);
			// ASTUtil.replaceInStatement(methodInvoke, initSubscription);
			unit.replace(methodInvoke, initSubscription);
		}
	}

	/**
	 * Method to refactor method invocation statements with name execute EX: new
	 * Task().execute();
	 */
	void replaceExecuteImplicit(BundledCompilationUnit unit, MethodInvocation methodInvoke,
			boolean withSubscription, Statement referenceStatement) {

		AST ast = unit.getAST();
		MethodInvocation execute = (MethodInvocation) ASTNode.copySubtree(ast, methodInvoke);
		
		if (execute.getName().toString().equals(EXECUTE_ON_EXECUTOR)) {
			execute.arguments().clear();
		}
		execute.setExpression(AsyncTaskASTUtils.getinstannceCreationStatement(ast, complexObservableclassName));
		execute.setName(ast.newSimpleName(asyncMethodName));

		MethodInvocation invocation = ast.newMethodInvocation();

		invocation.setExpression(execute);
		invocation.setName(ast.newSimpleName(SUBSCRIBE));
		// ASTUtil.replaceInStatement(methodInvoke, invocation);
		if (!withSubscription) {
			unit.replace(referenceStatement, invocation);
		} else {
			createSubscriptionDeclaration(unit, methodInvoke);
			Assignment initSubscription = ast.newAssignment();

			initSubscription.setLeftHandSide(
					ast.newSimpleName(getVariableName(methodInvoke.getExpression()) + subscription));
			initSubscription.setRightHandSide(invocation);
			unit.replace(methodInvoke, initSubscription);
		}
	}

	private String getVariableName(Expression e) {
		if (e instanceof FieldAccess)
			return ((FieldAccess) e).getName().toString();
		else
			return e.toString();
	}

	void createSubscriptionDeclaration(BundledCompilationUnit unit, MethodInvocation methodInvoke) {
		TypeDeclaration tyDec = ASTUtils.findParent(methodInvoke, TypeDeclaration.class);
		AST ast = unit.getAST();


		VariableDeclarationFragment variable = ast.newVariableDeclarationFragment();
		variable.setName(ast.newSimpleName(methodInvoke.getExpression().toString() + subscription));
		FieldDeclaration subscription = ast.newFieldDeclaration(variable);
		subscription.setType(ast.newSimpleType(ast.newSimpleName("Subscription")));
		addStatementToClass(unit, subscription, tyDec);
		unit.addImport("rx.Subscription");
	}

	private void updateImports(AsyncTaskWrapper asyncTask) {
		BundledCompilationUnit unit = asyncTask.getUnit();
		
		unit.addImport("rx.android.schedulers.AndroidSchedulers");
		unit.addImport("rx.schedulers.Schedulers");
		
		unit.addImport("rx.Observable");		
		unit.addImport("java.util.concurrent.Callable");

		if (asyncTask.getOnPreExecuteBlock() != null) {
			unit.addImport("rx.functions.Action0");
		}
		
		if (asyncTask.getOnPostExecuteBlock() != null) {
			unit.addImport("rx.functions.Action1");
			unit.removeImport("java.util.concurrent.ExecutionException");
			unit.removeImport("java.util.concurrent.TimeoutException");
		}
		if (asyncTask.getDoInBackgroundBlock() != null) {
			unit.addImport("rx.functions.Action1");
		}
		if (asyncTask.getOnProgressUpdateBlock() != null) {
			unit.addImport("rx.Subscriber");
			unit.addImport("java.util.Arrays");
		}
		if (asyncTask.getOnCancelled() != null) {
			unit.addImport("rx.functions.Action0");
		}
	}


	private void addRxObservable(AsyncTaskWrapper asyncTask, Statement referenceStatement) {
		
		BundledCompilationUnit unit = asyncTask.getUnit();
		
		AST ast = unit.getAST();
		
		boolean needInnerClassDecl = asyncTask.hasAdditionalAccess();
		boolean onProgressUpdateBlock = asyncTask.getOnProgressUpdateBlock() != null;
		
		//removeSuperInvocations(asyncTask);

//		SubscriberHolder subscriberHolder = null;
//		if (onProgressUpdateBlock) {
//			subscriberHolder = new SubscriberHolder(unit.getElementName(),
//					asyncTask.getProgressParameter().getType().toString() + "[]", asyncTask.getOnProgressUpdateBlock(),
//					asyncTask.getProgressParameter().toString());
//		}

		if (!needInnerClassDecl && asyncTask.getIsVoid()) {
			
			//Creates Observable.fromCallable ...
			AnonymousClassBuilder anonymousClassBuilder = new AnonymousClassBuilder(asyncTask);
			anonymousClassBuilder.addRelevantMethods();
			anonymousClassBuilder.addSubscribe();
			
			Expression observable = anonymousClassBuilder.getExpression();
			
//			String subscribedObservable = createObservable(asyncTask, subscriberHolder, rewriter).addSubscribe()
//					.build();
			
			//Adds a subscriber if needed
			if (onProgressUpdateBlock) {
				SubscriberBuilder subscriberBuilder = new SubscriberBuilder(asyncTask);
				
				
//				String newMethodString = subscriberHolder.getGetMethodDeclaration();
//				MethodDeclaration newMethod = ASTNodeFactory.createMethodFromText(ast, newMethodString);
				addMethodAfter(unit, subscriberBuilder.buildGetSubscriber(), asyncTask.getDeclaration());

//				String subscriberDecl = subscriberHolder.getSubscriberDeclaration();
//				Statement getSubscriberStatement = ASTNodeFactory.createSingleStatementFromText(ast, subscriberDecl);
				addStatementBefore(unit, subscriberBuilder.buildSubscriberDeclaration(), referenceStatement);
				
				replacePublishInvocations(asyncTask, subscriberBuilder);
			}

			
			VariableDeclarationFragment m = ast.newVariableDeclarationFragment();
			
			if (referenceStatement instanceof VariableDeclarationStatement)
				m.setName(ast.newSimpleName(
						((VariableDeclarationFragment) ((VariableDeclarationStatement) referenceStatement).fragments()
								.get(0)).getName().toString() + subscription));
			
			if (referenceStatement instanceof ExpressionStatement)
				m.setName(ast.newSimpleName((((Assignment) ((ExpressionStatement) referenceStatement).getExpression())
						.getLeftHandSide().toString() + subscription)));
			
			m.setInitializer(observable);
			
			VariableDeclarationStatement v = ast.newVariableDeclarationStatement(m);
			v.setType(ast.newSimpleType(ast.newSimpleName("Subscription")));

			addStatementBefore(unit, v, referenceStatement);

		} else {
			
//			List<FieldDeclaration> fieldDeclarations = asyncTask.getFieldDeclarations();
//			String subscriberDecl = EMPTY;
//			String subscriberGetRxUpdateMethod = EMPTY;
//			if (onProgressUpdateBlock) {
//				subscriberDecl = subscriberHolder.getSubscriberDeclaration();
//				subscriberGetRxUpdateMethod = subscriberHolder.getGetMethodDeclaration();
//			}

			SubscriberBuilder subscriberBuilder = new SubscriberBuilder(asyncTask);
			InnerClassBuilder innerClassBuilder = new InnerClassBuilder(asyncTask, subscriberBuilder.getId());
			TypeDeclaration observableWrapper = innerClassBuilder.buildInnerClass();
			
//			String observableStatement = createObservable(asyncTask, subscriberHolder, rewriter).buildReturnStatement();
			
//			String observableType = asyncTask.getResultType().toString();
//			ComplexObservableBuilder complexObservable = ComplexObservableBuilder
//					.newComplexRxObservable(unit.getElementName())
//					.withFields(fieldDeclarations)
//					.withGetAsyncObservable(observableType, subscriberDecl, observableStatement)
//					.withMethod(subscriberGetRxUpdateMethod)
//					.withMethods(asyncTask.getAdditionalMethodDeclarations());

			replacePublishInvocations(asyncTask, subscriberBuilder);

//			String complexRxObservableClass = complexObservable.build();
			// initialize class name which will be used for replacing execute
			// method usage
			complexObservableclassName = observableWrapper.getName().getIdentifier();

			// initialize asyncmethodname to be used at execute
			asyncMethodName = innerClassBuilder.getMethodName();

			// initialize asyncmethodname to be used at execute
			subscription = "subscription" + innerClassBuilder.getId();

		
			addInnerClassAfter(unit, observableWrapper, referenceStatement);
		}

	}

//	private ObservableBuilder createObservable(AsyncTaskWrapper asyncTask, SubscriberHolder subscriberHolder,
//			UnitWriter writer) {
//		Block doInBackgroundBlock = asyncTask.getDoInBackgroundBlock();
//		Block doOnCompletedBlock = asyncTask.getOnPostExecuteBlock();
//		Block onCancelled = asyncTask.getOnCancelled();
//
//		String type = (asyncTask.getResultType() == null ? "Void" : asyncTask.getResultType().toString());
//		String postExecuteParameters = asyncTask.getPostExecuteParameter().toString();
//
//		if (type == null) {
//			System.out.println("NULL type for Do In Background");
//		}
//
//		replacePublishInvocations(asyncTask, subscriberHolder);
//
//		ASTUtils.removeUnnecessaryCatchClauses(doOnCompletedBlock);
//
//		AsyncTaskASTUtils.replaceFieldsWithFullyQualifiedNameIn(doOnCompletedBlock, writer);
//
//		ObservableBuilder complexObservable = ObservableBuilder
//				.newObservable(asyncTask, writer, type, doInBackgroundBlock, SchedulerType.JAVA_MAIN_THREAD)
//				.addDoOnNext((doOnCompletedBlock == null ? "{}" : doOnCompletedBlock.toString()),
//						postExecuteParameters == null ? "arg" : postExecuteParameters, type, true);
//		Block onPreExecute = asyncTask.getOnPreExecuteBlock();
//		if (onPreExecute != null) {
//			complexObservable.addDoOnPreExecute(onPreExecute);
//		}
//
//		if (onCancelled != null) {
//			complexObservable.addDoOnCancelled(onCancelled);
//		}
//
//		return complexObservable;
//	}
	
}
