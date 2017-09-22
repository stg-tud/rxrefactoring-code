package de.tudarmstadt.rxrefactoring.core.ui;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

public class DefaultUI implements IUIIntegration {

	@Override
	public boolean showConfirmationDialog(Shell shell, String title, String text) {
		return MessageDialog.openConfirm(shell, title, text);

	}

}
