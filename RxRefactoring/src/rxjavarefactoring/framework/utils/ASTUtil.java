package rxjavarefactoring.framework.utils;

import org.eclipse.jdt.core.dom.*;

/**
 * Description: Util class for {@link ASTNode}s<br>
 * This class contains code from the tool
 * <a href="http://refactoring.info/tools/asyncdroid/">AsyncDroid</a>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/11/2016
 */
public final class ASTUtil
{
	private ASTUtil()
	{
		// This class should not be instantiated
	}

	/**
	 * Find the parent of a node given the target class
	 * 
	 * @param node
	 *            source node
	 * @param target
	 *            target node. (i.e. VariableDeclaration.class)
	 * @param <T>
	 *            Inferred from second parameter
	 * @return parent node based on the target
	 */
	public static <T> ASTNode findParent( ASTNode node, Class<?> target )
	{
		ASTNode parent = node.getParent();
		while ( parent != null && !target.isInstance( parent ) )
		{
			parent = parent.getParent();
		}
		return parent;
	}

	/**
	 * Determines whether a node is subclass of the a given target
	 * 
	 * @param type
	 *            current node (The {@link ASTNode} must be from type
	 *            {@link TypeDeclaration} or {@link AnonymousClassDeclaration})
	 * @param target
	 *            query
	 * @param isDirectChild
	 *            true if only direct children should be considered
	 * @return true if the current node is a subclass of target
	 */
	public static boolean isSubclassOf( ASTNode type, String target, boolean isDirectChild )
	{
		ITypeBinding superClass = null;
		if ( type instanceof TypeDeclaration )
		{
			superClass = ( (TypeDeclaration) type ).resolveBinding().getSuperclass();
		}
		else if ( type instanceof AnonymousClassDeclaration )
		{
			superClass = ( (AnonymousClassDeclaration) type ).resolveBinding().getSuperclass();
		}
		return isSubclassOf( superClass, target, isDirectChild );
	}

	/**
	 * Determines whether a node is subclass of the a given target
	 * 
	 * @param superClass
	 *            current type
	 * @param target
	 *            query
	 * @param isDirectChild
	 *            true if only direct children should be considered
	 * @return true if the current node is a subclass of target
	 */
	public static boolean isSubclassOf( ITypeBinding superClass, String target, boolean isDirectChild )
	{
		if ( !isDirectChild )
		{
			while ( superClass != null )
			{
				if ( superClass.getBinaryName().equals( target ) )
				{
					return true;
				}
				superClass = superClass.getSuperclass();
			}
		}
		else
		{
			if ( superClass != null && superClass.getBinaryName().equals( target ) )
				return true;
		}
		return false;
	}

	/**
	 * Checks if the current type is from target type. Superclasses and
	 * subclasses are not considered
	 * 
	 * @param type
	 *            current node (The {@link ASTNode} must be from type
	 *            {@link TypeDeclaration} or {@link AnonymousClassDeclaration})
	 * @param target
	 *            target type
	 * @return true if they match
	 */
	public static boolean isClassOf( ASTNode type, String target )
	{
		ITypeBinding classType = null;
		if ( type instanceof TypeDeclaration )
		{
			classType = ( (TypeDeclaration) type ).resolveBinding();
		}
		else if ( type instanceof AnonymousClassDeclaration )
		{
			classType = ( (AnonymousClassDeclaration) type ).resolveBinding();
		}
		return isClassOf( classType, target );
	}

	/**
	 * Checks whether the current class is a class or subclass of target
	 * 
	 * @param type
	 *            current node (The {@link ASTNode} must be from type
	 *            {@link TypeDeclaration} or {@link AnonymousClassDeclaration})
	 * @param target
	 *            target type
	 * @return true if the current class is of type target or a subclass of
	 *         target
	 */
	public static boolean isTypeOf( ASTNode type, String target )
	{
		return isSubclassOf( type, target, false ) || isClassOf( type, target );
	}

	/**
	 * Checks whether the current class is a class or subclass of target
	 * 
	 * @param type
	 *            current type
	 * @param target
	 *            target type
	 * @return true if the current class is of type target or a subclass of
	 *         target
	 */
	public static boolean isTypeOf( ITypeBinding type, String target )
	{
		return isSubclassOf( type, target, false ) || isClassOf( type, target );
	}

	/**
	 *
	 * @param node
	 *            an ast node
	 * @return The next parent of type {@link Statement}
	 */
	public static Statement getStmtParent( ASTNode node )
	{
		while ( node != null )
		{
			if ( node instanceof Statement )
				return (Statement) node;
			node = node.getParent();
		}
		return null;
	}

	/**
	 * Returns the variable name of a parameter given a
	 * {@link MethodDeclaration}
	 * 
	 * @param methodDeclaration
	 *            method declaration
	 * @param parameterIndex
	 *            index
	 * @return the variable name
	 * @throws IndexOutOfBoundsException
	 *             if there is no parameter with the given index
	 */
	public static String getVariableName( MethodDeclaration methodDeclaration, int parameterIndex ) throws IndexOutOfBoundsException
	{
		Object parameter = methodDeclaration.parameters().get( parameterIndex );
		SingleVariableDeclaration variableDecl = (SingleVariableDeclaration) parameter;
		return variableDecl.getName().toString();
	}

	/**
	 * Identifies if a node matches a target method from a class
	 * 
	 * @param node
	 *            input node
	 * @param methodName
	 *            target method name
	 * @param classBinaryName
	 *            target class of method
	 * @return true if matches, false otherwise
	 */
	public static boolean matchesTargetMethod( MethodInvocation node, String methodName, String classBinaryName )
	{
		IMethodBinding binding = node.resolveMethodBinding();
		if ( binding == null )
		{
			return false;
		}
		String bindingName = binding.getName();
		String className = binding.getDeclaringClass().getBinaryName();

		return bindingName.equals( bindingName ) && classBinaryName.equals( className );
	}

	// ### Private Methods ###

	private static boolean isClassOf( ITypeBinding classType, String target )
	{
		return classType != null && target.equals( classType.getBinaryName() );
	}
}
