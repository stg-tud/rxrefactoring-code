package rxrefactoring;

import rxrefactoring.anonymous.AnonymousClassCase12;
import rxrefactoring.anonymous.AnonymousClassCase3;

public class ApplicationSwingWorkers
{
    public static void main (String [] args) throws InterruptedException
    {
        new AnonymousClassCase12().start();

        Thread.sleep(5000L);
    }
}
