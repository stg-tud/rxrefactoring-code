package de.tudarmstadt.rxrefactoring.ext.asynctask.builders;

import java.util.List;

import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import de.tudarmstadt.rxrefactoring.core.codegen.IdManager;
import de.tudarmstadt.rxrefactoring.ext.asynctask.utils.AsyncTaskWrapper;

public class SubscriberBuilder extends AbstractBuilder {

	
	
	private final String id;

	public SubscriberBuilder(AsyncTaskWrapper asyncTask) {
		this(asyncTask, IdManager.getNextObserverId(asyncTask.getUnit().getElementName()));			
	}
	
	public SubscriberBuilder(AsyncTaskWrapper asyncTask, String id) {
		super(asyncTask);		
		this.id = id;		
	}

	/*
	 * Builds
	 * 
	 * private Subscriber<T> getRxUpdateSubscriber() {
	 *     return new Subscriber<T>() {
	 *         public onNext() {
	 *             PROGRESS_UPDATE_BLOCK
	 *         }
	 *         
	 *         public onCompleted() { }
	 *         
	 *         public onError(Throwable throwable) { }
	 *     }
	 * }
	 */
	@SuppressWarnings("unchecked")
	public MethodDeclaration buildGetSubscriber() {
		
		
		//Define variable: onNextParameter
		SingleVariableDeclaration onNextParameter = ast.newSingleVariableDeclaration();
		onNextParameter.setName(unit.copyNode(asyncTask.getProgressParameter().getName()));
		onNextParameter.setType(getParameterType());
		
		//Define method: onNext()
		MethodDeclaration onNextMethod = ast.newMethodDeclaration();
		onNextMethod.setName(ast.newSimpleName("onNext"));
		onNextMethod.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));
		
		onNextMethod.parameters().add(onNextParameter);
		onNextMethod.modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
		onNextMethod.modifiers().add(createOverrideAnnotation());
		onNextMethod.setBody(unit.copyNode(asyncTask.getOnProgressUpdateBlock()));
		
		
		//Define method: onCompleted
		MethodDeclaration onCompletedMethod = ast.newMethodDeclaration();
		onCompletedMethod.setName(ast.newSimpleName("onCompleted"));
		onCompletedMethod.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));		
		onCompletedMethod.modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
		onCompletedMethod.modifiers().add(createOverrideAnnotation());
		onCompletedMethod.setBody(null);
		
		
		//Define variable: onErrorParameter
		SingleVariableDeclaration onErrorParameter = ast.newSingleVariableDeclaration();
		onErrorParameter.setName(ast.newSimpleName("throwable"));
		onErrorParameter.setType(ast.newSimpleType(ast.newSimpleName("Throwable")));
		
		//Define method: onError
		MethodDeclaration onErrorMethod = ast.newMethodDeclaration();
		onErrorMethod.setName(ast.newSimpleName("onError"));
		onErrorMethod.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));
		
		onErrorMethod.parameters().add(onErrorParameter);
		onErrorMethod.modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
		onErrorMethod.modifiers().add(createOverrideAnnotation());
		onErrorMethod.setBody(null);
		
		
		//Define anonymous class
		AnonymousClassDeclaration subscriberDeclaration = ast.newAnonymousClassDeclaration();
		subscriberDeclaration.bodyDeclarations().add(onNextMethod);
		
		//Define constructor call: new Subscriber() { ... }
		ClassInstanceCreation constructorCall = ast.newClassInstanceCreation();
		constructorCall.setType(getSubscriberType());
		constructorCall.setAnonymousClassDeclaration(subscriberDeclaration);
		
		//Define method: getRxUpdateSubscriber
		MethodDeclaration getSubscriber = ast.newMethodDeclaration();
		getSubscriber.setName(ast.newSimpleName(getMethodName()));
		getSubscriber.setReturnType2(getSubscriberType());
		
		Block getSubscriberBlock = ast.newBlock();
		getSubscriberBlock.statements().add(ast.newExpressionStatement(constructorCall));
		
		getSubscriber.modifiers().add(ast.newModifier(ModifierKeyword.PRIVATE_KEYWORD));
		getSubscriber.setBody(getSubscriberBlock);
				
				
		return getSubscriber;
	}
	
	/*
	 * Builds
	 * 
	 * final Subscriber<T> rxUpdateSubscriber = getRxUpdateSubscriber();
	 */
	@SuppressWarnings("unchecked")
	public Statement buildSubscriberDeclaration() {
		
		Assignment assignment = ast.newAssignment();		
		
		VariableDeclarationFragment variable = ast.newVariableDeclarationFragment();
		variable.setName(ast.newSimpleName(getFieldName()));
		
		VariableDeclarationExpression expr = ast.newVariableDeclarationExpression(variable);
		expr.setType(getSubscriberType());
		expr.modifiers().add(ast.newModifier(ModifierKeyword.FINAL_KEYWORD));
		
		assignment.setLeftHandSide(expr);
		
		MethodInvocation invocation = ast.newMethodInvocation();
		invocation.setName(ast.newSimpleName(getMethodName()));
		
		assignment.setRightHandSide(invocation);
		
		return ast.newExpressionStatement(assignment);
	}
	
	
	/*
	 * Builds:
	 * 
	 * rxUpdateSubscriber.onNext(new T[] {EXPR, ...});	
	 */
	@SuppressWarnings("unchecked")
	public MethodInvocation getSubscriberPublish(List<Expression> arguments) {
		
		//Define array creation: new T[] { arguments, ... } 
		ArrayCreation array = ast.newArrayCreation();
		array.setType(getParameterType());
		
		ArrayInitializer arrayInit = ast.newArrayInitializer();
		arguments.forEach(argument -> arrayInit.expressions().add(unit.copyNode(argument)));
		
		array.setInitializer(arrayInit);
		
		//Define method invocation: rxUpdateSubscriber.onNext(ARRAY)
		MethodInvocation method = ast.newMethodInvocation();
		method.setName(ast.newSimpleName("onNext"));
		method.setExpression(ast.newSimpleName(getFieldName()));
		method.arguments().add(array);
		
		return method;
	}
		
	/*
	 * Builds type: Subscriber<PARAMETER_TYPE>
	 */
	@SuppressWarnings("unchecked")
	public ParameterizedType getSubscriberType() {
		ParameterizedType subscriberType = ast.newParameterizedType(ast.newSimpleType(ast.newSimpleName("Subscriber"))); //Subscriber<>
		subscriberType.typeArguments().add(getParameterType());
		
		return subscriberType;
	}
	
	public ArrayType getParameterType() {
		return ast.newArrayType(unit.copyNode(asyncTask.getProgressParameter().getType()));
	}
	
	public String getFieldName() {
		return RefactorNames.SUBSCRIBER_FIELD_NAME + getId();
	}
	
	public String getMethodName() {
		return RefactorNames.CREATE_SUBSCRIBER_METHOD_NAME + getId();
	}
	
	public String getId() {
		return id;
	}
}
