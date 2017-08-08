package de.tudarmstadt.rxrefactoring.core.utils;

public final class ConstantStrings {
	
	private ConstantStrings() { }
	
	public final static String DIALOG_CONFIRM_REFACTOR = "Are you sure that you want to perform this refactoring?\n\n"
			+ "All opened java projects in the workspace will be refactored. "
			+ "If you would like to exclude some projects, then click on Cancel, close "
			+ "the corresponding projects and start the refactoring command again.";

}
