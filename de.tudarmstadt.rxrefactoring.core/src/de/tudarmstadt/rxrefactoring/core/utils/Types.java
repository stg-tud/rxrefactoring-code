package de.tudarmstadt.rxrefactoring.core.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public final class Types {

	// There can be no instance of TypeUtils.
	private Types() { }
	
	/**
	 * Checks whether the given type binding adheres to any of the given
	 * binary names, e.g., {@code java.lang.String}.
	 * 
	 * @param type the type to check.
	 * @param typeBinaryNames the binary names to check against.
	 * 
	 * @return false, if the type does not adhere to any of the given binary names
	 * or if any argument is {@code null}.
	 */
	public static boolean isExactTypeOf(ITypeBinding type, String... typeBinaryNames) {
		if (type == null || typeBinaryNames == null) {
			return false;
		}
				
		String binaryName = type.getQualifiedName();		
		return Arrays.stream(typeBinaryNames).anyMatch(s -> Objects.equals(binaryName, s));		
	}
	
	/**
	 * Checks whether a given type or any of its super-types adhere to the
	 * given type name.
	 *  
	 * @param type the type to check.
	 * @param parentTypeQualifiedNames the names to compare the type against.
	 * 
	 * @return false, if the given type does not adhere to any of the type names or
	 * if any argument is {@code null}.
	 * 
	 * @see Types#isExactTypeOf(ITypeBinding, String...)
	 */
	public static boolean isTypeOf(ITypeBinding type, String... parentTypeQualifiedNames) {
				
		ITypeBinding superType = type;		
		while (superType != null) {
			if (isExactTypeOf(superType, parentTypeQualifiedNames) || isExactTypeOf(superType.getErasure(), parentTypeQualifiedNames)) {
				return true;
			}
			
			superType = superType.getSuperclass();
		}
		
		return false;		
		
	}
	
	/**
	 * Produces a new type from a given type binding. The implementation
	 * is taken from 
	 * <a href="https://stackoverflow.com/questions/11091791/convert-eclipse-jdt-itypebinding-to-a-type">https://stackoverflow.com/questions/11091791/convert-eclipse-jdt-itypebinding-to-a-type</a>.
	 *
	 * @param ast The AST used to build the new type.
	 * @param typeBinding The type binding to convert.
	 * 
	 * @return The type from the type binding.
	 */
	@SuppressWarnings("null")
	public static @NonNull Type fromBinding(@NonNull AST ast, @NonNull ITypeBinding typeBinding) {
		Objects.requireNonNull(ast);
		Objects.requireNonNull(typeBinding);

		if (typeBinding.isPrimitive()) {
			return ast.newPrimitiveType(PrimitiveType.toCode(typeBinding.getName()));
		}

		if (typeBinding.isCapture()) {
			ITypeBinding wildCard = typeBinding.getWildcard();
			org.eclipse.jdt.core.dom.WildcardType capType = ast.newWildcardType();
			ITypeBinding bound = wildCard.getBound();
			if (bound != null) {
				capType.setBound(fromBinding(ast, bound), wildCard.isUpperbound());
			}
			return capType;
		}

		if (typeBinding.isArray()) {
			Type elType = fromBinding(ast, typeBinding.getElementType());
			return ast.newArrayType(elType, typeBinding.getDimensions());
		}

		if (typeBinding.isParameterizedType()) {
			ParameterizedType type = ast.newParameterizedType(fromBinding(ast, typeBinding.getErasure()));

			@SuppressWarnings("unchecked")
			List<Type> newTypeArgs = type.typeArguments();
			for (ITypeBinding typeArg : typeBinding.getTypeArguments()) {
				newTypeArgs.add(fromBinding(ast, typeArg));
			}

			return type;
		}

		// simple or raw type
		String qualName = typeBinding.getQualifiedName();
		if ("".equals(qualName)) {
			throw new IllegalArgumentException("No name for type binding.");
		}
		return ast.newSimpleType(ast.newName(qualName));
	}
	
	
	/**
	 * Returns the declared type of a {@link VariableDeclarationFragment}.
	 * 
	 * @param variable the fragment to be checked
	 * @return the type of the fragment. This node is already present in the same AST as the fragment.
	 */
	@SuppressWarnings("null")
	public static @NonNull Type declaredTypeOf(@NonNull VariableDeclarationFragment variable) {
		
		ASTNode parent = variable.getParent();
		
		//TODO: Check for extra array dimensions, e.g.
		//int x = 1, y[] = new int[31];		
		
		if (parent instanceof VariableDeclarationExpression) {
			return ((VariableDeclarationExpression) parent).getType();
		} else if (parent instanceof VariableDeclarationStatement) {
			return ((VariableDeclarationStatement) parent).getType();
		} 
		
		throw new IllegalArgumentException("unparented VariableDeclarationFragment does not have a type");
	}
}
