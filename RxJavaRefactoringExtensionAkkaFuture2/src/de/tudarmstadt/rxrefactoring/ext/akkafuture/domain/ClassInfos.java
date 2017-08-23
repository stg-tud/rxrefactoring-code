package de.tudarmstadt.rxrefactoring.ext.akkafuture.domain;

import java.util.Arrays;

public class ClassInfos {
	
//	public static final ClassInfo Future = new ClassInfo(
//			"Future",
//			"java.util.concurrent.Future",
//			Arrays.asList(
//					"cancel",
//					"get",
//					"isCancelled",
//					"isDone"
//					),
//			Arrays.asList(
//					"cancel",
//					"isCancelled",
//					"isDone"
//					));
	
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
	
	
//	public static final ClassInfo FutureTask = new ClassInfo(
//			"FutureTask",
//			"java.util.concurrent.FutureTask",
//			Arrays.asList(),
//			Arrays.asList());
	
	private ClassInfos() {
	}
}
