package rxjavarefactoringtests.builders;

import static org.junit.Assert.assertEquals;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Block;
import org.junit.Test;

import rxjavarefactoring.framework.codegenerators.RxSubscriberHolder;
import rxjavarefactoring.framework.utils.CodeFactory;

/**
 * Description: Tests {@link RxSubscriberHolder}<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/18/2016
 */
public class RxSubscriberHolderTest
{
    @Test
	public void testSubscriberHolder()
    {
        // setup
        String doOnNextBlockString = "for (Integer i : chunks) { System.out.println(i); }";

        AST ast = AST.newAST(AST.JLS8);
        Block doOnNextBlock = CodeFactory.createStatementsBlockFromText(ast, doOnNextBlockString);
        String type = "List<Integer>";
        String variableName = "chunks";
		String className = "icuName";

        // build subscriber
		RxSubscriberHolder subscriberHolder = new RxSubscriberHolder( className, type, doOnNextBlock, variableName );

		String expectedGetSubscriberMethod = "private Subscriber<List<Integer>> getRxUpdateSubscriber() { return new Subscriber<List<Integer>>() {\n" +
                "@Override public void onCompleted() {}\n" +
                "@Override public void onError(Throwable throwable) {}\n" +
                "@Override public void onNext(List<Integer> chunks)\n" +
                "{\n" +
                "  for (  Integer i : chunks) {\n" +
                "    System.out.println(i);\n" +
                "  }\n" +
                "}\n" +
				"};}";

		assertEquals( expectedGetSubscriberMethod, subscriberHolder.getGetMethodDeclaration() );

    }
}
