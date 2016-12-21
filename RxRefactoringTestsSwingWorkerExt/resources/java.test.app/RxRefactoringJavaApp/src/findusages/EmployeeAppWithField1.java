package findusages;

import java.util.Date;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/26/2016
 */
public class EmployeeAppWithField1
{
	private Employee employeeA;
	
	public void main()
	{
		employeeA = new Employee( "EmployeeA" );
		HumanResources humanResources = new HumanResources();
		humanResources.hireEmployee( employeeA, "Administration" );

        Date birthday = employeeA.getBirthday();
        System.out.println(birthday);
    }

	private class HumanResources
	{
		private void hireEmployee( Employee employee, String department )
		{
			employee.setFirstDay( new Date() );
			employee.setDepartment( department );
		}
	}
}
