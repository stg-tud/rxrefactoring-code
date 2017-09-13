package de.tudarmstadt.rxrefactoring.core.codegen.util;

import static de.tudarmstadt.rxrefactoring.core.codegen.NodeSupplier.parameterizedTypeFrom;
import static de.tudarmstadt.rxrefactoring.core.codegen.NodeSupplier.simpleType;

import java.util.Objects;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;

import de.tudarmstadt.rxrefactoring.core.codegen.NodeSupplier;

public class ConsumerBuilder {
	
	private final @NonNull NodeSupplier<? extends Type> type;
	
	private final @NonNull NodeSupplier<SimpleName> variableName;
	
	private final @NonNull NodeSupplier<Block> body;

	
	public ConsumerBuilder(
			@NonNull NodeSupplier<? extends Type> type, 
			@NonNull NodeSupplier<SimpleName> variableName,
			@NonNull NodeSupplier<Block> body) {
		this.type = type;
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
		
		return ast -> {					
			//Define the call method
			MethodDeclaration acceptMethod = ast.newMethodDeclaration();
			acceptMethod.setName(ast.newSimpleName("accept"));
			acceptMethod.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));
			acceptMethod.modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
					
			SingleVariableDeclaration callParameter = ast.newSingleVariableDeclaration();
			callParameter.setName(variableName.apply(ast));
			callParameter.setType(type.apply(ast));
			
			acceptMethod.parameters().add(callParameter);
			
			acceptMethod.setBody(body.apply(ast));
					
			//Action1 class declaration
			AnonymousClassDeclaration anonClass = ast.newAnonymousClassDeclaration();
			anonClass.bodyDeclarations().add(acceptMethod);	
			
			ParameterizedType consumerType = parameterizedTypeFrom(simpleType("Consumer"), type).apply(ast);
					
			ClassInstanceCreation newConsumer = ast.newClassInstanceCreation();
			newConsumer.setType(consumerType);
			newConsumer.setAnonymousClassDeclaration(anonClass);
			
			return newConsumer;
		};
	}
	
}
