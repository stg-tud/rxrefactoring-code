package de.tudarmstadt.rxrefactoring.core.handler;

import org.eclipse.swt.widgets.Shell;

import de.tudarmstadt.rxrefactoring.core.utils.Log;

public class TestUI implements IUIIntegration {

	
	@Override
	public boolean showConfirmationDialog(Shell shell, String title, String text) {
		Log.info(getClass(), "Could not show confirmation dialog:\n" + title + " // " + text);
		return true;
	}

}
