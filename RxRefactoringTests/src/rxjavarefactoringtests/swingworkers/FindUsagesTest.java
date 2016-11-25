package rxjavarefactoringtests.swingworkers;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;
import org.junit.Test;

import framework.AbstractJavaTest;
import rxjavarefactoring.RxJavaRefactoringApp;
import rxjavarefactoring.analyzers.FindUsagesVisitor;

/**
 * Description:<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/16/2016
 */
public class FindUsagesTest extends AbstractJavaTest
{
	@Test
	public void testSwingWorkerRefactoringCase1() throws Exception
	{
		String targetFile = "FindUsagesClass1.java";

		RxJavaRefactoringApp app = new RxJavaRefactoringApp();

		app.refactorOnly( "" );
		app.start( null );

		ICompilationUnit[] units = app.getUnits();
		ICompilationUnit unit = null;
		for ( ICompilationUnit aUnit : units )
		{
			if ( aUnit.getElementName().equals( targetFile ) )
			{
				unit = aUnit;
				break;
			}
		}

		CompilationUnit cu = new RefactoringASTParser( AST.JLS8 ).parse( unit, true );
		FindUsagesVisitor visitor = new FindUsagesVisitor( Arrays.asList( unit ) );
		cu.accept( visitor );

		String expectedTree = "Project\n" +
				" \n" +
				" Type: FindUsagesClass1\n" +
				"  Instance creation: SwingWorker<String,Integer>\n" +
				"   Variable: swingWorker\n" +
				"    Method declaration: FindUsagesClass1#doSomething\n" +
				"     Variable: sameSwingWorker\n" +
				"      Method invocation: execute\n" +
				"      Method invocation: cancel\n" +
				" \n" +
				" Type: FindUsagesClass1\n" +
				"  Instance creation: SwingWorker<String,Integer>\n" +
				"   Method invocation: execute";
		assertEquals( expectedTree, visitor.getTreeRoot().toString() );

	}

}
