package rxjavarefactoringtests.builders;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Block;
import org.junit.Test;
import rxjavarefactoring.framework.builders.RxSubscriberStringBuilder;
import rxjavarefactoring.framework.utils.CodeFactory;

import static org.junit.Assert.assertEquals;

/**
 * Description: Tests {@link RxSubscriberStringBuilder}<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/18/2016
 */
public class RxSubscriberStringBuilderTest
{
    @Test
    public  void testSubscriberBuilder()
    {
        // setup
        String doOnNextBlockString = "for (Integer i : chunks) { System.out.println(i); }";

        AST ast = AST.newAST(AST.JLS8);
        Block doOnNextBlock = CodeFactory.createStatementsBlockFromText(ast, doOnNextBlockString);
        String type = "List<Integer>";
        String variableName = "chunks";

        // build subscriber
        String actualSubscriber = RxSubscriberStringBuilder.newSubscriber(type, doOnNextBlock, variableName);

        String expectedSubscriber = "new Subscriber<List<Integer>>() {\n" +
                "@Override public void onCompleted() {}\n" +
                "@Override public void onError(Throwable throwable) {}\n" +
                "@Override public void onNext(List<Integer> chunks)\n" +
                "{\n" +
                "  for (  Integer i : chunks) {\n" +
                "    System.out.println(i);\n" +
                "  }\n" +
                "}\n" +
                "};";

        assertEquals(expectedSubscriber, actualSubscriber);

    }
}
