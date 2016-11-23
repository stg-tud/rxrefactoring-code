package rxrefactoring;

import rxrefactoring.anonymous_complex.AnonymousComplexCase2;

public class ApplicationSwingWorkers
{
    public static void main (String [] args) throws InterruptedException
    {
        new AnonymousComplexCase2().start();

        Thread.sleep(11000L);
    }
}
