package rxjavarefactoring.framework.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.*;

import rxjavarefactoring.framework.codegenerators.ASTNodeFactory;
import rxjavarefactoring.framework.utils.visitors.TryStatementVisitor;

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
	public static <T extends ASTNode> T findParent( ASTNode node, Class<T> target )
	{
		if ( target.isInstance( node ) )
		{
			return (T) node;
		}

		ASTNode parent = node.getParent();
		while ( parent != null && !target.isInstance( parent ) )
		{
			parent = parent.getParent();
		}
		return (T) parent;
	}

	/**
	 * Determines whether a node is subclass of the a given target
	 *
	 * @param type
	 *            current node (The {@link ASTNode} must be from type
	 *            {@link TypeDeclaration}, {@link AnonymousClassDeclaration},
	 *            {@link ClassInstanceCreation}, {@link FieldDeclaration} or
	 *            {@link Type}
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
		else if ( type instanceof ClassInstanceCreation )
		{
			superClass = ( (ClassInstanceCreation) type ).getType().resolveBinding().getSuperclass();
		}
		else if ( type instanceof FieldDeclaration )
		{
			superClass = ( (FieldDeclaration) type ).getType().resolveBinding().getSuperclass();
		}
		else if ( type instanceof Type )
		{
			superClass = ( (Type) type ).resolveBinding().getSuperclass();
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
				String binaryName = superClass.getBinaryName();
				if ( binaryName == null )
				{
					return false;
				}

				if ( binaryName.equals( target ) )
				{
					return true;
				}
				superClass = superClass.getSuperclass();
			}
		}
		else
		{
			String binaryName = superClass.getBinaryName();
			if ( binaryName == null )
			{
				return false;
			}

			if ( superClass != null && binaryName.equals( target ) )
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if the current type is from target type. Superclasses and
	 * subclasses are not considered
	 *
	 * @param type
	 *            current node (The {@link ASTNode} must be from type
	 *            {@link TypeDeclaration}, {@link AnonymousClassDeclaration},
	 *            {@link ClassInstanceCreation}, {@link FieldDeclaration} or
	 *            {@link Type}
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
		else if ( type instanceof ClassInstanceCreation )
		{
			classType = ( (ClassInstanceCreation) type ).getType().resolveBinding();
		}
		else if ( type instanceof FieldDeclaration )
		{
			classType = ( (FieldDeclaration) type ).getType().resolveBinding();
		}
		else if ( type instanceof Type )
		{
			classType = ( (Type) type ).resolveBinding();
		}
		return isClassOf( classType, target );
	}

	/**
	 * Checks whether the current class is a class or subclass of target
	 *
	 * @param type
	 *            current node (The {@link ASTNode} must be from type
	 *            {@link TypeDeclaration}, {@link AnonymousClassDeclaration},
	 *            {@link ClassInstanceCreation} or {@link FieldDeclaration}
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
	 * Checks whether the current class is a class or subclass of any of the target classes
	 *
	 * @param type
	 *            current type
	 * @param targetClasses
	 *            target types
	 * @return true if the current class or subclass is type or subtype of the
	 *         target classes
	 */
	public static boolean isTypeOf( ASTNode type, List<String> targetClasses )
	{
		for ( String target : targetClasses )
		{
			boolean matchesType = isSubclassOf( type, target, false ) || isClassOf( type, target );
			if ( matchesType )
			{
				return true;
			}
		}
		return false;
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
	 * Returns the parameter type given a {@link MethodDeclaration}
	 * and the parameter index
	 * 
	 * @param methodDeclaration
	 *            method declaration
	 * @param parameterIndex
	 *            index
	 * @return the parameter type
	 * @throws IndexOutOfBoundsException
	 *             if there is no parameter with the given index
	 */
	public static String getParameterType( MethodDeclaration methodDeclaration, int parameterIndex ) throws IndexOutOfBoundsException
	{
		IMethodBinding methodBinding = methodDeclaration.resolveBinding();
		ITypeBinding argumentType = methodBinding.getParameterTypes()[ parameterIndex ];
		return argumentType.getName();
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
		IMethodBinding methodBinding = node.resolveMethodBinding();
		return matchesTargetMethod( methodName, classBinaryName, methodBinding );
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
	public static boolean matchesTargetMethod( MethodDeclaration node, String methodName, String classBinaryName )
	{
		IMethodBinding methodBinding = node.resolveBinding();
		return matchesTargetMethod( methodName, classBinaryName, methodBinding );
	}

	/**
	 * Verifies if a {@link MethodDeclaration} and a {@link MethodInvocation} match
	 * 
	 * @param methodDeclaration
	 *            method declaration
	 * @param methodInvocation
	 *            method invocation
	 * @return true if they match, false otherwise
	 */
	public static boolean matchesSignature( MethodDeclaration methodDeclaration, MethodInvocation methodInvocation )
	{
		IMethodBinding declBinding = methodDeclaration.resolveBinding();
		IMethodBinding invBinding = methodInvocation.resolveMethodBinding();

		// Check declaring class
		if ( !declBinding.getDeclaringClass().isEqualTo( invBinding.getDeclaringClass() ) )
		{
			return false;
		}

		// Check method name
		if ( !declBinding.getName().equals( invBinding.getName() ) )
		{
			return false;
		}

		// Check arguments
		ITypeBinding[] typeArguments1 = declBinding.getTypeArguments();
		ITypeBinding[] typeArguments2 = invBinding.getTypeArguments();

		if ( typeArguments1.length != typeArguments2.length )
		{
			return false;
		}

		for ( int i = 0; i < typeArguments1.length; i++ )
		{
			if ( typeArguments1[ i ].isEqualTo( typeArguments2[ i ] ) )
			{
				return false;
			}
		}
		return true;
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
	public static boolean matchesTargetMethod( SuperMethodInvocation node, String methodName, String classBinaryName )
	{
		IMethodBinding methodBinding = node.resolveMethodBinding();
		return matchesTargetMethod( methodName, classBinaryName, methodBinding );
	}

	/**
	 * Replaces the original node by a new node in a statement
	 * 
	 * @param originalNode
	 *            original node
	 * @param newNode
	 *            new node
	 * @param <T>
	 *            original node type
	 * @param <V>
	 *            new node type
	 */
	public static <T extends ASTNode, V extends ASTNode> void replaceInStatement( T originalNode, V newNode )
	{
		Block block = ASTUtil.findParent( originalNode, Block.class );
		if ( block != null )
		{
			String originalNodeString = originalNode.toString();
			String newNodeString = newNode.toString();

			ASTNode originalStatement = originalNode;
			if ( !( originalNode instanceof Statement ) )
			{
				originalStatement = ASTUtil.findParent( originalNode, Statement.class );
			}
			String originalStatementString = originalStatement.toString();
			String newStatementString = originalStatementString.replace( originalNodeString, newNodeString );
			Statement newStatement = ASTNodeFactory.createSingleStatementFromText( block.getAST(), newStatementString );

			int position = block.statements().indexOf( originalStatement );
			originalStatement.delete();
			block.statements().add( position, newStatement );
		}
	}

	/**
	 * Removes all "super." char sequences from a given node. The node
	 * must be part of a statement
	 * 
	 * @param node
	 *            source node
	 * @param <T>
	 *            type of the node
	 */
	public static <T extends ASTNode> void removeSuperKeywordInStatement( T node )
	{
		Statement statement = ASTUtil.findParent( node, Statement.class );
		String newStatementString = statement.toString().replace( "super.", "" );
		Statement newStatement = ASTNodeFactory.createSingleStatementFromText( node.getAST(), newStatementString );
		replaceInStatement( statement, newStatement );
	}

	/**
	 * Clones node remove the original parent, so the node can be added to
	 * another one.<br>
	 * Example: block.statements().add( position, node );<br>
	 * If node has a parent, then the add method throws
	 * {@link IllegalArgumentException}
	 * 
	 * @param node
	 *            node to be cloned
	 * @param <T>
	 *            type of the node inferred by the argument
	 * @return a clone of type T without parent
	 */
	public static <T extends ASTNode> T clone( T node )
	{
		if ( node == null )
		{
			return null;
		}
		return (T) ASTNode.copySubtree( node.getAST(), node );
	}

	/**
	 * Clones node remove the original parent, so the node can be added to
	 * another one.<br>
	 * Example: block.statements().add( position, node );<br>
	 * If node has a parent, then the add method throws
	 * {@link IllegalArgumentException}
	 * 
	 * @param astNode
	 *            node object. It must be castable to {@link ASTNode}
	 * @return a clone of type {@link ASTNode} withot parent
	 */
	public static ASTNode clone( Object astNode )
	{
		if ( astNode == null )
		{
			return null;
		}
		ASTNode node = (ASTNode) astNode;
		return clone( node );
	}

	/**
	 * Replaces a node by a given block. The node must already be contained in a
	 * block.<br>
	 * Example: replace a for loop by a block of statements. This method throws
	 * {@link IllegalArgumentException} if the node is not a child of a
	 * block<br>
	 * 
	 * @param node
	 *            node to be replaced
	 * @param block
	 *            block that replaces the node
	 * @param <T>
	 *            type of the node to be replaced
	 */
	public static <T extends ASTNode> void replaceByBlock( T node, Block block )
	{
		if ( node != null && block != null )
		{
			Block parentBlock = ASTUtil.findParent( node, Block.class );
			if ( parentBlock == null )
			{
				throw new IllegalArgumentException( "The node " + node.toString() + " must be inside of a AST Block" );
			}
			int nodePosition = parentBlock.statements().indexOf( node );
			node.delete();
			for ( Object statementObject : block.statements() )
			{
				parentBlock.statements().add( nodePosition++, ASTUtil.clone( statementObject ) );
			}
		}
	}

	/**
	 * Checks what exceptions are thrown inside of try-catch blocks and removes
	 * unnecessary catch clauses
	 * 
	 * @param block
	 *            the block where the try-catch blocks are found
	 */
	public static void removeUnnecessaryCatchClauses( Block block )
	{
		if ( block != null )
		{
			TryStatementVisitor tryStatementVisitor = new TryStatementVisitor();
			block.accept( tryStatementVisitor );

			// remove unnecessary catch clauses
			Map<TryStatement, Block> tryBodyMap = tryStatementVisitor.getTryBodyMap();
			Map<TryStatement, Set<ITypeBinding>> tryNeededExceptionsMap = tryStatementVisitor.getNeededExceptionsTypesMap();
			Map<TryStatement, Map<ITypeBinding, CatchClause>> tryCaughtClausesMap = tryStatementVisitor.getCaughtExceptionsMap();
			for ( TryStatement tryStatement : tryBodyMap.keySet() )
			{
				Set<ITypeBinding> neededExceptionsTypes = tryNeededExceptionsMap.get( tryStatement );
				Map<ITypeBinding, CatchClause> caughtExceptionsMap = tryCaughtClausesMap.get( tryStatement );

				if ( catchClausesNeeded( neededExceptionsTypes, caughtExceptionsMap ) )
				{
					Block tryBody = tryBodyMap.get( tryStatement );
					ASTUtil.replaceByBlock( tryStatement, tryBody );
				}
				else
				{
					removeIrrelevantCatchClauses( neededExceptionsTypes, caughtExceptionsMap );

				}
			}
		}
	}

	// ### Private Methods ###

	private static boolean isClassOf( ITypeBinding classType, String target )
	{
		return classType != null && target.equals( classType.getBinaryName() );
	}

	private static boolean catchClausesNeeded( Set<ITypeBinding> neededExceptionsTypes, Map<ITypeBinding, CatchClause> caughtExceptionsMap )
	{
		return neededExceptionsTypes.isEmpty() && !caughtExceptionsMap.isEmpty();
	}

	private static void removeIrrelevantCatchClauses( Set<ITypeBinding> neededExceptionsTypes, Map<ITypeBinding, CatchClause> caughtExceptionsMap )
	{
		for ( ITypeBinding caughtExceptionTypeBinding : caughtExceptionsMap.keySet() )
		{
			CatchClause catchClause = caughtExceptionsMap.get( caughtExceptionTypeBinding );
			SingleVariableDeclaration declaration = catchClause.getException();
			Type type = declaration.getType();
			if ( type instanceof UnionType )
			{
				// UnionType: i.e: catch (ExecutionException | TimeoutException e)
				cleanUnionTypes( neededExceptionsTypes, catchClause, (UnionType) type );

			}
			else // SimpleType: i.e: catch (Exception e)
			{
				cleanSimpleTypes( neededExceptionsTypes, caughtExceptionTypeBinding, catchClause );
			}
		}
	}

	private static void cleanUnionTypes( Set<ITypeBinding> neededExceptionsTypes, CatchClause catchClause, UnionType unionType )
	{
		List simpleTypes = unionType.types();
		List removedTypes = new ArrayList();
		for ( Object singleType : simpleTypes )
		{
			SimpleType simpleType = (SimpleType) singleType;
			ITypeBinding simpleTypeBinding = simpleType.resolveBinding();
			for ( ITypeBinding neededExceptionTypeBinding : neededExceptionsTypes )
			{
				if ( !ASTUtil.isTypeOf( neededExceptionTypeBinding, simpleTypeBinding.getBinaryName() ) )
				{
					removedTypes.add( singleType );
				}
			}
		}
		if ( removedTypes.size() == simpleTypes.size() )
		{
			catchClause.delete();
		}
		else
		{
			simpleTypes.removeAll( removedTypes );
		}
	}

	private static void cleanSimpleTypes( Set<ITypeBinding> neededExceptionsTypes, ITypeBinding caughtExceptionTypeBinding, CatchClause catchClause )
	{
		boolean deleteCatchClause = true;
		for ( ITypeBinding neededExceptionTypeBinding : neededExceptionsTypes )
		{
			if ( ASTUtil.isTypeOf( neededExceptionTypeBinding, caughtExceptionTypeBinding.getBinaryName() ) )
			{
				deleteCatchClause = false;
				break;
			}
		}

		if ( deleteCatchClause )
		{
			catchClause.delete();
		}
	}

	private static boolean matchesTargetMethod( String methodName, String classBinaryName, IMethodBinding methodBinding )
	{
		if ( methodBinding == null )
		{
			return false;
		}
		String bindingName = methodBinding.getName();
		String className = methodBinding.getDeclaringClass().getBinaryName();

		return methodName.equals( bindingName ) && classBinaryName.equals( className );
	}
}