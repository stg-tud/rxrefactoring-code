package de.tudarmstadt.rxrefactoring.ext.akkafuture.wrapper;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * Interface for methods that take a future and produce another future, e.g., future.map(...), Futures.sequence(future, ...)
 * @author mirko
 *
 */
public interface FutureMethodWrapper {

	public static boolean isFutureMethod(Expression method) {
		return FuturesSequenceWrapper.isFuturesSequence(method) ||
				FuturesMapWrapper.isFutureMap(method) ||
				FutureOnFailureWrapper.isFutureOnFailure(method);
	}
	
	public static FutureMethodWrapper createFromExpression(Expression method) {
		if (FuturesSequenceWrapper.isFuturesSequence(method))
			return FuturesSequenceWrapper.create(method);
		else if (FuturesMapWrapper.isFutureMap(method))
			return FuturesMapWrapper.create(method);
		else if (FutureOnFailureWrapper.isFutureOnFailure(method))
			return FutureOnFailureWrapper.create(method);
			
		
		return null;
	}
}
