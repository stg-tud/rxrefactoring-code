package algo;

/**
 * 
 * @author Rahul Jain
 * @see <pre>http://tech-queries.blogspot.com.au/2011/04/copy-linked-list-with-next-and-random.html
 */
public class CopyLinkedList {

    private LinkedListNode root;

    public LinkedListNode copy(LinkedListNode root) {
        LinkedListNode result;
        LinkedListNode cur = root;
        LinkedListNode next, tmp;
        while (cur != null) {
            tmp = new LinkedListNode();
            tmp.value = cur.value;
            tmp.random = null;
            tmp.next = cur.next;
            next = cur.next;
            cur.next = tmp;
            cur = next;// to move in loop
        }

        result = root.next;

        // copy the arbitrary link
        cur = root;
        while (cur != null) {
            // This works because original->next is nothing but copy of original
            // and Original->random->next is nothing but copy of random.
            cur.next.random = cur.random.next;// copying random of first node
                                              // into second one (copied) node.
            cur = cur.next.next;// move 2 nodes at a time.
        }

        // restore original and copy list
        cur = root;
        tmp = cur.next;
        while (cur != null && tmp != null) {
            cur.next = cur.next.next;// copied node now marking next of root
            cur = cur.next;
            if (tmp.next != null) {
                tmp.next = tmp.next.next;
                tmp = tmp.next;
            }
        }
        return result;
    }
}
