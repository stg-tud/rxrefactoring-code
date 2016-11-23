package rxrefactoring;

import rxrefactoring.anonymous_complex.AnonymousComplexCase1;

public class ApplicationSwingWorkers
{
    public static void main (String [] args) throws InterruptedException
    {
        new AnonymousComplexCase1().start();

        Thread.sleep(11000L);
    }
}
