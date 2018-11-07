package de.tudarmstadt.rxrefactoring.ext.javafuture.domain;

import java.util.Arrays;

public class ClassInfos {

	public static final ClassInfo Future = new ClassInfo("Future", "java.util.concurrent.Future",
			Arrays.asList("cancel", "get", "isCancelled", "isDone"), Arrays.asList("cancel", "isCancelled", "isDone"));

	public static final ClassInfo FutureTask = new ClassInfo("FutureTask", "java.util.concurrent.FutureTask",
			Arrays.asList(), Arrays.asList());

	private ClassInfos() {
	}
}
