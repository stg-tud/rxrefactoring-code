package rxjavarefactoringtests.swingworkers;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.junit.Test;

import framework.AbstractJavaTest;
import rxjavarefactoring.RxJavaRefactoringApp;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/16/2016
 */
public class SwingWorkerAnonymousClassTest extends AbstractJavaTest
{
	@Test
	public void testSwingWorkerRefactoringCase1() throws Exception
	{
		String targetFile = "AnonymousClassCase1.java";

		String expectedSourceCode = getSourceCode(
				"swingworker.anonymous.class",
				"ExpectedClassCase1.java" );

		executeTest( targetFile, expectedSourceCode );
	}

	@Test
	public void testSwingWorkerRefactoringCase2() throws Exception
	{
		String targetFile = "AnonymousClassCase2.java";

		String expectedSourceCode = getSourceCode(
				"swingworker.anonymous.class",
				"ExpectedClassCase2.java" );

		executeTest( targetFile, expectedSourceCode );
	}

	@Test
	public void testSwingWorkerRefactoringCase3() throws Exception
	{
		String targetFile = "AnonymousClassCase3.java";

		String expectedSourceCode = getSourceCode(
				"swingworker.anonymous.class",
				"ExpectedClassCase3.java" );

		executeTest( targetFile, expectedSourceCode );
	}

	@Test
	public void testSwingWorkerRefactoringCase4() throws Exception
	{
		String targetFile = "AnonymousClassCase4.java";

		String expectedSourceCode = getSourceCode(
				"swingworker.anonymous.class",
				"ExpectedClassCase4.java" );

		executeTest( targetFile, expectedSourceCode );
	}

	@Test
	public void testSwingWorkerRefactoringCase5() throws Exception
	{
		String targetFile = "AnonymousClassCase5.java";

		String expectedSourceCode = getSourceCode(
				"swingworker.anonymous.class",
				"ExpectedClassCase5.java" );

		executeTest( targetFile, expectedSourceCode );
	}

	@Test
	public void testSwingWorkerRefactoringCase6() throws Exception
	{
		String targetFile = "AnonymousClassCase6.java";

		String expectedSourceCode = getSourceCode(
				"swingworker.anonymous.class",
				"ExpectedClassCase6.java" );

		executeTest( targetFile, expectedSourceCode );
	}

	private void executeTest( String targetFile, String expectedSourceCode ) throws Exception
	{
		RxJavaRefactoringApp app = new RxJavaRefactoringApp();

		app.refactorOnly( targetFile );
		app.start( null );

		Map<ICompilationUnit, String> results = app.getOriginalCompilationUnitVsNewSourceCodeMap();
		int expectedSize = 1;
		assertEquals( expectedSize, results.keySet().size() );

		String actualSourceCode = getSourceCodeByFileName( targetFile, results );

		assertEqualSourceCodes( expectedSourceCode, actualSourceCode );
	}

}
