package rxjavarefactoring.framework.builders;


import org.eclipse.jdt.core.dom.Block;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/18/2016
 */
public final class RxSubscriberStringBuilder
{
    private static final String NEW_LINE = "\n";
    private static final String SPACE = " ";

    private RxSubscriberStringBuilder()
    {

    }

    public static String newSubscriber(String type, Block onNext, String onNextVariableName)
    {
        StringBuilder rxSubscriber = new StringBuilder();
        rxSubscriber.append("new Subscriber<");
        rxSubscriber.append(type);
        rxSubscriber.append(">() {");
        rxSubscriber.append(NEW_LINE);
        rxSubscriber.append("@Override public void onCompleted() {}");
        rxSubscriber.append(NEW_LINE);
        rxSubscriber.append("@Override public void onError(Throwable throwable) {}");
        rxSubscriber.append(NEW_LINE);
        rxSubscriber.append("@Override public void onNext(");
        rxSubscriber.append(type);
        rxSubscriber.append(SPACE);
        rxSubscriber.append(onNextVariableName);
        rxSubscriber.append(")");
        rxSubscriber.append(NEW_LINE);
        rxSubscriber.append(onNext.toString());
        rxSubscriber.append("};");
        return rxSubscriber.toString();
    }
}
