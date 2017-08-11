package de.tudarmstadt.rxrefactoring.core.ui;

import org.eclipse.swt.widgets.Shell;

public interface IUIIntegration {
	
	public boolean showConfirmationDialog(Shell shell, String title, String text);
}
