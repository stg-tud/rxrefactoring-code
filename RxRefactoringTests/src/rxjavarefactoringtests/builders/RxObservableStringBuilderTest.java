package rxjavarefactoringtests.builders;

import static org.junit.Assert.assertEquals;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Block;
import org.junit.Before;
import org.junit.Test;

import codegenerators.RxObservableStringBuilder;
import rxjavarefactoring.framework.codegenerators.ASTNodeFactory;
import rxjavarefactoring.framework.constants.SchedulerType;

/**
 * Description: Tests {@link RxObservableStringBuilder}<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/16/2016
 */
public class RxObservableStringBuilderTest
{
	private Block doInBackgroundBlock;
	private Block doOnNextBlock;
	private Block doOnSubscribeBlock;
	private String type;
	private String resultVariableName;

	@Before
	public void setup()
	{
		type = "Integer";
		resultVariableName = "result";
		String doInBackgroundStatements = "int a = 1; int b = 2; return a + b;";
		String doOnNextStatements = "System.out.println(result);";
		String doOnSubscribeStatements = "System.out.println(\"Subscribing Observable\");";

		AST ast = AST.newAST( AST.JLS8 );
		doInBackgroundBlock = ASTNodeFactory.createStatementsBlockFromText( ast, doInBackgroundStatements );
		doOnNextBlock = ASTNodeFactory.createStatementsBlockFromText( ast, doOnNextStatements );
		doOnSubscribeBlock = ASTNodeFactory.createStatementsBlockFromText( ast, doOnSubscribeStatements );
	}

	@Test
	public void testObservableCase1()
	{
		// Observable is not assigned to a variable
		// only doInBackground

		String observable = RxObservableStringBuilder
				.newObservable( type, doInBackgroundBlock, SchedulerType.ANDROID_MAIN_THREAD )
				.build();

		String expectedString = "Observable.fromCallable(new Callable<Integer>() {\n" +
				"@Override public Integer call() throws Exception\n" +
				"{\n" +
				"  int a=1;\n" +
				"  int b=2;\n" +
				"  return a + b;\n" +
				"}\n" +
				"})\n" +
				".subscribeOn(Schedulers.computation())\n" +
				".observeOn(AndroidSchedulers.mainThread());";

		assertEquals( expectedString, observable );
	}

	@Test
	public void testObservableCase2()
	{
		// Observable is not assigned to a variable
		// doInBackground and doOnNext

		String observable = RxObservableStringBuilder
				.newObservable( type, doInBackgroundBlock, SchedulerType.ANDROID_MAIN_THREAD )
				.addDoOnNext( doOnNextBlock, resultVariableName )
				.build();

		String expectedString = "Observable.fromCallable(new Callable<Integer>() {\n" +
				"@Override public Integer call() throws Exception\n" +
				"{\n" +
				"  int a=1;\n" +
				"  int b=2;\n" +
				"  return a + b;\n" +
				"}\n" +
				"})\n" +
				".subscribeOn(Schedulers.computation())\n" +
				".observeOn(AndroidSchedulers.mainThread())\n" +
				".doOnNext(new Action1<Integer>() {\n" +
				"@Override public void call(Integer result) \n" +
				"{\n" +
				"  System.out.println(result);\n" +
				"}\n" +
				"});";

		assertEquals( expectedString, observable );
	}

	@Test
	public void testObservableCase3()
	{
		// Observable is not assigned to a variable
		// doInBackground, doOnNext, doOnSubscribe

		String observable = RxObservableStringBuilder
				.newObservable( type, doInBackgroundBlock, SchedulerType.ANDROID_MAIN_THREAD )
				.addDoOnNext( doOnNextBlock, resultVariableName )
				.addDoOnSubscribe( doOnSubscribeBlock )
				.build();

		String expectedString = "Observable.fromCallable(new Callable<Integer>() {\n" +
				"@Override public Integer call() throws Exception\n" +
				"{\n" +
				"  int a=1;\n" +
				"  int b=2;\n" +
				"  return a + b;\n" +
				"}\n" +
				"})\n" +
				".subscribeOn(Schedulers.computation())\n" +
				".observeOn(AndroidSchedulers.mainThread())\n" +
				".doOnNext(new Action1<Integer>() {\n" +
				"@Override public void call(Integer result) \n" +
				"{\n" +
				"  System.out.println(result);\n" +
				"}\n" +
				"})\n" +
				".doOnSubscribe(new Action0() {\n" +
				"@Override public void call() \n" +
				"{\n" +
				"  System.out.println(\"Subscribing Observable\");\n" +
				"}\n" +
				"});";

		assertEquals( expectedString, observable );
	}

	@Test
	public void testObservableCase4()
	{
		// Observable is not assigned to a variable
		// doInBackground, doOnNext, doOnSubscribe and subscribe

		String observable = RxObservableStringBuilder
				.newObservable( type, doInBackgroundBlock, SchedulerType.ANDROID_MAIN_THREAD )
				.addDoOnNext( doOnNextBlock, resultVariableName )
				.addDoOnSubscribe( doOnSubscribeBlock )
				.addSubscribe()
				.build();

		String expectedString = "Observable.fromCallable(new Callable<Integer>() {\n" +
				"@Override public Integer call() throws Exception\n" +
				"{\n" +
				"  int a=1;\n" +
				"  int b=2;\n" +
				"  return a + b;\n" +
				"}\n" +
				"})\n" +
				".subscribeOn(Schedulers.computation())\n" +
				".observeOn(AndroidSchedulers.mainThread())\n" +
				".doOnNext(new Action1<Integer>() {\n" +
				"@Override public void call(Integer result) \n" +
				"{\n" +
				"  System.out.println(result);\n" +
				"}\n" +
				"})\n" +
				".doOnSubscribe(new Action0() {\n" +
				"@Override public void call() \n" +
				"{\n" +
				"  System.out.println(\"Subscribing Observable\");\n" +
				"}\n" +
				"})\n" +
				".subscribe();";

		assertEquals( expectedString, observable );
	}

	// Testing null cases
	@Test( expected = RuntimeException.class )
	public void testObservableCase5()
	{
		// Observable is not assigned to a variable
		// Background block cannot be null

		RxObservableStringBuilder
				.newObservable( type, null, SchedulerType.ANDROID_MAIN_THREAD )
				.addDoOnNext( doOnNextBlock, resultVariableName )
				.addDoOnSubscribe( doOnSubscribeBlock )
				.addSubscribe()
				.build();
	}

	@Test( expected = RuntimeException.class )
	public void testObservableCase6()
	{
		// Observable is not assigned to a variable
		// Type cannot be null

		RxObservableStringBuilder
				.newObservable( null, doInBackgroundBlock, SchedulerType.ANDROID_MAIN_THREAD )
				.addDoOnNext( doOnNextBlock, resultVariableName )
				.addDoOnSubscribe( doOnSubscribeBlock )
				.addSubscribe()
				.build();
	}

	@Test( expected = RuntimeException.class )
	public void testObservableCase7()
	{
		// Observable is not assigned to a variable
		// Scheduler cannot be null

		RxObservableStringBuilder
				.newObservable( type, doInBackgroundBlock, null )
				.addDoOnNext( doOnNextBlock, resultVariableName )
				.addDoOnSubscribe( doOnSubscribeBlock )
				.addSubscribe()
				.build();
	}

	@Test( expected = RuntimeException.class )
	public void testObservableCase8()
	{
		// Observable is not assigned to a variable
		// resultVariableName cannot be null

		RxObservableStringBuilder
				.newObservable( type, doInBackgroundBlock, SchedulerType.ANDROID_MAIN_THREAD )
				.addDoOnNext( doOnNextBlock, null )
				.addDoOnSubscribe( doOnSubscribeBlock )
				.addSubscribe()
				.build();
	}

	@Test
	public void testObservableCase9()
	{
		// Observable is not assigned to a variable
		// doInBackground, doOnNext, doOnSubscribe and subscribe

		String observable = RxObservableStringBuilder
				.newObservable( type, doInBackgroundBlock, SchedulerType.JAVA_MAIN_THREAD )
				.addDoOnNext( null, resultVariableName )
				.addDoOnSubscribe( doOnSubscribeBlock )
				.addSubscribe()
				.build();

		String expectedString = "Observable.fromCallable(new Callable<Integer>() {\n" +
				"@Override public Integer call() throws Exception\n" +
				"{\n" +
				"  int a=1;\n" +
				"  int b=2;\n" +
				"  return a + b;\n" +
				"}\n" +
				"})\n" +
				".subscribeOn(Schedulers.computation())\n" +
				".observeOn(Schedulers.immediate())\n" +
				".doOnSubscribe(new Action0() {\n" +
				"@Override public void call() \n" +
				"{\n" +
				"  System.out.println(\"Subscribing Observable\");\n" +
				"}\n" +
				"})\n" +
				".subscribe();";

		assertEquals( expectedString, observable );
	}

	@Test
	public void testObservableCase10()
	{
		// Observable is not assigned to a variable
		// doInBackground, doOnNext, doOnSubscribe and subscribe

		String observable = RxObservableStringBuilder
				.newObservable( type, doInBackgroundBlock, SchedulerType.JAVA_MAIN_THREAD )
				.addDoOnNext( doOnNextBlock, resultVariableName )
				.addDoOnSubscribe( null )
				.addSubscribe()
				.build();

		String expectedString = "Observable.fromCallable(new Callable<Integer>() {\n" +
				"@Override public Integer call() throws Exception\n" +
				"{\n" +
				"  int a=1;\n" +
				"  int b=2;\n" +
				"  return a + b;\n" +
				"}\n" +
				"})\n" +
				".subscribeOn(Schedulers.computation())\n" +
				".observeOn(Schedulers.immediate())\n" +
				".doOnNext(new Action1<Integer>() {\n" +
				"@Override public void call(Integer result) \n" +
				"{\n" +
				"  System.out.println(result);\n" +
				"}\n" +
				"})\n" +
				".subscribe();";

		assertEquals( expectedString, observable );
	}

	@Test
	public void testObservableCase11()
	{
		// Observable is not assigned to a variable
		// doInBackground, doOnNext, doOnSubscribe and subscribe

		String observable = RxObservableStringBuilder
				.newObservable( "myObservable", type, doInBackgroundBlock, SchedulerType.ANDROID_MAIN_THREAD )
				.addDoOnNext( doOnNextBlock, resultVariableName )
				.addDoOnSubscribe( doOnSubscribeBlock )
				.build();

		String expectedString = "Observable<Integer> myObservable = " +
				"Observable.fromCallable(new Callable<Integer>() {\n" +
				"@Override public Integer call() throws Exception\n" +
				"{\n" +
				"  int a=1;\n" +
				"  int b=2;\n" +
				"  return a + b;\n" +
				"}\n" +
				"})\n" +
				".subscribeOn(Schedulers.computation())\n" +
				".observeOn(AndroidSchedulers.mainThread())\n" +
				".doOnNext(new Action1<Integer>() {\n" +
				"@Override public void call(Integer result) \n" +
				"{\n" +
				"  System.out.println(result);\n" +
				"}\n" +
				"})\n" +
				".doOnSubscribe(new Action0() {\n" +
				"@Override public void call() \n" +
				"{\n" +
				"  System.out.println(\"Subscribing Observable\");\n" +
				"}\n" +
				"});";

		assertEquals( expectedString, observable );
	}

	@Test
	public void testObservableCase12()
	{
		// Observable is not assigned to a variable
		// doInBackground, doOnNext, doOnSubscribe and subscribe

		String observable = RxObservableStringBuilder
				.newObservable( "mySubscription", type, doInBackgroundBlock, SchedulerType.ANDROID_MAIN_THREAD )
				.addDoOnNext( doOnNextBlock, resultVariableName )
				.addDoOnSubscribe( doOnSubscribeBlock )
				.addSubscribe()
				.build();

		String expectedString = "Subscription mySubscription = " +
				"Observable.fromCallable(new Callable<Integer>() {\n" +
				"@Override public Integer call() throws Exception\n" +
				"{\n" +
				"  int a=1;\n" +
				"  int b=2;\n" +
				"  return a + b;\n" +
				"}\n" +
				"})\n" +
				".subscribeOn(Schedulers.computation())\n" +
				".observeOn(AndroidSchedulers.mainThread())\n" +
				".doOnNext(new Action1<Integer>() {\n" +
				"@Override public void call(Integer result) \n" +
				"{\n" +
				"  System.out.println(result);\n" +
				"}\n" +
				"})\n" +
				".doOnSubscribe(new Action0() {\n" +
				"@Override public void call() \n" +
				"{\n" +
				"  System.out.println(\"Subscribing Observable\");\n" +
				"}\n" +
				"})\n" +
				".subscribe();";

		assertEquals( expectedString, observable );
	}
}
