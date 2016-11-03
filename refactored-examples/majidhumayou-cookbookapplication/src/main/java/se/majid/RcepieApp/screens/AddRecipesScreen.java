package se.majid.RcepieApp.screens;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.AbstractAction;
import javax.swing.SwingWorker;

import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;

import org.apache.commons.codec.binary.Base64;
import org.imgscalr.Scalr;
import org.json.simple.JSONObject;

import rx.Observable;
import rx.schedulers.Schedulers;
import se.majid.RcepieApp.screens.MainScreen.EditRecipeAction;
import se.majid.RecipeApp.objects.CookBook;
import se.majid.RecipeApp.objects.Recipe;
import javax.swing.SpinnerNumberModel;
/**
 * In this screen either we add a new recipe or edit a selected recipe.It has two constructor, one is empty constructor
 * which uses while adding a new recipe. Other with selected index, it uses while editing a recipe.
 * @author Majid
 *
 */
public class AddRecipesScreen extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextField textField ;
	private JTextField Recipe_Name;
	private final Action SaveAction = new SaveAction();
	private JSpinner spinner;
	private JTextArea textArea;
	private JComboBox comboBox;
	private File imageFile ;
	private final Action BrowseAction = new LoadImageAction();
	private boolean update = false;
	private int index;
	private JButton btnNewButton;
	private final Action action = new CancelAction();
	private String imageString = "";

	public AddRecipesScreen(int index){
		this.index = index;
		update = true;
		AddRecipesScreenInitalizer();


	}
	public AddRecipesScreen(){
		AddRecipesScreenInitalizer();
	}    
	/**
	 * Create the dialog.
	 */
	public void AddRecipesScreenInitalizer() {
		setTitle("Add Recipe");
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,},
				new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		{
			JLabel lblRecepieName = new JLabel("Recepie Image:");
			contentPanel.add(lblRecepieName, "2, 2, right, default");
		}
		{
			textField = new JTextField();
			contentPanel.add(textField, "4, 2, fill, default");
			textField.setColumns(10);
		}
		{
			btnNewButton = new JButton("Browse");
			btnNewButton.setAction(BrowseAction);
			contentPanel.add(btnNewButton, "6, 2");
		}
		{
			JLabel lblRecepieName_1 = new JLabel("Recepie Name:");
			contentPanel.add(lblRecepieName_1, "2, 4");
		}
		{
			Recipe_Name = new JTextField();
			contentPanel.add(Recipe_Name, "4, 4, fill, default");
			Recipe_Name.setColumns(10);
		}
		{
			JLabel lblRank = new JLabel("Rank:");
			contentPanel.add(lblRank, "2, 6, right, default");
		}
		{
			spinner = new JSpinner();
			spinner.setModel(new SpinnerNumberModel(3, 1, 5, 1));
			contentPanel.add(spinner, "4, 6");
		}
		{
			JLabel lblRecepie = new JLabel("Description:");
			contentPanel.add(lblRecepie, "2, 8, right, default");
		}
		{
			textArea = new JTextArea();
			contentPanel.add(textArea, "4, 8, 3, 1, fill, fill");
		}
		{
			JLabel lblNewLabel = new JLabel("Recipe category");
			contentPanel.add(lblNewLabel, "2, 10, right, default");
		}
		{
			comboBox = new JComboBox();
			comboBox.setModel(new DefaultComboBoxModel(new String[] {"Asien", "Italian", "Pakistani", "Russian"}));
			contentPanel.add(comboBox, "4, 10, fill, default");
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("Save");
				okButton.setAction(SaveAction);
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setAction(action);
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
/**
 * In this method it save values of all the parameters of a recipe.
 * @author Majid
 *
 */
	private class SaveAction extends AbstractAction {
		public SaveAction() {
			putValue(NAME, "Save");    
			putValue(SHORT_DESCRIPTION, "Save new or changed recipe");
		}
		public void actionPerformed(ActionEvent e) {
			Recipe recipe = new Recipe();

			recipe.setName(Recipe_Name.getText());
			recipe.setCategory((String) comboBox.getSelectedItem());
			recipe.setRating(Integer.parseInt( spinner.getValue().toString()));
			recipe.setDescription(textArea.getText());
			SaveAction.setEnabled(false);

			// RxRefactoring: use Observable instead of SwingWorker
			createSetImageRxObservable(recipe).subscribe();
		}
	}
	public JTextField getpicture() {
		return textField;
	}
	public JTextField getRecipeName() {
		return Recipe_Name;
	}
	public JSpinner getRatingSpinner() {
		return spinner;
	}
	public JTextArea getDescriptionTextArea() {
		return textArea;
	}
	public JComboBox getCategoryBox() {
		return comboBox;
	}

	public File getImageFile() {
		return imageFile;
	}

	public void setImageFile(File imageFile) {
		this.imageFile = imageFile;
	}
	/**
	 * Here I use a SwingWorker in order to set the image of a recipe in a seprate thread. So that our application dont freeze or slow down 
	 * while setting the image.I do have put some checks so it don't gives any null point exceptions.
	 * @author Majid
	 *
	 */
	// RxRefactoring: creates rx Observable to save images
	private Observable<String> createSetImageRxObservable(Recipe recipe)
	{
		return Observable.fromCallable(() -> doInBackground())
				.subscribeOn(Schedulers.computation())
				.observeOn(Schedulers.immediate())
				.onErrorResumeNext(Observable.empty()) // RxRefactoring: doInBackground throws an Exception (According to a test, the exception would be ignore by the SwingWorker)
				.doOnCompleted(() -> done(recipe));
	}

	private String doInBackground() throws Exception {
		BufferedImage bi = ImageIO.read(imageFile);
		BufferedImage resizedimage = Scalr.resize(bi, 200);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(resizedimage, "jpeg", baos);

		return
				imageString =	Base64.encodeBase64String(baos.toByteArray());
	}

	// RxRefactoring: the argument of the constructor was only used in the method "done"
	private void done(Recipe recipe) {
		try {
			recipe.setImage(imageString);
			if(imageString == "" && update == true){
				JOptionPane.showMessageDialog(null, "No image was selected");
				SaveAction.setEnabled(true);
				return;
			}
			else if(update== true){
				CookBook.getInstance().updateRecipe(recipe, index);

			}
			else if(imageString == ""){
				JOptionPane.showMessageDialog(null, "No image selected");
				SaveAction.setEnabled(true);
				return;
			}
			else {
				CookBook.getInstance().addRecipe(recipe);
			}

			SaveAction.setEnabled(true);
			AddRecipesScreen.this.setVisible(false);
			MainScreen.updateList();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

/**
 * In this method we browse to choose an existing image file.
 * @author Majid
 *
 */
	private class LoadImageAction extends AbstractAction {
		public LoadImageAction() {
			putValue(NAME, "Browse Image");
			putValue(SHORT_DESCRIPTION, "Browse to select an image");
		}
		public void actionPerformed(ActionEvent e) {
			
			JFileChooser fileChooser = new JFileChooser();
			BrowseAction.setEnabled(false);
			int returnType = fileChooser.showOpenDialog(null);
			if(returnType == JFileChooser.APPROVE_OPTION){
				imageFile = fileChooser.getSelectedFile();
				final String imagePath = fileChooser.getSelectedFile().getPath();
				BrowseAction.setEnabled(true);
			}
			else{
				BrowseAction.setEnabled(true);
			}
		}
	}
/**
 * By this we close he screen.
 * @author Majid
 *
 */
	private class CancelAction extends AbstractAction {
		public CancelAction() {
			putValue(NAME, "Cancel");  
			putValue(SHORT_DESCRIPTION, "Close the dialog");
		}
		public void actionPerformed(ActionEvent e) {
			AddRecipesScreen.this.dispose();
		}
	}
}
