package de.tudarmstadt.rxrefactoring.ext.swingworker.dependencies;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collector;

import javax.swing.SwingWorker;

import org.apache.tools.ant.ProjectHelper;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.core.JavaProject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.internal.execution.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.internal.execution.RewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.internal.execution.RewriteCompilationUnitFactory;
import de.tudarmstadt.rxrefactoring.core.internal.testing.MethodScanner;



@RunWith(MockitoJUnitRunner.class)
public class DependencyCheckerSwingWorkerTest extends RxRefactoringTest {
	
	@InjectMocks
	DependencyCheckerSwingWorker sut;
	
	@Mock
	MethodScanner scanner;
	
			
	@Before
	public void setUp() {
		/*AST ast = AST.newAST(AST.JLS13, false);
	    //CompilationUnit cu =  ast.newCompilationUnit();
	   // ICompilationUnit icu = (ICompilationUnit) cu;
		JavaProject project = new JavaProject();
		Set<RewriteCompilationUnit> unitsRewrite = Collections.emptySet();
		units = new ProjectUnits((IJavaProject) project, unitsRewrite);
		
		//RewriteCompilationUnitFactory factory = new RewriteCompilationUnitFactory();
		
		//RewriteCompilationUnit rewriteUnit1 = factory.from((ICompilationUnit) cu);
		
		/*Block block = parse("String foo = \"foo\";\n"
				+ "foo.toString();");
		Expression expression = getExpression(block, 1);
		MethodInvocation op = assertHasType(expression, MethodInvocation.class);
		Expression foo = getVarInit((ASTNode) block.statements().get(0));*/
		
       /* CompilationUnit cu =  ast.newCompilationUnit();
        TypeDeclaration typeDecl = ast.newTypeDeclaration();
        typeDecl.setName(ast.newSimpleName("MyClass"));
        cu.types().add(typeDecl);
        MethodDeclaration method = cu.getAST().newMethodDeclaration();
        method.setName(ast.newSimpleName("myMethod"));
        typeDecl.bodyDeclarations().add(method);

        // (2) create an ASTParser and parse the method body as ASTParser.K_STATEMENTS
        ASTParser parser = ASTParser.newParser(AST.JLS13);
        parser.setSource("System.out.println(\"Hello\" + \" world\");".toCharArray());
        parser.setKind(ASTParser.K_STATEMENTS);
        Block block = (Block) parser.createAST(null);

        // (3) copy the statements to the existing AST
        block = (Block) ASTNode.copySubtree(ast, block);
        method.setBody(block);*/
		
	}
	
	
	@Test
	public void runDependendencyCheck() throws Exception {

	
	//ProjectUnits units = sut.runDependendencyCheck();
			
	}
	

}
