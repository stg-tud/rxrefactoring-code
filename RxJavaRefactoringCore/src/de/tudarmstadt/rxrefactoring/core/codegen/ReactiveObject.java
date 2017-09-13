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
	private final Map<Object, ReactiveInput> inputs;
	
	private final Map<Object, ReactiveOutput> outputs;
	
	private final Map<Object, ReactiveComputation> computations;
	
	private NodeSupplier<SimpleName> className;
	
	
	public ReactiveObject() {
		inputs = Maps.newHashMap();
		outputs = Maps.newHashMap();
		computations = Maps.newHashMap();
	}
	
	public void addInput(@NonNull Object identifier, @NonNull ReactiveInput input) {
		inputs.put(identifier, input);
	}
	
	public void addComputation(@NonNull Object identifier, @NonNull ReactiveComputation computation) {
		computations.put(identifier, computation);
	}
	
	public void setClassName(NodeSupplier<SimpleName> className) {
		Objects.requireNonNull(className);
		this.className = className;
	}
	
	public SimpleName buildClassName(AST ast) {
		Objects.requireNonNull(ast);
		Objects.requireNonNull(className);
		return className.apply(ast);
	}

	
	
	private MethodDeclaration buildConstructorDeclaration(AST ast) {
		
		MethodDeclaration result = ast.newMethodDeclaration();
		result.setConstructor(true);
		result.setName(className.apply(ast));
		result.modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
		
		Block resultBody = ast.newBlock();
		result.setBody(resultBody);
		
		for (ReactiveComputation internal : computations.values()) {
			SimpleName internalName = internal.buildName(ast);			
			resultBody.statements().add(buildSubscribeInvoke(ast, internalName));
		}
		
		for (ReactiveOutput output : outputs.values()) {
			SimpleName outputName = output.buildName(ast);
			resultBody.statements().add(buildSubscribeInvoke(ast, outputName));
		}
		
		
		return result;
	}

	/**
	 * <p>Builds an invocation to subscribe: {@code internalName.subscribe();}
	 * @param ast The AST used for building. 
	 * @param internalName The name of the expression of the invocation.
	 */
	private ExpressionStatement buildSubscribeInvoke(AST ast, SimpleName internalName) {
		MethodInvocation invokeSubscribe = ast.newMethodInvocation();
		invokeSubscribe.setName(ast.newSimpleName("subscribe"));
		invokeSubscribe.setExpression(internalName);
		
		return ast.newExpressionStatement(invokeSubscribe);
		
	}
	
	public TypeDeclaration buildTypeDeclaration(AST ast) {
		Objects.requireNonNull(ast);
		
		TypeDeclaration reactiveType = ast.newTypeDeclaration();
		reactiveType.setName(buildClassName(ast));
		
		reactiveType.bodyDeclarations().add(buildConstructorDeclaration(ast));
		
		for (ReactiveInput input : inputs.values()) {
			input.addToTypeDeclaration(ast, reactiveType);
		}
		
		for (ReactiveComputation computation : computations.values()) {
			reactiveType.bodyDeclarations().add(
					computation.buildFieldDeclaration(ast));
		}
		
		return reactiveType;
	}
	
}
