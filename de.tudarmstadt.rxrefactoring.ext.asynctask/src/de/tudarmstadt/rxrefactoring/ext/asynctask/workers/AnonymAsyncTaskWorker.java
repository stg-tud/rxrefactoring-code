package de.tudarmstadt.rxrefactoring.ext.asynctask.workers;

import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.IWorkerV1;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.utils.ASTNodes;
import de.tudarmstadt.rxrefactoring.core.utils.Types;
import de.tudarmstadt.rxrefactoring.ext.asynctask.builders.AnonymousClassBuilder;
import de.tudarmstadt.rxrefactoring.ext.asynctask.builders.InnerClassBuilder;
import de.tudarmstadt.rxrefactoring.ext.asynctask.builders.RefactorNames;
import de.tudarmstadt.rxrefactoring.ext.asynctask.builders.SubscriberBuilder;
import de.tudarmstadt.rxrefactoring.ext.asynctask.utils.AsyncTaskASTUtils;
import de.tudarmstadt.rxrefactoring.ext.asynctask.utils.AsyncTaskWrapper;

/**
 * Refactors anonymous classes of AsyncTask.
 */
public class AnonymAsyncTaskWorker implements IWorkerV1<AsyncTaskCollector, Void>, WorkerEnvironment {

	private final String EXECUTE = "execute";
	private final String CANCEL = "cancel";
	private final String SUBSCRIBE = "subscribe";
	private final String UN_SUBSCRIBE = "unsubscribe";
	final String EXECUTE_ON_EXECUTOR = "executeOnExecutor";
//	private String complexObservableclassName = "complexObservableclassName";
	private String subscription = "subscription";
//	private String asyncMethodName = "asyncMethodName";
	
//	private InnerClassBuilder innerClassBuilder = null;


	@Override
	public Void refactor(IProjectUnits units, AsyncTaskCollector collector, WorkerSummary summary) throws Exception {
		// All anonymous class declarations
		Multimap<IRewriteCompilationUnit, AnonymousClassDeclaration> anonymousClasses = collector.getAnonymousClasses();

		// Iterate through all relevant compilation units
		for (IRewriteCompilationUnit unit : anonymousClasses.keySet()) {
			// Iterate through all anonymous AsyncTask declarations
			for (AnonymousClassDeclaration asyncTaskDeclaration : anonymousClasses.get(unit)) {

				//Retrieves information about the AsyncTask declaration
				AsyncTaskWrapper asyncTask = new AsyncTaskWrapper(asyncTaskDeclaration, unit);

				//If there is no doInBackground, then we can't do any refactoring
				if (!AsyncTaskASTUtils.canBeRefactored(asyncTask)) {
					summary.addSkipped("asynctask-anonymous");
					continue;
				}
					
				
				//Retrieves the statement that defines the AsyncTask.
				Optional<Statement> referenceStatement = ASTNodes.findParent(asyncTaskDeclaration, Statement.class);
				
				if (!referenceStatement.isPresent())
					throw new IllegalArgumentException("asyncTaskDeclaration is not a child of a Statement");
							
				//Gets the AST used for this compilation unit
				AST ast = unit.getAST();
				
				//Update imports accordingly
				updateImports(asyncTask, unit);	
				
				//Replace the anonymous AsyncTask declaration with an observable
				InnerClassBuilder builder = addObservable(asyncTask, unit, referenceStatement.get(), asyncTaskDeclaration);
				
				if (builder != null) {
					updateUsageFor(asyncTask, collector.getRelevantUsages(), builder);
				}
					
				
				summary.addCorrect("asynctask-anonymous");
				//if (asyncTask.hasAdditionalAccess()) {
				//innerClassBuilder != null, if there has to be a inner class for the observable (instead of an anonymous class).
//				if (innerClassBuilder != null) {
//					if (!checkForimplicitExecute(collector.getRelevantUsages(), ast, asyncTaskDeclaration,	unit))
//						unit.remove(asyncTaskDeclaration);	
//					
//					updateUsage(collector.getRelevantUsages(), ast, asyncTaskDeclaration, unit);
//				}						
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
//	private boolean checkForimplicitExecute(Multimap<IRewriteCompilationUnit, MethodInvocation> cuRelevantUsagesMap, AST ast,
//			AnonymousClassDeclaration asyncTaskDeclaration, IRewriteCompilationUnit unit) {
//		
//	//	String variableName = "getObservable";
//		
//	//TODO: Does not find execute inovcation as the parent
//		MethodInvocation execute = ASTUtils.findParent(asyncTaskDeclaration, MethodInvocation.class); //AsyncTaskASTUtils.findOuterParent(asyncTaskDeclaration, MethodInvocation.class);
//		
//		if (execute != null	 && (execute.getName().toString().equals(EXECUTE)
//					|| execute.getName().toString().equals(EXECUTE_ON_EXECUTOR))) {					
//				
//			Expression expr = createExecuteReplacement(unit);
//			unit.replace(execute, expr);
//
//			return true;
//		}
//		
//		return false;
//	}
	
	
	
	private void updateUsageFor(AsyncTaskWrapper asyncTask, Multimap<IRewriteCompilationUnit, MethodInvocation> usages, InnerClassBuilder builder) {
		
		for (Entry<IRewriteCompilationUnit, MethodInvocation> entry : usages.entries()) {
			
			IRewriteCompilationUnit unit = entry.getKey();
			MethodInvocation method = entry.getValue();
			
			ITypeBinding asyncTaskBinding = asyncTask.resolveTypeBinding();
			IMethodBinding methodBinding = method.resolveMethodBinding();
			
			ITypeBinding methodReceiverBinding;
			
			//Get binding of method expression
			if (method.getExpression() != null) {
				methodReceiverBinding = method.getExpression().resolveTypeBinding();
			} else if (methodBinding != null) {
				methodReceiverBinding = methodBinding.getDeclaringClass();
			} else {
				methodReceiverBinding = null;
			}
			
			
			if (asyncTaskBinding != null && methodReceiverBinding != null && asyncTaskBinding.isEqualTo(methodReceiverBinding)) {				
				if (method.getName().toString().equals(EXECUTE) || method.getName().toString().equals(EXECUTE_ON_EXECUTOR))
					replaceExecute(unit, method, builder, false);
			}			
		}		
	}
	
	

//	private void updateUsage(Multimap<IRewriteCompilationUnit, MethodInvocation> cuRelevantUsagesMap, AST ast,
//			AnonymousClassDeclaration asyncTaskDeclaration,	IRewriteCompilationUnit icuOuter) {
//		
//		for (IRewriteCompilationUnit unit : cuRelevantUsagesMap.keySet()) {
//			for (MethodInvocation methodInvoke : cuRelevantUsagesMap.get(unit)) {
//				
//				if (methodInvoke.getName().toString().equals(EXECUTE)
//						|| methodInvoke.getName().toString().equals(EXECUTE_ON_EXECUTOR)) {
//
//					if (icuOuter.getElementName().equals(unit.getElementName()))
//						replaceCancel(cuRelevantUsagesMap, methodInvoke, icuOuter, ast);
//					else {
//						// String newName = ((TypeDeclaration)
//						// ASTUtil.findParent(methodInvoke,
//						// TypeDeclaration.class))
//						// .getName().toString();
//						// tyDec = (TypeDeclaration)
//						// ASTUtil.findParent(methodInvoke,
//						// TypeDeclaration.class);
//						// astInvoke = tyDec.getAST();
//						// if
//						// (!SingleUnitExtensionWriterMap.keySet().contains(newName))
//						// {
//						// singleChangeWriterNew = new SingleUnitExtensionWriter(unit,
//						// astInvoke,
//						// getClass().getSimpleName());
//						// SingleUnitExtensionWriterMap.put(newName, new
//						// ComplexMap(singleChangeWriterNew, unit));
//						// }
//						// replaceCancel(cuRelevantUsagesMap, methodInvoke, unit,
//						// astInvoke);
//					}
//
//				}
//			}
//
//		}
//
//	}

	/**
	 * Replace asyncTask.cancel() method with Subscription.unsubscribe()
	 *
	 * @param cuRelevantUsagesMap
	 * @param methodInvoke
	 * @param unit
	 * @param astInvoke
	 */
//	private void replaceCancel(Multimap<IRewriteCompilationUnit, MethodInvocation> cuRelevantUsagesMap, MethodInvocation methodInvoke,
//			IRewriteCompilationUnit unit, AST astInvoke) {
//		boolean isCancelPresent = false;
//		for (MethodInvocation methodReference : cuRelevantUsagesMap.get(unit)) {
//			try {
//				if (methodInvoke.getExpression().toString().equals(methodReference.getExpression().toString())) {
//					if (methodReference.getName().toString().equals(CANCEL)) {
//						isCancelPresent = true;
//						
//						replaceExecute(unit, methodInvoke, true);
//						updateCancelInvocation(unit, methodReference, astInvoke);
//
//					}
//				}
//			} catch (Exception e) {
//
//			}
//		}
//		if (!isCancelPresent) {
//
//			replaceExecute(unit, methodInvoke, false);
//		}
//	}

	/**
	 * Add cancel method if AsyncTask.cacel() method was invoked
	 */
//	private void updateCancelInvocation(IRewriteCompilationUnit unit, MethodInvocation methodInvoke, AST astInvoke) {
//
//		MethodInvocation unSubscribe = astInvoke.newMethodInvocation();
//		unSubscribe
//				.setExpression(astInvoke.newSimpleName(getVariableName(methodInvoke.getExpression()) + subscription));
//		unSubscribe.setName(astInvoke.newSimpleName(UN_SUBSCRIBE));
//		unit.replace(methodInvoke, unSubscribe);
//
//	}

	/**
	 * Method to refactor method invocation statements with name execute EX: new
	 * Task().execute();
	 */
//	void replaceExecute(UnitWriterExt writer, MethodInvocation methodInvoke, IRewriteCompilationUnit unit, AST astInvoke, boolean withSubscription) {
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

//	private MethodInvocation createExecuteReplacement(IRewriteCompilationUnit unit, InnerClassBuilder builder) {
//		//InnerClassBuilder should be initialized in the addObservable method.
//		Objects.requireNonNull(builder, "InnerClassBuilder has not been initialized.");
//		
//		//Initialize the AST used for this unit.
//		AST ast = unit.getAST();
//		
//		
//		//Builds: new ObservableWrapper()
//		ClassInstanceCreation constructor = builder.buildNewObservableWrapper(ast);		
//		//ClassInstanceCreation constructor = AsyncTaskASTUtils.getinstannceCreationStatement(ast, complexObservableclassName);
//		
//		//Builds: new ObservableWrapper().create()
//		MethodInvocation createInvoke = ast.newMethodInvocation();
//		createInvoke.setName(ast.newSimpleName(RefactorNames.CREATE_OBSERVABLE_METHOD_NAME));
//		createInvoke.setExpression(constructor);
//		
//		
//		
//		//TODO: Remove this part and produce a completely new expression
//		//Refactor: getObservable.execute()  -->  new ObservableWrapper().create().subscribe()
////		MethodInvocation execute = (MethodInvocation) ASTNode.copySubtree(methodAST, executeInvoke);
////		if (execute.getName().toString().equals(EXECUTE_ON_EXECUTOR)) {
////			execute.arguments().clear();
////		}
////		execute.setExpression();
////		execute.setName(methodAST.newSimpleName(asyncMethodName));
//
//		
//		//Builds: new ObservableWrapper().create().subscribe()
//		MethodInvocation subscribeInvoke = ast.newMethodInvocation();
//		subscribeInvoke.setExpression(createInvoke);
//		subscribeInvoke.setName(ast.newSimpleName(SUBSCRIBE));
//		
//		return subscribeInvoke;
//	}
	
//	private void addSubscribe(Expression expr, IRewriteCompilationUnit unit, boolean withSubscription) {
//		AST ast = unit.getAST();
//		
//		MethodInvocation subscribe = ast.newMethodInvocation();
//		subscribe.setName(ast.newSimpleName("subscribe"));
//		subscribe.setExpression(expr);
//	}
	
	/**
	 * Method to refactor method invocation statements with name execute EX: new
	 * Task().execute();
	 */
	private void replaceExecute(IRewriteCompilationUnit unit, MethodInvocation executeInvoke, InnerClassBuilder builder, boolean withSubscription) {
		//InnerClassBuilder should be initialized in the addObservable method.
		Objects.requireNonNull(builder, "InnerClassBuilder has not been initialized.");

		AST ast = unit.getAST();
		
		
		//Builds: new ObservableWrapper()
		ClassInstanceCreation constructor = builder.buildNewObservableWrapper(ast);
		
		
		
		//Builds: new ObservableWrapper().create()
		MethodInvocation createInvoke = ast.newMethodInvocation();
		createInvoke.setName(ast.newSimpleName(RefactorNames.CREATE_OBSERVABLE_METHOD_NAME));
		createInvoke.setExpression(constructor);
		executeInvoke.arguments().forEach(x -> createInvoke.arguments().add(unit.copyNode((Expression) x)));
		
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

	void createSubscriptionDeclaration(IRewriteCompilationUnit unit, MethodInvocation methodInvoke) {
		Optional<TypeDeclaration> tyDec = ASTNodes.findParent(methodInvoke, TypeDeclaration.class);
		AST astInvoke = tyDec.get().getAST();

		VariableDeclarationFragment variable = astInvoke.newVariableDeclarationFragment();
		variable.setName(astInvoke.newSimpleName(methodInvoke.getExpression().toString() + subscription));
		FieldDeclaration subscription = astInvoke.newFieldDeclaration(variable);
		subscription.setType(astInvoke.newSimpleType(astInvoke.newSimpleName("Subscription")));

		unit.getListRewrite(tyDec.get(), TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertFirst(subscription, null);;
		unit.addImport("rx.Subscription");
	}

	private void updateImports(AsyncTaskWrapper asyncTask, IRewriteCompilationUnit unit) {
		
		unit.addImport("rx.android.schedulers.AndroidSchedulers");
		unit.addImport("rx.schedulers.Schedulers");
		
		unit.addImport("rx.Observable");		
		unit.addImport("java.util.concurrent.Callable");

		if (asyncTask.getOnPreExecute() != null) {
			unit.addImport("rx.functions.Action0");
		}
		
		if (asyncTask.getOnPostExecute() != null) {
			unit.addImport("rx.functions.Action1");
			unit.removeImport("java.util.concurrent.ExecutionException");
			unit.removeImport("java.util.concurrent.TimeoutException");

		}

		if (asyncTask.getDoInBackground() != null) {
			unit.addImport("rx.functions.Action1");
		}

		if (asyncTask.getOnProgressUpdate() != null) {
			unit.addImport("rx.Subscriber");
			unit.addImport("java.util.Arrays");
		}

		if (asyncTask.getOnCancelled() != null) {
			unit.addImport("rx.functions.Action0");
		}
	}


	private InnerClassBuilder addObservable(AsyncTaskWrapper asyncTask, IRewriteCompilationUnit unit,
			Statement referenceStatement, AnonymousClassDeclaration anonymousCachedClassDeclaration) {
		
		
		//removeSuperInvocations(asyncTask);

//		SubscriberHolder subscriberHolder = null;
//		if (hasProgressUpdateBlock) {
//			subscriberHolder = new SubscriberHolder(unit.getElementName(),
//					asyncTask.getProgressType().toString() + "[]", asyncTask.getOnProgressUpdateBlock(),
//					asyncTask.getProgressParameters());
//		}
		
		
		
		AST ast = referenceStatement.getAST();
		
		//Build an anonymous class if possible
		if (!asyncTask.hasAdditionalAccess() && asyncTask.inputIsVoid()) {
			
			//Produces the builder that creates the observable
			//AnonymousClassBuilder builder = observableBuilder(asyncTask, writer, subscriberHolder);
				
			
			//Adds the progress-update subscriber
			if (asyncTask.hasOnProgressUpdate()) {
				SubscriberBuilder subscriberBuilder = new SubscriberBuilder(asyncTask);
				
				//TODO:Should this not be done by the InnerClassBuilder?
				//Add the method that creates the subscriber (getRxSubscriber)
				addMethodAfter(unit, subscriberBuilder.buildGetSubscriber(), anonymousCachedClassDeclaration);
				//Adds the subscriber declaration
				//addStatementBefore(unit, subscriberBuilder.buildSubscriberDeclaration(), referenceStatement);
				
				replacePublishInvocations(asyncTask, subscriberBuilder);
			}
			
			
			//Gets the observable expression and adds it to the observable.
			Expression observable = AnonymousClassBuilder.from(asyncTask, null);			
			
			
			//Check whether there is an execute invoke.
			Optional<MethodInvocation> parentInvoke = ASTNodes.findParent(asyncTask.getDeclaration(), MethodInvocation.class);
			if (parentInvoke.isPresent() && (parentInvoke.get().getName().toString().equals(EXECUTE)
					|| parentInvoke.get().getName().toString().equals(EXECUTE_ON_EXECUTOR))) {
				
				MethodInvocation subscribeInvoke = ast.newMethodInvocation();
				subscribeInvoke.setExpression(observable);
				subscribeInvoke.setName(ast.newSimpleName(SUBSCRIBE));
				
				observable = subscribeInvoke;
			}
			
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
			
			return null;

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
			
			
			Optional<MethodInvocation> parentInvocation = ASTNodes.findParent(asyncTask.getDeclaration(), MethodInvocation.class);
			
			if (parentInvocation.isPresent()) {
				//Check if execute method
				if (parentInvocation.get().getName().toString().equals(EXECUTE) || parentInvocation.get().getName().toString().equals(EXECUTE_ON_EXECUTOR)) {
					replaceExecute(unit, parentInvocation.get(), builder, asyncTask.hasOnProgressUpdate());
				}
			}
			
			
			replacePublishInvocations(asyncTask, subscriberBuilder);
			

//			String complexRxObservableClass = complexObservable.build();
			// initialize class name which will be used for replacing execute
			// method usage
//			complexObservableclassName = observable.getName().getIdentifier();

			// initialize asyncmethodname to be used at execute
//			asyncMethodName = builder.getMethodName();
			
			//subscription = complexObservable.getAsynSubscription();
//			subscription = "subscription" + builder.getId();

//			TypeDeclaration complexRxObservableDecl = ASTNodeFactory.createTypeDeclarationFromText(ast,
//					complexRxObservableClass);
			addInnerClassAfter(unit, observable, referenceStatement);
			
			return builder;
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



}
