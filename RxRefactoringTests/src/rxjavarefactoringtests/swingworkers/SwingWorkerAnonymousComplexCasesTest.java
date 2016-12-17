package rxjavarefactoringtests.swingworkers;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.junit.Test;

import framework.AbstractJavaTest;
import rxjavarefactoring.RxJavaRefactoringApp;
import rxjavarefactoring.SwingWorkerExtension;

/**
 * Description: Test complex anonymous SwingWorker.
 * Classes are not assigned to a variable. The class contains private fields and/or methods<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/16/2016
 */
public class SwingWorkerAnonymousComplexCasesTest extends AbstractJavaTest
{
	@Test
	public void testSwingWorkerRefactoringCase1() throws Exception
	{
		executeTest( "1" );
	}

	@Test
	public void testSwingWorkerRefactoringCase2() throws Exception
	{
		executeTest( "2" );
	}
	//
	// @Test
	// public void testSwingWorkerRefactoringCase3() throws Exception
	// {
	// executeTest( "3" );
	// }
	//
	// @Test
	// public void testSwingWorkerRefactoringCase4() throws Exception
	// {
	// executeTest( "4" );
	// }
	//
	// @Test
	// public void testSwingWorkerRefactoringCase5() throws Exception
	// {
	// executeTest( "5" );
	// }
	//
	// @Test
	// public void testSwingWorkerRefactoringCase6() throws Exception
	// {
	// executeTest( "6" );
	// }
	//
	// @Test
	// public void testSwingWorkerRefactoringCase7() throws Exception
	// {
	// executeTest( "7" );
	// }
	//
	// @Test
	// public void testSwingWorkerRefactoringCase8() throws Exception
	// {
	// executeTest( "8" );
	// }
	//
	// @Test
	// public void testSwingWorkerRefactoringCase9() throws Exception
	// {
	// executeTest( "9" );
	// }
	//
	// @Test
	// public void testSwingWorkerRefactoringCase10() throws Exception
	// {
	// executeTest( "10" );
	// }
	//
	// @Test
	// public void testSwingWorkerRefactoringCase11() throws Exception
	// {
	// executeTest( "11" );
	// }
	//
	// @Test
	// public void testSwingWorkerRefactoringCase12() throws Exception
	// {
	// executeTest( "12" );
	// }
	//
	// @Test
	// public void testSwingWorkerRefactoringCase13() throws Exception
	// {
	// executeTest( "13" );
	// }

	private void executeTest( String caseNumber ) throws Exception
	{
		String targetFile = "AnonymousComplexCase" + caseNumber + ".java";

		String expectedSourceCode = getSourceCode(
				"swingworker.anonymous.complex.cases",
				"ExpectedClassCase" + caseNumber + ".java" );

		RxJavaRefactoringApp app = new RxJavaRefactoringApp();
		SwingWorkerExtension swingWorkerExtension = new SwingWorkerExtension();
		app.setCommandId( swingWorkerExtension.getId() );
		app.setExtension( swingWorkerExtension );

		app.refactorOnly( targetFile );
		app.start( null );

		Map<ICompilationUnit, String> results = app.getOriginalCompilationUnitVsNewSourceCodeMap();
		int expectedSize = 1;
		assertEquals( expectedSize, results.keySet().size() );

		String actualSourceCode = getSourceCodeByFileName( targetFile, results );

		assertEqualSourceCodes( expectedSourceCode, actualSourceCode );
	}

}
