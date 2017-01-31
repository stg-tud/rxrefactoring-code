package se.majid.RcepieApp.screens;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.JTextPane;
import javax.swing.BoxLayout;
/**
 * In this screen it has some basic information about the application.
 * @author Majid
 *
 */
public class AboutScreen extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private final Action action = new CloseAction();
	/**
	 * Create the dialog.
	 */
	public AboutScreen() {
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.X_AXIS));
		{
			JTextPane txtpnAuthorMajidHumayou = new JTextPane();
			txtpnAuthorMajidHumayou.setContentType("html");
			txtpnAuthorMajidHumayou.setText(" <head>Name:   Yummy Recipes</head>\r\n<body>Author: Majid Humayou\r\nCreated " +
			"since: 2013-03-12\r\nDescription:\r\n                      A very handy and easy to use recipes application.\r\n");
			contentPanel.add(txtpnAuthorMajidHumayou);
			txtpnAuthorMajidHumayou.setEditable(false);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setAction(action);
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
		}
	}

	private class CloseAction extends AbstractAction {
		public CloseAction() {
			putValue(NAME, "Close");
			putValue(SHORT_DESCRIPTION, "Close the dialog");
		}
		public void actionPerformed(ActionEvent e) {
			AboutScreen.this.dispose();
		}
	}
}
