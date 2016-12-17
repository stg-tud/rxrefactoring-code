package rxjavarefactoring.analyzers;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;

import rxjavarefactoring.framework.utils.ASTUtil;

/**
 * Description: This class builds a tree containing the links
 * between {@link MethodInvocation} and the object ({@link ClassInstanceCreation})
 * where the invocation is done.<br>
 *
 * Limitations: <br>
 * It only keeps track of method invoked in a {@link SimpleName}.
 * i.e: <br>
 * Foo foo = new Foo();<br>
 * foo.doSomething(...);<br>
 * a link between foo and doSomething will be generated<br>
 *
 * This class can't manage builders<br>
 * i.e: <br>
 * StringBuilder sb = new StringBuilder();<br>
 * sb.append("Foo").append("Bar");<br>
 * only a link between sb and append("Foo") will be generated.
 * append("Bar") will be ignored.
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/25/2016
 */
// TODO: remove this class if no longer needed.
public class InstanceToInvocationVisitor extends ASTVisitor
{

	public static final String EMPTY = "";
	public static final String KEYWORD_THIS = "this.";
	private UsagesTreeNode treeRoot;
	private Map<String, ICompilationUnit> compilationUnits;
	private List<String> targetBinaryNames;

	public InstanceToInvocationVisitor( Map<String, ICompilationUnit> compilationUnits )
	{
		setupVisitor( compilationUnits );
	}

	public InstanceToInvocationVisitor( Map<String, ICompilationUnit> compilationUnits, String... targetBinaryNames )
	{
		setupVisitor( compilationUnits );
		setTargetBinaryNames( targetBinaryNames );
	}

	private void setupVisitor( Map<String, ICompilationUnit> compilationUnits )
	{
		this.compilationUnits = compilationUnits;
		this.treeRoot = new UsagesTreeNode<>( null );
	}

	public void setTargetBinaryNames( String... targetBinaryNames )
	{
		this.targetBinaryNames = Arrays.asList( targetBinaryNames );
	}

	public UsagesTreeNode getTreeRoot()
	{
		return treeRoot;
	}

	@Override
	public boolean visit( ClassInstanceCreation classInstanceCreation )
	{
		if ( targetBinaryNames != null && !ASTUtil.isTypeOf( classInstanceCreation, targetBinaryNames ) )
		{
			// ignore this class instance creation
			return true;
		}
		TypeDeclaration parentTypeDecl = ASTUtil.findParent( classInstanceCreation, TypeDeclaration.class );
		// Create Link Root -> TypeDeclaration
		UsagesTreeNode<TypeDeclaration> rootToTypeDecl = new UsagesTreeNode<>( parentTypeDecl );
		this.treeRoot.addChild( rootToTypeDecl );

		// Create Link TypeDeclaration -> ClassInstanceCreation
		// Result: Root -> TypeDeclaration -> ClassInstanceCreation
		UsagesTreeNode<ClassInstanceCreation> typeDeclToInstance = new UsagesTreeNode( classInstanceCreation );
		rootToTypeDecl.addChild( typeDeclToInstance );

		VariableDeclaration varDecl = ASTUtil.findParent( classInstanceCreation, VariableDeclaration.class );
		Assignment assignment = ASTUtil.findParent( classInstanceCreation, Assignment.class );
		Block parentBlock = ASTUtil.findParent( classInstanceCreation, Block.class );
		if ( varDecl != null ) // Foo foo = new Foo();
		{
			// Create Link ClassInstanceCreation -> VariableDeclaration
			// Result: Root -> TypeDeclaration -> ClassInstanceCreation -> VariableDeclaration
			UsagesTreeNode<VariableDeclaration> instanceToVarDecl = new UsagesTreeNode<>( varDecl );
			typeDeclToInstance.addChild( instanceToVarDecl );

			// Find usages of the class instance creation based on the name "variableName"
			String variableName = varDecl.resolveBinding().getName();
			parentBlock.accept( getVariableVisitor( instanceToVarDecl, variableName, VariableType.SIMPLE_NAME ) );
		}
		else if ( assignment != null )
		{
			// Create Link ClassInstanceCreation -> Assignment
			// Result: Root -> TypeDeclaration -> ClassInstanceCreation -> Assignment
			UsagesTreeNode<Assignment> instanceToAssignment = new UsagesTreeNode<>( assignment );
			typeDeclToInstance.addChild( instanceToAssignment );

			Expression leftHandSide = assignment.getLeftHandSide();
			if ( leftHandSide instanceof SimpleName )
			{
				// Find usages of the class instance creation based on "variableName"
				String variableName = ( (SimpleName) leftHandSide ).getIdentifier();
				parentBlock.accept( getVariableVisitor( instanceToAssignment, variableName, VariableType.SIMPLE_NAME ) );
			}
			else if ( leftHandSide instanceof FieldAccess )
			{
				// Find usages of the class instance creation based on "variableName"
				String fieldName = ( (FieldAccess) leftHandSide ).getName().toString();
				parentBlock.accept( getVariableVisitor( instanceToAssignment, fieldName, VariableType.FIELD_ACCESS ) );
			}

		}
		else // new Foo();
		{
			Statement parentStatement = ASTUtil.findParent( classInstanceCreation, Statement.class );
			parentStatement.accept( new ASTVisitor()
			{
				@Override
				public boolean visit( MethodInvocation methodInvocation )
				{
					// Add all method invocations that are not inside of the ClassInstanceCreation
					ClassInstanceCreation instanceCreationParent = ASTUtil.findParent( methodInvocation, ClassInstanceCreation.class );
					Expression expression = methodInvocation.getExpression();
					if ( ( instanceCreationParent == null || instanceCreationParent != classInstanceCreation ) &&
							!( expression instanceof SimpleName ) )
					{
						// Create Link ClassInstaceCreation -> MethodInvocation
						// Result: Root -> TypeDeclaration -> ClassInstanceCreation -> MethodInvocation
						UsagesTreeNode<MethodInvocation> instanceToMethodInv = new UsagesTreeNode<>( methodInvocation );
						typeDeclToInstance.addChild( instanceToMethodInv );
					}
					return true;
				}
			} );

		}
		return true;
	}

	/**
	 * Finds usages of an object base on a reference to the object {@link VariableDeclaration} and the variableName
	 * 
	 * @param nodeToVarDecl
	 *            node where the children found will be added
	 * @param variableName
	 *            name of the analyzed variable
	 * @param variableType
	 * @return a visitor that updates the {@link this#treeRoot}
	 */
	private ASTVisitor getVariableVisitor( final UsagesTreeNode nodeToVarDecl, final String variableName, VariableType variableType )
	{
		return new ASTVisitor()
		{
			@Override
			public boolean visit( MethodInvocation methodInvocation )
			{
				// If the caller's name equals variableName
				// then the method invocation should be added to the tree
				Expression callerExpression = methodInvocation.getExpression();
				String callerName = callerExpression.toString().replace( KEYWORD_THIS, EMPTY );
				if ( callerExpression != null && variableName.equals( callerName ) )
				{
					// Create Link VariableDeclaration -> MethodInvocation
					// Result: Root -> TypeDeclaration -> ClassInstanceCreation -> VariableDeclaration -> MethodInvocation
					UsagesTreeNode<MethodInvocation> varDeclToMethodInv = new UsagesTreeNode<>( methodInvocation );
					nodeToVarDecl.addChild( varDeclToMethodInv );
				}
				else
				{
					// If an argument in the method invocation matches variableName
					// then do the following:
					// 1. Find the declaring class of the method
					// 2. Check the name of the variable used in the method declaration
					// 3. Repeat the search with the new variable declaration and name in the current method

					int counter = -1;
					// Checking the name of all arguments
					for ( Object argument : methodInvocation.arguments() )
					{
						counter++;
						String argumentName = argument.toString().replace( KEYWORD_THIS, EMPTY );
						if ( variableName.equals( argumentName ) )
						{
							// Matching argument was found
							final int argumentIndex = counter;
							ITypeBinding declaringClass = methodInvocation.resolveMethodBinding().getDeclaringClass();

							// Searching the declaring class
							for ( ICompilationUnit icu : compilationUnits.values() )
							{
								CompilationUnit cu = new RefactoringASTParser( AST.JLS8 ).parse( icu, true );
								cu.accept( getTypeDeclVisitor( nodeToVarDecl, declaringClass, cu, methodInvocation, argumentIndex ) );
							}
						}
					}
				}
				return true;
			}

			@Override
			public boolean visit( VariableDeclarationFragment varDeclFrag )
			{
				Expression initializer = varDeclFrag.getInitializer();
				if ( initializer instanceof SimpleName )
				{
					if ( variableName.equals( ( (SimpleName) initializer ).getIdentifier() )
							&& !variableType.equals( VariableType.FIELD_ACCESS )
							&& !variableType.equals( VariableType.QUALIFIED_NAME ) )
					{
						// Create Link VariableDeclaration -> VariableDeclarationFragment
						// Result: Root -> TypeDeclaration -> ClassInstanceCreation -> VariableDeclaration -> VariableDeclarationFragment
						UsagesTreeNode<VariableDeclarationFragment> varDeclToVarDeclFrag = new UsagesTreeNode<>( varDeclFrag );
						nodeToVarDecl.getPredecessor().addChild( varDeclToVarDeclFrag );
						Block parentBlock = ASTUtil.findParent( varDeclFrag, Block.class );
						String newVariableName = varDeclFrag.resolveBinding().getName();
						parentBlock.accept( getVariableVisitor( varDeclToVarDeclFrag, newVariableName, VariableType.SIMPLE_NAME ) );
					}
				}
				return true;
			}

			@Override
			public boolean visit( Assignment assignment )
			{
				Expression rightHandSide = assignment.getRightHandSide();
				if ( rightHandSide instanceof SimpleName )
				{
					if ( variableName.equals( ( (SimpleName) rightHandSide ).getIdentifier() ) )
					{
						// Create Link VariableDeclaration -> VariableDeclarationFragment
						// Result: Root -> TypeDeclaration -> ClassInstanceCreation -> VariableDeclaration -> Assignment
						UsagesTreeNode<Assignment> varDeclToAssigment = new UsagesTreeNode<>( assignment );
						nodeToVarDecl.getPredecessor().addChild( varDeclToAssigment );
						Expression leftHandSide = assignment.getLeftHandSide();
						Block parentBlock = ASTUtil.findParent( assignment, Block.class );
						if ( leftHandSide instanceof SimpleName )
						{
							String newVariableName = ( (SimpleName) leftHandSide ).getIdentifier();
							if ( !variableName.equals( newVariableName ) )
							{
								parentBlock.accept( getVariableVisitor( varDeclToAssigment, newVariableName, VariableType.SIMPLE_NAME ) );
							}
						}
						if ( leftHandSide instanceof FieldAccess )
						{
							String fieldName = ( (FieldAccess) leftHandSide ).getName().toString();
							if ( !variableName.equals( fieldName ) || !variableType.equals( VariableType.FIELD_ACCESS ) )
							{
								parentBlock.accept( getVariableVisitor( varDeclToAssigment, fieldName, VariableType.FIELD_ACCESS ) );
							}
						}
						if ( leftHandSide instanceof QualifiedName )
						{
							String qualifiedName = ( (QualifiedName) leftHandSide ).getName().toString();
							if ( !variableName.equals( qualifiedName ) || !variableType.equals( VariableType.QUALIFIED_NAME ) )
							{
								parentBlock.accept( getVariableVisitor( varDeclToAssigment, qualifiedName, VariableType.QUALIFIED_NAME ) );
							}
						}
					}
				}
				return true;
			}
		};
	}

	private ASTVisitor getTypeDeclVisitor(
			final UsagesTreeNode nodeToVarDecl,
			final ITypeBinding declaringClass,
			final CompilationUnit cu,
			final MethodInvocation methodInvocation,
			final int argumentIndex )
	{
		return new ASTVisitor()
		{
			@Override
			public boolean visit( TypeDeclaration typeDeclaration )
			{
				if ( typeDeclaration.resolveBinding().getBinaryName().equals( declaringClass.getBinaryName() ) )
				{
					// Searching the target method declaration
					cu.accept( getMethodDeclVisitor( nodeToVarDecl, methodInvocation, argumentIndex ) );
				}
				return true;
			}
		};
	}

	private ASTVisitor getMethodDeclVisitor(
			final UsagesTreeNode nodeToVarDecl,
			MethodInvocation methodInvocation,
			final int argumentIndex )
	{
		return new ASTVisitor()
		{
			@Override
			public boolean visit( MethodDeclaration methodDeclaration )
			{
				boolean targetMethod = ASTUtil.matchesSignature( methodDeclaration, methodInvocation );
				if ( targetMethod )
				{
					// Target method declaration found

					// Create Link: VariableName -> MethodDeclaration
					// Result: Root -> TypeDeclaration -> ClassInstanceCreation -> VariableDeclaration -> MethodDeclaration
					UsagesTreeNode<MethodDeclaration> varDeclToMethodDecl = new UsagesTreeNode<>( methodDeclaration );
					nodeToVarDecl.addChild( varDeclToMethodDecl );

					// Determine new variable name and repeat recursively
					String newVariableName = ASTUtil.getVariableName( methodDeclaration, argumentIndex );
					methodDeclaration.accept( getSingleVarDeclVisitor( varDeclToMethodDecl, methodDeclaration, newVariableName ) );
				}
				return true;
			}
		};
	}

	private ASTVisitor getSingleVarDeclVisitor(
			final UsagesTreeNode<MethodDeclaration> varDeclToMethodDecl,
			final MethodDeclaration methodDeclaration,
			final String newVariableName )
	{
		return new ASTVisitor()
		{
			@Override
			public boolean visit( SingleVariableDeclaration singleVar )
			{
				if ( singleVar.getName().toString().equals( newVariableName ) )
				{
					// Create Link: MethodDeclaration -> SingleVariableDeclaration
					// Result: Root -> TypeDeclaration -> ClassInstanceCreation -> VariableDeclaration -> MethodDeclaration -> SingleVariableDeclaration
					UsagesTreeNode<SingleVariableDeclaration> methodDeclToVarDel = new UsagesTreeNode<>( singleVar );
					varDeclToMethodDecl.addChild( methodDeclToVarDel );

					// VariableName found: run algorithm again
					methodDeclaration.accept( getVariableVisitor( methodDeclToVarDel, newVariableName, VariableType.SIMPLE_NAME ) ); // recursive call
					return false; // the variable name is unique. No need to keep checking
				}
				return true;
			}
		};
	}

	private enum VariableType
	{
		SIMPLE_NAME( SimpleName.class ), FIELD_ACCESS( FieldAccess.class ), QUALIFIED_NAME( QualifiedName.class );

		Class type;

		VariableType( Class type )
		{
			this.type = type;
		}

		public Class getType()
		{
			return type;
		}
	}
}
