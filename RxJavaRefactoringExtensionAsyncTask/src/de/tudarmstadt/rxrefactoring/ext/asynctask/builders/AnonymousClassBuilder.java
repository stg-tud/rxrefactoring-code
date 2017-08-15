package de.tudarmstadt.rxrefactoring.ext.asynctask.builders;

import java.util.Objects;

import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleType;

import de.tudarmstadt.rxrefactoring.ext.asynctask.utils.AsyncTaskWrapper;

public class AnonymousClassBuilder extends AbstractBuilder {

	private Expression node;
	/**
	 * The name of the (new) superclass that contains
	 * the anonymous class.
	 * The name is used to correctly identify this
	 * expressions.
	 * Can be null, if there is no superclass. 
	 */
	private String superClassName;
	
	public AnonymousClassBuilder(AsyncTaskWrapper asyncTask, String superClassName) {		
		super(asyncTask);
		
		this.superClassName = superClassName;
		
		/*
		 * Builds
		 * 
		 * Observable.fromCallable(new Callable<RETURN_TYPE>() {
		 *     public RETURN_TYPE call() throws Exception {
		 *         DO_IN_BACKGROUND_BLOCK
		 *     }
		 * })
		 */
		//Define type: Callable
		ParameterizedType tCallable = ast.newParameterizedType(ast.newSimpleType(ast.newSimpleName("Callable"))); //Callable<>
		tCallable.typeArguments().add(unit.copyNode(asyncTask.getResultTypeOrVoid())); //Callable<T>
		
				
		//Define method: call()
		MethodDeclaration callMethod = ast.newMethodDeclaration();
		callMethod.setName(ast.newSimpleName("call"));
		callMethod.setReturnType2(unit.copyNode(asyncTask.getResultType()));
		callMethod.thrownExceptionTypes().add(ast.newSimpleType(ast.newSimpleName("Exception")));
		callMethod.modifiers().add(createOverrideAnnotation());
		callMethod.modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
		
		if (asyncTask.getDoInBackground() != null) {
			callMethod.setBody(unit.copyNode(preprocessBlock(asyncTask.getDoInBackground().getBody(), "doInBackground", superClassName, false)));
		}
		
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
		
		node = invoke;
	}
	
		
	
	
	/*
	 * Builds
	 * 
	 * Observable.fromCallable(new Callable<RETURN_TYPE>() {
	 *     public RETURN_TYPE call() throws Exception {
	 *         DO_IN_BACKGROUND_BLOCK
	 *     }
	 * }).doOnNext( ... ) ...
	 */
	/**
	 * Returns the expression containing the built observable
	 * created by this builder. Do only use this expression
	 * when the build is finished.
	 *  
	 * @return The expression created by this builder.
	 */
	public Expression create() {		
		addRelevantMethods();		
		return getExpression();
	}
	
	public static Expression from(AsyncTaskWrapper asyncTask, String superClassName) {
		AnonymousClassBuilder builder = new AnonymousClassBuilder(asyncTask, superClassName);
		return builder.create();
	}
	
	public void addRelevantMethods() {
		addSubscribeOnComputation();
		addObserveOnMain();
		
		//Adds the additional functionality to the observable
		if (asyncTask.getOnPreExecute() != null) {
			addDoOnSubscribe();
		}		
		if (asyncTask.getOnPostExecute() != null) {
			addDoOnNext();
		}
		if (asyncTask.getOnCancelled() != null) {
			addDoOnUnsubscribe();
		}
	}
	
	public Expression getExpression() {
		return node;
	}
	
	/*
	 * Builds
	 * 
	 * observable.doOnNext(new Action0 () {
	 *     public void call() {
	 *         actionBody
	 *     }
	 * })
	 */
	@SuppressWarnings("unchecked")
	public AnonymousClassBuilder addDoOnNext() {
	
		
		//Define type: Action1
		ParameterizedType tAction1 = ast.newParameterizedType(ast.newSimpleType(ast.newSimpleName("Action1"))); //Callable<>
		tAction1.typeArguments().add(unit.copyNode(asyncTask.getResultTypeOrVoid()));				
		
				
		//Define method: call()
		MethodDeclaration callMethod = ast.newMethodDeclaration();
		callMethod.setName(ast.newSimpleName("call"));
		callMethod.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));
		callMethod.modifiers().add(createOverrideAnnotation());		
		callMethod.modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));		
		callMethod.parameters().add(unit.copyNode(asyncTask.getOnPostExecuteParameter()));		
		
		if (asyncTask.getOnPostExecute() != null)
			callMethod.setBody(unit.copyNode(preprocessBlock(asyncTask.getOnPostExecute().getBody(), "onPostExecute", superClassName, true)));
		
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
//	public AnonymousClassBuilder addDoOnCompleted() {
//		checkInitialized();
//		
//		node = invokeWithAction0(node, "doOnCompleted", preprocessBlock(unit.copyNode(asyncTask.getOnPostExecuteBlock().getBody()), "onPostExecute"));
//		return this;
//	}
	
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
		Objects.requireNonNull(asyncTask.getOnPreExecute(), "The AsyncTask needs to have an onPreExecute method in order to add an doOnSubscribe.");
		
		node = invokeWithAction0(node, "doOnSubscribe", preprocessBlock(unit.copyNode(asyncTask.getOnPreExecute().getBody()), "onPreExecute", superClassName, true));
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
		Objects.requireNonNull(asyncTask.getOnCancelled(), "The AsyncTask needs to have an onCancelled method in order to add an doOnUnsubscribe.");
		
		node = invokeWithAction0(node, "doOnUnsubscribe", preprocessBlock(unit.copyNode(asyncTask.getOnCancelled().getBody()), "onCancelled", superClassName, true));
		return this;
	}
	
	public AnonymousClassBuilder addSubscribeOnComputation() {
			
		MethodInvocation invokeScheduler = ast.newMethodInvocation();
		invokeScheduler.setName(ast.newSimpleName("computation"));
		invokeScheduler.setExpression(ast.newSimpleName("Schedulers"));
		
		MethodInvocation invokeSubscribeOn = ast.newMethodInvocation();
		invokeSubscribeOn.setName(ast.newSimpleName("subscribeOn"));
		invokeSubscribeOn.setExpression(node);
		invokeSubscribeOn.arguments().add(invokeScheduler);
		
		node = invokeSubscribeOn;
		
		return this;
	}
	
	public AnonymousClassBuilder addObserveOnMain() {
		
		MethodInvocation invokeScheduler = ast.newMethodInvocation();
		invokeScheduler.setName(ast.newSimpleName("mainThread"));
		invokeScheduler.setExpression(ast.newSimpleName("AndroidSchedulers"));
		
		MethodInvocation invokeSubscribeOn = ast.newMethodInvocation();
		invokeSubscribeOn.setName(ast.newSimpleName("observeOn"));
		invokeSubscribeOn.setExpression(node);
		invokeSubscribeOn.arguments().add(invokeScheduler);
		
		node = invokeSubscribeOn;
		
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
		callMethod.modifiers().add(createOverrideAnnotation());
		callMethod.modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));		
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
		

		//Define method invoke: observableRef.subscribe()
		MethodInvocation invoke = ast.newMethodInvocation();
		invoke.setName(ast.newSimpleName("subscribe"));
		invoke.setExpression(node);
		
		node = invoke;
	}
	
	
	

	
	
	
	
	
}


