package rxjavarefactoringtests.asynctasks;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.junit.Test;

import framework.AbstractAndroidTest;
import rxjavarefactoring.RxJavaRefactoringApp;

/**
 * Description: Test Refactoring of AsyncTasks that are anonymous and not
 * assigned to a variable<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/13/2016
 */
public class AsyncTaskAnonymousClassNoAssignment extends AbstractAndroidTest
{
	@Test
	public void testAsyncTaskRefactoring() throws Exception
	{
		RxJavaRefactoringApp app = new RxJavaRefactoringApp();

		String targetFile = "AnonymousClassNoAssignment.java";
		app.refactorOnly( targetFile );
		app.start( null );

		Map<ICompilationUnit, String> results = app.getIcuVsNewSourceCodeMap();
		int expectedSize = 1;
		assertEquals( expectedSize, results.keySet().size() );

		String expectedSourceCode = getSourceCode(
				"asynctask.anonymous.class.no.assignment",
				"ExpectedFile.java" );

		String actualSourceCode = getSourceCodeByFileName( targetFile, results );

		assertEqualSourceCodes( expectedSourceCode, actualSourceCode );
	}

}
