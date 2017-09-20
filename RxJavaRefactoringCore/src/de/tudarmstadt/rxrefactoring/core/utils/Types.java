package de.tudarmstadt.rxrefactoring.core.utils;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

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
	
	public static boolean hasSignature(ITypeBinding type, String typeBinaryName) {
		if (type == null || typeBinaryName == null) {
			return false;
		}
				
		String binaryName = type.getBinaryName();
		
		return Objects.equals(binaryName, typeBinaryName);		
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
	public static @NonNull Type typeFromBinding(@NonNull AST ast, @NonNull ITypeBinding typeBinding) {
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
				capType.setBound(typeFromBinding(ast, bound), wildCard.isUpperbound());
			}
			return capType;
		}

		if (typeBinding.isArray()) {
			Type elType = typeFromBinding(ast, typeBinding.getElementType());
			return ast.newArrayType(elType, typeBinding.getDimensions());
		}

		if (typeBinding.isParameterizedType()) {
			ParameterizedType type = ast.newParameterizedType(typeFromBinding(ast, typeBinding.getErasure()));

			@SuppressWarnings("unchecked")
			List<Type> newTypeArgs = type.typeArguments();
			for (ITypeBinding typeArg : typeBinding.getTypeArguments()) {
				newTypeArgs.add(typeFromBinding(ast, typeArg));
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
