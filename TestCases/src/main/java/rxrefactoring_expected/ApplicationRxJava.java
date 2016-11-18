package rxrefactoring_expected;

import rxrefactoring_expected.anonymous.AnonymousClassCase1;
import rxrefactoring_expected.anonymous.AnonymousClassCase12;
import rxrefactoring_expected.anonymous.AnonymousClassCase3;

public class ApplicationRxJava
{
    public static void main (String [] args) throws InterruptedException
    {
        new AnonymousClassCase12().start();

        Thread.sleep(11000L);
    }
}
