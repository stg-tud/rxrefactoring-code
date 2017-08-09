/**
 * 
 */
package de.tudarmstadt.rxrefactoring.ext.asynctask.workers;

import java.util.Collection;
import java.util.Objects;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.logging.Log;
import de.tudarmstadt.rxrefactoring.core.parser.BundledCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.parser.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.utils.ASTUtils;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.workers.IWorker;
import de.tudarmstadt.rxrefactoring.ext.asynctask.builders.InnerClassBuilder;
import de.tudarmstadt.rxrefactoring.ext.asynctask.builders.RefactorNames;
import de.tudarmstadt.rxrefactoring.ext.asynctask.builders.SubscriberBuilder;
import de.tudarmstadt.rxrefactoring.ext.asynctask.utils.AsyncTaskWrapper;


/**
 * Description: This worker is in charge of refactoring classes that extends
 * AsyncTasks that are assigned to a variable.<br>
 * Example: class Task extends AsyncTask<>{...}<br>
 * Author: Ram<br>
 * Created: 11/12/2016
 */
public class SubClassAsyncTaskWorker implements IWorker<AsyncTaskCollector, Void>, WorkerEnvironment {
	final String SUBSCRIPTION = "Subscription";
	final String EXECUTE = "execute";
	final String EXECUTE_ON_EXECUTOR = "executeOnExecutor";
	final String CANCEL = "cancel";
	final String SUBSCRIBE = "subscribe";
	final String UN_SUBSCRIBE = "unsubscribe";
	
	private int numOfAsyncTasks = 0;
	private int numOfAbstractClasses = 0;

	/**
	 * @return the nUMBER_OF_ASYNC_TASKS
	 */
	public int numOfAsyncTasks() {
		return numOfAsyncTasks;
	}

	/**
	 * @return the nUMBER_OF_ABSTRACT_TASKS
	 */
	public int numOfAbstractClasses() {
		return numOfAbstractClasses;
	}


	@Override
	public Void refactor(ProjectUnits units, AsyncTaskCollector collector, WorkerSummary summary) throws Exception {
		
		//Reset counters
		numOfAsyncTasks = 0;
		numOfAbstractClasses = 0;
		
		Multimap<BundledCompilationUnit, TypeDeclaration> cuAnonymousClassesMap = collector.getSubclasses();

		for (BundledCompilationUnit unit : cuAnonymousClassesMap.keySet()) {
			Collection<TypeDeclaration> declarations = cuAnonymousClassesMap.get(unit);
			for (TypeDeclaration asyncTaskDeclaration : declarations) {

				AsyncTaskWrapper asyncTask = new AsyncTaskWrapper(asyncTaskDeclaration, unit);

				if (asyncTask.getDoInBackgroundBlock() == null) {
					numOfAbstractClasses++;
					continue;
				}

				AST ast = asyncTaskDeclaration.getAST();
				ASTNode root = unit.getRoot();							
				
				
				updateUsage(collector.getRelevantUsages(), ast, asyncTaskDeclaration, unit);
				removeSuperClass(unit, asyncTaskDeclaration);
				
				updateImports(asyncTask);
				createLocalObservable(asyncTask, asyncTaskDeclaration, root);
			}

		}
		
		summary.setCorrect("asynctask-subclasses", numOfAsyncTasks);
		summary.setSkipped("asynctask-subclasses", numOfAbstractClasses);
		

		return null;
	}

	private void updateUsage(Multimap<BundledCompilationUnit, MethodInvocation> relevantUsages, AST ast,
			TypeDeclaration asyncTaskDeclaration, BundledCompilationUnit unit) {
		
		
		for (BundledCompilationUnit usageUnit : relevantUsages.keySet()) {
			TypeDeclaration tyDec = ast.newTypeDeclaration();

						
			for (MethodInvocation methodInvoke : relevantUsages.get(usageUnit)) {
				if (methodInvoke.getName().toString().equals(EXECUTE)
						|| methodInvoke.getName().toString().equals(EXECUTE_ON_EXECUTOR)) {

					if (unit.getElementName().equals(usageUnit.getElementName())) {
						
						replaceCancel(relevantUsages, methodInvoke, unit, ast);
					} else {
						tyDec = ASTUtils.findParent(methodInvoke, TypeDeclaration.class);
						AST astInvoke = tyDec.getAST();						
						replaceCancel(relevantUsages, methodInvoke, usageUnit, astInvoke);
					}

				}
			}

		}

	}

	/**
	 * Replace asyncTask.cancel() method with Subscription.unsubscribe()
	 */
	void replaceCancel(Multimap<BundledCompilationUnit, MethodInvocation> relevantUsages, MethodInvocation methodInvoke,
			BundledCompilationUnit unit, AST astInvoke) {
				
		boolean isCancelPresent = false;
		
		for (MethodInvocation methodReference : relevantUsages.get(unit)) {
			if (Objects.toString(methodInvoke.getExpression()) .equals (Objects.toString(methodReference.getExpression()))) {
				if (methodReference.getName().toString().equals(CANCEL)) {
					isCancelPresent = true;
					Log.info(getClass(), "METHOD=replaceCancel - updating cancel invocation for class: "
							+ unit.getElementName());
					replaceExecuteWithSubscription(unit, methodInvoke, astInvoke);
					updateCancelInvocation(unit, methodReference, astInvoke);

				}
			}			
		}
		
		if (!isCancelPresent) {
			replaceExecute(unit, methodInvoke, astInvoke);
		}
	}

	/**
	 * Method to refactor method invocation statements with name execute EX: new
	 * Task().execute();
	 */
	void replaceExecute(BundledCompilationUnit unit, MethodInvocation methodInvoke, AST astInvoke) {

		AST methodAST = methodInvoke.getAST();
		MethodInvocation execute = (MethodInvocation) ASTNode.copySubtree(methodAST, methodInvoke);
		if (execute.getName().toString().equals(EXECUTE_ON_EXECUTOR)) {
			execute.arguments().clear();
		}
		execute.setName(methodAST.newSimpleName(RefactorNames.CREATE_OBSERVABLE_METHOD_NAME));

		MethodInvocation invocation = methodAST.newMethodInvocation();
		invocation.setExpression(execute);
		invocation.setName(methodAST.newSimpleName(SUBSCRIBE));
		// ASTUtil.replaceInStatement(methodInvoke, invocation);
		unit.replace(methodInvoke, invocation);
		// singleChangeWriter.replace(methodInvoke, invocation);
	}

	/**
	 * Method to refactor method invocation statements with name execute EX: new
	 * Task().execute();
	 */
	void replaceExecuteWithSubscription(BundledCompilationUnit unit, MethodInvocation methodInvoke, AST astInvoke) {


	
		createSubscriptionDeclaration(unit, methodInvoke);

		MethodInvocation execute = (MethodInvocation) ASTNode.copySubtree(astInvoke, methodInvoke);
		execute.setName(astInvoke.newSimpleName(RefactorNames.CREATE_OBSERVABLE_METHOD_NAME));
		MethodInvocation invocation = astInvoke.newMethodInvocation();
		invocation.setExpression(execute);
		invocation.setName(astInvoke.newSimpleName(SUBSCRIBE));

		Assignment initSubscription = astInvoke.newAssignment();

		initSubscription
				.setLeftHandSide(astInvoke.newSimpleName(getVariableName(methodInvoke.getExpression()) + SUBSCRIPTION));
		initSubscription.setRightHandSide(invocation);
		unit.replace(methodInvoke, initSubscription);
	}

	void createSubscriptionDeclaration(BundledCompilationUnit unit, MethodInvocation methodInvoke) {

		
		TypeDeclaration tyDec = ASTUtils.findParent(methodInvoke, TypeDeclaration.class);
		AST astInvoke = tyDec.getAST();

		VariableDeclarationFragment variable = astInvoke.newVariableDeclarationFragment();
		Expression e = methodInvoke.getExpression();

		variable.setName(astInvoke.newSimpleName(getVariableName(e) + SUBSCRIPTION));

		FieldDeclaration subscription = astInvoke.newFieldDeclaration(variable);
		subscription.setType(astInvoke.newSimpleType(astInvoke.newSimpleName(SUBSCRIPTION)));
		
		addStatementToClass(unit, subscription, tyDec);
		unit.addImport("rx.Subscription");
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
	private void updateCancelInvocation(BundledCompilationUnit unit, MethodInvocation methodInvoke, AST astInvoke) {

		MethodInvocation unSubscribe = astInvoke.newMethodInvocation();
		unSubscribe
				.setExpression(astInvoke.newSimpleName(getVariableName(methodInvoke.getExpression()) + SUBSCRIPTION));
		unSubscribe.setName(astInvoke.newSimpleName(UN_SUBSCRIBE));
		unit.replace(methodInvoke, unSubscribe);

	}

	private void updateImports(AsyncTaskWrapper asyncTask) {
		
		BundledCompilationUnit unit = asyncTask.getUnit();
		
		unit.addImport("rx.android.schedulers.AndroidSchedulers");
		unit.addImport("rx.schedulers.Schedulers");
		
		unit.addImport("rx.Observable");		
		unit.addImport("java.util.concurrent.Callable");

		if (numOfAbstractClasses == 0) {
			unit.removeImport("android.os.AsyncTask");
		}

		if (asyncTask.getOnPostExecuteBlock() != null) {
			unit.addImport("rx.functions.Action1");
			unit.removeImport("java.util.concurrent.ExecutionException");
			unit.removeImport("java.util.concurrent.TimeoutException");

		}

		if (asyncTask.getDoInBackgroundBlock() != null) {
			unit.addImport("rx.Subscriber");
			unit.addImport("java.util.Arrays");
			unit.addImport("rx.Subscription");
			unit.addImport("rx.functions.Action1");
		}

		if (asyncTask.getOnProgressUpdateBlock() != null) {
			unit.addImport("java.util.Arrays");
		}

		if (asyncTask.getOnCancelled() != null) {
			unit.addImport("rx.functions.Action0");
		}

		if (asyncTask.getOnPreExecuteBlock() != null) {
			unit.addImport("rx.functions.Action0");
		}
	}

	private void createLocalObservable(AsyncTaskWrapper asyncTask, TypeDeclaration taskObject, ASTNode root) {
		
		//replacePublishInvocations(asyncTask, rewriter);
		
		SubscriberBuilder subscriberBuilder = new SubscriberBuilder(asyncTask);
		InnerClassBuilder innerClassBuilder = new InnerClassBuilder(asyncTask, subscriberBuilder.getId());
		
		TypeDeclaration typeDecl = innerClassBuilder.buildInnerClassWithSubscriber(subscriberBuilder);
		if (asyncTask.hasProgressUpdate()) {
			replacePublishInvocations(asyncTask, subscriberBuilder);
		}
		
				
		asyncTask.getUnit().replace(taskObject, typeDecl);
		//replaceInstanceCreations(writer, innerClassBuilder, taskObject, root);
		
		//Define method
		/*
		 * public Observable<T> getAsyncObservable(final PARAMETER) {
		 *   return NEW OBSERVABLE
		 * }
		 */		
//		SubscriberBuilder subscriberBuilder = new SubscriberBuilder(asyncTask, writer);
//		ObservableMethodBuilder observableMethodBuilder = new ObservableMethodBuilder(asyncTask, writer, subscriberBuilder.getId());
//		writer.replaceStatement(asyncTask.getDoInBackgroundmethod(), observableMethodBuilder.buildCreateMethod());
//
//		// Remove postExecute method
//		if (asyncTask.getOnPostExecuteBlock() != null)
//			writer.removeMethod(asyncTask.getOnPostExecuteBlock().getParent(), taskObject);
//		// Remove updateProgress method
//		if (asyncTask.getOnProgressUpdateBlock() != null) {
//			replacePublishInvocations(asyncTask, writer, subscriberBuilder);
//			writer.removeMethod(asyncTask.getOnProgressUpdateBlock().getParent(), taskObject);
//		}
//			
//		// Remove onCancelled method
//		if (asyncTask.getOnCancelled() != null)
//			writer.removeMethod(asyncTask.getOnCancelled().getParent(), taskObject);
	
	}
	
	private void replaceInstanceCreations(BundledCompilationUnit unit, InnerClassBuilder builder, TypeDeclaration oldClass, ASTNode root) {
		
		
		final ITypeBinding oldClassType = oldClass.resolveBinding();
		if (oldClassType == null) {
			Log.info(getClass(), "Class could not have been resolved to a type: " + oldClass.getName());
			return;
		}
		
		
		class ClassInstanceCreationVisitor extends ASTVisitor {
			
						
			@Override 
			public boolean visit(ClassInstanceCreation node) {
				
				ITypeBinding type = node.resolveTypeBinding();
				
				if (type == null) return true;
				
				//if (ASTUtils.isTypeOf(node.getType(), oldClassType)) {
				if (type.getQualifiedName().equals(oldClassType.getQualifiedName())) {
					unit.replace(node, builder.buildNewObservableWrapper());
				}					
				
				return true;
			}			
		}
		
		
		//TODO: This only looks for subclasses that have been found until now. 
		root.accept(new ClassInstanceCreationVisitor()); 
		
//		for (Entry<BundledCompilationUnit, ASTNode> entry : collector.getRootNodes().entrySet()) {
//			
//			UnitWriter writer = UnitWriters.getOrPut(entry.getKey(), () -> new UnitWriterExt(entry.getKey(), entry.getValue().getAST()));
//			
//			ClassInstanceCreationVisitor v = new ClassInstanceCreationVisitor(writer);
//			entry.getValue().accept(v);
//		}	
		
		
		
		
		
	}
}


