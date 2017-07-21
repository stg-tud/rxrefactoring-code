package de.tudarmstadt.rxrefactoring.ext.asynctask.builders;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import de.tudarmstadt.rxrefactoring.core.utils.Log;
import de.tudarmstadt.rxrefactoring.core.writers.UnitWriter;
import de.tudarmstadt.rxrefactoring.ext.asynctask.utils.AsyncTaskWrapper;

public class FromCallableBuilder {

	private final AsyncTaskWrapper asyncTask;
	private final UnitWriter writer;
	
	private final AST ast;
	private final ASTRewrite astRewrite;

	
	public FromCallableBuilder(AsyncTaskWrapper asyncTask, UnitWriter writer) {
		
		this.asyncTask = asyncTask;
		this.writer = writer;		
		this.ast = writer.getAST();
		this.astRewrite = writer.getAstRewriter();		
		
	}
	
	
	public Expression build() {
		
		Log.info(getClass(), "Initiate FromCallableBuilder...");

		// Observable.fromCallable(...)
		Expression observable = createObservable();
		
		if (asyncTask.getOnPostExecuteBlock() != null) {
			observable = addDoOnCompleted(observable);
		}
		
		
		Log.info(getClass(), "Build result: " + observable);

		return observable;
	}
	
	@SuppressWarnings("unchecked")
	private <T extends ASTNode> T copy(T node) {
		return (T) astRewrite.createCopyTarget(node);
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
	public Expression createObservable() {
		
		//Define type: Callable
		ParameterizedType tCallable = ast.newParameterizedType(ast.newSimpleType(ast.newSimpleName("Callable"))); //Callable<>
		tCallable.typeArguments().add(copy(returnTypeOrVoid())); //Callable<T>
		
				
		//Define method: call()
		MethodDeclaration callMethod = ast.newMethodDeclaration();
		callMethod.setName(ast.newSimpleName("call"));
		callMethod.setReturnType2(copy(asyncTask.getReturnedType()));
		callMethod.thrownExceptionTypes().add(ast.newSimpleType(ast.newSimpleName("Exception")));
		callMethod.modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
		callMethod.modifiers().add(createOverrideAnnotation());
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
	 * expr.methodName(new Action0 () {
	 *     public void call() {
	 *         actionBody
	 *     }
	 * })
	 */
	public Expression addDoOnNext(Expression observableRef) {
		//Define type: Action1
		ParameterizedType tAction1 = ast.newParameterizedType(ast.newSimpleType(ast.newSimpleName("Action1"))); //Callable<>
		tAction1.typeArguments().add(copy(returnTypeOrVoid()));				
		
		//Define local variable: final 
		SingleVariableDeclaration var = ast.newSingleVariableDeclaration();
		
		//Define method: call()
		MethodDeclaration callMethod = ast.newMethodDeclaration();
		callMethod.setName(ast.newSimpleName("call"));
		callMethod.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));
		callMethod.modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
		callMethod.modifiers().add(createOverrideAnnotation());
		callMethod.parameters().add(copy(asyncTask.getPostExecuteParameter()));
		callMethod.setBody(copy(asyncTask.getOnPostExecuteBlock()));
		
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
		invoke.setExpression(observableRef);
		invoke.arguments().add(initAction);
		
		return invoke;
	}
	
	
	/*
	 * Builds
	 * 
	 * observableRef.doOnCompleted(new Action0 () {
	 *     public void call() {
	 *         POST_EXECUTE_BLOCK
	 *     }
	 * })
	 */
	public Expression addDoOnCompleted(Expression observableRef) {		
		return invokeWithAction0(observableRef, "doOnCompleted", copy(asyncTask.getOnPostExecuteBlock()));
	}
	
	/*
	 * Builds
	 * 
	 * observableRef.doOnSubscribe(new Action0 () {
	 *     public void call() {
	 *         PRE_EXECUTE_BLOCK
	 *     }
	 * })
	 */
	public Expression addDoOnSubscribe(Expression observableRef) {	
		return invokeWithAction0(observableRef, "doOnSubscribe", copy(asyncTask.getOnPreExecuteBlock()));
	}
	
	/*
	 * Builds
	 * 
	 * observableRef.doOnUnsubscribe(new Action0 () {
	 *     public void call() {
	 *         CANCELLED_BLOCK
	 *     }
	 * })
	 */
	public Expression addDoOnUnsubscribe(Expression observableRef) {	
		return invokeWithAction0(observableRef, "doOnUnsubscribe", copy(asyncTask.getOnCancelled()));
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
		SimpleType tAction0 = ast.newSimpleType(ast.newSimpleName("Action0")); //Callable<>
						
		
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
		
		//Define method invoke: observableRef.doOnCompleted(new Action0 ...)
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
	public Expression addSubscribe(Expression observableRef) {
		//Define method invoke: observableRef.subscribe()
		MethodInvocation invoke = ast.newMethodInvocation();
		invoke.setName(ast.newSimpleName("subscribe"));
		invoke.setExpression(observableRef);
		
		return invoke;
	}
	
	
	/**
	 * Produces an Override annotation AST Node.
	 * 
	 * @return An unparented Override annotation.
	 */
	private Annotation createOverrideAnnotation() {
		MarkerAnnotation annotation = ast.newMarkerAnnotation();
		annotation.setTypeName(ast.newSimpleName("Override"));
		
		return annotation;
	}
	
	private Type returnTypeOrVoid() {
		Type t = asyncTask.getReturnedType(); 
		
		if (t == null)
			return ast.newPrimitiveType(PrimitiveType.VOID);
		else
			return t;
	}

	
	
	
	
	
}


