package se.majid.RecipeApp.objects;

import java.awt.Image;

import org.json.simple.JSONObject;
/**
 * In this class it contains information abut all the recipes regarding their name,category,rating,description and 
 * image. It has getters and setters for all these parameters. 
 * @author Majid
 *
 */
public class Recipe {
	private String name;
	private String category;
	private long rating;
	private String description;
	private String image;
	
	public Recipe(String name,String category,long rating,String description,String image){
	 
	this.name = name;
	this.category = category;
	this.rating = rating;
	this.description = description;
} 
	public Recipe(){
		
	}
		
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public long getRating() {
		return rating;
	}
	public void setRating(long rating) {
		this.rating = rating;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * Here it stores all the parameters of recipe in a JsonObject for persistence storage.
	 * @return
	 */
	public JSONObject getJsonObject(){
		
		JSONObject jobj = new JSONObject();
		jobj.put("name", name);
		jobj.put("category", category);
		jobj.put("rating", rating);
		jobj.put("description", description);
		jobj.put("image", image);
		
		return jobj;
		
	}
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
}
