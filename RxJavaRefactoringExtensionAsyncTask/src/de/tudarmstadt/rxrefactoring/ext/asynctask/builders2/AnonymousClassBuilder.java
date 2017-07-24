package de.tudarmstadt.rxrefactoring.ext.asynctask.builders2;

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import com.google.common.collect.Lists;

import de.tudarmstadt.rxrefactoring.core.utils.ASTUtils;
import de.tudarmstadt.rxrefactoring.core.utils.Log;
import de.tudarmstadt.rxrefactoring.core.writers.UnitWriter;
import de.tudarmstadt.rxrefactoring.ext.asynctask.utils.AsyncTaskASTUtils;
import de.tudarmstadt.rxrefactoring.ext.asynctask.utils.AsyncTaskWrapper;

public class AnonymousClassBuilder extends AbstractBuilder<Expression> {

	
	private AnonymousClassBuilder(AsyncTaskWrapper asyncTask, UnitWriter writer) {		
		super(asyncTask, writer);
	}
	
	
	private void checkInitialized() {
		Objects.requireNonNull(node, "Initialize the observable before adding functionality.");
	}
	
	
	/**
	 * Removes method invocations such as super.onCompleted() and
	 * removes unnecessary catch clauses.
	 * 
	 * @param body The statement block.
	 * @param methodName The super method to look for.
	 * 
	 * @return Returns the same block that has been given as argument.
	 */
	private Block preprocessBlock(Block body, String methodName) {
		
		/*
		 * Remove super calls such as super.onCompleted()
		 */
		final List<SuperMethodInvocation> methods = Lists.newLinkedList();
		
		class SuperVisitor extends ASTVisitor {
			@Override public boolean visit(SuperMethodInvocation node) {
				if (ASTUtils.matchesTargetMethod(node, methodName, "android.os.AsyncTask")) {
					methods.add(node);
				}
				return true;
			}
		}
		
		SuperVisitor v = new SuperVisitor();
		body.accept(v);
		
		for (SuperMethodInvocation methodInvocation : methods) {
			Statement statement = ASTUtils.findParent(methodInvocation, Statement.class);
			astRewrite.remove(statement, null);
		}		
		
		/*
		 * Remove unnecessary catch clauses.
		 */
		ASTUtils.removeUnnecessaryCatchClauses(body);
		
		/*
		 * Replace field references with fully qualified name 
		 */
		AsyncTaskASTUtils.replaceFieldsWithFullyQualifiedNameIn(body, writer);

		
		return body;		
	}
	
	/**
	 * Returns the expression containing the built observable
	 *  created by this builder. Do only use this expression
	 *  when the build is finished.
	 *  
	 * @return The expression created by this builder.
	 */
	@Override public Expression create() {
		return node;
	}
	
	public static Expression from(AsyncTaskWrapper asyncTask, UnitWriter writer) {
//		Block doInBackgroundBlock = asyncTask.getDoInBackgroundBlock();
//		Block doOnCompletedBlock = asyncTask.getOnPostExecuteBlock();

		//String type = (asyncTask.getReturnedType() == null ? "Void" : asyncTask.getReturnedType().toString());
		//String postExecuteParameters = asyncTask.getPostExecuteParameter();

//		if (type == null) {
//			Log.error(getClass(), "NULL type for DoInBackground");
//		}
		
		
		
		//Creates a new observable from a callable
		AnonymousClassBuilder builder = new AnonymousClassBuilder(asyncTask, writer);
				
		//Adds the additional functionality to the observable
		if (asyncTask.getOnPreExecuteBlock() != null) {
			builder.addDoOnSubscribe();
		}		
		if (asyncTask.getOnPostExecuteBlock() != null) {
			builder.addDoOnNext();
		}
		if (asyncTask.getOnCancelled() != null) {
			builder.addDoOnUnsubscribe();
		}
		
//		ObservableBuilder complexObservable = ObservableBuilder
//				.newObservable(asyncTask, writer, type, doInBackgroundBlock, SchedulerType.JAVA_MAIN_THREAD)
//				.addDoOnNext((doOnCompletedBlock == null ? "{}" : doOnCompletedBlock.toString()),
//						postExecuteParameters == null ? "arg" : postExecuteParameters, type, true);
//		
//		Block preExec = asyncTask.getOnPreExecuteBlock();
//		if (preExec != null) {
//			complexObservable.addDoOnPreExecute(preExec);
//		}
//
//		Block onCancelled = asyncTask.getOnCancelled();
//		if (onCancelled != null) {
//			complexObservable.addDoOnCancelled(onCancelled);
//		}
		
		return builder.create();
	}
	
	/*
	 * Builds
	 * 
	 * Observable.fromCallable(new Callable<RETURN_TYPE>() {
	 *     public RETURN_TYPE call() throws Exception {
	 *         DO_IN_BACKGROUND_BLOCK
	 *     }
	 * })
	 */
	@SuppressWarnings("unchecked")
	@Override Expression initial() {
		
		//Define type: Callable
		ParameterizedType tCallable = ast.newParameterizedType(ast.newSimpleType(ast.newSimpleName("Callable"))); //Callable<>
		tCallable.typeArguments().add(copy(returnTypeOrVoid())); //Callable<T>
		
				
		//Define method: call()
		MethodDeclaration callMethod = ast.newMethodDeclaration();
		callMethod.setName(ast.newSimpleName("call"));
		callMethod.setReturnType2(copy(asyncTask.getReturnType()));
		callMethod.thrownExceptionTypes().add(ast.newSimpleType(ast.newSimpleName("Exception")));
		callMethod.modifiers().add(createOverrideAnnotation());
		callMethod.modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
		callMethod.setBody(copy(asyncTask.getDoInBackgroundBlock()));
		
		//Define anonymous class
		AnonymousClassDeclaration classDecl = ast.newAnonymousClassDeclaration();
		classDecl.bodyDeclarations().add(callMethod);
		
		//Define constructor call: new Callable() { ... }
		ClassInstanceCreation initCallable = ast.newClassInstanceCreation();
		initCallable.setType(tCallable);
		initCallable.setAnonymousClassDeclaration(classDecl);
		
		//Define method invoke: Observable.fromCallable(new Callable ...)
		MethodInvocation invoke = ast.newMethodInvocation();
		invoke.setName(ast.newSimpleName("fromCallable"));
		invoke.setExpression(ast.newSimpleName("Observable"));
		invoke.arguments().add(initCallable);
					
		return invoke;
	}
	
	/*
	 * Builds
	 * 
	 * private class MyObservable ... {
	 * 
	 * 
	 * }
	 */
	
	
	/*
	 * Builds
	 * 
	 * observable.methodName(new Action0 () {
	 *     public void call() {
	 *         actionBody
	 *     }
	 * })
	 */
	public AnonymousClassBuilder addDoOnNext() {
		checkInitialized();
	
		
		//Define type: Action1
		ParameterizedType tAction1 = ast.newParameterizedType(ast.newSimpleType(ast.newSimpleName("Action1"))); //Callable<>
		tAction1.typeArguments().add(copy(returnTypeOrVoid()));				
		
		//Define local variable: final 
		SingleVariableDeclaration var = ast.newSingleVariableDeclaration();
		
		//Define method: call()
		MethodDeclaration callMethod = ast.newMethodDeclaration();
		callMethod.setName(ast.newSimpleName("call"));
		callMethod.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));
		callMethod.modifiers().add(createOverrideAnnotation());		
		callMethod.modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
		
		callMethod.parameters().add(copy(asyncTask.getPostExecuteParameter()));		
		
		callMethod.setBody(copy(preprocessBlock(asyncTask.getOnPostExecuteBlock(), "onPostExecute")));
		
		//Define anonymous class
		AnonymousClassDeclaration classDecl = ast.newAnonymousClassDeclaration();
		classDecl.bodyDeclarations().add(callMethod);
		
		//Define constructor call: new Action0() { ... }
		ClassInstanceCreation initAction = ast.newClassInstanceCreation();
		initAction.setType(tAction1);
		initAction.setAnonymousClassDeclaration(classDecl);
		
		//Define method invoke: observableRef.doOnCompleted(new Action0 ...)
		MethodInvocation invoke = ast.newMethodInvocation();
		invoke.setName(ast.newSimpleName("doOnNext"));
		invoke.setExpression(node);
		invoke.arguments().add(initAction);
		
		node = invoke;
		
		return this;
	}
	
	
	/*
	 * Builds
	 * 
	 * observable.doOnCompleted(new Action0 () {
	 *     public void call() {
	 *         POST_EXECUTE_BLOCK
	 *     }
	 * })
	 */
	public AnonymousClassBuilder addDoOnCompleted() {
		checkInitialized();
		
		node = invokeWithAction0(node, "doOnCompleted", preprocessBlock(copy(asyncTask.getOnPostExecuteBlock()), "onPostExecute"));
		return this;
	}
	
	/*
	 * Builds
	 * 
	 * observable.doOnSubscribe(new Action0 () {
	 *     public void call() {
	 *         PRE_EXECUTE_BLOCK
	 *     }
	 * })
	 */
	public AnonymousClassBuilder addDoOnSubscribe() {	
		checkInitialized();
		
		node = invokeWithAction0(node, "doOnSubscribe", preprocessBlock(copy(asyncTask.getOnPreExecuteBlock()), "onPreExecute"));
		return this;
	}
	
	/*
	 * Builds
	 * 
	 * observable.doOnUnsubscribe(new Action0 () {
	 *     public void call() {
	 *         CANCELLED_BLOCK
	 *     }
	 * })
	 */
	public AnonymousClassBuilder addDoOnUnsubscribe() {	
		checkInitialized();

		node = invokeWithAction0(node, "doOnUnsubscribe", preprocessBlock(copy(asyncTask.getOnCancelled()), "onCancelled"));
		return this;
	}
	
	
	/*
	 * Builds
	 * 
	 * expr.methodName(new Action0 () {
	 *     public void call() {
	 *         actionBody
	 *     }
	 * })
	 */
	private Expression invokeWithAction0(Expression expr, String methodName, Block actionBody) {
		//Define type: Action0
		SimpleType tAction0 = ast.newSimpleType(ast.newSimpleName("Action0")); //Action0
						
		
		//Define method: call()
		MethodDeclaration callMethod = ast.newMethodDeclaration();
		callMethod.setName(ast.newSimpleName("call"));
		callMethod.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));
		callMethod.modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
		callMethod.modifiers().add(createOverrideAnnotation());
		callMethod.setBody(actionBody);
		
		//Define anonymous class
		AnonymousClassDeclaration classDecl = ast.newAnonymousClassDeclaration();
		classDecl.bodyDeclarations().add(callMethod);
		
		//Define constructor call: new Action0() { ... }
		ClassInstanceCreation initAction = ast.newClassInstanceCreation();
		initAction.setType(tAction0);
		initAction.setAnonymousClassDeclaration(classDecl);
		
		//Define method invoke: expr.methodName(new Action0 ...)
		MethodInvocation invoke = ast.newMethodInvocation();
		invoke.setName(ast.newSimpleName(methodName));
		invoke.setExpression(expr);
		invoke.arguments().add(initAction);
		
		return invoke;
	}
	
	/*
	 * Builds
	 * 
	 * observableRef.subscribe()
	 */
	public void addSubscribe() {
		checkInitialized();

		//Define method invoke: observableRef.subscribe()
		MethodInvocation invoke = ast.newMethodInvocation();
		invoke.setName(ast.newSimpleName("subscribe"));
		invoke.setExpression(node);
		
		node = invoke;
	}
	
	
	

	
	
	
	
	
}


