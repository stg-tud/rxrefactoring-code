package rxjavarefactoring.framework.holders;

import java.util.List;

import org.eclipse.jdt.core.dom.Block;

import rx.Subscriber;

/**
 * Description: Holds the necessary information to create Subscribers as text<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/18/2016
 */
public final class RxSubscriberHolder
{
	private static final String NEW_LINE = "\n";
	private static final String SPACE = " ";
	private static final String RIGHT_REC_BRACE = "]";
	private static final String LEFT_REC_BRACE = "[";
	private static final String EMPTY = "";

	private static int counter = -1;

	private String classInstantiation;
	private String type;

	/**
	 * Initializes a {@link rx.Subscriber} with the action {@link rx.Subscriber#onNext(Object)}.
	 * The methods {@link Subscriber#onCompleted()} and {@link Subscriber#onError(Throwable)}
	 * are empty.
	 *
	 * @param type
	 *            subscriber type
	 * @param onNext
	 *            block to be placed in the block onNext
	 * @param onNextVariableName
	 *            name of the variable used in the block onNext
	 * @return a string corresponding to the subscriber. "new Subscriber<...>(){ ... };"
	 */
	public RxSubscriberHolder( String type, Block onNext, String onNextVariableName )
	{
		StringBuilder rxSubscriber = new StringBuilder();
		rxSubscriber.append( "new Subscriber<" );
		rxSubscriber.append( type );
		rxSubscriber.append( ">() {" );
		rxSubscriber.append( NEW_LINE );
		rxSubscriber.append( "@Override public void onCompleted() {}" );
		rxSubscriber.append( NEW_LINE );
		rxSubscriber.append( "@Override public void onError(Throwable throwable) {}" );
		rxSubscriber.append( NEW_LINE );
		rxSubscriber.append( "@Override public void onNext(" );
		rxSubscriber.append( type );
		rxSubscriber.append( SPACE );
		rxSubscriber.append( onNextVariableName );
		rxSubscriber.append( ")" );
		rxSubscriber.append( NEW_LINE );
		rxSubscriber.append( onNext.toString() );
		rxSubscriber.append( "};" );
		classInstantiation = rxSubscriber.toString();
		this.type = type;
		counter++;
	}

	/**
	 * Resets the counter used to create unique subscriber names
	 */
	public static void resetCounter()
	{
		counter = -1;
	}

	/**
	 * Creates a string corresponding to the method declaration that
	 * creates a subscriber based on the arguments passed to the constructor<br>
	 *
	 * The first name will be getRxUpdateSubscriber<br>
	 * The next names will be enumerated. i.e: getRxUpdateSubscriber1, getRxUpdateSubscriber2, etc.
	 * 
	 * @return the method as string
	 */
	public String getGetMethodDeclaration()
	{
		return "private Subscriber<" + type + "> getRxUpdateSubscriber" + getNumber() + "() " +
				"{ return " + classInstantiation + "}";
	}

	/**
	 * Creates a statement as string based on the given arguments.
	 * 
	 * @param arguments
	 *            arguments
	 * @return the statement in charge of generating the next emission of the subscriber
	 */
	public String getOnNextInvocation( List arguments )
	{
		String argumentsString = arguments.toString().replace( RIGHT_REC_BRACE, EMPTY ).replace( LEFT_REC_BRACE, EMPTY );
		return "rxUpdateSubscriber" + getNumber() + " .onNext(Arrays.asList(" + argumentsString + "))";
	}

	/**
	 * Creates a subscriber declaration based on the type passed to constructor
	 * 
	 * @return a subscriber declaration as string
	 */
	public String getSubscriberDeclaration()
	{
		return "final Subscriber<" + type + "> rxUpdateSubscriber" + getNumber() +
				" = getRxUpdateSubscriber" + getNumber() + "()";
	}

	private String getNumber()
	{
		String number = String.valueOf( counter );
		if ( counter == 0 )
		{
			number = EMPTY;
		}
		return number;
	}

}
