package com.example.RecipeHero;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import static com.example.RecipeHero.R.*;

public class Recipe implements Serializable {
    private static final long serialVersionUID = 1234567L;
    private int ID;
    private Date doc;
    private String recipeTitle = "";
    private ArrayList<Ingredient> Ingredients = new ArrayList<>();
    private String recipeInstructions = "";
    private Nutrition nutritionSummary;
    public boolean nutritionSummaryLocked = false;
    private double rating = 0; //0-10.0
    private RecipeType recipeType = RecipeType.NONE;
    //private ArrayList<String> savedImageLocations = new ArrayList<>();
    private BitmapSerializable recipeIcon;
    public String videoTutorial = "";
    private int cartQuantity = 0;
    private String sourceUrl = "";
    public String sourceSite = "";
    public enum RecipeType {
        AlcoholicDrink{
            @Override
            public int getDrawable() {
                return drawable.ic_iconmonstr_beer_3;
            }
        }, Desert {
            @Override
            public int getDrawable() {
                return drawable.ic_iconmonstr_candy_13;
            }
        }, Drink {
            @Override
            public int getDrawable() {
                return drawable.ic_iconmonstr_drink;
            }
        }, Meal {
            @Override
            public int getDrawable() {
                return drawable.ic_iconmonstr_entree;
            }
        }, NONE {
            @Override
            public int getDrawable() {
                return drawable.ic_baseline_library_none;
            }
        }, Side {
            @Override
            public int getDrawable() {
                return drawable.ic_iconmonstr_side;
            }
        };
        public abstract int getDrawable();
    }


    public Recipe(String name) {
        recipeTitle = name;
        ID = MainActivity.UniqueID;
        MainActivity.UniqueID++;
        doc = new Date();
    }
    public Recipe(String name, ArrayList<Ingredient> ingr) {
        recipeTitle = name;
        Ingredients = ingr;
        ID = MainActivity.UniqueID;
        MainActivity.UniqueID++;;
        doc = new Date();
    }
    public Recipe(String name, Ingredient ingr) {
        recipeTitle = name;
        Ingredients.add(ingr);
        ID = MainActivity.UniqueID;
        MainActivity.UniqueID++;
        doc = new Date();
    }
    public Recipe(String name, Ingredient ingr, int cartQuant) {
        recipeTitle = name;
        Ingredients.add(ingr);
        ID = MainActivity.UniqueID;
        MainActivity.UniqueID++;
        doc = new Date();
        cartQuantity = cartQuant;
    }
    void incrementQuant(){
        cartQuantity++;
    }
    void decrementQuant(){
        if(cartQuantity >= 1) cartQuantity--;
    }

    public int getCartQuantity() {
        return cartQuantity;
    }
    public void setCartQuantity(int newQuantity){
        if(newQuantity >= 0) cartQuantity = newQuantity;
        else cartQuantity = 0;
    }
    public ArrayList<Ingredient> getIngredientsTimesCart(){

        Recipe temp = new Recipe(this.getRecipeTitle(),  this.getIngredients());
        ArrayList<Ingredient> rec = new ArrayList<>();

        //rec.addAll(temp.getIngredients());

        for (Ingredient ingr : temp.getIngredients()) {
            rec.add(new Ingredient(ingr.getName() ,ingr.getQuantity()* cartQuantity, ingr.getMeasurementType(), ingr.getAdditionalNote()));
        }
        return rec;
    }
    public void setNutritionSummary(Nutrition nut){
        nutritionSummary = nut;
    }
    public Nutrition getNutritionSummary(){
        return nutritionSummary;
    }
    public int getID() {
        return ID;
    }

    public void setRecipeType(RecipeType rt){
        recipeType = rt;
    }
    public RecipeType getRecipeType(){
        return recipeType;
    }
    public String getRecipeTitle() {
        return recipeTitle;
    }

    public void setRecipeTitle(String recipeTitle) {
        this.recipeTitle = recipeTitle;
    }

    public void setIngredients(ArrayList<Ingredient> ingredients) {
        Ingredients = ingredients;
        //Ingredients.addAll(ingredients);
    }

    public String getRecipeInstructions() {
        return recipeInstructions;
    }

    public void setRecipeInstructions(String recipeInstructions) {
        this.recipeInstructions = recipeInstructions;
    }
    public void setVideoTutorial(String url){
        videoTutorial = url;
    }
    public String getVideoTutorial(){
        return videoTutorial;
    }
    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }


    public ArrayList<Ingredient> getIngredients(){
        return Ingredients;
    }

    public String getIngredientsAsStringList(){
        String ingredientsSoFar = "";
        for(int i = 0; i < getIngredients().size(); i++){
            ingredientsSoFar += getIngredients().get(i).toString();
            if(i < getIngredients().size() -1) ingredientsSoFar += " \n";
        }
        return ingredientsSoFar;
    }

    public void setIcon(Bitmap icon, Context context){
        recipeIcon = new BitmapSerializable(icon);
        //addImage(MainActivity.saveToInternalStorage(icon, context));
    }
    public Bitmap getRecipeIcon(){
        if(recipeIcon == null) return null;
        return recipeIcon.bitmap;
    }
    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }
}
