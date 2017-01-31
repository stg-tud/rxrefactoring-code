package se.majid.RecipeApp.objects;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
/**
 * In this class it has an arraylist which contains all the recipes of CookBook from the file.
 * @author Majid
 *
 */
public class CookBook {
	public File file;
	public ArrayList<Recipe> recipes = null;
	private static CookBook cookbook = null;

	private CookBook(){

	}
/**
 * Here it creates a singelton of the class, which means all the instances of the class will be interacting with the same object
 * of the class.
 * @return
 */
	public static CookBook getInstance(){

		if(cookbook == null){
			cookbook = new CookBook();
			cookbook.recipes = new ArrayList<Recipe>();
		}

		return cookbook;
	}
/**
 * Here we loads all the recipes from JSON and save them to arraylist.We even check whether file has information for 
 * all the elements of recipe.
 */
	public void loadRecipes(){
		if(file!= null){  
			BufferedReader br;
			try {
				
				br = new BufferedReader(new FileReader(file));
				JSONArray jobj = (JSONArray) JSONValue.parse(br);
				Object[] recipeData = jobj.toArray();
				for(int i = 0; i< recipeData.length; i++){
					
					JSONObject jsonObject = (JSONObject) recipeData[i];
					if(jsonObject.get("name")==null || jsonObject.get("category")== null
						|| jsonObject.get("rating")== null || jsonObject.get("description")== null
						|| jsonObject.get("image")== null){
						JOptionPane.showMessageDialog(null, "You are loading a file with wrong format or missing values." +
								" It will not run properly!");
						//setFile(null);
						//break;
					}
					else{
					Recipe recipe = new Recipe();
					recipe.setName((String) jsonObject.get("name"));
					recipe.setCategory((String) jsonObject.get("category"));
					recipe.setRating((long) jsonObject.get("rating"));
					recipe.setDescription((String) jsonObject.get("description"));
					recipe.setImage((String) jsonObject.get("image"));

					recipes.add(recipe);
					}
				}
				
				br.close();	
			} 
			catch (Exception e) {
				
				JOptionPane.showMessageDialog(null, "You are loading an empty Cookbook or wrong file!");
				return;
			}
		}
	}
/**
 * In this method it read all the data from arraylist and store it into Jsonarray for persistence purpose.
 */
	public void saveRecipes(){
		
		if(file != null){
			JSONArray rootAttribute = new JSONArray();
			for(Recipe recipe : recipes){
				rootAttribute.add(recipe.getJsonObject());
			}
			try {
				BufferedWriter	bw = new BufferedWriter(new FileWriter(file));
				rootAttribute.writeJSONString(bw);
				bw.close();

			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void addRecipe(Recipe recipe){
		recipes.add(recipe); 
		saveRecipes();
	}

	public Recipe getRecipe(int index){
		return recipes.get(index);
	}

	public ArrayList<Recipe> getAllRecipes(){
		return recipes;
	}
	public void removeRecipe(int index){
		recipes.remove(index);
		saveRecipes();
	}
	public void updateRecipe(Recipe recipe, int index){
		recipes.set(index, recipe);
		saveRecipes();
	}

	public void setFile(File file){

		this.file = file;
	}

	public void deleteCookBook(File file){

		this.file.delete();
		recipes.clear();
	}

}



