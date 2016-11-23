package rxrefactoring;

import rxrefactoring.anonymous.AnonymousClassCase12;
import rxrefactoring.anonymous.AnonymousClassCase13;
import rxrefactoring.anonymous.AnonymousClassCase3;

public class ApplicationSwingWorkers
{
    public static void main (String [] args) throws InterruptedException
    {
        new AnonymousClassCase13().start();

        Thread.sleep(11000L);
    }
}
