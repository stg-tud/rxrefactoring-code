package de.tudarmstadt.rxrefactoring.core.ir;

import java.util.List;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import de.tudarmstadt.rxrefactoring.core.RewriteCompilationUnit;

public class ReactiveComputation implements IReactiveComputation {
	
	private final @NonNull NodeSupplier<SimpleName> internalName;
	
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
	private IReactiveInput input;
	
		
	
	public ReactiveComputation(
			@NonNull IReactiveInput input, 
			@NonNull NodeSupplier<SimpleName> internalName, 
			@NonNull NodeSupplier<? extends Expression> doWithInput,
			@Nullable NodeSupplier<? extends Expression> scheduler) {
		this.internalName = internalName;
		this.input = input;
		this.doWithInput = doWithInput;
		this.scheduler = scheduler;		
	}
	
	@SuppressWarnings("unchecked")
	public @NonNull NodeSupplier<FieldDeclaration> supplyFieldDeclaration() {
		return unit -> {
			AST ast = unit.getAST();
			
			//initializer = name;
			Expression initializer = input.supplyInternalName().apply(unit);
			
			//If there is a scheduler...
			//initializer.observeOn(scheduler)
			if (scheduler != null) {
				MethodInvocation invoke = ast.newMethodInvocation();
				invoke.setName(ast.newSimpleName("observeOn"));
				invoke.setExpression(initializer);
				invoke.arguments().add(scheduler.apply(unit));
				
				initializer = invoke;
			}
			
			//If there is an action defined on the input
			//initializer.doOnNext(doWithInput)		
			MethodInvocation invoke = ast.newMethodInvocation();
			invoke.setName(ast.newSimpleName("doOnNext"));
			invoke.setExpression(initializer);
			
			invoke.arguments().add((Expression) doWithInput.apply(unit));
			
			initializer = invoke;		
			
			
			VariableDeclarationFragment variable = ast.newVariableDeclarationFragment();
			variable.setName(supplyInternalName().apply(unit));
			variable.setInitializer(initializer);
			
			FieldDeclaration internalField = ast.newFieldDeclaration(variable);
			internalField.setType(supplyFlowableType().apply(unit));
			internalField.modifiers().add(ast.newModifier(ModifierKeyword.PRIVATE_KEYWORD));
			internalField.modifiers().add(ast.newModifier(ModifierKeyword.FINAL_KEYWORD));
			
			return internalField;
		};
	}

	@Override
	public @NonNull NodeSupplier<? extends Type> supplyType() {
		return input.supplyType();
	}

	@Override
	public @NonNull NodeSupplier<SimpleName> supplyInternalName() {
		return internalName;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addToTypeDeclaration(@NonNull RewriteCompilationUnit unit, @NonNull List bodyDeclarations) {
		NodeSupplier<FieldDeclaration> fieldDeclaration = supplyFieldDeclaration();	
		FieldDeclaration field = fieldDeclaration.apply(unit);
		bodyDeclarations.add(field);		
	}
	
}
