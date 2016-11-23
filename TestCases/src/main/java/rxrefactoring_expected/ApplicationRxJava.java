package rxrefactoring_expected;

import rxrefactoring_expected.anonymous_complex.AnoymousComplexCase1;

public class ApplicationRxJava
{
    public static void main (String [] args) throws InterruptedException
    {
        new AnoymousComplexCase1().start();

        Thread.sleep(11000L);
    }
}
