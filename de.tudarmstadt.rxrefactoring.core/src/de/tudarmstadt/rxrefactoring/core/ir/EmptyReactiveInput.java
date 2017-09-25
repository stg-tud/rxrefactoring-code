package de.tudarmstadt.rxrefactoring.core.ir;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import de.tudarmstadt.rxrefactoring.core.RewriteCompilationUnit;

public class EmptyReactiveInput implements IReactiveInput {

	private final @NonNull NodeSupplier<SimpleName> name;
	
	
	public EmptyReactiveInput(@NonNull NodeSupplier<SimpleName> name) {
		this.name = name;
	}

	@Override
	public @NonNull NodeSupplier<? extends Type> supplyType() {
		return NodeSupplier.OBJECT_TYPE;
	}

	@Override
	public @NonNull NodeSupplier<SimpleName> supplyInternalName() {
		return name;
	}

	@Override
	public @NonNull NodeSupplier<SimpleName> supplyExternalName() {
		return name;
	}
	
	@SuppressWarnings("unchecked")
	protected @NonNull NodeSupplier<FieldDeclaration> supplyExternalField() {
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
	@Override
	public void addToTypeDeclaration(@NonNull RewriteCompilationUnit unit, @SuppressWarnings("rawtypes") @NonNull List bodyDeclarations) {	
		bodyDeclarations.add(supplyExternalField().apply(unit));
	}
	
}
