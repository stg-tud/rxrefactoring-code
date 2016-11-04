import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ArrayListVsLinkedList {

  public static void main(String[] args) {
    List ls = new ArrayList();
    ls.add(1);
    ls.add(2);
    ls.add(4);
    ls.add(5);

    System.out.println(ls);
    ls.remove(2);
    System.out.println(ls);

    List ls1 = new LinkedList();
    ls1.add(1);
    ls1.add(2);
    ls1.add(4);
    ls1.add(5);

    System.out.println(ls1);
    ls1.remove((Integer) 2);
    System.out.println(ls1);
    
    ArrayListVsLinkedList a = new ArrayListVsLinkedList();
    a.get(1);
    a.get(1L);
    Long l = 1L;
    a.get(l);
  }

  long get(long l) {
    System.out.println("ArrayListVsLinkedList.get(long)");
    return l;
  }

  Long get(Long l) {
    System.out.println("ArrayListVsLinkedList.get(Long)");
    return l;
  }
}
