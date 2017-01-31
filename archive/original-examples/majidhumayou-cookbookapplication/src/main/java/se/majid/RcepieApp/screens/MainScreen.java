package se.majid.RcepieApp.screens;
/**
 * @since 2013-03-14
 */
import java.awt.EventQueue;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JButton;
import java.awt.FlowLayout;
import javax.swing.JList;
import javax.swing.AbstractListModel;
import javax.swing.ListSelectionModel;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.SwingConstants;
import javax.swing.plaf.FileChooserUI;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import se.majid.RecipeApp.objects.CookBook;
import se.majid.RecipeApp.objects.Recipe;
import javax.swing.JLabel;
/**
 * This is our mainscreen.Here we can create a new Cookbook or we can load an existing Cookbook or
 *  either delete an already loaded Cookbook. We can see all the recipes which exists in our loaded Cookbook.
 *  We have even functions for viewing, editing or deleting a selected recipe.We can even add a new recipe into our
 *  loaded Cookbook.
 * @author Majid
 *
 */
public  class MainScreen {

	private JFrame frmYummyRecepies;
	private final Action AddRecipeDialog = new AddNewRecepieAction();
	private final Action showDetailsDialog = new ShowDetailsAction();
	private final Action CreateNewCookBook = new NewCookbookAction();
	private static JList list;
	private final Action loadCookbookAction = new LoadCookbokAction();
	private final Action deleteCookbookAction = new DeletCookBookAction();
	private File file;
	private final Action deleteRecipeAction = new DeleteRecipeAction();
	private final Action exitAction = new ExitAction();
	private final Action editRecipeAction = new EditRecipeAction();
	private JButton btnEditRecepie;
	private JLabel lblNoCookbookIs;
	private JButton btnAddRecepie;
	private JMenuItem mntmNewRecepie;
	private final Action action = new AboutYummyRecipesAction();


	/**
	 * Through our main method we Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainScreen window = new MainScreen();
					window.frmYummyRecepies.setVisible(true);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Here it creates the layout of the application.Then it sets the data for all the contents of the frame.
	 */
	public MainScreen() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmYummyRecepies = new JFrame();
		frmYummyRecepies.setTitle("Aatika,s Recipes");
		frmYummyRecepies.setBounds(100, 100, 497, 316);
		frmYummyRecepies.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		AddRecipeDialog.setEnabled(false);
		editRecipeAction.setEnabled(false);
		showDetailsDialog.setEnabled(false);
		deleteRecipeAction.setEnabled(false);

		JMenuBar menuBar = new JMenuBar();
		frmYummyRecepies.setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		JMenuItem mntmNewCookbook = new JMenuItem("Create New Cookbook");
		mntmNewCookbook.setAction(CreateNewCookBook);
		mnFile.add(mntmNewCookbook);

		JMenuItem mntmLoadCookbook = new JMenuItem("Load Cookbook");
		mntmLoadCookbook.setAction(loadCookbookAction);
		mnFile.add(mntmLoadCookbook);

		JMenuItem mntmDeleteCookbook = new JMenuItem("Delete Cookbook");
		mntmDeleteCookbook.setAction(deleteCookbookAction);
		mnFile.add(mntmDeleteCookbook);

		JSeparator separator = new JSeparator();
		mnFile.add(separator);

		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.setAction(exitAction);
		mnFile.add(mntmExit);

		JMenu mnEdit = new JMenu("Recipe");
		menuBar.add(mnEdit);

		mntmNewRecepie = new JMenuItem("New Recepie");
		mntmNewRecepie.setAction(AddRecipeDialog);
		mntmNewRecepie.setEnabled(false);
		mnEdit.add(mntmNewRecepie);

		JMenuItem mntmNewMenuItem = new JMenuItem("Edit Recepie");
		mntmNewMenuItem.setAction(editRecipeAction);
		mnEdit.add(mntmNewMenuItem);

		JMenuItem mntmDeleteRecepie = new JMenuItem("Delete Recepie");
		mntmDeleteRecepie.setAction(deleteRecipeAction);
		mnEdit.add(mntmDeleteRecepie);

		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);

		JMenuItem mntmAskAboutRecipe = new JMenuItem("About Yummy Recipe");
		mntmAskAboutRecipe.setAction(action);
		mnHelp.add(mntmAskAboutRecipe);
		frmYummyRecepies.getContentPane().setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		frmYummyRecepies.getContentPane().add(panel, BorderLayout.SOUTH);

		btnAddRecepie = new JButton("Add Recepie");
		btnAddRecepie.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		btnAddRecepie.setAction(AddRecipeDialog);
		panel.add(btnAddRecepie);
		btnAddRecepie.setEnabled(false);

		JButton btnEditRecepie = new JButton("Edit Recepie");
		btnEditRecepie.setAction(deleteRecipeAction);
		btnEditRecepie.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		btnEditRecepie.setHorizontalAlignment(SwingConstants.LEFT);
		panel.add(btnEditRecepie);

		btnEditRecepie = new JButton("Edit Recepie");
		btnEditRecepie.setAction(editRecipeAction);
		panel.add(btnEditRecepie);

		JButton btnVieRecepie = new JButton("View Recepie");
		btnVieRecepie.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		btnVieRecepie.setAction(showDetailsDialog);
		panel.add(btnVieRecepie);

		list = new JList();
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		frmYummyRecepies.getContentPane().add(list, BorderLayout.CENTER);


		lblNoCookbookIs = new JLabel("No Cookbook is loaded!");
		frmYummyRecepies.getContentPane().add(lblNoCookbookIs, BorderLayout.NORTH);
		lblNoCookbookIs.setVisible(true);

	}
/**
 * By this action we add a new recipe through AddRecipeScreen to the loaded Cookbook.
 * @author Majid
 *
 */
	private class AddNewRecepieAction extends AbstractAction {
		public AddNewRecepieAction() {
			putValue(NAME, "Add Recepie");
			putValue(SHORT_DESCRIPTION, "Add a new recepie to the file");
		}
		public void actionPerformed(ActionEvent e) {

			AddRecipesScreen addReceipeScreen = new AddRecipesScreen();
			addReceipeScreen.setVisible(true);
		}
	}
/**
 * By this action we can see details of a selected recipe.
 * @author Majid
 *
 */
	private class ShowDetailsAction extends AbstractAction {
		public ShowDetailsAction() {
			putValue(NAME, "View Recepie");
			putValue(SHORT_DESCRIPTION, "View detail of Recipe");
		}
		public void actionPerformed(ActionEvent e) {

			try {
				int choosenRecipe = getList().getSelectedIndex();
				if(choosenRecipe >= 0){

					RecipeDetailsScreen recepiedetail = new RecipeDetailsScreen();
					recepiedetail.setVisible(true);	
					Recipe recipe = CookBook.getInstance().getRecipe(choosenRecipe);
					recepiedetail.getLblName().setText(recipe.getName());
					recepiedetail.getLblCategory().setText(recipe.getCategory());
					recepiedetail.getLblRank().setText("Rating:" +Long.toString(recipe.getRating()));
					recepiedetail.getTextPane().setText(recipe.getDescription());

					ImageIcon imageIcon = new ImageIcon(Base64.decodeBase64(recipe.getImage()));
					recepiedetail.getLblRecipeImageIcon().setIcon(imageIcon);
				}
				else{
					JOptionPane.showMessageDialog(null, "Select a recipe first!");
				}

			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}
/**
 * By this action we can create a new Cookbook. It can even check also that if created Cookbook already exist or not in the same directory.
 * @author Majid
 *
 */
	private class NewCookbookAction extends AbstractAction {
		public NewCookbookAction() {
			putValue(NAME, "New Cookbook");
			putValue(SHORT_DESCRIPTION, "Create a new CookBook");
		}
		public void actionPerformed(ActionEvent e) {

			CookBook cookbook = CookBook.getInstance();
			cookbook.recipes.clear();

			JFileChooser fileChooser = new JFileChooser();
			int returnType = fileChooser.showSaveDialog(null);

			if(returnType== JFileChooser.APPROVE_OPTION){
				File file = fileChooser.getSelectedFile();
				String fileName = file.getName();
				String extension = fileName.substring(fileName.lastIndexOf(".")+ 1, fileName.length());
				String Json = "json";
				if(Json.equalsIgnoreCase(extension)){

					if(!file.exists()){ 
						try {

							file.createNewFile();						
							cookbook.setFile(file);
							updateList();
							lblNoCookbookIs.setVisible(false);

						} catch (IOException e1) {
							e1.printStackTrace();
						}


					}
					else{
						JOptionPane.showMessageDialog(null, "You can not overwrite an existing Cookbook.");
					}}
				else{
					JOptionPane.showMessageDialog(null, "Only Json files are permitted!");
					return;
				}

				AddRecipeDialog.setEnabled(true);
				editRecipeAction.setEnabled(true);
				deleteRecipeAction.setEnabled(true);
				showDetailsDialog.setEnabled(true);
			}

		}
	}
	/**
	 * Here it update all the changes that occurs in the arraylist, where all the recipes of the loaded Cookbook is stored.
	 * @author Majid
	 *
	 */
	public static void updateList(){
		ArrayList<Recipe> recipes = CookBook.getInstance().getAllRecipes();
		final String[] values = new String[recipes.size()];

		for(int i = 0; i<values.length; i++){
			values[i]= recipes.get(i).getName();
		}
		getList().setModel(new AbstractListModel<String>(){

			@Override
			public int getSize() {
				return values.length;
			}

			@Override
			public String getElementAt(int index) {
				return values[index];
			}
		});
	}
/**
 * Through this method we get a static list, which contains all the recipes of loaded Cookbook.
 * @return
 */
	public static JList getList() {
		return list;
	}
/**
 * By this action we load an existing Cookbook.It also check the format of the Cookbook file.
 * @author Majid
 *
 */
	private class LoadCookbokAction extends AbstractAction {
		public LoadCookbokAction() {
			putValue(NAME, "Load CookBook");
			putValue(SHORT_DESCRIPTION, "Load an existing CookBook");
		}
		public void actionPerformed(ActionEvent e) {

			CookBook cookbook = CookBook.getInstance();
			cookbook.recipes.clear();

			JFileChooser fileChooser = new JFileChooser();
			int returnType = fileChooser.showOpenDialog(null);
			if(returnType == JFileChooser.APPROVE_OPTION ){

				File file = fileChooser.getSelectedFile();
				String fileName = file.getName();
				String extension = fileName.substring(fileName.lastIndexOf(".")+ 1, fileName.length());
				String Json = "json";

				if(Json.equalsIgnoreCase(extension)){

					cookbook.setFile(file);
					cookbook.loadRecipes();
					updateList();
					lblNoCookbookIs.setVisible(false);
					
				}
				else{
					JOptionPane.showMessageDialog(null, "Choose a JSON file");
					return;
				}  
				AddRecipeDialog.setEnabled(true);
				editRecipeAction.setEnabled(true);
				deleteRecipeAction.setEnabled(true);
				showDetailsDialog.setEnabled(true);
			}			
		}
	}
	/**
	 * By this action we delete a loaded Cookbook.
	 * @author Majid
	 *
	 */
	private class DeletCookBookAction extends AbstractAction {
		public DeletCookBookAction() {
			putValue(NAME, "Delete CookBook");
			putValue(SHORT_DESCRIPTION, "Delete loaded Cookbook");
		}
		public void actionPerformed(ActionEvent e) {
			CookBook cookbook = CookBook.getInstance();
			if(cookbook.file!= null){
				cookbook.deleteCookBook(file);
				updateList();
				
				lblNoCookbookIs.setVisible(true);
				AddRecipeDialog.setEnabled(false);
				editRecipeAction.setEnabled(false);
				showDetailsDialog.setEnabled(false);
				deleteRecipeAction.setEnabled(false);
			}
			else{
				cookbook.recipes.clear();
				JOptionPane.showMessageDialog(null,"No Cookbook is Loaded!" );
			}
		}
	}
/**
 * By this action we delete a selected recipe from the loaded Cookbook.
 * @author Majid
 *
 */
	private class DeleteRecipeAction extends AbstractAction {
		public DeleteRecipeAction() {
			putValue(NAME, "Delete Recipe");
			putValue(SHORT_DESCRIPTION, "Delete selected Recipe");
		}
		public void actionPerformed(ActionEvent e) {
			int selectedRecipe = getList().getSelectedIndex();
			if(selectedRecipe >= 0){
				CookBook.getInstance().removeRecipe(selectedRecipe);
				updateList();
			}
			else{
				JOptionPane.showMessageDialog(null, "Select a recipe first!");
			}
		}
	}
	/**
	 * By this action we can close our application.
	 * @author Majid
	 *
	 */
	private class ExitAction extends AbstractAction {
		public ExitAction() {
			putValue(NAME, "Exit");
			putValue(SHORT_DESCRIPTION, "Exit from application");
		}
		public void actionPerformed(ActionEvent e) {
			System.exit(0);
		}
	}
	/**
	 * By this action we can edit a selected recipe from loaded Cookbook.
	 * @author Majid
	 *
	 */
	public class EditRecipeAction extends AbstractAction {
		public EditRecipeAction() {
			putValue(NAME, "Edit Recipe"); 
			putValue(SHORT_DESCRIPTION, "Edit selected recipe");
		}
		public void actionPerformed(ActionEvent e) {
			int selectedRecipe = getList().getSelectedIndex();
			if(selectedRecipe >= 0){
				
				AddRecipesScreen editrecipe = new AddRecipesScreen(selectedRecipe);
				editrecipe.setVisible(true);

				Recipe recipe = CookBook.getInstance().getRecipe(selectedRecipe);
				editrecipe.getRecipeName().setText(recipe.getName());
				editrecipe.getRatingSpinner().setValue(recipe.getRating());
				editrecipe.getDescriptionTextArea().setText(recipe.getDescription());
				editrecipe.getCategoryBox().setSelectedItem(recipe.getCategory());
				editrecipe.getpicture().setText(null);
			}
			else{
				JOptionPane.showMessageDialog(null, "Select a recipe first!");
			}
		}
	} 
/**
 * Here we can read some information about the application.
 * @author Majid
 *
 */
	private class AboutYummyRecipesAction extends AbstractAction {
		public AboutYummyRecipesAction() {
			putValue(NAME, "About Aatika,s Recipes");
			putValue(SHORT_DESCRIPTION, "Information about Aatika,s recipes application");
		}
		public void actionPerformed(ActionEvent e) {
			AboutScreen aboutScreen = new AboutScreen();
			aboutScreen.setVisible(true);
		}
	}
}