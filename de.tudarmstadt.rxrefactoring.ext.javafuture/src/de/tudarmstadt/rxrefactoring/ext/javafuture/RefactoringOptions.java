package de.tudarmstadt.rxrefactoring.ext.javafuture;

import java.util.EnumSet;

/**
 * For now: This defines what refactoring processes should be done by this
 * extension. We can refactor Future or FutureTask.
 * 
 * Futures were no cancel, etc. is used can be refactored using the simple
 * Future workers. Additionally we support a special FutureWrapper.
 * 
 * Note that if you choose FUTURE then FUTURE_WRAPPER will be ignored.
 * 
 * @author Steve
 *
 */
public enum RefactoringOptions {
	FUTURE, FUTURETASK, SEPARATE_OCCURENCIES;

	public static final EnumSet<RefactoringOptions> ALL_OPTIONS = EnumSet.allOf(RefactoringOptions.class);
}
