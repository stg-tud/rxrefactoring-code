package de.tudarmstadt.rxrefactoring.core.codegen;

import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;

public class ReactiveComputation extends AbstractReactiveValue {
	
	
	/**
	 * An expression of type {@link Consumer} that consumes input values.
	 */
	//TODO: Change to Nullable?
	private final @NonNull NodeSupplier<? extends Expression> doWithInput;
	
	/**
	 * An expression of type Scheduler.
	 */
	private final @Nullable NodeSupplier<? extends Expression> scheduler;
	
	
	/**
	 * The input of this computation.	
	 */
	private ReactiveInput input;
	
		
	
	public ReactiveComputation(
			@NonNull ReactiveInput input, 
			@NonNull NodeSupplier<SimpleName> name, 
			@NonNull NodeSupplier<? extends Expression> doWithInput,
			@Nullable NodeSupplier<? extends Expression> scheduler) {
		super(input.type, name);
		this.input = input;
		this.doWithInput = doWithInput;
		this.scheduler = scheduler;		
	}
		
	public @NonNull SimpleName buildName(@NonNull AST ast) {
		return name.apply(ast);
	}
	
	
	public @NonNull FieldDeclaration buildFieldDeclaration(@NonNull AST ast) {
		//initializer = name;
		Expression initializer = input.buildInternalName(ast);
		
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
		MethodInvocation invoke = ast.newMethodInvocation();
		invoke.setName(ast.newSimpleName("doOnNext"));
		invoke.setExpression(initializer);
		invoke.arguments().add(doWithInput.apply(ast));
		
		initializer = invoke;		
		
		
		VariableDeclarationFragment variable = ast.newVariableDeclarationFragment();
		variable.setName(buildName(ast));
		variable.setInitializer(initializer);
		
		FieldDeclaration internalField = ast.newFieldDeclaration(variable);
		internalField.setType(buildFlowableType(ast));
		internalField.modifiers().add(ast.newModifier(ModifierKeyword.PRIVATE_KEYWORD));
		internalField.modifiers().add(ast.newModifier(ModifierKeyword.FINAL_KEYWORD));
		
		return internalField;
	}
	
}
