package de.tudarmstadt.rxrefactoring.core.ir.util;

import static de.tudarmstadt.rxrefactoring.core.NodeSupplier.parameterizedType;
import static de.tudarmstadt.rxrefactoring.core.NodeSupplier.simpleType;

import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;

import de.tudarmstadt.rxrefactoring.core.NodeSupplier;

import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;

public class FunctionBuilder {
	
	private final @NonNull NodeSupplier<? extends Type> inputType;
	
	private final @NonNull NodeSupplier<? extends Type> outputType;
	
	private final @NonNull NodeSupplier<SimpleName> variableName;
	
	private final @NonNull NodeSupplier<Block> body;

	
	public FunctionBuilder(
			@NonNull NodeSupplier<? extends Type> inputType, 
			@NonNull NodeSupplier<? extends Type> outputType,
			@NonNull NodeSupplier<SimpleName> variableName,
			@NonNull NodeSupplier<Block> body) {
		this.inputType = inputType;
		this.outputType = outputType;
		this.variableName = variableName;
		this.body = body;
	}
	
	/**
	 * Builds an expression of type {@link Consumer}. The expression
	 * creates a new anonymous class consumer with the specified
	 * parameter type, body and variable name.
	 * 
	 * @param ast The AST used to build the {@link Consumer}.
	 * @return An expression of type {@link Consumer}.
	 */
	@SuppressWarnings("unchecked")
	public NodeSupplier<ClassInstanceCreation> supplyAnonymousClass() {
		return unit -> {
			AST ast = unit.getAST();
			
			//Define the call method
			MethodDeclaration acceptMethod = ast.newMethodDeclaration();
			acceptMethod.setName(ast.newSimpleName("apply"));
			acceptMethod.setReturnType2(outputType.apply(unit));
			acceptMethod.modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
					
			SingleVariableDeclaration callParameter = ast.newSingleVariableDeclaration();
			callParameter.setName(variableName.apply(unit));
			callParameter.setType(inputType.apply(unit));
			
			acceptMethod.parameters().add(callParameter);
			
			acceptMethod.setBody(body.apply(unit));
					
			//Action1 class declaration
			AnonymousClassDeclaration anonClass = ast.newAnonymousClassDeclaration();
			anonClass.bodyDeclarations().add(acceptMethod);	
			
			ParameterizedType consumerType = parameterizedType(simpleType("Function"), inputType, outputType).apply(unit);
					
			ClassInstanceCreation newConsumer = ast.newClassInstanceCreation();
			newConsumer.setType(consumerType);
			newConsumer.setAnonymousClassDeclaration(anonClass);
			
			return newConsumer;
		};
		
	}
	
}
