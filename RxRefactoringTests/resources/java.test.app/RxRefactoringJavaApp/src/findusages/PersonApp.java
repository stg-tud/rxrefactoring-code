package findusages;

import java.util.Date;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/26/2016
 */
public class PersonApp
{
	public void main(String[] args)
	{
		Person personA = new Person("A");
		Person personB = new Person("B");

		Person referenceToB = personB;

		Events events = new Events();

		events.marryCouple(personA, personB);
		String childA = "Child A";
		String childB = "Child B";
		events.addChild(personA, childA);
		events.addChild(referenceToB, childB);
	}

	private class Events {

		private void marryCouple(Person person1, Person person2)
		{
			person1.setPartner(person2);
		}

		private void addChild(Person person1, String childName)
		{
			Person child = new Person(childName);
			child.setLastName(person1.getLastName());
			child.setBirthday(new Date());
			person1.addChild(child);
		}
	}
}