package rxrefactoring;

import rxrefactoring.anonymous.AnonymousClassCase3;

public class ApplicationSwingWorkers
{
    public static void main (String [] args) throws InterruptedException
    {
        new AnonymousClassCase3().start();

        Thread.sleep(5000L);
    }
}
