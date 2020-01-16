package de.tudarmstadt.rxrefactoring.core;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class ProcessDialog extends Dialog {

    public ProcessDialog(Shell parentShell) {
        super(parentShell);
        setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE);
        setBlockOnOpen(false);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        Label label = new Label(container, SWT.FLAT);
        GridData gdl = new GridData();
        gdl.grabExcessHorizontalSpace = true;
        label.setLayoutData(gdl);
        label.setText("The refactoring started and could take a while!");
        return container;
    }

    @Override
    protected Point getInitialSize() {
        return new Point(450, 300);
    }
    
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText("Informtion");
     }
}

