package thesis_example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 01/25/2017
 */
public class FunctionalProgramming
{
	public static void main( String[] args )
	{
        List<Integer> numbers = Arrays.asList( 1, 3, 4, 5, 8, 13, 15 );
        List<String> evenNumbers = new ArrayList<>();
        for ( int i = 0; i < numbers.size() ; i++ )
        {
            Integer n = numbers.get(i);
            if ( n % 2 == 0)
            {
                evenNumbers.add(String.valueOf(n));
            }
        }

		doSomething(evenNumbers);

        List<String> eventNumbers2 = numbers.stream()
                .filter(n -> n % 2 == 0)
                .map(n -> String.valueOf(n))
                .collect(Collectors.toList());

        doSomething(eventNumbers2);
    }

    private static void doSomething(List<String> evenNumbers)
    {
        System.out.println(evenNumbers);
    }
}
