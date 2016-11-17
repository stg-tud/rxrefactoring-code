package rxjavarefactoring.framework.utils;

import org.eclipse.jdt.core.dom.*;

import rxjavarefactoring.framework.exceptions.RxInvalidSyntaxException;

/**
 * Description: Helper class to create {@link ASTNode} objects from text.<br>
 * General Steps:<br>
 * <ol>
 * <li>Generate an auxiliary class that contains the target code</li>
 * <li>Parse the auxiliary class using {@link ASTParser}</li>
 * <li>Create {@link CompilationUnit} using the parser</li>
 * <li>Extract the relevant {@link ASTNode} using a {@link ASTVisitor}</li>
 * <li>Return the {@link ASTNode}</li>
 * </ol>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/10/2016
 */
public final class CodeFactory extends ASTVisitor
{
	private MethodDeclaration methodDeclaration;

	private CodeFactory()
	{
		// This class should not be instantiated
	}

	@Override
	public boolean visit( MethodDeclaration node )
	{
		methodDeclaration = node;
		return false;
	}

	/**
	 * Creates a {@link Statement} given its source code (without ";")
	 * 
	 * @param targetAST
	 *            target ast of level {@link AST#JLS8}
	 * @param statement
	 *            statement source code. It must have a valid syntax. It cannot
	 *            contain a ";".
	 * @return a {@link Statement} based on the source code
	 */
	public static Statement createSingleStatementFromTest( AST targetAST, String statement )
	{
		if ( statement.replace( ";", "" ).length() < statement.length() - 1 )
		{
			throw new RxInvalidSyntaxException( "String contains \";\". Single statement expected", statement );
		}
		String auxMethodStart = "public void auxMethod() {";
		String auxMethodEnd = "}";
		String auxMethod = auxMethodStart + statement + auxMethodEnd;
		MethodDeclaration methodFromText1 = createMethodFromText( targetAST, auxMethod );
		return (Statement) ASTNode.copySubtree( targetAST, (ASTNode) methodFromText1.getBody().statements().get( 0 ) );
	}

	/**
	 * Creates a {@link Block} given its source code
	 * 
	 * @param targetAST
	 *            target ast of level {@link AST#JLS8}
	 * @param codeBlock
	 *            code block source code. It must have a valid syntax
	 * @return a {@link Block} based on the source code
	 */
	public static Block createStatementsBlockFromText( AST targetAST, String codeBlock )
	{
		String auxMethodStart = "public void auxMethod() {";
		String auxMethodEnd = "}";
		String auxMethod = auxMethodStart + codeBlock + auxMethodEnd;
		MethodDeclaration methodFromText1 = createMethodFromText( targetAST, auxMethod );
		return (Block) ASTNode.copySubtree( targetAST, methodFromText1.getBody() );
	}

	/**
	 * Creates a {@link MethodDeclaration} given its source code
	 * 
	 * @param targetAST
	 *            target ast of level {@link AST#JLS8}
	 * @param method
	 *            method source code. It must have a valid syntax
	 * @return a {@link MethodDeclaration} based on the source code
	 */
	public static MethodDeclaration createMethodFromText( AST targetAST, String method )
	{
		String auxClassStart = "public class AuxClass { ";
		String auxClassEnd = "}";
		String auxClass = auxClassStart + method + auxClassEnd;

		ASTParser javaParser = ASTParser.newParser( AST.JLS8 );
		javaParser.setSource( auxClass.toCharArray() );
		CompilationUnit compilationUnit = (CompilationUnit) javaParser.createAST( null );
		CodeFactory visitor = new CodeFactory();
		compilationUnit.accept( visitor );
		MethodDeclaration methodDeclaration = visitor.methodDeclaration;
		return (MethodDeclaration) ASTNode.copySubtree( targetAST, methodDeclaration );
	}
}
