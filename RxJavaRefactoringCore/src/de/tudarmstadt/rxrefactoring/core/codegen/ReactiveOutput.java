package de.tudarmstadt.rxrefactoring.core.codegen;

import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;

public class ReactiveOutput extends AbstractReactiveValue {

	/**
	 * The internal name used to refer to this output.
	 */
	private final @NonNull NodeSupplier<SimpleName> internalName;
	
	/**
	 * An expression that returns a scheduler.
	 */
	private final @Nullable NodeSupplier<? extends Expression> scheduler;
	
	/**
	 * An expression that returns a {@link Consumer} which consumes elements of the input type.
	 */
	private final @Nullable NodeSupplier<? extends Expression> doWithOutput;
	
	
	public ReactiveOutput(
			@NonNull NodeSupplier<? extends Type> type, 
			@NonNull NodeSupplier<SimpleName> name,
			@Nullable NodeSupplier<? extends Expression> scheduler,
			@Nullable NodeSupplier<? extends Expression> doWithOutput) {
		super(type, name);
		this.scheduler = scheduler;
		this.doWithOutput = doWithOutput;
		internalName = name.map((ast, n) -> ast.newSimpleName("__" + n.getIdentifier()));
	}
	
	
	public NodeSupplier<SimpleName> supplyInternalName() {
		return internalName;
	}

	public @NonNull NodeSupplier<FieldDeclaration> supplyInternalField() {
		return ast -> {
			MethodInvocation createPublishProcessor = ast.newMethodInvocation();
			createPublishProcessor.setName(ast.newSimpleName("create"));
			createPublishProcessor.setExpression(ast.newSimpleName("PublishProcessor"));
			
			VariableDeclarationFragment variable = ast.newVariableDeclarationFragment();
			variable.setName(supplyInternalName().apply(ast));
			variable.setInitializer(createPublishProcessor);
			
			FieldDeclaration inputField = ast.newFieldDeclaration(variable);
			inputField.setType(supplyFlowableType().apply(ast));
			inputField.modifiers().add(ast.newModifier(ModifierKeyword.PRIVATE_KEYWORD));
			inputField.modifiers().add(ast.newModifier(ModifierKeyword.FINAL_KEYWORD));
			
			return inputField;
		};
	}
	
	@SuppressWarnings("unchecked")
	public @NonNull NodeSupplier<FieldDeclaration> supplyExternalField() {
		
		return ast -> { 
			//initializer = name;
			Expression initializer = supplyInternalName().apply(ast);
			
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
			if (doWithOutput != null) {
				MethodInvocation invoke = ast.newMethodInvocation();
				invoke.setName(ast.newSimpleName("doOnNext"));
				invoke.setExpression(initializer);
				invoke.arguments().add(doWithOutput.apply(ast));
				
				initializer = invoke;
			}
			
			
			
			VariableDeclarationFragment variable = ast.newVariableDeclarationFragment();
			variable.setName(supplyName().apply(ast));
			variable.setInitializer(initializer);
			
			FieldDeclaration outputField = ast.newFieldDeclaration(variable);
			outputField.setType(supplyFlowableType().apply(ast));
			outputField.modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
			outputField.modifiers().add(ast.newModifier(ModifierKeyword.FINAL_KEYWORD));
			
			return outputField;
		};
	}
	
	public void addToTypeDeclaration(@NonNull AST ast, @NonNull TypeDeclaration type) {
		type.bodyDeclarations().add(supplyInternalField().apply(ast));
		type.bodyDeclarations().add(supplyExternalField().apply(ast));
	}
	
}
