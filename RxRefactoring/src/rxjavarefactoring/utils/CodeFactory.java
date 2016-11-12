package rxjavarefactoring.utils;

import org.eclipse.jdt.core.dom.*;

/**
 * Description: Helper class to create {@link ASTNode} objects from text.<br>
 * General Steps:<br>
 * <ol>
 * <li>Generate a dummy class that contains the target code</li>
 * <li>Parse the dummy class using {@link ASTParser}</li>
 * <li>Create {@link CompilationUnit} using the parser</li>
 * <li>Extract the relevant {@link ASTNode} using a {@link ASTVisitor}</li>
 * <li>Return the {@link ASTNode}</li>
 * </ol>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/10/2016
 */
public class CodeFactory extends ASTVisitor
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
	 * Creates a {@link Block} given its source code
	 * 
	 * @param targetAST
	 *            target ast of level {@link AST#JLS4}
	 * @param codeBlock
	 *            code block source code. It must have a valid syntax
	 * @return a {@link Block} based on the source code
	 */
	public static Block getStatementsBlockFromText( AST targetAST, String codeBlock )
	{
		String dummyMethodStart = "public void dummyMethod() {";
		String dummyMethodEnd = "}";
		String dummyMethod = dummyMethodStart + codeBlock + dummyMethodEnd;
		MethodDeclaration methodFromText1 = getMethodFromText( targetAST, dummyMethod );
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
	public static MethodDeclaration getMethodFromText( AST targetAST, String method )
	{
		String dummyClassStart = "public class DummyClass { ";
		String dummyClassEnd = "}";
		String dummyClass = dummyClassStart + method + dummyClassEnd;

		ASTParser javaParser = ASTParser.newParser( AST.JLS8 );
		javaParser.setSource( dummyClass.toCharArray() );
		CompilationUnit compilationUnit = (CompilationUnit) javaParser.createAST( null );
		CodeFactory visitor = new CodeFactory();
		compilationUnit.accept( visitor );
		MethodDeclaration methodDeclaration = visitor.methodDeclaration;
		return (MethodDeclaration) ASTNode.copySubtree( targetAST, methodDeclaration );
	}
}
