package de.tudarmstadt.rxrefactoring.core.codegen;

import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class ReactiveInput extends AbstractReactiveValue {

	private final @NonNull NodeSupplier<SimpleName> internalName;

	
	/**
	 * An expression that returns a scheduler.
	 */
	private @Nullable NodeSupplier<Expression> scheduler;
	
	/**
	 * An expression that returns a {@link Consumer} which consumes elements of the input type.
	 */
	private @Nullable NodeSupplier<Expression> doWithInput;
	
		
	public ReactiveInput(@NonNull NodeSupplier<? extends Type> type, @NonNull NodeSupplier<SimpleName> name) {
		super(type, name);
		internalName = name.map((ast, n) -> ast.newSimpleName("__" + n.getIdentifier()));
		
	}
		
	

	public @NonNull SimpleName buildInternalName(@NonNull AST ast) {
		return internalName.apply(ast);
	}
	
	public void setScheduler(@NonNull NodeSupplier<Expression> scheduler) {
		this.scheduler = scheduler;
	}

	public @NonNull FieldDeclaration buildExternalField(@NonNull AST ast) {
		MethodInvocation createPublishProcessor = ast.newMethodInvocation();
		createPublishProcessor.setName(ast.newSimpleName("create"));
		createPublishProcessor.setExpression(ast.newSimpleName("PublishProcessor"));
		
		VariableDeclarationFragment variable = ast.newVariableDeclarationFragment();
		variable.setName(buildName(ast));
		variable.setInitializer(createPublishProcessor);
		
		FieldDeclaration inputField = ast.newFieldDeclaration(variable);
		inputField.setType(buildFlowableType(ast));
		inputField.modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
		inputField.modifiers().add(ast.newModifier(ModifierKeyword.FINAL_KEYWORD));
		
		return inputField;
	}
	
	public @NonNull FieldDeclaration buildInternalField(@NonNull AST ast) {
		
		//initializer = name;
		Expression initializer = buildName(ast);
		
		//If there is a scheduler...
		//initializer.observeOn(scheduler)
		if (scheduler != null) {
			MethodInvocation invoke = ast.newMethodInvocation();
			invoke.setName(ast.newSimpleName("observeOn"));
			invoke.setExpression(initializer);
			invoke.arguments().add(scheduler.apply(ast));
			
			initializer = invoke;
		}
		
		//If there is an action defined on the input
		//initializer.doOnNext(doWithInput)
		if (doWithInput != null) {
			MethodInvocation invoke = ast.newMethodInvocation();
			invoke.setName(ast.newSimpleName("doOnNext"));
			invoke.setExpression(initializer);
			invoke.arguments().add(doWithInput.apply(ast));
			
			initializer = invoke;
		}
		
		
		VariableDeclarationFragment variable = ast.newVariableDeclarationFragment();
		variable.setName(buildInternalName(ast));
		variable.setInitializer(initializer);
		
		FieldDeclaration inputField = ast.newFieldDeclaration(variable);
		inputField.setType(buildFlowableType(ast));
		inputField.modifiers().add(ast.newModifier(ModifierKeyword.PRIVATE_KEYWORD));
		inputField.modifiers().add(ast.newModifier(ModifierKeyword.FINAL_KEYWORD));
		
		return inputField;
	}
	
	public void addToTypeDeclaration(@NonNull AST ast, @NonNull TypeDeclaration type) {
		type.bodyDeclarations().add(buildExternalField(ast));
		type.bodyDeclarations().add(buildInternalField(ast));
	}
	
	

}
