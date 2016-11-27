package rxjavarefactoringtests.findusages;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;
import org.junit.Test;

import framework.AbstractJavaTest;
import rxjavarefactoring.RxJavaRefactoringApp;
import rxjavarefactoring.analyzers.InstanceToInvocationVisitor;

/**
 * Description: Tests {@link InstanceToInvocationVisitor}.<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/16/2016
 */
public class FindUsagesTest extends AbstractJavaTest
{
	@Test
	public void testFindUsages1() throws Exception
	{
		RxJavaRefactoringApp app = new RxJavaRefactoringApp();
		app.refactorOnly( "Nothing!" );
		app.start( null );

		Map<String, ICompilationUnit> unitsMap = app.getCompilationUnitsMap();
		Map<String, ICompilationUnit> filteredMap = getFilteredMap( unitsMap,
				"findusages.PersonApp",
				"findusages.Person" );

		ICompilationUnit targetUnit = unitsMap.get( "findusages.PersonApp" );
		CompilationUnit cu = new RefactoringASTParser( AST.JLS8 ).parse( targetUnit, true );
		InstanceToInvocationVisitor visitor = new InstanceToInvocationVisitor( filteredMap );
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
		RxJavaRefactoringApp app = new RxJavaRefactoringApp();
		app.refactorOnly( "Nothing!" );
		app.start( null );

		Map<String, ICompilationUnit> unitsMap = app.getCompilationUnitsMap();
		Map<String, ICompilationUnit> filteredMap = getFilteredMap( unitsMap,
				"findusages.PersonApp",
				"findusages.Person" );

		ICompilationUnit targetUnit = unitsMap.get( "findusages.PersonApp" );
		CompilationUnit cu = new RefactoringASTParser( AST.JLS8 ).parse( targetUnit, true );
		InstanceToInvocationVisitor visitor = new InstanceToInvocationVisitor( filteredMap, "findusages.Person" );
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
		RxJavaRefactoringApp app = new RxJavaRefactoringApp();
		app.refactorOnly( "Nothing!" );
		app.start( null );

		Map<String, ICompilationUnit> unitsMap = app.getCompilationUnitsMap();
		Map<String, ICompilationUnit> filteredMap = getFilteredMap( unitsMap,
				"findusages.EmployeeApp",
				"findusages.Employee",
				"findusages.Person" );

		ICompilationUnit targetUnit = unitsMap.get( "findusages.EmployeeApp" );
		CompilationUnit cu = new RefactoringASTParser( AST.JLS8 ).parse( targetUnit, true );
		InstanceToInvocationVisitor visitor = new InstanceToInvocationVisitor( filteredMap, "findusages.Person" );
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

	private Map<String, ICompilationUnit> getFilteredMap( Map<String, ICompilationUnit> unitMap, String... targetUnits )
	{
		Map<String, ICompilationUnit> resultMap = new HashMap<>();
		List<String> targetList = Arrays.asList( targetUnits );
		for ( String unitName : unitMap.keySet() )
		{
			if ( targetList.contains( unitName ) )
			{
				resultMap.put( unitName, unitMap.get( unitName ) );
			}
		}
		return resultMap;
	}

}
