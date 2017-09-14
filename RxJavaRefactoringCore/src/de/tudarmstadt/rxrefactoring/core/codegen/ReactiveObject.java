package de.tudarmstadt.rxrefactoring.core.codegen;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.google.common.collect.Maps;

/*
 * A reactive object has the following information flow
 * 
 * 	Inputs			Internal		Outputs
 * 					Computations	
 * 
 * 	A1		---->	B1		---->	C1
 * 							---->	C3 
 * 	A2		---->	B2		---->	C2
 * 			---->	B3		---->	C1
 * 
 */
public class ReactiveObject {
	
	
	//lists for inputs and outputs
	private final @NonNull Map<Object, ReactiveInput> inputs;
	
	private final @NonNull Map<Object, ReactiveOutput> outputs;
	
	private final @NonNull Map<Object, ReactiveComputation> computations;
	
	private final @NonNull NodeSupplier<SimpleName> className;
	
	
	@SuppressWarnings("null")
	public ReactiveObject(@NonNull NodeSupplier<SimpleName> className) {
		inputs = Maps.newHashMap();
		outputs = Maps.newHashMap();
		computations = Maps.newHashMap();
		
		this.className = className;
	}
	
	public void addInput(@NonNull Object identifier, @NonNull ReactiveInput input) {
		inputs.put(identifier, input);
	}
	
	public void addComputation(@NonNull Object identifier, @NonNull ReactiveComputation computation) {
		computations.put(identifier, computation);
	}
	
	public void addOutput(@NonNull Object identifier, @NonNull ReactiveOutput output) {
		outputs.put(identifier, output);
	}
	
		
	public @NonNull NodeSupplier<SimpleName> supplyClassName() {		
		return className;
	}

	
	
	private @NonNull NodeSupplier<MethodDeclaration> supplyConstructorDeclaration() {
		return ast -> {
			MethodDeclaration result = ast.newMethodDeclaration();
			result.setConstructor(true);
			result.setName(className.apply(ast));
			result.modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
			
			Block resultBody = ast.newBlock();
			result.setBody(resultBody);
			
			for (ReactiveComputation internal : computations.values()) {
				SimpleName internalName = internal.buildName(ast);			
				resultBody.statements().add(supplySubscribeInvoke(internalName).apply(ast));
			}
			
			for (ReactiveOutput output : outputs.values()) {
				SimpleName outputName = output.supplyName().apply(ast);
				resultBody.statements().add(supplySubscribeInvoke(outputName).apply(ast));
			}
						
			return result;
		};
	}

	/**
	 * <p>Builds an invocation to subscribe: {@code internalName.subscribe();}
	 * @param ast The AST used for building. 
	 * @param internalName The name of the expression of the invocation.
	 */
	private @NonNull NodeSupplier<ExpressionStatement> supplySubscribeInvoke(final SimpleName internalName) {
		return ast -> {
			MethodInvocation invokeSubscribe = ast.newMethodInvocation();
			invokeSubscribe.setName(ast.newSimpleName("subscribe"));
			invokeSubscribe.setExpression(internalName);
			
			return ast.newExpressionStatement(invokeSubscribe);
		};
		
	}
	
	public @NonNull NodeSupplier<TypeDeclaration> supplyTypeDeclaration() {
		return ast -> {
			Objects.requireNonNull(ast);
			
			TypeDeclaration reactiveType = ast.newTypeDeclaration();
			reactiveType.setName(supplyClassName().apply(ast));
			
			reactiveType.bodyDeclarations().add(supplyConstructorDeclaration().apply(ast));
			
			for (ReactiveInput input : inputs.values()) {
				input.addToTypeDeclaration(ast, reactiveType);
			}
			
			for (ReactiveComputation computation : computations.values()) {
				reactiveType.bodyDeclarations().add(
						computation.buildFieldDeclaration(ast));
			}
			
			for (ReactiveOutput output : outputs.values()) {
				output.addToTypeDeclaration(ast, reactiveType);
			}
			
			return reactiveType;
		};
	}
	
}
