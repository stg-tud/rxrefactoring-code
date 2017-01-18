package builders;

import org.eclipse.jdt.core.dom.Block;

import rx.Subscriber;

/**
 * Description: Builder to create Subcribers as text<br>
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

	/**
	 * Builds an {@link Subscriber} with the action {@link Subscriber#onNext(Object)}.
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
	public static String newSubscriber( String type, Block onNext, String onNextVariableName )
	{
		StringBuilder rxSubscriber = new StringBuilder();
		rxSubscriber.append( "new Subscriber<" );
		rxSubscriber.append( type );
		rxSubscriber.append( "[]>() {" );
		rxSubscriber.append( NEW_LINE );
		rxSubscriber.append( "@Override public void onCompleted() {}" );
		rxSubscriber.append( NEW_LINE );
		rxSubscriber.append( "@Override public void onError(Throwable throwable) {}" );
		rxSubscriber.append( NEW_LINE );
		rxSubscriber.append( "@Override public void onNext(" );
		rxSubscriber.append( type );
		rxSubscriber.append( SPACE );
		rxSubscriber.append( onNextVariableName );
		rxSubscriber.append( "[])" );
		rxSubscriber.append( NEW_LINE );
		rxSubscriber.append( onNext.toString() );
		rxSubscriber.append( "};" );
		return rxSubscriber.toString();
	}


}
