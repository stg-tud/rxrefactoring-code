package rxjavarefactoringtests.aexampletest;

import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.junit.Test;

import framework.AbstractNewJavaProjectTest;
import framework.TestFilesDto;

/**
 * Description: Example test to show how resources are read and how different
 * source codes can be compared<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/13/2016
 */
public class ExampleTest extends AbstractNewJavaProjectTest
{

	@Test
	public void testSourceCodeEquivalence() throws Exception
	{
		// This test compares the file from
		// resources/_example.test/InputFile.java with
		// resources/_example.test/ExpectedFile.java
		// These files are semantically the same, but the line breaks are different
		// The test must show that these files are equivalent

		// Define information for input file
		TestFilesDto inputTestFiles = new TestFilesDto(
				"_example.test",
				"testpackage",
				"InputFile" );

		// Create compilation unit for input file
		List<ICompilationUnit> inputICUs = createCompilationUnits( inputTestFiles );
		ICompilationUnit inputICU = inputICUs.get( 0 );

		// Define information for expected file (assuming that an algorithm did a transformation)
		TestFilesDto expectedFiles = new TestFilesDto(
				"_example.test",
				"testpackage",
				"ExpectedFile" );

		// Create compilation unit for expected file
		List<ICompilationUnit> expectedICUs = createCompilationUnits( expectedFiles );
		ICompilationUnit expectedICU = expectedICUs.get( 0 );

		// source code assertion: source codes should be equal
		assertEqualSourceCodes( expectedICU.getSource(), inputICU.getSource() );
	}
}
