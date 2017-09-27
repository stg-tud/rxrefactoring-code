package de.tudarmstadt.rxrefactoring.core.ir;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;

import de.tudarmstadt.rxrefactoring.core.NodeSupplier;
import de.tudarmstadt.rxrefactoring.core.internal.execution.RewriteCompilationUnit;

import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class SimpleReactiveInput implements IReactiveInput {

	private final @NonNull NodeSupplier<? extends Type> type;
	
	private final @NonNull NodeSupplier<SimpleName> name;
	
	
	public SimpleReactiveInput(@NonNull NodeSupplier<? extends Type> type, @NonNull NodeSupplier<SimpleName> name) {
		this.type = type;
		this.name = name;
	}

	@Override
	public @NonNull NodeSupplier<? extends Type> supplyType() {
		return type;
	}

	@Override
	public @NonNull NodeSupplier<SimpleName> supplyInternalName() {
		return name;
	}

	@Override
	public @NonNull NodeSupplier<SimpleName> supplyExternalName() {
		return name;
	}
	
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
			inputField.setType(supplyFlowableType().apply(unit));
			inputField.modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
			inputField.modifiers().add(ast.newModifier(ModifierKeyword.FINAL_KEYWORD));
			
			return inputField;
		};
	} 
	
	@Override
	public void addToTypeDeclaration(@NonNull RewriteCompilationUnit unit, @NonNull List bodyDeclarations) {	
		bodyDeclarations.add(supplyExternalField().apply(unit));
	}
	
}
