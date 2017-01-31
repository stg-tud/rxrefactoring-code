package reichart.andreas.deexifier;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class AboutJFrame extends JFrame {

    /**
     * 
     */
    private static final long serialVersionUID = -6852855001881318538L;
    private JPanel contentPane;
    private static final String copyrightString = "<html><center>Copyright Andreas Reichart, 2012</center><br>"
	    + "<center>Contains software licensed under the Apache License: Sanselan</center></html>";

    /**
     * Create the frame.
     * 
     * @param version
     */
    public AboutJFrame(String version) {
	setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	getContentPane().setLayout(null);
	setResizable(false);
	setSize(300, 190);
	contentPane = new JPanel();
	contentPane.setLayout(null);
	setContentPane(contentPane);

	JLabel topLevel = new JLabel("DeExifier");
	topLevel.setHorizontalAlignment(SwingConstants.CENTER);
	topLevel.setFont(new Font("SansSerif", Font.PLAIN, 15));
	topLevel.setBounds(112, 15, 72, 25);
	contentPane.add(topLevel);

	JLabel versionLabel = new JLabel("Version " + version);
	versionLabel.setHorizontalAlignment(SwingConstants.CENTER);
	versionLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
	versionLabel.setBounds(83, 40, 140, 10);
	contentPane.add(versionLabel);

	JLabel copyrightNotice = new JLabel(copyrightString);
	copyrightNotice.setFont(new Font("Arial", Font.PLAIN, 9));
	copyrightNotice.setBounds(16, 52, 265, 80);
	contentPane.add(copyrightNotice);

	JButton closeButton = new JButton("Close");
	closeButton.setBounds(110, 135, 87, 25);
	contentPane.add(closeButton);
	closeButton.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent arg0) {
		frameClose();

	    }
	});

    }

    private void frameClose() {
	this.setVisible(false);
	this.dispose();
    }

}
