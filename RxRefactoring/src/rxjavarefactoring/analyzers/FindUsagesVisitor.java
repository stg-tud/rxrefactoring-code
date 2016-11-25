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
		this.treeRoot = new UsagesTreeNode<>( null, null );
	}

	@Override
	public boolean visit( ClassInstanceCreation classInstanceCreation )
	{
		TypeDeclaration parentTypeDecl = ASTUtil.findParent( classInstanceCreation, TypeDeclaration.class );
		// Create link Root -> TypeDeclaration
		UsagesTreeNode<ASTNode, TypeDeclaration> rootToTypeDecl = new UsagesTreeNode<>( null, parentTypeDecl );
		this.treeRoot.addChild( rootToTypeDecl );

		// Create Link TypeDeclaration -> ClassInstanceCreation
		// Result: Root -> TypeDeclaration -> ClassInstanceCreation
		UsagesTreeNode<TypeDeclaration, ClassInstanceCreation> typeDeclToInstance = new UsagesTreeNode( parentTypeDecl, classInstanceCreation );
		rootToTypeDecl.addChild( typeDeclToInstance );

		VariableDeclaration varDecl = ASTUtil.findParent( classInstanceCreation, VariableDeclaration.class );
		boolean instanceCached = varDecl != null;
		if ( instanceCached ) // Foo foo = new Foo();
		{
			final String variableName = varDecl.resolveBinding().getName();
			// Create Link ClassInstanceCreation -> VariableDeclaration
			// Result: Root -> TypeDeclaration -> ClassInstanceCreation -> VariableDeclaration
			UsagesTreeNode<ClassInstanceCreation, VariableDeclaration> instanceToVarDecl = new UsagesTreeNode<>( classInstanceCreation, varDecl );
			typeDeclToInstance.addChild( instanceToVarDecl );

			Block parentBlock = ASTUtil.findParent( classInstanceCreation, Block.class );

			parentBlock.accept( getVisitor( instanceToVarDecl, varDecl, variableName ) );

		}
		else // new Foo();
		{

		}
		return true;
	}

	private ASTVisitor getVisitor( final UsagesTreeNode nodeToVarDecl, final VariableDeclaration varDecl, final String variableName )
	{
		return new ASTVisitor()
		{
			@Override
			public boolean visit( MethodInvocation methodInvocation )
			{
				// The caller's name equals variableName
				Expression callerName = methodInvocation.getExpression();
				if ( callerName != null && variableName.equals( callerName.toString() ) )
				{
					// Create Link VariableDeclaration -> MethodInvocation
					// Result: Root -> TypeDeclaration -> ClassInstanceCreation -> VariableDeclaration -> MethodInvocation
					UsagesTreeNode<VariableDeclaration, MethodInvocation> varDeclToMethodInv = new UsagesTreeNode<>( varDecl, methodInvocation );
					nodeToVarDecl.addChild( varDeclToMethodInv );
				}
				else // argument in invocation equals variableName
				{
					int counter = -1;
					for ( Object argument : methodInvocation.arguments() )
					{
						counter++;
						if ( variableName.equals( argument.toString() ) )
						{
							final int argumentIndex = counter;
							ITypeBinding declaringClass = methodInvocation.resolveMethodBinding().getDeclaringClass();
							for ( ICompilationUnit icu : compilationUnits )
							{
								CompilationUnit cu = new RefactoringASTParser( AST.JLS8 ).parse( icu, true );
								cu.accept( new ASTVisitor()
								{
									@Override
									public boolean visit( TypeDeclaration typeDeclaration )
									{
										if ( typeDeclaration.resolveBinding().getBinaryName().equals( declaringClass.getBinaryName() ) )
										{
											String className = declaringClass.getBinaryName();
											String methodName = methodInvocation.getName().toString();
											cu.accept( new ASTVisitor()
											{
												@Override
												public boolean visit( MethodDeclaration methodDeclaration )
												{
													boolean targetMethod = ASTUtil.matchesTargetMethod( methodDeclaration, methodName, className );
													if ( targetMethod )
													{
														// Create Link: VariableName -> MethodDeclaration
														// Result: Root -> TypeDeclaration -> ClassInstanceCreation -> VariableDeclaration -> MethodDeclaration
														UsagesTreeNode<VariableDeclaration, MethodDeclaration> varDeclToMethodDecl = new UsagesTreeNode<>( varDecl, methodDeclaration );
														nodeToVarDecl.addChild( varDeclToMethodDecl );

														String newVariableName = ASTUtil.getVariableName( methodDeclaration, argumentIndex );
														methodDeclaration.accept( new ASTVisitor()
														{
															@Override
															public boolean visit( SingleVariableDeclaration singleVar )
															{
																if ( singleVar.getName().toString().equals( newVariableName ) )
																{
																	// Create Link: MethodDeclaration -> SingleVariableDeclaration
																	// Result: Root -> TypeDeclaration -> ClassInstanceCreation -> VariableDeclaration -> MethodDeclaration -> SingleVariableDeclaration
																	UsagesTreeNode<MethodDeclaration, SingleVariableDeclaration> methodDeclToVarDel = new UsagesTreeNode<>( methodDeclaration, singleVar );
																	varDeclToMethodDecl.addChild( methodDeclToVarDel );

																	// VariableName found: run algorithm again
																	methodDeclaration.accept( getVisitor( methodDeclToVarDel, singleVar, newVariableName ) );
																}
																return true;
															}
														} );
													}
													return true;
												}
											} );
										}
										return true;
									}
								} );
							}
						}
					}
				}
				return true;
			}

			@Override
			public boolean visit( Assignment node )
			{
				// Case 3: variable name is assign to another object
				return true;
			}
		};
	}

	public UsagesTreeNode getTreeRoot()
	{
		return treeRoot;
	}
}
