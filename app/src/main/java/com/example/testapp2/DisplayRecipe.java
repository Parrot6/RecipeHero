package com.example.testapp2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.text.DecimalFormat;

public class DisplayRecipe extends AppCompatActivity {
    private static int REQUEST_CODE = 100;
    TextView name;
    TextView ingredients;
    TextView instructions;
    TextView nutrition;
    int recipeIndex;
    Button edit;
    Button export = null; //to do
    int layoutPosition; //if preview'd
    Button delete;
    Recipe recipe;
    String returnAction = "none";
    ImageView icon;
    ImageView recipeType;
    ImageView vidTutorial;
    boolean preview = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_recipe);

        Intent intent = getIntent();
        //recipes = (ArrayList<Recipe>) getIntent().getSerializableExtra("Recipes");
        recipe = (Recipe) intent.getSerializableExtra("Recipe");
        recipeIndex = (int) intent.getSerializableExtra("Index");
        //String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        name = findViewById(R.id.text_RecipeTitle);
        name.setText(recipe.getRecipeTitle());
        ingredients = findViewById(R.id.text_Ingredients);
        if(!recipe.getVideoTutorial().equals("")) {
            vidTutorial = findViewById(R.id.button_display_recipe_videoTut);
            vidTutorial.setVisibility(View.VISIBLE);
            vidTutorial.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(recipe.getVideoTutorial()));
                    startActivity(browserIntent);
                }
            });
        }
        icon = findViewById(R.id.displayrecipe_icon);
        if(recipe.getRecipeIcon() == null) icon.setVisibility(View.INVISIBLE);
        else {
            Bitmap b = recipe.getRecipeIcon();
            icon.setImageBitmap(b);
        }
        recipeType = findViewById(R.id.display_recipe_recipeType);
        recipeType.setImageResource(recipe.getRecipeType().getDrawable());
        if(recipe.getRecipeType() == Recipe.RecipeType.NONE) recipeType.setVisibility(View.INVISIBLE);

        ingredients.setText(recipe.getIngredientsAsStringList());
        instructions = findViewById(R.id.Text_RecipeInstuctions);
        instructions.setText(recipe.getRecipeInstructions());
        if(recipe.getRecipeInstructions() == "") instructions.setText("No Instructions");
        nutrition = findViewById(R.id.layout_deisplay_recipe_nutritionHolder);
        final Nutrition thisNut = recipe.getNutritionSummary();
        if(thisNut == null) nutrition.setText("Edit Recipe to load nutrition!");
        else {
            DecimalFormat df2 = new DecimalFormat("#.##");
            StringBuilder sb = new StringBuilder();
            for (final Field f : thisNut.getClass().getDeclaredFields()) {
                try {
                    if (f.get(thisNut) instanceof Double && f.get(thisNut) != null && (Double) f.get(thisNut) != 0.0) {
                        sb.append(Nutrition.getFormatted(f, thisNut));
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            nutrition.setText(sb.toString());
        }
        edit = findViewById(R.id.button_saveEdits);
        export = findViewById(R.id.button_export);
        delete = findViewById(R.id.button_cancelEdits);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edit();
            }
        });
        export.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", printRecipe());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getApplicationContext(),"Copied to Clipboard!",Toast.LENGTH_SHORT).show();

            }
        });
        if(intent.getAction() == "Preview"){
            preview = true;
            delete.setText("Go Back");
            edit.setText("Add To Recipes");
            layoutPosition = (int) intent.getExtras().get("layoutPosition");
        }
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("passed_item", recipe);
                returnIntent.putExtra("layoutPosition", layoutPosition);
                returnIntent.putExtra("Index", recipeIndex);
                returnAction = "Delete";
                returnIntent.putExtra("Action", returnAction);
                // setResult(RESULT_OK);
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });

    }

    private String printRecipe() {
        StringBuilder sb = new StringBuilder();
        sb.append("*" + recipe.getRecipeTitle() + "*\nIngredients: \n");
        sb.append(recipe.getIngredientsAsStringList()+"Instructions: \n");
        sb.append(recipe.getRecipeInstructions() + "\n");
        sb.append("Nutrition: \n" + nutrition.getText().toString());
        String s = sb.toString();
        return s;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            Recipe passedItem = (Recipe) data.getExtras().get("passed_item");
            recipeIndex = (int) data.getExtras().get("Index");
            returnAction = (String) data.getExtras().get("Action");

            // deal with the item yourself
            recipe = passedItem;
            returnAction = "Save";
            Intent returnIntent = new Intent();
            returnIntent.putExtra("passed_item", recipe);
            returnIntent.putExtra("Index", recipeIndex);
            returnIntent.putExtra("Action", returnAction);

            setResult(RESULT_OK, returnIntent);
            finish();
        }
    }

    public void edit(){
        if(preview){
            returnAction = "Save";
            Intent returnIntent = new Intent();
            returnIntent.putExtra("passed_item", recipe);
            returnIntent.putExtra("Action", returnAction);
            returnIntent.putExtra("layoutPosition", layoutPosition);
            setResult(RESULT_OK, returnIntent);
            finish();
        } else {
            Intent newIntent = new Intent(this, EditRecipe.class);
            newIntent.putExtra("Recipe", recipe);
            newIntent.putExtra("Index", recipeIndex);
            startActivityForResult(newIntent, REQUEST_CODE);
        }
    }
}