package oracle6_Deadlock;

/**
 * Created: 19.10.16 creation date
 */
public class DeadLock
{
    public void main()
    {
        final Friend alphonse = new Friend("Alphonse");
        final Friend gaston = new Friend("Gaston");

        Thread t1 = new Thread(() -> alphonse.bow(gaston));
        Thread t2 = new Thread(() -> gaston.bow(alphonse));

        t1.start();
        t2.start();

        try
        {
            t1.join();
            t2.join();
        }
        catch ( InterruptedException e )
        {
            System.out.println("Task interrupted");
        }

    }

    class Friend
    {
        private final String name;

        public Friend(String name)
        {
            this.name = name;
        }

        public String getName()
        {
            return this.name;
        }

        public synchronized void bow(Friend bower)
        {
            System.out.format("%s: %s" + "  has bowed to me!%n", this.name, bower.getName());
            bower.bowBack(this);
        }

        public synchronized void bowBack(Friend bower)
        {
            System.out.format("%s: %s" + " has bowed back to me!%n", this.name, bower.getName());
        }
    }
}
