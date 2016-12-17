package rxjavarefactoringtests.asynctasks;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.junit.Ignore;
import org.junit.Test;

import framework.AbstractAndroidTest;
import rxjavarefactoring.RxJavaRefactoringApp;

/**
 * Description: Test Refactoring of AsyncTasks that are anonymous and not
 * assigned to a variable<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/13/2016
 */
@Ignore
public class AsyncTaskAnonymousClassTest extends AbstractAndroidTest
{
	@Test
	public void testAsyncTaskRefactoringCase1() throws Exception
	{
		String targetFile = "AnonymousClassCase1.java";

		String expectedSourceCode = getSourceCode(
				"asynctask.anonymous.class",
				"ExpectedClassCase1.java" );

		executeTest( targetFile, expectedSourceCode );
	}

	@Test
	public void testAsyncTaskRefactoringCase2() throws Exception
	{
		String targetFile = "AnonymousClassCase2.java";

		String expectedSourceCode = getSourceCode(
				"asynctask.anonymous.class",
				"ExpectedClassCase2.java" );

		executeTest( targetFile, expectedSourceCode );
	}

	@Test
	public void testAsyncTaskRefactoringCase3() throws Exception
	{
		String targetFile = "AnonymousClassCase3.java";

		String expectedSourceCode = getSourceCode(
				"asynctask.anonymous.class",
				"ExpectedClassCase3.java" );

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
