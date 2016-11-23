package rxrefactoring_expected;

import rxrefactoring_expected.anonymous_complex.AnonymousComplexCase2;

public class ApplicationRxJava
{
    public static void main (String [] args) throws InterruptedException
    {
        new AnonymousComplexCase2().start();

        Thread.sleep(11000L);
    }
}
