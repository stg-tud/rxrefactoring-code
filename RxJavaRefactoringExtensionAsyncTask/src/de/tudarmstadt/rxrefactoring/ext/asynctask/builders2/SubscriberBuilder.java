package de.tudarmstadt.rxrefactoring.ext.asynctask.builders2;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
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
import de.tudarmstadt.rxrefactoring.core.writers.UnitWriter;
import de.tudarmstadt.rxrefactoring.ext.asynctask.utils.AsyncTaskWrapper;

public class SubscriberBuilder extends AbstractBuilder<MethodDeclaration> {

	private static final String FIELD_NAME = "rxUpdateSubscriber";
	private static final String METHOD_NAME = "getRxUpdateSubscriber";
	
	private final String id;

	public SubscriberBuilder(AsyncTaskWrapper asyncTask, UnitWriter writer) {
		super(asyncTask, writer);
		
		id = IdManager.getNextObserverId(writer.getUnit().getElementName());
	}

	@Override
	public MethodDeclaration create() {
		return node;
	}

	public static MethodDeclaration from(AsyncTaskWrapper asyncTask, UnitWriter writer) {
		SubscriberBuilder builder = new SubscriberBuilder(asyncTask, writer);
		return builder.create();
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
	@Override MethodDeclaration initial() {		
		
		//Define type: Subscriber
		Type subscriberType = getSubscriberType();
				
		
		//Define variable: onNextParameter
		SingleVariableDeclaration onNextParameter = ast.newSingleVariableDeclaration();
		onNextParameter.setName(copy(asyncTask.getProgressParameter().getName()));
		onNextParameter.setType(getParameterType());
		
		//Define method: onNext()
		MethodDeclaration onNextMethod = ast.newMethodDeclaration();
		onNextMethod.setName(ast.newSimpleName("onNext"));
		onNextMethod.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));
		
		onNextMethod.parameters().add(onNextParameter);
		onNextMethod.modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
		onNextMethod.modifiers().add(createOverrideAnnotation());
		onNextMethod.setBody(copy(asyncTask.getOnProgressUpdateBlock()));
		
		
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
		constructorCall.setType(subscriberType);
		constructorCall.setAnonymousClassDeclaration(subscriberDeclaration);
		
		//Define method: getRxUpdateSubscriber
		MethodDeclaration getSubscriber = ast.newMethodDeclaration();
		getSubscriber.setName(ast.newSimpleName(getMethodName()));
		getSubscriber.setReturnType2(subscriberType);
		
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
	public Statement getSubscriberDeclaration() {
		
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
	
	
		
	public MethodInvocation getSubscriberPublish(List<Expression> arguments) {
		
		//Define array creation: new T[] { arguments, ... } 
		ArrayCreation array = ast.newArrayCreation();
		array.setType(getParameterType());
		
		ArrayInitializer arrayInit = ast.newArrayInitializer();
		arrayInit.expressions().addAll(arguments);
		
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
	public ParameterizedType getSubscriberType() {
		ParameterizedType subscriberType = ast.newParameterizedType(ast.newSimpleType(ast.newSimpleName("Subscriber"))); //Subscriber<>
		subscriberType.typeArguments().add(getParameterType());
		
		return subscriberType;
	}
	
	public ArrayType getParameterType() {
		return ast.newArrayType(copy(asyncTask.getProgressParameter().getType()));
	}
	
	public String getFieldName() {
		return FIELD_NAME + getId();
	}
	
	public String getMethodName() {
		return METHOD_NAME + getId();
	}
	
	public String getId() {
		return id;
	}
}
