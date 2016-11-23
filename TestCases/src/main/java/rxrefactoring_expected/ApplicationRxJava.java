package rxrefactoring_expected;

import rxrefactoring.anonymous.AnonymousClassCase13;
import rxrefactoring_expected.anonymous.AnonymousClassCase12;

public class ApplicationRxJava
{
    public static void main (String [] args) throws InterruptedException
    {
        new AnonymousClassCase13().start();

        Thread.sleep(11000L);
    }
}
