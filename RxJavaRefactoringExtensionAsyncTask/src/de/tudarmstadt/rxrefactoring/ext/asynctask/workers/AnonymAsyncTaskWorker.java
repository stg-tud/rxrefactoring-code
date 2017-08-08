package de.tudarmstadt.rxrefactoring.ext.asynctask.workers;

import java.util.Collection;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.logging.Log;
import de.tudarmstadt.rxrefactoring.core.parser.BundledCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.parser.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.utils.ASTUtils;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.workers.IWorker;
import de.tudarmstadt.rxrefactoring.ext.asynctask.builders2.AnonymousClassBuilder;
import de.tudarmstadt.rxrefactoring.ext.asynctask.builders2.InnerClassBuilder;
import de.tudarmstadt.rxrefactoring.ext.asynctask.builders2.SubscriberBuilder;
import de.tudarmstadt.rxrefactoring.ext.asynctask.collect.AsyncTaskCollector;
import de.tudarmstadt.rxrefactoring.ext.asynctask.utils.AsyncTaskASTUtils;
import de.tudarmstadt.rxrefactoring.ext.asynctask.utils.AsyncTaskWrapper;

/**
 * Author: Template<br>
 * Created: 01/18/2017
 */
public class AnonymAsyncTaskWorker implements IWorker<AsyncTaskCollector, Void>, WorkerEnvironment {

	private int numOfAnonymousClasses = 0;

	private final String EXECUTE = "execute";
	private final String CANCEL = "cancel";
	private final String SUBSCRIBE = "subscribe";
	private final String UN_SUBSCRIBE = "unsubscribe";
	final String EXECUTE_ON_EXECUTOR = "executeOnExecutor";
	private String complexObservableclassName = "complexObservableclassName";
	private String subscription = "subscription";
	private String asyncMethodName = "asyncMethodName";


	@Override
	public Void refactor(ProjectUnits units, AsyncTaskCollector collector, WorkerSummary summary) throws Exception {
			
		// All anonymous class declarations
		Multimap<BundledCompilationUnit, AnonymousClassDeclaration> anonymousClasses = collector.getAnonymousClasses();
		

		// Iterate through all relevant compilation units
		for (BundledCompilationUnit unit : anonymousClasses.keySet()) {

			Collection<AnonymousClassDeclaration> declarations = anonymousClasses.get(unit);

			// Iterate through all anonymous AsyncTask declarations
			for (AnonymousClassDeclaration asyncTaskDeclaration : declarations) {

				//Retrieves information about the AsyncTask declaration
				AsyncTaskWrapper asyncTask = new AsyncTaskWrapper(asyncTaskDeclaration, unit);

				//If there is no doInBackground, then we can't do any refactoring
				if (asyncTask.getDoInBackgroundBlock() == null)
					continue;
	
							
				AST ast = unit.getAST();

				Statement referenceStatement = ASTUtils.findParent(asyncTaskDeclaration, Statement.class);

				//Adds the observable
				addObservable(asyncTask, unit, referenceStatement, asyncTaskDeclaration);

				if (asyncTask.hasAdditionalAccess()) {
					if (!checkForimplicitExecute(collector.getRelevantUsages(), ast, asyncTaskDeclaration,
							unit))
						unit.remove(asyncTaskDeclaration);
					updateUsage(collector.getRelevantUsages(), ast, asyncTaskDeclaration, unit);
				}

				updateImports(asyncTask, unit);				
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
	 * @param cuRelevantUsagesMap
	 * @param ast
	 * @param singleChangeWriter
	 * @param asyncTaskDeclaration
	 * @param unit
	 */
	private boolean checkForimplicitExecute(Multimap<BundledCompilationUnit, MethodInvocation> cuRelevantUsagesMap, AST ast,
			AnonymousClassDeclaration asyncTaskDeclaration, BundledCompilationUnit unit) {
		
		String variableName = "getObservable";
		
		MethodInvocation implicitExecute = null;
		ASTNode execute = AsyncTaskASTUtils.findOuterParent(asyncTaskDeclaration, MethodInvocation.class);
		
		if (execute != null)
			implicitExecute = (MethodInvocation) execute;
		
		if (implicitExecute != null)
			if (implicitExecute.getName().toString().equals(EXECUTE)
					|| implicitExecute.getName().toString().equals(EXECUTE_ON_EXECUTOR)) {
				
				MethodInvocation me = ast.newMethodInvocation();
				me.setName(ast.newSimpleName(implicitExecute.getName().toString()));
				me.setExpression(ast.newSimpleName(variableName));
				
			
				replaceExecute(me, unit, false);
				return true;
			}
		return false;
	}

	private void updateUsage(Multimap<BundledCompilationUnit, MethodInvocation> cuRelevantUsagesMap, AST ast,
			AnonymousClassDeclaration asyncTaskDeclaration,	BundledCompilationUnit icuOuter) {
		for (BundledCompilationUnit unit : cuRelevantUsagesMap.keySet()) {
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
						replaceExecute(methodInvoke, unit, true);
						updateCancelInvocation(unit, methodReference, astInvoke);

					}
				}
			} catch (Exception e) {

			}
		}
		if (!isCancelPresent) {

			replaceExecute(methodInvoke, unit, false);
		}
	}

	/**
	 * Add cancel method if AsyncTask.cacel() method was invoked
	 */
	private void updateCancelInvocation(BundledCompilationUnit unit, MethodInvocation methodInvoke, AST astInvoke) {

//		UnitWriterExt writer = UnitWriters.getOrElse(unit,
//				() -> new UnitWriterExt(unit, astInvoke, getClass().getSimpleName()));
		
		Log.info(getClass(),
				"METHOD=updateCancelInvocation - update Cancel Invocation for class: " + unit.getElementName());
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
//	void replaceExecute(UnitWriterExt writer, MethodInvocation methodInvoke, ICompilationUnit unit, AST astInvoke, boolean withSubscription) {
//
////		UnitWriterExt writer = UnitWriters.getOrElse(unit,
////				() -> new UnitWriterExt(unit, astInvoke, getClass().getSimpleName()));
//
//		AST methodAST = methodInvoke.getAST();
//		MethodInvocation execute = (MethodInvocation) ASTNode.copySubtree(methodAST, methodInvoke);
//		if (execute.getName().toString().equals(EXECUTE_ON_EXECUTOR)) {
//			execute.arguments().clear();
//		}
//		execute.setExpression(AsyncTaskASTUtils.getinstannceCreationStatement(methodAST, complexObservableclassName));
//		execute.setName(methodAST.newSimpleName(asyncMethodName));
//
//		MethodInvocation invocation = methodAST.newMethodInvocation();
//
//		invocation.setExpression(execute);
//		invocation.setName(methodAST.newSimpleName(SUBSCRIBE));
//		// ASTUtil.replaceInStatement(methodInvoke, invocation);
//		if (!withSubscription) {
//			writer.replace(methodInvoke, invocation);
//		} else {
//			createSubscriptionDeclaration(writer, unit, methodInvoke);
//			Assignment initSubscription = methodAST.newAssignment();
//
//			initSubscription.setLeftHandSide(
//					methodAST.newSimpleName(getVariableName(methodInvoke.getExpression()) + subscription));
//			initSubscription.setRightHandSide(invocation);
//			// ASTUtil.replaceInStatement(methodInvoke, initSubscription);
//			writer.replace(methodInvoke, initSubscription);
//		}
//	}

	/**
	 * Method to refactor method invocation statements with name execute EX: new
	 * Task().execute();
	 */
	void replaceExecute(MethodInvocation executeInvoke, BundledCompilationUnit unit, boolean withSubscription) {

//		UnitWriterExt writer = UnitWriters.getOrElse(unit,
//				() -> new UnitWriterExt(unit, ast, getClass().getSimpleName()));

		AST ast = unit.getAST();
		
		
		//Builds: new ObservableWrapper()
		ClassInstanceCreation constructor = AsyncTaskASTUtils.getinstannceCreationStatement(ast, complexObservableclassName);
		
		//Builds: new ObservableWrapper().create()
		MethodInvocation createInvoke = ast.newMethodInvocation();
		createInvoke.setName(ast.newSimpleName(asyncMethodName));
		createInvoke.setExpression(constructor);
		
		//TODO: Remove this part and produce a completely new expression
		//Refactor: getObservable.execute()  -->  new ObservableWrapper().create().subscribe()
//		MethodInvocation execute = (MethodInvocation) ASTNode.copySubtree(methodAST, executeInvoke);
//		if (execute.getName().toString().equals(EXECUTE_ON_EXECUTOR)) {
//			execute.arguments().clear();
//		}
//		execute.setExpression();
//		execute.setName(methodAST.newSimpleName(asyncMethodName));

		
		//Builds: new ObservableWrapper().create().subscribe()
		MethodInvocation subscribeInvoke = ast.newMethodInvocation();
		subscribeInvoke.setExpression(createInvoke);
		subscribeInvoke.setName(ast.newSimpleName(SUBSCRIBE));		
		
		//Statement ss = ASTNodeFactory.createSingleStatementFromText(methodAST, invocation.toString());
		if (!withSubscription) {
			unit.replace(executeInvoke, subscribeInvoke);
		} else {
			createSubscriptionDeclaration(unit, executeInvoke);
			Assignment initSubscription = ast.newAssignment();

			initSubscription.setLeftHandSide(
					ast.newSimpleName(getVariableName(executeInvoke.getExpression()) + subscription));
			initSubscription.setRightHandSide(subscribeInvoke);
			// ASTUtil.replaceInStatement(methodInvoke, initSubscription);
			unit.replace(executeInvoke, initSubscription);
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
		AST astInvoke = tyDec.getAST();

//		UnitWriterExt writer = UnitWriters.getOrElse(unit,
//				() -> new UnitWriterExt(unit, astInvoke, getClass().getSimpleName()));

		VariableDeclarationFragment variable = astInvoke.newVariableDeclarationFragment();
		variable.setName(astInvoke.newSimpleName(methodInvoke.getExpression().toString() + subscription));
		FieldDeclaration subscription = astInvoke.newFieldDeclaration(variable);
		subscription.setType(astInvoke.newSimpleType(astInvoke.newSimpleName("Subscription")));

		unit.getListRewrite(tyDec, TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertFirst(subscription, null);;
		unit.addImport("rx.Subscription");
	}

	private void updateImports(AsyncTaskWrapper asyncTask, BundledCompilationUnit unit) {
		unit.addImport("rx.Observable");
		unit.addImport("rx.schedulers.Schedulers");
		unit.addImport("java.util.concurrent.Callable");
		unit.removeImport("android.os.AsyncTask");

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


	private void addObservable(AsyncTaskWrapper asyncTask, BundledCompilationUnit unit,
			Statement referenceStatement, AnonymousClassDeclaration anonymousCachedClassDecleration) {
		
		
		boolean hasProgressUpdateBlock = asyncTask.getOnProgressUpdateBlock() != null;
		//removeSuperInvocations(asyncTask);

//		SubscriberHolder subscriberHolder = null;
//		if (hasProgressUpdateBlock) {
//			subscriberHolder = new SubscriberHolder(unit.getElementName(),
//					asyncTask.getProgressType().toString() + "[]", asyncTask.getOnProgressUpdateBlock(),
//					asyncTask.getProgressParameters());
//		}
		
		
		
		AST ast = referenceStatement.getAST();
		
		if (!asyncTask.hasAdditionalAccess() && asyncTask.getIsVoid()) {
			
			//Produces the builder that creates the observable
			//AnonymousClassBuilder builder = observableBuilder(asyncTask, writer, subscriberHolder);
			
			
			
			//Adds the progress-update subscriber
			if (hasProgressUpdateBlock) {
				SubscriberBuilder subscriberBuilder = new SubscriberBuilder(asyncTask);
				
				//Add the method that creates the subscriber (getRxSubscriber)
				addMethodAfter(unit, subscriberBuilder.buildGetSubscriber(), anonymousCachedClassDecleration);
				//Adds the subscriber declaration
				addStatementBefore(unit, subscriberBuilder.buildSubscriberDeclaration(), referenceStatement);
				
				replacePublishInvocations(asyncTask, subscriberBuilder);
			}
			
			
			//Gets the observable expression and adds it to the observable.
			Expression observable = AnonymousClassBuilder.from(asyncTask);			
			Statement stmt = ast.newExpressionStatement(observable);
			
			unit.replace(referenceStatement, stmt);
			
			/*
			 * Old code
			 */
//			String subscribedObservable = createObservable(asyncTask, writer, subscriberHolder).addSubscribe().build();
//		
//			if (onProgressUpdateBlock) {
//				String newMethodString = subscriberHolder.getGetMethodDeclaration();
//				MethodDeclaration newMethod = ASTNodeFactory.createMethodFromText(ast, newMethodString);
//				writer.addMethodAfter(newMethod, anonymousCachedClassDecleration);
//
//				String subscriberDecl = subscriberHolder.getSubscriberDeclaration();
//				Statement getSubscriberStatement = ASTNodeFactory.createSingleStatementFromText(ast, subscriberDecl);
//				writer.addStatementBefore(getSubscriberStatement, referenceStatement);
//			}

//			Statement newStatement = ASTNodeFactory.createSingleStatementFromText(ast, subscribedObservable);
//			
//			writer.replaceStatement(referenceStatement, newStatement);

		} else {

			SubscriberBuilder subscriberBuilder = new SubscriberBuilder(asyncTask);
			
			InnerClassBuilder builder = new InnerClassBuilder(asyncTask);
			TypeDeclaration observable = builder.buildInnerClass();
			
//			List<FieldDeclaration> fieldDeclarations = asyncTask.getFieldDeclarations();
//			String subscriberDecl = EMPTY;
//			String subscriberGetRxUpdateMethod = EMPTY;
//
//			if (hasProgressUpdateBlock) {
//				subscriberDecl = subscriberHolder.getSubscriberDeclaration();
//				subscriberGetRxUpdateMethod = subscriberHolder.getGetMethodDeclaration();
//			}
//
//			String observableStatement = observableBuilder(asyncTask, writer, subscriberHolder).buildReturnStatement();
//			String observableType = asyncTask.getReturnType().toString();
//
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
			complexObservableclassName = observable.getName().getIdentifier();

			// initialize asyncmethodname to be used at execute
			asyncMethodName = builder.getMethodName();

			//subscription = complexObservable.getAsynSubscription();
			subscription = "subscription" + builder.getId();

//			TypeDeclaration complexRxObservableDecl = ASTNodeFactory.createTypeDeclarationFromText(ast,
//					complexRxObservableClass);
			addInnerClassAfter(unit, observable, referenceStatement);
		}

	}

//	private AnonymousClassBuilder observableBuilder(AsyncTaskWrapper asyncTask, UnitWriter writer,
//			SubscriberHolder subscriberHolder) {
//
//		Block doInBackgroundBlock = asyncTask.getDoInBackgroundBlock();
//		Block doOnCompletedBlock = asyncTask.getOnPostExecuteBlock();
//
//		//String type = (asyncTask.getReturnedType() == null ? "Void" : asyncTask.getReturnedType().toString());
//		//String postExecuteParameters = asyncTask.getPostExecuteParameter();
//
////		if (type == null) {
////			Log.error(getClass(), "NULL type for DoInBackground");
////		}
//		
//		replacePublishInvocations(asyncTask, subscriberHolder);
//		ASTUtils.removeUnnecessaryCatchClauses(doOnCompletedBlock);
//		AsyncTaskASTUtils.removeMethodInvocations(doOnCompletedBlock);
//		
//		//doOnCompletedBlock.
//		AsyncTaskASTUtils.replaceFieldsWithFullyQualifiedNameIn(doOnCompletedBlock, writer);
//
//		
//		return AnonymousClassBuilder.from(asyncTask, writer);
//	}

	
	

	public int getNUMBER_OF_ANONYMOUS_TASKS() {
		return numOfAnonymousClasses;
	}



	

}
