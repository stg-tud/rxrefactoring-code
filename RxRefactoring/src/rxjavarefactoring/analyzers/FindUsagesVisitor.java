package rxjavarefactoring.analyzers;

import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;

import rxjavarefactoring.framework.utils.ASTUtil;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/25/2016
 */
public class FindUsagesVisitor extends ASTVisitor
{
	private UsagesTreeNode treeRoot;
	private List<ICompilationUnit> compilationUnits;

	public FindUsagesVisitor( List<ICompilationUnit> compilationUnits )
	{
		this.compilationUnits = compilationUnits;
		this.treeRoot = new UsagesTreeNode<>( null );
	}

	// TODO: TypeDeclaration

	@Override
	public boolean visit( ClassInstanceCreation classInstanceCreation )
	{
		TypeDeclaration parentTypeDecl = ASTUtil.findParent( classInstanceCreation, TypeDeclaration.class );
		// Create link Root -> TypeDeclaration
		UsagesTreeNode<TypeDeclaration> rootToTypeDecl = new UsagesTreeNode<>( parentTypeDecl );
		this.treeRoot.addChild( rootToTypeDecl );

		// Create Link TypeDeclaration -> ClassInstanceCreation
		// Result: Root -> TypeDeclaration -> ClassInstanceCreation
		UsagesTreeNode<ClassInstanceCreation> typeDeclToInstance = new UsagesTreeNode( classInstanceCreation );
		rootToTypeDecl.addChild( typeDeclToInstance );

		VariableDeclaration varDecl = ASTUtil.findParent( classInstanceCreation, VariableDeclaration.class );
		boolean instanceCached = varDecl != null;
		if ( instanceCached ) // Foo foo = new Foo();
		{

			// Create Link ClassInstanceCreation -> VariableDeclaration
			// Result: Root -> TypeDeclaration -> ClassInstanceCreation -> VariableDeclaration
			UsagesTreeNode<VariableDeclaration> instanceToVarDecl = new UsagesTreeNode<>( varDecl );
			typeDeclToInstance.addChild( instanceToVarDecl );

			// Find usages of the class instance creation based on the name "variableName"
			Block parentBlock = ASTUtil.findParent( classInstanceCreation, Block.class );
			parentBlock.accept( getVariableVisitor( instanceToVarDecl, varDecl ) );
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
					if ( instanceCreationParent == null || instanceCreationParent != classInstanceCreation )
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
	 * @param varDecl
	 *            variable declaration of the target object
	 * @return a visitor that updates the {@link this#treeRoot}
	 */
	private ASTVisitor getVariableVisitor( final UsagesTreeNode nodeToVarDecl, final VariableDeclaration varDecl )
	{
		final String variableName = varDecl.resolveBinding().getName();
		return new ASTVisitor()
		{
			@Override
			public boolean visit( MethodInvocation methodInvocation )
			{
				// If the caller's name equals variableName
				// then the method invocation should be added to the tree
				Expression callerName = methodInvocation.getExpression();
				if ( callerName != null && variableName.equals( callerName.toString() ) )
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
						if ( variableName.equals( argument.toString() ) )
						{
							// Matching argument was found
							final int argumentIndex = counter;
							ITypeBinding declaringClass = methodInvocation.resolveMethodBinding().getDeclaringClass();

							// Searching the declaring class
							for ( ICompilationUnit icu : compilationUnits )
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
			public boolean visit( Assignment node )
			{
				// TODO:
				// Case 3: variable name is assign to another object
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
					// Declaring class found
					String className = declaringClass.getBinaryName();
					String methodName = methodInvocation.getName().toString();

					// Searching the target method declaration
					cu.accept( getMethodDeclVisitor( nodeToVarDecl, className, methodName, argumentIndex ) );
				}
				return true;
			}
		};
	}

	private ASTVisitor getMethodDeclVisitor(
			final UsagesTreeNode nodeToVarDecl,
			final String className,
			final String methodName,
			final int argumentIndex )
	{
		return new ASTVisitor()
		{
			@Override
			public boolean visit( MethodDeclaration methodDeclaration )
			{
				boolean targetMethod = ASTUtil.matchesTargetMethod( methodDeclaration, methodName, className );
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
					methodDeclaration.accept( getVariableVisitor( methodDeclToVarDel, singleVar ) ); // recursive call
				}
				return true;
			}
		};
	}

	public UsagesTreeNode getTreeRoot()
	{
		return treeRoot;
	}
}
