package rxjavarefactoringtests.javaapp;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.junit.Ignore;
import org.junit.Test;

import framework.AbstractJavaTest;
import rxjavarefactoring.Extension;
import rxjavarefactoring.RxJavaRefactoringApp;

/**
 * Description: Test Class for Java applications<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/16/2016
 */
public class JavaAppTestExample extends AbstractJavaTest
{
	/*
	Test set to ignore because the test fails. The Extension
	must be implemented first, and the ExampleClass and ExpectedClass
	must be adapted to a real scenario
	 */
	@Test
	@Ignore
	public void testRefactoring() throws Exception
	{
		String targetFile = "ExampleClass.java";

		// The name of the class does not need to match the name of the file
		String expectedSourceCode = getSourceCode(
				"expected.java.code",
				"ExpectedClass.java" );

		executeTest( targetFile, expectedSourceCode );
	}

	// Add more methods for further classes

	private void executeTest( String targetFile, String expectedSourceCode ) throws Exception
	{
		RxJavaRefactoringApp app = new RxJavaRefactoringApp();
		Extension refactoringExtension = new Extension();
		app.setCommandId( refactoringExtension.getId() );
		app.setExtension( refactoringExtension );
		app.refactorOnly( targetFile );
		app.start( null );

		Map<ICompilationUnit, String> results = app.getOriginalCompilationUnitVsNewSourceCodeMap();
		int expectedSize = 1;
		assertEquals( expectedSize, results.keySet().size() );

		String actualSourceCode = getSourceCodeByFileName( targetFile, results );

		assertEqualSourceCodes( expectedSourceCode, actualSourceCode );
	}

}
