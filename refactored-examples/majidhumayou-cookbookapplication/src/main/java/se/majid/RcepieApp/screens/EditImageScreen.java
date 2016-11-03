package se.majid.RcepieApp.screens;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JSlider;

import org.imgscalr.Scalr;

import se.majid.RecipeApp.objects.Recipe;
/**
 * In this Screen we can test different effects with the image of selected recipe.
 * @author Majid
 *
 */
public class EditImageScreen extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private final Action action_1 = new CancelAction();
	private JPanel panel;
	private JLabel imagelabel;
	private ImageIcon imageIcon;
	private final Action action_2 = new RotateImageAction();

	public EditImageScreen(ImageIcon imageIcon){
		this.imageIcon = imageIcon;
		initialize();
		getimageLabel().setIcon(imageIcon);

	}


	/**
	 * Create the dialog.
	 */
	public EditImageScreen() {
		initialize();
	}
	public void initialize(){
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setLayout(new FlowLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		{
			panel = new JPanel();
			contentPanel.add(panel);
			{
				imagelabel = new JLabel("");
				panel.add(imagelabel);
			}
			{
				/**
				 * Here we connect the image with Jslider values, So we can decrease or increase image size respectively.
				 */
				JSlider slider = new JSlider(50,500);
				slider.addChangeListener(new ChangeListener() {

					@Override
					public void stateChanged(ChangeEvent e) {

						JSlider slider = (JSlider) e.getSource();
						if(!slider.getValueIsAdjusting()){

							Icon icon = (Icon) new ImageIcon(imageIcon.getImage());
							BufferedImage bi = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(),BufferedImage.TYPE_INT_ARGB);
							Graphics g = bi.createGraphics();
							// paint the Icon to the BufferedImage  .
							icon.paintIcon(null, g, 0,0);
							g.dispose();
							BufferedImage bi2 = Scalr.resize(bi, slider.getValue(), slider.getValue(), null);
							Icon icon2 = new ImageIcon(bi2);
							imagelabel.setIcon(icon2); 
						}

					}
				});
				panel.add(slider);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton btnRotate = new JButton("Rotate");
				btnRotate.setAction(action_2);
				buttonPane.add(btnRotate);
			}
			{
				JButton cancelButton = new JButton("Close");
				cancelButton.setAction(action_1);
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
/**
 * By this action we close the screen.
 * @author Majid
 *
 */
	private class CancelAction extends AbstractAction {
		public CancelAction() {
			putValue(NAME, "Close");
			putValue(SHORT_DESCRIPTION, "Close screen");
		}
		public void actionPerformed(ActionEvent e) {
			EditImageScreen.this.dispose();
		}
	}

	public JPanel getImagePanel() {
		return panel;
	}

	public JLabel getimageLabel() {
		return imagelabel;
	}
/**
 * Here we set rotate image function in SwingWorker, so it dont freez or slow down our application.
 * @author Majid
 *
 */
	// RxRefactoring: not refactored, because I couldn't get this piece of code running using the UI
	public class RotateImagework extends SwingWorker<Icon, Void>{

		private Recipe recipe;
		public RotateImagework(Recipe recipe) {
			this.recipe = recipe;
		}

		@Override
		protected Icon doInBackground() throws Exception {

			Icon icon = imagelabel.getIcon();
			BufferedImage bi = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(),BufferedImage.TYPE_INT_ARGB);
			Graphics g = bi.createGraphics();
			// paint the Icon to the BufferedImage.
			icon.paintIcon(null, g, 0,0);
			g.dispose();
			BufferedImage b2 = Scalr.rotate(bi, Scalr.Rotation.CW_90);
			Icon icon2= new ImageIcon(b2);
			return icon2;
		}

		@Override
		protected void done() {
			try {
				imagelabel.setIcon(get());
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * By this action we execute our rotate image function.
	 * @author Majid
	 *
	 */
	private class RotateImageAction extends AbstractAction {
		public RotateImageAction() {
			putValue(NAME, "Rotate Image");
			putValue(SHORT_DESCRIPTION, "Rotate loaded image");
		}
		public void actionPerformed(ActionEvent e) {
			Recipe recipe = new Recipe();
			new RotateImagework(recipe).execute();
		}
	}
}
