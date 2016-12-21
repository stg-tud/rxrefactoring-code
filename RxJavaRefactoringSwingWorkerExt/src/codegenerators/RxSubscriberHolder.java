package codegenerators;

import java.util.List;

import org.eclipse.jdt.core.dom.Block;

import rx.Subscriber;
import rxjavarefactoring.framework.codegenerators.DynamicIdsMapHolder;

/**
 * Description: Holds the necessary information to create Subscribers as text<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/18/2016
 */
public class RxSubscriberHolder
{
	private static final String NEW_LINE = "\n";
	private static final String SPACE = " ";
	private static final String RIGHT_REC_BRACE = "]";
	private static final String LEFT_REC_BRACE = "[";
	private static final String EMPTY = "";

	private String classInstantiation;
	private String type;
	private String idNumber;

	/**
	 * Initializes a {@link Subscriber} with the action {@link Subscriber#onNext(Object)}.
	 * The methods {@link Subscriber#onCompleted()} and {@link Subscriber#onError(Throwable)}
	 * are empty.
	 *
	 *
	 * @param icuName
	 * @param type
	 *            subscriber type
	 * @param onNext
	 *            block to be placed in the block onNext
	 * @param onNextVariableName
	 *            name of the variable used in the block onNext
	 * @return a string corresponding to the subscriber. "new Subscriber<...>(){ ... };"
	 */
	public RxSubscriberHolder( String icuName, String type, Block onNext, String onNextVariableName )
	{
		this.type = type;
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
		idNumber = DynamicIdsMapHolder.getNextObserverId( icuName );
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
		return "rxUpdateSubscriber" + getNumber() + " .onNext(Arrays.asList(" + argumentsString + "));";
	}

	/**
	 * Creates a subscriber declaration based on the type passed to constructor
	 * 
	 * @return a subscriber declaration as string
	 */
	public String getSubscriberDeclaration()
	{
		return "final Subscriber<" + type + "> rxUpdateSubscriber" + getNumber() +
				" = getRxUpdateSubscriber" + getNumber() + "();";
	}

	// ### Private Methods ###

	private String getNumber()
	{
		return idNumber;
	}

}
