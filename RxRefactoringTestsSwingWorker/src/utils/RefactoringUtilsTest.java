package utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Description: Tests for {@link RefactoringUtils}<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/22/2016
 */
public class RefactoringUtilsTest
{
	@Test
	public void testGetNewName()
	{
		assertTest( "SwingWorker", "RxObserver" );
		assertTest( "swingWorker", "rxObserver" );
		assertTest( "mySwingWorker", "myRxObserver" );
		assertTest( "mySwingWorker1", "myRxObserver1" );
		assertTest( "something", "something" );
		assertTest( "worker", "rxObserver" );
		assertTest( "Worker", "RxObserver" );
		assertTest( "aWorker", "aRxObserver" );
		assertTest( "aWorker1", "aRxObserver1" );
	}

	private void assertTest( String inputName, String expectedNewName )
	{
		String actualNewName = RefactoringUtils.getNewVarName( inputName );
		assertEquals( expectedNewName, actualNewName );
	}
}
