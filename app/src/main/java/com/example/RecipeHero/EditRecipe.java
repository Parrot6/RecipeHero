package com.example.RecipeHero;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class EditRecipe extends AppCompatActivity implements AdapterView.OnItemSelectedListener, IngredientsAdapter.MyClickListener{
    TextView name;
    RecyclerView ingredients;
    RecyclerView nutritionResultRV;
    TextView newIngredient;
    TextView addNote;
    TextView instructions;
    int recipeIndex = 999;
    Button cancelButton;
    Button saveButton;
    Button addPhoto;
    Button addIngredient;
    ImageView recipeTypeImg;
    Button recipeType;
    Recipe recipe;
    ImageView icon;
    Button getNutrition;
    TextView nutritionAttribution;
    ConstraintLayout hideForNutrition;
    ConstraintLayout nutritionLayout;
    IngredientsAdapter adapter;
    Context context;
    private ArrayList<Ingredient> currentIngredients = new ArrayList<Ingredient>();
    private Spinner measureType;
    private Spinner quantity;
    static final int REQUEST_IMAGE_CAPTURE = 1;

    //private static final String[] types = {"tsp.","tbsp.","fl oz","cup","Other Value","fl pt","ft qt","gal","mL","L"};
    //private static String[] quantities = {"1","2","3","4","Other Value"};
    public ArrayList<String> quantitiesArr = new ArrayList<>();
    public ArrayList<String> typesArr = new ArrayList<>();
    public ProgressBar nutritionLoading;
    private static int PICK_IMAGE = 77;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_recipe);
        context = EditRecipe.this;
        Intent intent = getIntent();
        recipe = (Recipe) getIntent().getSerializableExtra("Recipe");
        //String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        recipeIndex = (int) getIntent().getSerializableExtra("layoutPosition");

        quantitiesArr.addAll(MainActivity.getQuantities());
        typesArr.addAll(MainActivity.getTypes());
        currentIngredients.addAll(recipe.getIngredients());

        name = findViewById(R.id.text_RecipeTitle);
        name.setText(recipe.getRecipeTitle());

        addPhoto = findViewById(R.id.button_addPhoto);
        addPhoto.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View view) {
                MenuBuilder menuBuilder = new MenuBuilder(context);
                MenuInflater inflater = new MenuInflater(context);
                inflater.inflate(R.menu.addphoto_options, menuBuilder);

                MenuPopupHelper optionsMenu = new MenuPopupHelper(context, menuBuilder, view);

                optionsMenu.setForceShowIcon(true);

                menuBuilder.setCallback(new MenuBuilder.Callback() {
                    @Override
                    public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.rt_choosephoto: // Handle option1 Click
                                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                                photoPickerIntent.setType("image/*");
                                startActivityForResult(photoPickerIntent, PICK_IMAGE);
                                return true;
                            case R.id.rt_takephoto: // Handle option2 Click
                                dispatchTakePictureIntent();
                                addPhoto.setText("new");
                                return true;
                            default:
                                return false;
                        }
                    }

                    @Override
                    public void onMenuModeChange(MenuBuilder menu) {}
                });

                optionsMenu.show();

            }
          });


        icon = findViewById(R.id.editRecipe_icon);
        if(recipe.getRecipeIcon() == null){
            icon.setImageResource(R.drawable.ic_baseline_add_a_photo_24);
        } else {
            icon.setImageBitmap(recipe.getRecipeIcon());
            addPhoto.setText("new");
        }


        ingredients = findViewById(R.id.recycleView_Ingredients);
        ingredients.setLayoutManager(new LinearLayoutManager(this));
        adapter = new IngredientsAdapter(this, currentIngredients, this);

        ingredients.setAdapter(adapter);

        newIngredient = findViewById(R.id.text_newIngredient);

        newIngredient.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                boolean bl = (newIngredient.getText().length() > 0);
                addIngredient.setEnabled(bl);
                addIngredient.setClickable(bl);
                if(!bl) addIngredient.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY); else addIngredient.getBackground().setColorFilter(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        addNote = findViewById(R.id.text_Note);
        View ingredBar = findViewById(R.id.layout_ingredientsNameBar);
        View addBar = findViewById(R.id.layout_editrecipe_addIngred);
        nutritionLayout = findViewById(R.id.nutrition);
        nutritionLayout.setVisibility(View.GONE);
        Button editInstructions = findViewById(R.id.button_editrecipe_editInstructions);
        editInstructions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                //builder.set
                final View customLayout = getLayoutInflater().inflate(R.layout.edit_instructions_dialog, null);
                final String item_value = instructions.getText().toString();
                final EditText input = customLayout.findViewById(R.id.edit_instructions_text);
                input.setText(item_value);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setSingleLine(false);
                input.setVerticalScrollBarEnabled(true);
                input.setMaxLines(14);
                input.setLines(14);
                input.setGravity(Gravity.LEFT | Gravity.TOP);
                builder.setView(customLayout);
                builder.setCancelable(false);
                Button confirm = customLayout.findViewById(R.id.edit_instructions_text_confirm);
                AlertDialog alert = builder.create();
                alert.show();
                confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        instructions.setText(input.getText());
                        alert.dismiss();
                    }
                });
                Button cancel = customLayout.findViewById(R.id.edit_instructions_cancel);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alert.dismiss();
                    }
                });

            }
        });
        instructions = findViewById(R.id.text_editRecipeInstructions);

        if(recipe.getRecipeInstructions() != null) {
            instructions.setText(recipe.getRecipeInstructions());
        } else instructions.setText("");

        saveButton = findViewById(R.id.button_saveEdits);
        nutritionLoading = findViewById(R.id.progressBar_nutritionLoading);
        nutritionResultRV = findViewById(R.id.recyclerView_nutrition_info);
        nutritionResultRV.setLayoutManager(new LinearLayoutManager(this));
        //nutritionResultRV.setVisibility(View.GONE);

        hideForNutrition = findViewById(R.id.layout_edit_recipe_toggleLayout);
        nutritionAttribution = findViewById(R.id.text_nutrition_attribution);
        nutritionAttribution.setMovementMethod(LinkMovementMethod.getInstance());
        //nutritionAttribution.setVisibility(View.GONE);

        getNutrition = findViewById(R.id.button_edit_recipe_getNutrition);
        Boolean Fetch = false;
        for(Ingredient ing : currentIngredients){
            if(ing.getNutrition() == null) {
                Fetch = true;
                getNutrition.setText(R.string.nutritionButtonLoad);
            }
        }
        if(!Fetch) getNutrition.setText("View Nutrition");
        getNutrition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentIngredients.size() == 0) return;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                nutritionLoading.setVisibility(View.VISIBLE);
                                toggleView(hideForNutrition);
                                toggleView(nutritionLayout);
                                String state = getNutrition.getText().toString();
                                if(state.equals(getString(R.string.ViewLoadedNutritionButton))) {
                                    getNutrition.setText(getString(R.string.BackToRecipeNutritionButton));
                                    return;
                                } else if(state.equals(getString(R.string.BackToRecipeNutritionButton))){
                                    getNutrition.setText(getString(R.string.ViewLoadedNutritionButton));
                                    return;
                                } else getNutrition.setText(getString(R.string.BackToRecipeNutritionButton));
                                getNutrition.setEnabled(false);
                            }
                        });


                        NutritionQuery query;
                        Nutrition totalNutritionSoFar = new Nutrition();
                        for (Ingredient ing: currentIngredients
                        ) {
                           if(ing.getNutrition() != null){
                                totalNutritionSoFar.getCombined(ing.getNutrition());
                                continue;
                            }
                            query = new NutritionQuery(ing);
                            while(!query.finished){

                            }
                            if(!query.failed) {
                                ing.setNutrition(query.getBestResult());
                                totalNutritionSoFar.getCombined(ing.getNutrition());
                            }
                        }
                        Nutrition finalTotalNutritionSoFar = totalNutritionSoFar;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                nutritionLoading.setVisibility(View.GONE);
                                getNutrition.setEnabled(true);
                                recipe.setNutritionSummary(finalTotalNutritionSoFar);
                                nutritionResultRV.setAdapter(new NutritionAdapter(context, currentIngredients, recipe));
                                nutritionResultRV.setVisibility(View.VISIBLE);
                            }
                        });

                    }
                }).start();

            }
        });


        cancelButton = findViewById(R.id.button_cancelEdits);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recipe.setRecipeTitle(name.getText().toString());
                recipe.setRecipeInstructions(instructions.getText().toString());
                recipe.setIngredients(currentIngredients);
                updateRecipeTotalNutrition();
                Intent returnIntent = new Intent();
                returnIntent.putExtra("passed_item", recipe);
                returnIntent.putExtra("Action", "Save");
                returnIntent.putExtra("layoutPosition", recipeIndex);
                setResult(RESULT_OK, returnIntent); //By not passing the intent in the result, the calling activity will get null data.
                finish();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        quantity = findViewById(R.id.spinner_quantity);
        final ArrayAdapter<String> adapterSpinnerQuantity = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, quantitiesArr);
        adapterSpinnerQuantity.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        quantity.setAdapter(adapterSpinnerQuantity);
        quantity.setOnItemSelectedListener(this);

        measureType = findViewById(R.id.spinner_measurement);
        ArrayAdapter<String> adapterSpinnerType = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, typesArr);
        adapterSpinnerType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        measureType.setAdapter(adapterSpinnerType);
        measureType.setOnItemSelectedListener(this);
        addIngredient = findViewById(R.id.button_addIngredient);
        addIngredient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //currentIngredients.add(new Ingredient("Test"));
                currentIngredients.add( new Ingredient(newIngredient.getText().toString() , Double.parseDouble(quantity.getSelectedItem().toString()), measureType.getSelectedItem().toString(), addNote.getText().toString()));
                adapter.notifyDataSetChanged();
                ingredients.scrollToPosition(currentIngredients.size()-1);
                newIngredient.setText("");
                quantity.setSelection(0);
                measureType.setSelection(0);
                addNote.setText("Note");
                getNutrition.setText(R.string.nutritionButtonLoad);
            }
        });
        addIngredient.setEnabled(false);
        addIngredient.setClickable(false);
        addIngredient.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
        recipeType = findViewById(R.id.button_edit_recipe_recipeType);
        recipeTypeImg = findViewById(R.id.edit_recipe_recipeType);
        recipeTypeImg.setImageResource(recipe.getRecipeType().getDrawable());
        if(recipe.getRecipeType() == Recipe.RecipeType.NONE) recipeType.setText("choose");

        recipeType.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View view) {
                MenuBuilder menuBuilder = new MenuBuilder(context);
                MenuInflater inflater = new MenuInflater(context);
                inflater.inflate(R.menu.recipe_type_menu, menuBuilder);
                MenuPopupHelper optionsMenu = new MenuPopupHelper(context, menuBuilder, view);
                optionsMenu.setForceShowIcon(true);

// Set Item Click Listener
                menuBuilder.setCallback(new MenuBuilder.Callback() {
                    @Override
                    public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.rt_Alcohol: // Handle option1 Click
                                recipe.setRecipeType(Recipe.RecipeType.AlcoholicDrink);
                                recipeType.setText("change");
                                recipeTypeImg.setImageResource(recipe.getRecipeType().getDrawable());
                                return true;
                            case R.id.rt_Desert: // Handle option2 Click
                                recipe.setRecipeType(Recipe.RecipeType.Desert);
                                recipeType.setText("change");
                                recipeTypeImg.setImageResource(recipe.getRecipeType().getDrawable());
                                return true;
                            case R.id.rt_Drink: // Handle option2 Click
                                recipe.setRecipeType(Recipe.RecipeType.Drink);
                                recipeType.setText("change");
                                recipeTypeImg.setImageResource(recipe.getRecipeType().getDrawable());
                                return true;
                            case R.id.rt_Meal: // Handle option2 Click
                                recipe.setRecipeType(Recipe.RecipeType.Meal);
                                recipeType.setText("change");
                                recipeTypeImg.setImageResource(recipe.getRecipeType().getDrawable());
                                return true;
                            case R.id.rt_Side: // Handle option2 Click
                                recipe.setRecipeType(Recipe.RecipeType.Side);
                                recipeType.setText("change");
                                recipeTypeImg.setImageResource(recipe.getRecipeType().getDrawable());
                                return true;
                            default:
                                recipe.setRecipeType(Recipe.RecipeType.NONE);
                                recipeType.setText("choose");
                                recipeTypeImg.setImageResource(recipe.getRecipeType().getDrawable());
                                return false;
                        }
                    }

                    @Override
                    public void onMenuModeChange(MenuBuilder menu) {}
                });

                optionsMenu.show();

            }
        });


    }

    private void updateRecipeTotalNutrition() {
        if(recipe.nutritionSummaryLocked) return;
        Nutrition recipeSummary = new Nutrition();
        for (Ingredient ing :
                currentIngredients) {
            if(ing.getNutrition() == null) continue;
            recipeSummary.getCombined(ing.getNutrition());
        }
        recipe.setNutritionSummary(recipeSummary);
    }

    public void toggleView(View view){
        if(view.getVisibility() == View.GONE || view.getVisibility() == View.INVISIBLE) view.setVisibility(View.VISIBLE);
        else view.setVisibility(View.GONE);
    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if(adapterView.getId() == R.id.spinner_quantity) {
           if(adapterView.getSelectedItem().toString().equals("Other Value")){
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                    builder1.setTitle("Quantity");
                    builder1.setMessage("Write your custom quantity below");
                    builder1.setCancelable(true);
// Set up the input
                    final EditText input = new EditText(this);
                    input.setBackground(ContextCompat.getDrawable(context, R.color.pantryOtherTypes));
                    input.setGravity(Gravity.CENTER);
                    input.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    builder1.setView(input);
                    quantity.setSelection(0);
                    builder1.setPositiveButton(
                            "Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    String inputText = input.getText().toString();
                                    quantitiesArr.add(inputText);
                                    quantity.setSelection(quantitiesArr.size() - 1);
                                    dialog.cancel();
                                }
                            });
                    builder1.setNegativeButton(
                            "No",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alert11 = builder1.create();
                    alert11.show();
            }
        }
        if(adapterView.getId() == R.id.spinner_measurement) {
            if( adapterView.getSelectedItem().toString().equals("Other Value")){
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                    builder1.setTitle("Measurement");
                    builder1.setMessage("Write your custom measurement below");
                    builder1.setCancelable(true);

                    final EditText input = new EditText(this);
                    input.setBackground(ContextCompat.getDrawable(context, R.color.pantryOtherTypes));
                    input.setGravity(Gravity.CENTER);
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    builder1.setView(input);
                    measureType.setSelection(0);
                    builder1.setPositiveButton(
                            "Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    String inputText = input.getText().toString();
                                    typesArr.add(inputText);
                                    measureType.setSelection(typesArr.size() - 1);
                                    dialog.cancel();
                                }
                            });

                    builder1.setNegativeButton(
                            "No",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alert11 = builder1.create();
                    alert11.show();
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if(resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                //imageView.setImageBitmap(imageBitmap);
                icon.setVisibility(View.VISIBLE);
                icon.setImageBitmap(imageBitmap);
                recipe.setIcon(imageBitmap, getApplicationContext());
            }
        }
        if (requestCode == PICK_IMAGE ) {
            if(resultCode == RESULT_OK) {
                try {
                    final Uri imageUri = data.getData();
                    final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    final Bitmap resized = Bitmap.createScaledBitmap(selectedImage, 300, 300, false);
                    icon.setVisibility(View.VISIBLE);
                    icon.setImageBitmap(resized);
                    recipe.setIcon(resized, getApplicationContext());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(EditRecipe.this, "Something went wrong", Toast.LENGTH_LONG).show();
                }

                }

            }
    }

    @Override
    public void onBackPressed()
    {
       /* Intent returnIntent = new Intent();
        returnIntent.putExtra("passed_item", recipe);
        returnIntent.putExtra("Action", "Back");
        returnIntent.putExtra("Index", recipeIndex);
        setResult(RESULT_OK, returnIntent);*/
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        setResult(RESULT_CANCELED);
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        setResult(RESULT_CANCELED);
        super.onDestroy();
    }
    @Override
    public void deleteIngredient(int layoutPosition) {
        currentIngredients.remove(layoutPosition);
        adapter.notifyItemRemoved(layoutPosition);
    }

    @Override
    public void updateIngredient(int layoutPosition) {
      //  currentIngredients.remove(layoutPosition);
      //  adapter.notifyItemRemoved(layoutPosition);
    }
}