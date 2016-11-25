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
public class FindUSagesTest extends AbstractJavaTest
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

		String expectedTree = "root -> [FindUsagesClass1 -> [SwingWorker<String,Integer> -> [swingWorker -> [doSomething -> [sameSwingWorker -> [execute, cancel]]]]]]";
		assertEquals( expectedTree, visitor.getTreeRoot().toString() );

	}

}
