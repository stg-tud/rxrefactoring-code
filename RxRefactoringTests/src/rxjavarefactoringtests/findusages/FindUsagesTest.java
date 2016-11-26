package rxjavarefactoringtests.findusages;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
	public void testFindUsages1() throws Exception
	{
		String targetFile = "PersonApp.java";
		List<String> targetFiles = Arrays.asList(
				"PersonApp.java",
				"Person.java" );

		RxJavaRefactoringApp app = new RxJavaRefactoringApp();
		app.refactorOnly( "Nothing!" );
		app.start( null );

		ICompilationUnit[] allUnits = app.getUnits();
		List<ICompilationUnit> relevantUnits = getRelevantUnits( allUnits, targetFiles );
		ICompilationUnit unit = getTargetUnit( targetFile, relevantUnits );

		CompilationUnit cu = new RefactoringASTParser( AST.JLS8 ).parse( unit, true );
		FindUsagesVisitor visitor = new FindUsagesVisitor( relevantUnits );
		cu.accept( visitor );

		String expectedTree = "0> Project\n" +
				"1-> Type: PersonApp\n" +
				"2--> Instance creation: Person\n" +
				"3---> Variable: personA\n" +
				"4----> Method declaration: Events#marryCouple\n" +
				"5-----> Variable: person1\n" +
				"6------> Method invocation: setPartner\n" +
				"4----> Method declaration: Events#addChild\n" +
				"5-----> Variable: person1\n" +
				"6------> Method invocation: getLastName\n" +
				"6------> Method invocation: addChild\n" +
				"1-> Type: PersonApp\n" +
				"2--> Instance creation: Person\n" +
				"3---> Variable: personB\n" +
				"4----> Method declaration: Events#marryCouple\n" +
				"5-----> Variable: person2\n" +
				"6------> Method declaration: Person#setPartner\n" +
				"7-------> Variable: partner\n" +
				"3---> Variable: referenceToB\n" +
				"4----> Method declaration: Events#addChild\n" +
				"5-----> Variable: person1\n" +
				"6------> Method invocation: getLastName\n" +
				"6------> Method invocation: addChild\n" +
				"1-> Type: PersonApp\n" +
				"2--> Instance creation: Events\n" +
				"3---> Variable: events\n" +
				"4----> Method invocation: marryCouple\n" +
				"4----> Method invocation: addChild\n" +
				"4----> Method invocation: addChild\n" +
				"1-> Type: Events\n" +
				"2--> Instance creation: Person\n" +
				"3---> Variable: child\n" +
				"4----> Method invocation: setLastName\n" +
				"4----> Method invocation: setBirthday\n" +
				"4----> Method declaration: Person#addChild\n" +
				"5-----> Variable: child\n" +
				"1-> Type: Events\n" +
				"2--> Instance creation: Date";
		assertEquals( expectedTree, visitor.getTreeRoot().toString() );

	}

	@Test
	public void testFindUsages2() throws Exception
	{
		String targetFile = "PersonApp.java";
		List<String> targetFiles = Arrays.asList(
				"PersonApp.java",
				"Person.java" );

		RxJavaRefactoringApp app = new RxJavaRefactoringApp();
		app.refactorOnly( "Nothing!" );
		app.start( null );

		ICompilationUnit[] allUnits = app.getUnits();
		List<ICompilationUnit> relevantUnits = getRelevantUnits( allUnits, targetFiles );
		ICompilationUnit unit = getTargetUnit( targetFile, relevantUnits );

		CompilationUnit cu = new RefactoringASTParser( AST.JLS8 ).parse( unit, true );
		FindUsagesVisitor visitor = new FindUsagesVisitor( relevantUnits );
		visitor.setTargetBinaryNames( Arrays.asList( "findusages.Person" ) );
		cu.accept( visitor );

		String expectedTree = "0> Project\n" +
				"1-> Type: PersonApp\n" +
				"2--> Instance creation: Person\n" +
				"3---> Variable: personA\n" +
				"4----> Method declaration: Events#marryCouple\n" +
				"5-----> Variable: person1\n" +
				"6------> Method invocation: setPartner\n" +
				"4----> Method declaration: Events#addChild\n" +
				"5-----> Variable: person1\n" +
				"6------> Method invocation: getLastName\n" +
				"6------> Method invocation: addChild\n" +
				"1-> Type: PersonApp\n" +
				"2--> Instance creation: Person\n" +
				"3---> Variable: personB\n" +
				"4----> Method declaration: Events#marryCouple\n" +
				"5-----> Variable: person2\n" +
				"6------> Method declaration: Person#setPartner\n" +
				"7-------> Variable: partner\n" +
				"3---> Variable: referenceToB\n" +
				"4----> Method declaration: Events#addChild\n" +
				"5-----> Variable: person1\n" +
				"6------> Method invocation: getLastName\n" +
				"6------> Method invocation: addChild\n" +
				"1-> Type: Events\n" +
				"2--> Instance creation: Person\n" +
				"3---> Variable: child\n" +
				"4----> Method invocation: setLastName\n" +
				"4----> Method invocation: setBirthday\n" +
				"4----> Method declaration: Person#addChild\n" +
				"5-----> Variable: child";
		assertEquals( expectedTree, visitor.getTreeRoot().toString() );

	}

	@Test
	public void testFindUsages3() throws Exception
	{
		String targetFile = "EmployeeApp.java";
		List<String> targetFiles = Arrays.asList(
				"EmployeeApp.java",
				"Employee.java",
				"Person.java" );

		RxJavaRefactoringApp app = new RxJavaRefactoringApp();
		app.refactorOnly( "Nothing!" );
		app.start( null );

		ICompilationUnit[] allUnits = app.getUnits();
		List<ICompilationUnit> relevantUnits = getRelevantUnits( allUnits, targetFiles );
		ICompilationUnit unit = getTargetUnit( targetFile, relevantUnits );

		CompilationUnit cu = new RefactoringASTParser( AST.JLS8 ).parse( unit, true );
		FindUsagesVisitor visitor = new FindUsagesVisitor( relevantUnits );
		visitor.setTargetBinaryNames( Arrays.asList( "findusages.Person" ) );
		cu.accept( visitor );

		String expectedTree = "0> Project\n" +
				"1-> Type: EmployeeApp\n" +
				"2--> Instance creation: Employee\n" +
				"3---> Variable: employeeA\n" +
				"4----> Method declaration: HumanResources#hireEmployee\n" +
				"5-----> Variable: employeeA\n" +
				"6------> Method invocation: setFirstDay\n" +
				"6------> Method invocation: setDepartment\n" +
				"4----> Method invocation: getBirthday";
		assertEquals( expectedTree, visitor.getTreeRoot().toString() );

	}

	private ICompilationUnit getTargetUnit( String targetFile, List<ICompilationUnit> relevantUnits )
	{
		for ( ICompilationUnit unit : relevantUnits )
		{
			if ( unit.getElementName().equals( targetFile ) )
			{
				return unit;
			}
		}
		return null;
	}

	private List<ICompilationUnit> getRelevantUnits( ICompilationUnit[] allUnits, List<String> targetFiles )
	{
		List<ICompilationUnit> relevantUnits;
		relevantUnits = new ArrayList<>();
		for ( ICompilationUnit unit : allUnits )
		{
			if ( targetFiles.contains( unit.getElementName() ) )
			{
				relevantUnits.add( unit );
			}
		}
		return relevantUnits;
	}

}
