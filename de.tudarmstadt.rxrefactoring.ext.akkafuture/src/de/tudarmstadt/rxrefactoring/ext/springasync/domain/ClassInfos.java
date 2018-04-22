package de.tudarmstadt.rxrefactoring.ext.springasync.domain;

import java.util.Arrays;

public class ClassInfos {
	
	public static final ClassInfo AkkaFuture = new ClassInfo(
			"Akka Future",
			"akka.dispatch.Future",
			Arrays.asList(
					"isCompleted",
					"onComplete",
					"ready",
					"result",
					"value",
					"andThen",
					"collect",
					"failed",
					"fallbackTo",
					"filter",
					"flatMap",
					"foreach",
					"map",
					"mapTo",
					"onFailure",
					"onSuccess",
					"recover",
					"recoverWith",
					"transform",
					"withFilter",
					"zip"
					),
			Arrays.asList(
					));
	
	
	
	private ClassInfos() {
	}
}
