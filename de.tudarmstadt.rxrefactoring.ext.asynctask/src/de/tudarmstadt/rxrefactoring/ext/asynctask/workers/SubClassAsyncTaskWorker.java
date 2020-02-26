/**
 * 
 */
package de.tudarmstadt.rxrefactoring.ext.asynctask.workers;

import java.util.Collection;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorScope;
import de.tudarmstadt.rxrefactoring.ext.asynctask.builders.InnerClassBuilder;
import de.tudarmstadt.rxrefactoring.ext.asynctask.builders.RefactorNames;
import de.tudarmstadt.rxrefactoring.ext.asynctask.builders.SubscriberBuilder;
import de.tudarmstadt.rxrefactoring.ext.asynctask.utils.AsyncTaskASTUtils;
import de.tudarmstadt.rxrefactoring.ext.asynctask.utils.AsyncTaskWrapper;


/**
 * Refactors subclasses of AsyncTask that appear in the code.
 */
public class SubClassAsyncTaskWorker implements IWorker<AsyncTaskCollector, Void>, WorkerEnvironment {
	final String SUBSCRIPTION = "Subscription";
	final String EXECUTE = "execute";
	final String EXECUTE_ON_EXECUTOR = "executeOnExecutor";
	final String CANCEL = "cancel";
	final String SUBSCRIBE = "subscribe";
	final String UN_SUBSCRIBE = "unsubscribe";
	

	@Override
	public Void refactor(IProjectUnits units, AsyncTaskCollector collector, WorkerSummary summary) throws Exception {
				
		Multimap<IRewriteCompilationUnit, TypeDeclaration> anonymousClasses = collector.getSubclasses();

		for (IRewriteCompilationUnit unit : anonymousClasses.keySet()) {
			Collection<TypeDeclaration> declarations = anonymousClasses.get(unit);
			for (TypeDeclaration asyncTaskDeclaration : declarations) {

				AsyncTaskWrapper asyncTask = new AsyncTaskWrapper(asyncTaskDeclaration, unit);

				if (!AsyncTaskASTUtils.canBeRefactored(asyncTask)) {
					summary.addSkipped("asynctask-subclasses");
					continue;
				}
				
//				removeSuperClass(unit, asyncTaskDeclaration);
				
				updateImports(asyncTask);
				InnerClassBuilder builder = createLocalObservable(asyncTask, asyncTaskDeclaration);
				
				updateUsageFor(asyncTask, collector.getRelevantUsages(), builder);
				//updateUsage(collector.getRelevantUsages(), ast, asyncTaskDeclaration, unit);
				
				summary.addCorrect("asynctask-subclasses");
			}
		}		

		return null;
	}
	
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
					replaceExecute(unit, method);
			}			
		}		
	}

	/**
	 * Replace asyncTask.cancel() method with Subscription.unsubscribe()
	 */
//	void replaceCancel(Multimap<IRewriteCompilationUnit, MethodInvocation> relevantUsages, MethodInvocation methodInvoke,
//			IRewriteCompilationUnit unit) {
//				
//		boolean isCancelPresent = false;
//		AST ast = unit.getAST();
//		
//		for (MethodInvocation methodReference : relevantUsages.get(unit)) {
//			if (Objects.toString(methodInvoke.getExpression()) .equals (Objects.toString(methodReference.getExpression()))) {
//				if (methodReference.getName().toString().equals(CANCEL)) {
//					isCancelPresent = true;
//					
//					replaceExecuteWithSubscription(unit, methodInvoke, ast);
//					updateCancelInvocation(unit, methodReference, ast);
//
//				}
//			}			
//		}
//		
//		if (!isCancelPresent) {
//			replaceExecute(unit, methodInvoke);
//		}
//	}

	/**
	 * Method to refactor method invocation statements with name execute EX: new
	 * Task().execute();
	 */
	private void replaceExecute(IRewriteCompilationUnit unit, MethodInvocation methodInvoke) {

		AST ast = unit.getAST();
		
		//TODO: Change copy subtree to ASTRewrite functionality
		MethodInvocation execute = (MethodInvocation) ASTNode.copySubtree(ast, methodInvoke);
		if (execute.getName().toString().equals(EXECUTE_ON_EXECUTOR)) {
			execute.arguments().clear();
		}
		execute.setName(ast.newSimpleName(RefactorNames.CREATE_OBSERVABLE_METHOD_NAME));

		MethodInvocation invocation = ast.newMethodInvocation();
		invocation.setExpression(execute);
		invocation.setName(ast.newSimpleName(SUBSCRIBE));
		// ASTUtil.replaceInStatement(methodInvoke, invocation);
		unit.replace(methodInvoke, invocation);
		// singleChangeWriter.replace(methodInvoke, invocation);
	}

	/**
	 * Method to refactor method invocation statements with name execute EX: new
	 * Task().execute();
	 */
//	private void replaceExecuteWithSubscription(IRewriteCompilationUnit unit, MethodInvocation methodInvoke, AST astInvoke) {
//
//
//	
//		createSubscriptionDeclaration(unit, methodInvoke);
//
//		MethodInvocation execute = (MethodInvocation) ASTNode.copySubtree(astInvoke, methodInvoke);
//		execute.setName(astInvoke.newSimpleName(RefactorNames.CREATE_OBSERVABLE_METHOD_NAME));
//		MethodInvocation invocation = astInvoke.newMethodInvocation();
//		invocation.setExpression(execute);
//		invocation.setName(astInvoke.newSimpleName(SUBSCRIBE));
//
//		Assignment initSubscription = astInvoke.newAssignment();
//
//		initSubscription
//				.setLeftHandSide(astInvoke.newSimpleName(getVariableName(methodInvoke.getExpression()) + SUBSCRIPTION));
//		initSubscription.setRightHandSide(invocation);
//		unit.replace(methodInvoke, initSubscription);
//	}

//	private void createSubscriptionDeclaration(IRewriteCompilationUnit unit, MethodInvocation methodInvoke) {
//
//		
//		TypeDeclaration tyDec = ASTUtils.findParent(methodInvoke, TypeDeclaration.class);
//		AST astInvoke = tyDec.getAST();
//
//		VariableDeclarationFragment variable = astInvoke.newVariableDeclarationFragment();
//		Expression e = methodInvoke.getExpression();
//
//		variable.setName(astInvoke.newSimpleName(getVariableName(e) + SUBSCRIPTION));
//
//		FieldDeclaration subscription = astInvoke.newFieldDeclaration(variable);
//		subscription.setType(astInvoke.newSimpleType(astInvoke.newSimpleName(SUBSCRIPTION)));
//		
//		addStatementToClass(unit, subscription, tyDec);
//		unit.addImport("rx.Subscription");
//	}

//	private String getVariableName(Expression e) {
//		if (e instanceof FieldAccess)
//			return ((FieldAccess) e).getName().toString();
//		else
//			return e.toString();
//	}

	/**
	 * Add cancel method if AsyncTask.cacel() method was invoked
	 */
//	private void updateCancelInvocation(IRewriteCompilationUnit unit, MethodInvocation methodInvoke, AST astInvoke) {
//
//		MethodInvocation unSubscribe = astInvoke.newMethodInvocation();
//		unSubscribe
//				.setExpression(astInvoke.newSimpleName(getVariableName(methodInvoke.getExpression()) + SUBSCRIPTION));
//		unSubscribe.setName(astInvoke.newSimpleName(UN_SUBSCRIBE));
//		unit.replace(methodInvoke, unSubscribe);
//
//	}

	private void updateImports(AsyncTaskWrapper asyncTask) {
		
		IRewriteCompilationUnit unit = asyncTask.getUnit();
		
		unit.addImport("rx.android.schedulers.AndroidSchedulers");
		unit.addImport("rx.schedulers.Schedulers");
		
		unit.addImport("rx.Observable");		
		unit.addImport("java.util.concurrent.Callable");

		if (asyncTask.getOnPostExecute() != null) {
			unit.addImport("rx.functions.Action1");
			unit.removeImport("java.util.concurrent.ExecutionException");
			unit.removeImport("java.util.concurrent.TimeoutException");
		}

		if (asyncTask.getDoInBackground() != null) {
			unit.addImport("rx.Subscriber");
			unit.addImport("java.util.Arrays");
			unit.addImport("rx.Subscription");
			unit.addImport("rx.functions.Action1");
		}

		if (asyncTask.getOnProgressUpdate() != null) {
			unit.addImport("java.util.Arrays");
		}

		if (asyncTask.getOnCancelled() != null) {
			unit.addImport("rx.functions.Action0");
		}

		if (asyncTask.getOnPreExecute() != null) {
			unit.addImport("rx.functions.Action0");
		}
	}

	private InnerClassBuilder createLocalObservable(AsyncTaskWrapper asyncTask, TypeDeclaration taskObject) {
		
		//replacePublishInvocations(asyncTask, rewriter);
		
		SubscriberBuilder subscriberBuilder = new SubscriberBuilder(asyncTask);
		InnerClassBuilder innerClassBuilder = new InnerClassBuilder(asyncTask, subscriberBuilder.getId());
		
		TypeDeclaration typeDecl = innerClassBuilder.buildInnerClassWithSubscriber(subscriberBuilder);
		if (asyncTask.hasOnProgressUpdate()) {
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
		
		return innerClassBuilder;
	
	}

	@Override
	public Void refactor(IProjectUnits units, AsyncTaskCollector input, WorkerSummary summary, RefactorScope scope)
			throws Exception {
		// TODO Auto-generated method stub
		// only needed if RefactorScope is implemented in this extension
		return null;
	}
	
//	private void replaceInstanceCreations(IRewriteCompilationUnit unit, InnerClassBuilder builder, TypeDeclaration oldClass, ASTNode root) {
//		
//		
//		final ITypeBinding oldClassType = oldClass.resolveBinding();
//		if (oldClassType == null) {
//			Log.info(getClass(), "Class could not have been resolved to a type: " + oldClass.getName());
//			return;
//		}
//		
//		
//		class ClassInstanceCreationVisitor extends ASTVisitor {
//			
//						
//			@Override 
//			public boolean visit(ClassInstanceCreation node) {
//				
//				ITypeBinding type = node.resolveTypeBinding();
//				
//				if (type == null) return true;
//				
//				//if (ASTUtils.isTypeOf(node.getType(), oldClassType)) {
//				if (type.getQualifiedName().equals(oldClassType.getQualifiedName())) {
//					unit.replace(node, builder.buildNewObservableWrapper());
//				}					
//				
//				return true;
//			}			
//		}
//		
//		
//		//TODO: This only looks for subclasses that have been found until now. 
//		root.accept(new ClassInstanceCreationVisitor()); 
//		
////		for (Entry<BundledCompilationUnit, ASTNode> entry : collector.getRootNodes().entrySet()) {
////			
////			UnitWriter writer = UnitWriters.getOrPut(entry.getKey(), () -> new UnitWriterExt(entry.getKey(), entry.getValue().getAST()));
////			
////			ClassInstanceCreationVisitor v = new ClassInstanceCreationVisitor(writer);
////			entry.getValue().accept(v);
////		}
//}
		
		
		
		
		
	
}


