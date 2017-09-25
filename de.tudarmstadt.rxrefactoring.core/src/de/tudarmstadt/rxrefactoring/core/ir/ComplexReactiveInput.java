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


public class ComplexReactiveInput implements IReactiveInput {

	private final @NonNull NodeSupplier<? extends Type> type;
	
	private final @NonNull NodeSupplier<SimpleName> externalName;
	private final @NonNull NodeSupplier<SimpleName> internalName;
	
	/**
	 * An expression that returns a scheduler.
	 */
	private final @Nullable NodeSupplier<? extends Expression> scheduler;
	
	/**
	 * An expression that returns a {@link Consumer} which consumes elements of the input type.
	 */
	private final @Nullable NodeSupplier<? extends Expression> doWithInput;
	
		
	public ComplexReactiveInput(
			@NonNull NodeSupplier<? extends Type> type, 
			@NonNull NodeSupplier<SimpleName> name,
			@Nullable NodeSupplier<? extends Expression> scheduler,
			@Nullable NodeSupplier<? extends Expression> doWithInput) {
		this.type = type;
		
		this.externalName = name;
		this.internalName = name.map((unit, n) -> unit.getAST().newSimpleName("__" + n.getIdentifier()));
		
		this.scheduler = scheduler;
		this.doWithInput = doWithInput;		
	}
	

	@Override
	public @NonNull NodeSupplier<? extends Type> supplyType() {
		return type;
	}

	@Override
	public @NonNull NodeSupplier<SimpleName> supplyExternalName() {
		return externalName;
	}

	public @NonNull NodeSupplier<SimpleName> supplyInternalName() {
		return internalName;
	}
	
	@SuppressWarnings("unchecked")
	public @NonNull NodeSupplier<FieldDeclaration> supplyExternalField() {
		return unit -> {
			AST ast = unit.getAST();
			
			MethodInvocation createPublishProcessor = ast.newMethodInvocation();
			createPublishProcessor.setName(ast.newSimpleName("create"));
			createPublishProcessor.setExpression(ast.newSimpleName("PublishProcessor"));
			
			VariableDeclarationFragment variable = ast.newVariableDeclarationFragment();
			variable.setName(supplyExternalName().apply(unit));
			variable.setInitializer(createPublishProcessor);
			
			FieldDeclaration inputField = ast.newFieldDeclaration(variable);
			inputField.setType(supplyPublishProcessorType().apply(unit));
			inputField.modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
			inputField.modifiers().add(ast.newModifier(ModifierKeyword.FINAL_KEYWORD));
			
			return inputField;
		};
	}
	
	@SuppressWarnings("unchecked")
	public @NonNull NodeSupplier<FieldDeclaration> supplyInternalField() {
		
		return unit -> {
			AST ast = unit.getAST();
			
			//initializer = name;
			Expression initializer = supplyExternalName().apply(unit);
			
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
			if (doWithInput != null) {
				MethodInvocation invoke = ast.newMethodInvocation();
				invoke.setName(ast.newSimpleName("doOnNext"));
				invoke.setExpression(initializer);
				invoke.arguments().add(doWithInput.apply(unit));
				
				initializer = invoke;
			}
			
			
			VariableDeclarationFragment variable = ast.newVariableDeclarationFragment();
			variable.setName(supplyInternalName().apply(unit));
			variable.setInitializer(initializer);
			
			FieldDeclaration inputField = ast.newFieldDeclaration(variable);
			inputField.setType(supplyFlowableType().apply(unit));
			inputField.modifiers().add(ast.newModifier(ModifierKeyword.PRIVATE_KEYWORD));
			inputField.modifiers().add(ast.newModifier(ModifierKeyword.FINAL_KEYWORD));
			
			
			return inputField;
		};
	}
	
	@Override
	public void addToTypeDeclaration(@NonNull RewriteCompilationUnit unit, @NonNull List bodyDeclarations) {	
		bodyDeclarations.add(supplyExternalField().apply(unit));
		bodyDeclarations.add(supplyInternalField().apply(unit));
	}

	
	
	

}
