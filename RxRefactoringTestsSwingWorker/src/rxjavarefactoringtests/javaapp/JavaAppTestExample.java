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
	@Test
	public void testFieldDeclarations() throws Exception
	{
		String targetFile = "FieldDeclaration.java";

		// The name of the class does not need to match the name of the file
		String expectedSourceCode = getSourceCode(
				"expected.java.code",
				"FieldDeclarationRefactored.java" );

		executeTest( targetFile, expectedSourceCode );
	}

	@Test
	public void testMethodInvocations() throws Exception
	{
		String targetFile = "MethodInvocation.java";

		String expectedSourceCode = getSourceCode(
				"expected.java.code",
				"MethodInvocationRefactored.java" );

		executeTest( targetFile, expectedSourceCode );
	}

	@Test
	public void testAssignments() throws Exception
	{
		String targetFile = "Assignment.java";

		String expectedSourceCode = getSourceCode(
				"expected.java.code",
				"AssignmentRefactored.java" );

		executeTest( targetFile, expectedSourceCode );
	}

	@Test
	public void testVariableDeclarationStatements() throws Exception
	{
		String targetFile = "VariableDeclStatement.java";

		String expectedSourceCode = getSourceCode(
				"expected.java.code",
				"VariableDeclStatementRefactored.java" );

		executeTest( targetFile, expectedSourceCode );
	}

	@Test
	public void testSimpleName() throws Exception
	{
		String targetFile = "SimpleName.java";

		String expectedSourceCode = getSourceCode(
				"expected.java.code",
				"SimpleNameRefactored.java" );

		executeTest( targetFile, expectedSourceCode );
	}

	@Test
	public void testClassInstanceCreation() throws Exception
	{
		String targetFile = "ClassInstanceCreation.java";

		String expectedSourceCode = getSourceCode(
				"expected.java.code",
				"ClassInstanceCreationRefactored.java" );

		executeTest( targetFile, expectedSourceCode );
	}

	@Test
	@Ignore
	/*
	 * Set to ignore because expected file depends
	 * on the order of execution (not deterministic).
	 * Enable the test to see how all workers interact in one
	 * file and analyze if the result is correct
	 */
	public void testGeneralCase() throws Exception
	{
		String targetFile = "GeneralCase.java";

		String expectedSourceCode = getSourceCode(
				"expected.java.code",
				"GeneralCaseRefactored.java" );

		executeTest( targetFile, expectedSourceCode );
	}

	@Test
	public void testStatefulAssignments() throws Exception
	{
		String targetFile = "StatefulAssignment.java";

		String expectedSourceCode = getSourceCode(
				"expected.java.code",
				"StatefulAssignmentRefactored.java" );

		executeTest( targetFile, expectedSourceCode );
	}

	@Test
	public void testStatefulVariableDeclStatement() throws Exception
	{
		String targetFile = "StatefulVariableDeclStatement.java";

		String expectedSourceCode = getSourceCode(
				"expected.java.code",
				"StatefulVariableDeclStatementRefactored.java" );

		executeTest( targetFile, expectedSourceCode );
	}



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
