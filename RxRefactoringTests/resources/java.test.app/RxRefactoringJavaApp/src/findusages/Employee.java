package findusages;

import java.util.Date;

public class Employee extends Person
{
	private String department;
	private Date firstDay;
	private Date lastDay;

	public Employee( String name )
	{
		super( name );
	}

	public String getDepartment()
	{
		return department;
	}

	public void setDepartment(String department)
	{
		this.department = department;
	}

	public Date getFirstDay()
	{
		return firstDay;
	}

	public void setFirstDay(Date firstDay)
	{
		this.firstDay = firstDay;
	}

	public Date getLastDay()
	{
		return lastDay;
	}

	public void setLastDay(Date lastDay)
	{
		this.lastDay = lastDay;
	}
}