package findusages;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/26/2016
 */
public class Person
{
    private Person father;
    private Person mother;
    private Person partner;
    private List<Person> children;
    private String name;
    private String lastName;
    private Date birthday;

    public Person(String name)
    {
        this.name = name;
        children = new ArrayList<>();
    }

    public Person getFather()
    {
        return father;
    }

    public Person setFather(Person father)
    {
        this.father = father;
        return this;
    }

    public Person getMother()
    {
        return mother;
    }

    public void setMother(Person mother)
    {
        this.mother = mother;
    }

    public Person getPartner()
    {
        return partner;
    }

    public void setPartner(Person partner)
    {
        partner.partner = this;
        this.partner = partner;
    }

    public List<Person> getChildren()
    {
        return children;
    }

    public void addChild(Person child)
    {
        children.add(child);
        if (partner != null)
        {
            partner.children.add(child);
        }
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getLastName()
    {
        return lastName;
    }

    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    public Date getBirthday()
    {
        return birthday;
    }

    public void setBirthday(Date birthday)
    {
        this.birthday = birthday;
    }

    @Override
    public boolean equals(Object o)
    {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;

        Person person = (Person) o;

        if ( getFather() != null ? !getFather().equals(person.getFather()) : person.getFather() != null ) return false;
        if ( getMother() != null ? !getMother().equals(person.getMother()) : person.getMother() != null ) return false;
        if ( getChildren() != null ? !getChildren().equals(person.getChildren()) : person.getChildren() != null )
            return false;
        if ( !getName().equals(person.getName()) ) return false;
        if ( !getLastName().equals(person.getLastName()) ) return false;
        return getBirthday() != null ? getBirthday().equals(person.getBirthday()) : person.getBirthday() == null;

    }

    @Override
    public int hashCode()
    {
        int result = getFather() != null ? getFather().hashCode() : 0;
        result = 31 * result + (getMother() != null ? getMother().hashCode() : 0);
        result = 31 * result + (getChildren() != null ? getChildren().hashCode() : 0);
        result = 31 * result + getName().hashCode();
        result = 31 * result + getLastName().hashCode();
        result = 31 * result + (getBirthday() != null ? getBirthday().hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "Person{" +
                "father=" + father +
                ", mother=" + mother +
                ", children=" + children +
                ", name='" + name + '\'' +
                ", lastName='" + lastName + '\'' +
                ", birthday=" + birthday +
                '}';
    }
}