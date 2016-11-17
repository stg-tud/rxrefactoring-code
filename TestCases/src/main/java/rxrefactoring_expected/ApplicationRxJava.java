package rxrefactoring_expected;

import rxrefactoring_expected.anonymous.AnonymousClassCase3;

public class ApplicationRxJava
{
    public static void main (String [] args) throws InterruptedException
    {
        new AnonymousClassCase3().start();

        Thread.sleep(5000L);
    }
}
