package winterbe8_ConcurrentMaps;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created: 20.10.16 creation date
 */
public class ConcurrentMapExample
{
    public void main()
    {
        ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
        map.put("foo", "bar");
        map.put("han", "solo");
        map.put("r2", "d2");
        map.put("c3", "p0");

        // For Each
        System.out.println("\n####");
        map.forEach(
                1,
                (key, value) -> System.out.printf("key: %s; value: %s; thread: %s\n", key, value,
                        Thread.currentThread().getName()));

        // Search
        System.out.println("\n####");
        String result = map.search(
                1,
                (key, value) ->
                {
                    System.out.println(Thread.currentThread().getName());
                    if ( "foo".equals(key) )
                    {
                        return value;
                    }
                    return null;
                });
        System.out.println("Result: " + result);

        // Search
        System.out.println("\n####");
        String result2 = map.searchValues(
                1, value ->
                {
                    System.out.println(Thread.currentThread().getName());
                    if ( value.length() > 3 )
                    {
                        return value;
                    }
                    return null;
                });

        System.out.println("Result: " + result2);

        // Reduce
        System.out.println("\n####");
        String result3 = map.reduce(
                1,
                (key, value) ->
                {
                    System.out.println("Transform: " + Thread.currentThread().getName());
                    return key + "=" + value;
                },
                (s1, s2) ->
                {
                    System.out.println("Reduce: " + Thread.currentThread().getName());
                    return s1 + ", " + s2;
                });

        System.out.println("Result: " + result3);

    }
}
