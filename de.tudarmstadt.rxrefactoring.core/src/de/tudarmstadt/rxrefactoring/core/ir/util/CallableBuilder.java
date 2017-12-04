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
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;

public class CallableBuilder {
	
	private final @NonNull NodeSupplier<? extends Type> type;
	
	private final @NonNull NodeSupplier<Block> body;

	
	public CallableBuilder(
			@NonNull NodeSupplier<? extends Type> type, 
			@NonNull NodeSupplier<Block> body) {
		this.type = type;
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
	public @NonNull NodeSupplier<ClassInstanceCreation> supplyClassInstanceCreation() {
		
		return unit -> {
			AST ast = unit.getAST();
			
			//Define the call method
			MethodDeclaration callMethod = ast.newMethodDeclaration();
			callMethod.setName(ast.newSimpleName("call"));
			callMethod.setReturnType2(type.apply(unit));
			callMethod.modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
					
							
			callMethod.setBody(body.apply(unit));
					
			//Action1 class declaration
			AnonymousClassDeclaration anonClass = ast.newAnonymousClassDeclaration();
			anonClass.bodyDeclarations().add(callMethod);	
			
			ParameterizedType callableType = parameterizedType(simpleType("Callable"), type).apply(unit);
					
			ClassInstanceCreation newCallable = ast.newClassInstanceCreation();
			newCallable.setType(callableType);
			newCallable.setAnonymousClassDeclaration(anonClass);
			
			return newCallable;
		};
	}
	
}
