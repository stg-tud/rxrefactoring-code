package de.tudarmstadt.rxrefactoring.core.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.UnionType;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import de.tudarmstadt.rxrefactoring.core.logging.Log;
import de.tudarmstadt.rxrefactoring.core.parser.RewriteCompilationUnit;

/**
 * Defines utility methods for ASTs.
 *
 * @see ASTNode
 * 
 * @author Grebiel Jose Ifill Brito, Mirko Köhler
 */
public final class ASTUtils {
	
	private ASTUtils() {
		// This class should not be instantiated
	}

	/**
	 * Find the parent of a node given the target class.
	 * If the given node is already of the target class,
	 * then this node is returned.
	 *
	 * @param node
	 *            The source node whose class should be found.
	 * @param target
	 *            The class of the parent node that should be found (i.e. VariableDeclaration.class).
	 * @param <T>
	 *            The type of the node that is returned.
	 *            
	 * @return The parent node based on the target, or the given node if it is already an
	 * instance of the given class, or null if no matching parent could be found.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends ASTNode> T findParent( ASTNode node, Class<T> target )	{
		Objects.requireNonNull(node, "Argument node can not be null.");
		Objects.requireNonNull(target, "Argument target can not be null.");
		
		//If node 
		if (target.isInstance(node)) {
			return (T) node;
		}

		ASTNode parent = node.getParent();
		while ( parent != null && !target.isInstance(parent)) {
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
		if ( type == null )
		{
			return false;
		}
		ITypeBinding superClass = null;
		if ( type instanceof TypeDeclaration )
		{
			ITypeBinding typeBinding = ( (TypeDeclaration) type ).resolveBinding();
			if ( typeBinding == null )
			{
				return false;
			}
			superClass = typeBinding.getSuperclass();
		}
		else if ( type instanceof AnonymousClassDeclaration )
		{
			ITypeBinding typeBinding = ( (AnonymousClassDeclaration) type ).resolveBinding();
			if ( typeBinding == null )
			{
				return false;
			}
			superClass = typeBinding.getSuperclass();
		}
		else if ( type instanceof ClassInstanceCreation )
		{
			ITypeBinding typeBinding = ( (ClassInstanceCreation) type ).getType().resolveBinding();
			if ( typeBinding == null )
			{
				return false;
			}
			superClass = typeBinding.getSuperclass();
		}
		else if ( type instanceof FieldDeclaration )
		{
			ITypeBinding typeBinding = ( (FieldDeclaration) type ).getType().resolveBinding();
			if ( typeBinding == null )
			{
				return false;
			}
			superClass = typeBinding.getSuperclass();
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
	 * Checks whether an AST contains a node that fulfills the given
	 * predicate.
	 * 
	 * @param root The root node of the AST.
	 * @param predicate The predicate to be checked.
	 * @return The first encountered AST node where predicate.apply(n) == true, or an empty Optional if no node has been found.
	 */
	public static Optional<ASTNode> findNode(ASTNode root, Function<ASTNode, Boolean> predicate) {		
		
		class Visitor extends ASTVisitor {
			ASTNode result = null;
					
			public boolean preVisit2(ASTNode node) {
				//Log.info(getClass(), "Visit node: " + node);
				
				if (Objects.isNull(result) && predicate.apply(node)) result = node;
				
				return Objects.isNull(result);
			}
		}
		
		Visitor v = new Visitor();		
		root.accept(v);		
		return Optional.ofNullable(v.result);
	}
	
	public static boolean containsNode(ASTNode root, Function<ASTNode, Boolean> predicate) {
		return findNode(root, predicate).isPresent();
	}
	
	/**
	 * Checks if the given types qualified name matches the given regex.
	 * If the types binding can not be resolved, then this method returns
	 * false.
	 * 
	 * @param regex The regex to check against.
	 * @param type The type to check.
	 * 
	 * @return True, if the type's binding can be resolved and its qualified
	 * name matches the regex.
	 * 
	 * @see ASTUtils#matchType(String, ITypeBinding)
	 * 
	 */
	public static boolean matchType(String regex, Type type) {
		ITypeBinding tb = type.resolveBinding();
		
		if (Objects.isNull(tb)) {
			Log.info(ASTUtils.class, "Type binding was null for " + type);
			return false;
		}
		
		return matchType(regex, tb);		
	}
	
	public static boolean matchType(String regex, ITypeBinding typeBinding) {			
		//Objects.requireNonNull(typeBinding, "The type binding can not be null. regex was " + regex);
		
		//Log.info(ASTUtils.class, "### Type : " + typeBinding.getQualifiedName() + " ###");
		boolean result = Pattern.matches(regex, typeBinding.getQualifiedName());
		//RxLogger.info(ASTUtil.class, "regex = " + regex + " matches? " + result);
		return result;
	}
	
	public static final int SIGNATURE_MATCH = 0;
	public static final int SIGNATURE_UNMATCH = 1;
	public static final int SIGNATURE_UNAVAILABLE = 2;


	
	public static int matchSignature(IMethodBinding mb, String classRegex, String methodName, String returnTypeRegex, String... parameterTypeRegexes) {
			
		if (mb != null) {
			ITypeBinding[] mbParameters = mb.getParameterTypes();				
			
//			Log.info(ASTUtils.class, "Class " + mb.getDeclaringClass().getQualifiedName() + " match " + matchType(classRegex, mb.getDeclaringClass()));
//			Log.info(ASTUtils.class, "Return " + mb.getReturnType().getQualifiedName() + " match " + matchType(returnTypeRegex, mb.getReturnType()));
//			Log.info(ASTUtils.class, "Name " + mb.getName());
//			Log.info(ASTUtils.class, "Return " + Arrays.toString(mbParameters));

			
			boolean result = matchType(classRegex, mb.getDeclaringClass()) 
					&& matchType(returnTypeRegex, mb.getReturnType()) 
					&& mb.getName().equals(methodName)
					&& parameterTypeRegexes.length == mbParameters.length;
			
			if (!result) return SIGNATURE_UNMATCH;
			
			for (int i = 0; i < parameterTypeRegexes.length; i++) {
				result = result && matchType(parameterTypeRegexes[i], mbParameters[i]);
			}		
			
			return result ? SIGNATURE_MATCH : SIGNATURE_UNMATCH;	
			
		} else {
			
			return SIGNATURE_UNAVAILABLE;
		}			
	}
	
	public static boolean matchSignature(MethodInvocation inv, String className, String methodName, String returnType, String... parameterTypes) {
		IMethodBinding mb = inv.resolveMethodBinding();
		switch (matchSignature(mb, className, methodName, returnType, parameterTypes)) {
			case SIGNATURE_MATCH : 
				return true;
			case SIGNATURE_UNMATCH : 
				return false;
			case SIGNATURE_UNAVAILABLE : 
				//Log.error(ASTUtils.class, "Note: methodbinding for " + inv.getName().getIdentifier() + " not available!");
				return inv.getName().getIdentifier().equals(methodName);
			default :
				throw new IllegalStateException("Unexpected return value from matchSignature.");
		}
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
		if ( type == null )
		{
			return false;
		}
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
		if ( type == null )
		{
			return false;
		}
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
		if ( parameterIndex >= methodDeclaration.parameters().size() )
		{
			return null;
		}
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
	public static boolean matchesTargetMethod(SuperMethodInvocation node, String methodName, String classBinaryName )
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
//	@Deprecated
//	public static void replaceInStatement( ASTNode originalNode, ASTNode newNode )
//	{
//		Block block = ASTUtils.findParent( originalNode, Block.class );
//		if ( block != null )
//		{
//			String originalNodeString = originalNode.toString();
//			String newNodeString = newNode.toString();
//
//			ASTNode originalStatement = originalNode;
//			if ( !( originalNode instanceof Statement ) )
//			{
//				originalStatement = ASTUtils.findParent( originalNode, Statement.class );
//			}
//			String originalStatementString = originalStatement.toString();
//			String newStatementString = originalStatementString.replace( originalNodeString, newNodeString );
//			Statement newStatement = ASTNodeFactory.createSingleStatementFromText( block.getAST(), newStatementString );
//
//			int position = block.statements().indexOf( originalStatement );
//			originalStatement.delete();
//			block.statements().add( position, newStatement );
//		}
//	}

	/**
	 * Removes all "super." char sequences from a given node. The node
	 * must be part of a statement
	 * 
	 * @param node
	 *            source node
	 * @param <T>
	 *            type of the node
	 */
	
//	public static <T extends ASTNode> void removeSuperKeywordInStatement( T node )
//	{
//		Statement statement = ASTUtils.findParent( node, Statement.class );
//		String newStatementString = statement.toString().replace( "super.", "" );
//		Statement newStatement = ASTNodeFactory.createSingleStatementFromText( node.getAST(), newStatementString );
//		replaceInStatement( statement, newStatement );
//	}

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
//	@Deprecated
//	public static <T extends ASTNode> T clone( T node )
//	{
//		if ( node == null )
//		{
//			return null;
//		}
//		return (T) ASTNode.copySubtree( node.getAST(), node );
//	}

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
//	public static ASTNode clone( Object astNode )
//	{
//		if ( astNode == null )
//		{
//			return null;
//		}
//		ASTNode node = (ASTNode) astNode;
//		return clone( node );
//	}

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
//	public static <T extends ASTNode> void replaceByBlock( T node, Block block )
//	{
//		if ( node != null && block != null )
//		{
//			Block parentBlock = ASTUtils.findParent( node, Block.class );
//			if ( parentBlock == null )
//			{
//				throw new IllegalArgumentException( "The node " + node.toString() + " must be inside of a AST Block" );
//			}
//			int nodePosition = parentBlock.statements().indexOf( node );
//			node.delete();
//			for ( Object statementObject : block.statements() )
//			{
//				parentBlock.statements().add( nodePosition++, ASTUtils.clone( statementObject ) );
//			}
//		}
//	}

	/**
	 * Checks what exceptions are thrown inside of try-catch blocks and removes
	 * unnecessary catch clauses.
	 * 
	 * @param block
	 *            the block where the try-catch blocks are found
	 */
	public static void removeUnnecessaryCatchClauses(RewriteCompilationUnit unit, Block block)
	{
		
		class TryStatementVisitor extends ASTVisitor
		{
			private Map<TryStatement, Set<ITypeBinding>> neededExceptionsTypesMap;
			private Map<TryStatement, Map<ITypeBinding, CatchClause>> caughtExceptionsMap;
			private Map<TryStatement, Block> tryBodyMap;

			public TryStatementVisitor()
			{
				neededExceptionsTypesMap = new HashMap<>();
				caughtExceptionsMap = new HashMap<>();
				tryBodyMap = new HashMap<>();
			}

			@Override
			public boolean visit( TryStatement node )
			{
				TryBodyVisitor tryBodyVisitor = new TryBodyVisitor();
				TryCatchClausesVisitor tryCatchClausesVisitor = new TryCatchClausesVisitor();
				node.accept( tryBodyVisitor );
				node.accept( tryCatchClausesVisitor );
				neededExceptionsTypesMap.put( node, tryBodyVisitor.getNeededExceptionsTypes() );
				caughtExceptionsMap.put( node, tryCatchClausesVisitor.getCaughtExceptionsMap() );
				tryBodyMap.put( node, unit.copyNode( node.getBody() ) );
				return true;
			}

			public Map<TryStatement, Set<ITypeBinding>> getNeededExceptionsTypesMap()
			{
				return neededExceptionsTypesMap;
			}

			public Map<TryStatement, Map<ITypeBinding, CatchClause>> getCaughtExceptionsMap()
			{
				return caughtExceptionsMap;
			}

			public Map<TryStatement, Block> getTryBodyMap()
			{
				return tryBodyMap;
			}
			
			
			/**
			 * Description: Analyzes the body of a try catch block<br>
			 * Author: Grebiel Jose Ifill Brito<br>
			 * Created: 11/17/2016
			 */
			class TryBodyVisitor extends ASTVisitor
			{
				private Set<ITypeBinding> neededExceptionsTypes;

				TryBodyVisitor()
				{
					neededExceptionsTypes = new HashSet<>();
				}

				@Override
				public boolean visit( MethodInvocation node )
				{
					if ( isInTryBlock( node ) )
					{
						IMethodBinding methodBinding = node.resolveMethodBinding();
						if ( methodBinding != null )
						{
							ITypeBinding[] exceptionTypes = methodBinding.getExceptionTypes();
							neededExceptionsTypes.addAll( Arrays.asList( exceptionTypes ) );
						}
					}
					return true;
				}

				Set<ITypeBinding> getNeededExceptionsTypes()
				{
					return neededExceptionsTypes;
				}

				private boolean isInTryBlock( MethodInvocation node )
				{
					return ASTUtils.findParent( node, CatchClause.class ) == null;
				}
			}
			
			
			/**
			 * Analyzes the catch clauses of a try-catch block.
			 * 
			 * @author grebiel, mirko
			 *
			 */
			class TryCatchClausesVisitor extends ASTVisitor
			{
				private Map<ITypeBinding, CatchClause> caughtExceptionsMap;

				TryCatchClausesVisitor()
				{
					caughtExceptionsMap = new HashMap<>();
				}

				@Override
				public boolean visit( CatchClause node )
				{
					SingleVariableDeclaration exception = node.getException();
					Type exceptionType = exception.getType();
					ITypeBinding exceptionTypeBinding = exceptionType.resolveBinding();
					caughtExceptionsMap.put( exceptionTypeBinding, node );
					return true;
				}

				Map<ITypeBinding, CatchClause> getCaughtExceptionsMap()
				{
					return caughtExceptionsMap;
				}
			}

		}
		
		if ( block != null ) {
			
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
					//TODO: flatten block into other block here
					unit.replace( tryStatement, tryBody );
				}
				else
				{
					removeIrrelevantCatchClauses(unit, neededExceptionsTypes, caughtExceptionsMap );

				}
			}
		}
	}
	
	public static Type typeFromBinding(AST ast, ITypeBinding typeBinding) {
	    if( ast == null ) 
	        throw new NullPointerException("ast is null");
	    if( typeBinding == null )
	        throw new NullPointerException("typeBinding is null");

	    if( typeBinding.isPrimitive() ) {
	        return ast.newPrimitiveType(
	            PrimitiveType.toCode(typeBinding.getName()));
	    }

	    if( typeBinding.isCapture() ) {
	        ITypeBinding wildCard = typeBinding.getWildcard();
	        org.eclipse.jdt.core.dom.WildcardType capType = ast.newWildcardType();
	        ITypeBinding bound = wildCard.getBound();
	        if( bound != null ) {
	            capType.setBound(typeFromBinding(ast, bound),
	                wildCard.isUpperbound());
	        }
	        return capType;
	    }

	    if( typeBinding.isArray() ) {
	        Type elType = typeFromBinding(ast, typeBinding.getElementType());
	        return ast.newArrayType(elType, typeBinding.getDimensions());
	    }

	    if( typeBinding.isParameterizedType() ) {
	        ParameterizedType type = ast.newParameterizedType(
	            typeFromBinding(ast, typeBinding.getErasure()));

	        @SuppressWarnings("unchecked")
	        List<Type> newTypeArgs = type.typeArguments();
	        for( ITypeBinding typeArg : typeBinding.getTypeArguments() ) {
	            newTypeArgs.add(typeFromBinding(ast, typeArg));
	        }

	        return type;
	    }

	    // simple or raw type
	    String qualName = typeBinding.getQualifiedName();
	    if( "".equals(qualName) ) {
	        throw new IllegalArgumentException("No name for type binding.");
	    }
	    return ast.newSimpleType(ast.newName(qualName));
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

	private static void removeIrrelevantCatchClauses(RewriteCompilationUnit unit, Set<ITypeBinding> neededExceptionsTypes, Map<ITypeBinding, CatchClause> caughtExceptionsMap )
	{
		for ( ITypeBinding caughtExceptionTypeBinding : caughtExceptionsMap.keySet() )
		{
			CatchClause catchClause = caughtExceptionsMap.get( caughtExceptionTypeBinding );
			SingleVariableDeclaration declaration = catchClause.getException();
			Type type = declaration.getType();
			if ( type instanceof UnionType )
			{
				// UnionType: i.e: catch (ExecutionException | TimeoutException e)
				cleanUnionTypes(unit, neededExceptionsTypes, catchClause, (UnionType) type );

			}
			else // SimpleType: i.e: catch (Exception e)
			{
				cleanSimpleTypes(unit, neededExceptionsTypes, caughtExceptionTypeBinding, catchClause );
			}
		}
	}

	private static void cleanUnionTypes(RewriteCompilationUnit unit, Set<ITypeBinding> neededExceptionsTypes, CatchClause catchClause, UnionType unionType ) {
		
		List<?> elements = unionType.types();
		List<Type> removedTypes = new LinkedList<Type>();
		
		for (Object element : elements )	{
			Type type = (Type) element;	
			
			ITypeBinding simpleTypeBinding = type.resolveBinding();
			
			for ( ITypeBinding neededExceptionTypeBinding : neededExceptionsTypes )	{
				if ( !ASTUtils.isTypeOf( neededExceptionTypeBinding, simpleTypeBinding.getBinaryName() ) ) {
					removedTypes.add( type );
				}
			}
		}
		
		if ( removedTypes.size() == elements.size() ){
			unit.remove(catchClause);
		} else {
			ListRewrite rewrite = unit.getListRewrite(unionType, UnionType.TYPES_PROPERTY);
			removedTypes.forEach(type -> rewrite.remove(type, null));
			//elements.removeAll( removedTypes );
		}
	}

	private static void cleanSimpleTypes(RewriteCompilationUnit unit, Set<ITypeBinding> neededExceptionsTypes, ITypeBinding caughtExceptionTypeBinding, CatchClause catchClause ) {
		for (ITypeBinding neededExceptionTypeBinding : neededExceptionsTypes)	{
			if (ASTUtils.isTypeOf(neededExceptionTypeBinding, caughtExceptionTypeBinding.getBinaryName())) {
				unit.remove(catchClause);
				return;
			}
		}
	}

	private static boolean matchesTargetMethod( String methodName, String classBinaryName, IMethodBinding methodBinding )
	{
		if ( methodBinding == null ) {
			return false;
		}
		
		String bindingName = methodBinding.getName();

		boolean result = Objects.equals(methodName, bindingName) && isTypeOf(methodBinding.getDeclaringClass(), classBinaryName);
		
		return result;
	}

}
