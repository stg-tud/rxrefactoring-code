package se.majid.RcepieApp.screens;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.SwingConstants;
import javax.swing.Box;
import javax.swing.JLabel;
import java.awt.Color;
import javax.swing.BoxLayout;
import java.awt.Component;
import javax.swing.JTextPane;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import javax.swing.Action;

import se.majid.RecipeApp.objects.CookBook;
import se.majid.RecipeApp.objects.Recipe;
import java.awt.Font;
import org.eclipse.wb.swing.FocusTraversalOnArray;
/**
 * In this screen, it shows the detail of a selected recipe.It also contains different methods for recipe and its image.
 * @author Majid
 *
 */
public class RecipeDetailsScreen extends JDialog   {

	private final JPanel contentPanel = new JPanel();
	private JLabel lblName;
	private JLabel lblRank;
	private JLabel lblCategory;
	private JTextPane textPane;
	private JPanel Panel;
	public JLabel lblRecipeIcon;
	private final Action editRecipeAction = new EditRecipeAction();
	private final Action deleteRecipeAction = new DeleteRecipeAction();
	private final Action closeAction = new CloseAction();
	private JButton btnEditImage;
	private final Action action_3 = new EditImageAction();

	/**
	 * Create the dialog.
	 */
	public RecipeDetailsScreen() {
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.X_AXIS));
		{
			Box verticalBox = Box.createVerticalBox();
			verticalBox.setFont(new Font("Dialog", Font.PLAIN, 5));
			contentPanel.add(verticalBox);
			{
				lblName = new JLabel("Receipe Name");
				
				verticalBox.add(lblName);
			}
			{
				lblRank = new JLabel("Rank:");
				verticalBox.add(lblRank);
			}
			{
				lblCategory = new JLabel("Category:");
				verticalBox.add(lblCategory);
			}
			{
				textPane = new JTextPane();
				textPane.setEditable(false);
				textPane.setAlignmentX(Component.LEFT_ALIGNMENT);
				textPane.setAlignmentY(Component.TOP_ALIGNMENT);
				verticalBox.add(textPane);
			}
			verticalBox.setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{lblName, lblRank, lblCategory, textPane}));
		}
		{
			Box verticalBox = Box.createVerticalBox();
			contentPanel.add(verticalBox);
			{
				Panel = new JPanel();
				verticalBox.add(Panel);
				{
					lblRecipeIcon = new JLabel("");
					Panel.add(lblRecipeIcon);
				}
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.LEFT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("Edit");
				okButton.setAction(editRecipeAction);
				okButton.setHorizontalAlignment(SwingConstants.LEFT);
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Delete");
				cancelButton.setAction(deleteRecipeAction);
				cancelButton.setHorizontalAlignment(SwingConstants.LEFT);
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
			{
				JButton btnClose = new JButton("Close");
				btnClose.setAction(closeAction);
				buttonPane.add(btnClose);
			}
			{
				btnEditImage = new JButton("Edit Image");
				btnEditImage.setAction(action_3);
				buttonPane.add(btnEditImage);
			}
		}
	}

	public JLabel getLblName() {
		return lblName;
	}
	public JLabel getLblRank() {
		return lblRank;
	}
	public JLabel getLblCategory() {  
		return lblCategory;
	}
	public JTextPane getTextPane() {
		return textPane;
	}
	public JLabel getLblRecipeImageIcon() {
		return lblRecipeIcon;
	}
	/**
	 * By this action we can edit selected recipe.
	 * @author Majid
	 *
	 */
	private class EditRecipeAction extends AbstractAction {
		public EditRecipeAction() {
			putValue(NAME, "Edit");
			putValue(SHORT_DESCRIPTION, "Edit opened recipe");
		}
		public void actionPerformed(ActionEvent e) {
		
			int selectedRecipe =MainScreen.getList().getSelectedIndex();
			
			AddRecipesScreen editrecipe = new AddRecipesScreen(selectedRecipe);
			editrecipe.setVisible(true);
			Recipe recipe = CookBook.getInstance().getRecipe(selectedRecipe);
			editrecipe.getRecipeName().setText(recipe.getName());
			editrecipe.getRatingSpinner().setValue(recipe.getRating());
			editrecipe.getDescriptionTextArea().setText(recipe.getDescription());
			
			editrecipe.getCategoryBox().setSelectedItem(recipe.getCategory());
			editrecipe.getpicture().setText(recipe.getImage());
			RecipeDetailsScreen.this.dispose();		
		}
	}
	/**
	 * Here we delete selected recipe.
	 * @author Majid
	 *
	 */
	private class DeleteRecipeAction extends AbstractAction {
		public DeleteRecipeAction() {
			putValue(NAME, "Delete");
			putValue(SHORT_DESCRIPTION, "Delete opened recipe");
		}
		public void actionPerformed(ActionEvent e) {
			int selectedRecipe =MainScreen.getList().getSelectedIndex();
			CookBook.getInstance().removeRecipe(selectedRecipe);
			MainScreen.updateList();
			RecipeDetailsScreen.this.dispose();			
		}
	}
	/**
	 * By this action we close screen.
	 * @author Majid
	 *
	 */
	private class CloseAction extends AbstractAction {
		public CloseAction() {
			putValue(NAME, "Close");
			putValue(SHORT_DESCRIPTION, "Close the dialog");
		}
		public void actionPerformed(ActionEvent e) {
			RecipeDetailsScreen.this.dispose();
		}
	}
	/**
	 * By this action we can check different effects on our image in a new screen.
	 * @author Majid
	 *
	 */
	private class EditImageAction extends AbstractAction {
		public EditImageAction() {
			putValue(NAME, "Image Effects");
			putValue(SHORT_DESCRIPTION, "Edit image of opened recipe");
		}
		public void actionPerformed(ActionEvent e) {
			
			int selectedRecipe = MainScreen.getList().getSelectedIndex();
			EditImageScreen editImage = new EditImageScreen((ImageIcon) lblRecipeIcon.getIcon());
			editImage.setVisible(true);
		}
	}
}
