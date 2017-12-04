package de.tudarmstadt.rxrefactoring.core.ir;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.NodeSupplier;
import de.tudarmstadt.rxrefactoring.core.ir.util.CallableBuilder;

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
public class ReactiveObject implements IReactiveObject {
	
	
	//lists for inputs and outputs
	private final @NonNull Map<Object, IReactiveInput> inputs;
	
	private final @NonNull Map<Object, IReactiveOutput> outputs;
	
	private final @NonNull Map<Object, IReactiveComputation> computations;
	
	private final @NonNull List<NodeSupplier<? extends BodyDeclaration>> additionalDeclarations;
	
	private final @NonNull NodeSupplier<SimpleName> className;
	
	
	@SuppressWarnings("null")
	public ReactiveObject(@NonNull NodeSupplier<SimpleName> className) {
		inputs = Maps.newHashMap();
		outputs = Maps.newHashMap();
		computations = Maps.newHashMap();
		
		additionalDeclarations = Lists.newLinkedList();
		
		this.className = className;
	}
	
	public void addInput(@NonNull Object identifier, @NonNull IReactiveInput input) {
		inputs.put(identifier, input);
	}
	
	public void addComputation(@NonNull Object identifier, @NonNull IReactiveComputation computation) {
		computations.put(identifier, computation);
	}
	
	public void addOutput(@NonNull Object identifier, @NonNull IReactiveOutput output) {
		outputs.put(identifier, output);
	}
	
	public void addAdditionalDeclaration(@NonNull NodeSupplier<? extends BodyDeclaration> declaration) {
		additionalDeclarations.add(declaration);
	}
	
		
	public @NonNull NodeSupplier<SimpleName> supplyClassName() {		
		return className;
	}

	
	
	@SuppressWarnings("unchecked")
	private @NonNull NodeSupplier<MethodDeclaration> supplyConstructorDeclaration() {
		return unit -> {
			AST ast = unit.getAST();
			
			MethodDeclaration result = ast.newMethodDeclaration();
			result.setConstructor(true);
			result.setName(className.apply(unit));
			result.modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
			
			Block resultBody = ast.newBlock();
			result.setBody(resultBody);
			
			for (IReactiveComputation computation : computations.values()) {
				SimpleName internalName = computation.supplyInternalName().apply(unit);			
				resultBody.statements().add(supplySubscribeInvoke(internalName).apply(unit));
			}
			
			for (IReactiveOutput output : outputs.values()) {
				SimpleName outputName = output.supplyExternalName().apply(unit);
				resultBody.statements().add(supplySubscribeInvoke(outputName).apply(unit));
			}
						
			return result;
		};
	}

	/**
	 * <p>Builds an invocation to subscribe: {@code internalName.subscribe();}
	 * @param ast The AST used for building. 
	 * @param internalName The name of the expression of the invocation.
	 */
	@SuppressWarnings("null")
	private @NonNull NodeSupplier<ExpressionStatement> supplySubscribeInvoke(@NonNull SimpleName internalName) {
		return unit -> {
			AST ast = unit.getAST();
			
			MethodInvocation invokeSubscribe = ast.newMethodInvocation();
			invokeSubscribe.setName(ast.newSimpleName("subscribe"));
			invokeSubscribe.setExpression(internalName);
			
			return ast.newExpressionStatement(invokeSubscribe);
		};
		
	}
	
	/*
	 * Supplying entries for type declaration
	 */
	
	@SuppressWarnings({ "unchecked", "null" })
	public @NonNull NodeSupplier<TypeDeclaration> supplyTypeDeclaration() {
		return unit -> {
			AST ast = unit.getAST();
			
			TypeDeclaration reactiveType = ast.newTypeDeclaration();
			reactiveType.setName(supplyClassName().apply(unit));
			
			reactiveType.bodyDeclarations().add(supplyConstructorDeclaration().apply(unit));
			
			for (IReactiveInput input : inputs.values()) {
				input.addToTypeDeclaration(unit, reactiveType.bodyDeclarations());
			}
			
			for (IReactiveOutput output : outputs.values()) {
				output.addToTypeDeclaration(unit, reactiveType.bodyDeclarations());
			}
			
			for (IReactiveComputation computation : computations.values()) {
				computation.addToTypeDeclaration(unit, reactiveType.bodyDeclarations());
			}
			
			
			
			for (NodeSupplier<? extends BodyDeclaration> declaration : additionalDeclarations) {
				reactiveType.bodyDeclarations().add(declaration.apply(unit));
			}
			
			return reactiveType;
		};
	}
	
	public @NonNull InstanceCreationBuilder supplyInstanceCreation() {
		return new InstanceCreationBuilder();
	}
	
	/*
	 * Supplying entries for expression declaration
	 */
	public boolean isMethodCreation() {
		return computations.size() == 1 && outputs.size() == 1 && inputs.size() == 1 && inputs.values().iterator().next() instanceof EmptyReactiveInput;
	}
	
	public @NonNull NodeSupplier<Expression> supplyMethodCreation() {
		if (!isMethodCreation()) {
			throw new IllegalStateException("method creation cannot be supplied");
		}
		
		final IReactiveOutput output = outputs.values().iterator().next();
		final IReactiveComputation computation = computations.values().iterator().next();
		
		return unit -> {
			AST ast = unit.getAST();
			
			MethodInvocation fromCallable = ast.newMethodInvocation();
			fromCallable.setName(ast.newSimpleName("fromCallable"));
			fromCallable.setExpression(ast.newSimpleName("Flowable"));
			
			//TODO: get body of computation
			CallableBuilder builder = new CallableBuilder(output.supplyType(), null);
			
			
			return builder.supplyClassInstanceCreation().apply(unit);
		};
	}
	
	
	public class InstanceCreationBuilder implements NodeSupplier<Expression> {
		
		private @Nullable IReactiveValue accessValue;
		
		public void accessOutput(@NonNull Object identifier) {
			accessValue = outputs.get(identifier);
		}
	

		@Override
		public @NonNull Expression apply(@NonNull IRewriteCompilationUnit unit) {
			AST ast = unit.getAST();
			
			Expression expr;
			
			ClassInstanceCreation newReactiveObject = ast.newClassInstanceCreation();
			newReactiveObject.setType(ast.newSimpleType(supplyClassName().apply(unit)));
			expr = newReactiveObject;
			
			
			if (accessValue != null) {
				FieldAccess field = ast.newFieldAccess();
				field.setName(accessValue.supplyExternalName().apply(unit));
				field.setExpression(expr);
				expr = field;
			}
			
			return expr;
		}
	}
	
}
