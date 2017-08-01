package de.tudarmstadt.rxrefactoring.ext.asynctask.workers;

import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import de.tudarmstadt.rxrefactoring.ext.asynctask.builders2.SubscriberBuilder;
import de.tudarmstadt.rxrefactoring.ext.asynctask.utils.AsyncTaskWrapper;
import de.tudarmstadt.rxrefactoring.ext.asynctask.writers.UnitWriterExt;

interface WorkerEnvironment {

	/**
	 * Replaces all invocations to {@code publish} with invocations to {@subscriber.onNext}
	 * 
	 * @param asyncTask The AsyncTask which publish invocations are replaced.
	 * @param writer The writer responsible for the replacement.
	 * @param builder The builder of the subscriber.
	 */
	default void replacePublishInvocations(AsyncTaskWrapper asyncTask, UnitWriterExt writer, SubscriberBuilder builder) {
		//Iterate over all publish invocations
		for (MethodInvocation publishInvocation : asyncTask.getPublishInvocations()) {
			
			List<?> argumentList = publishInvocation.arguments();
						
			@SuppressWarnings("unchecked")
			MethodInvocation invoke = builder.getSubscriberPublish((List<Expression>) argumentList);
			
			writer.replace(publishInvocation, invoke);
		
		}
		
	}
}
