package com.example.testapp2;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintJob;
import android.print.PrintManager;
import android.print.pdf.PrintedPdfDocument;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;


public class MainActivity extends AppCompatActivity{

    BottomNavigationView bottomNavigationView;

    static ArrayList<Recipe> recipes = new ArrayList<>();
    static ArrayList<SameNameIngredients> pantry = new ArrayList<>();
    Context context;
    //Fragments
    public static Recipe.RecipeType CURRENT_SORT = Recipe.RecipeType.NONE;
    public static int UniqueID = 0; //global id count
    private static final String[] types = {"tsp","tbsp","fl oz","cup","g","Other Value","fl pt","ft qt","gal","mL","L"};
    private static String[] quantities = {"1","2","3","4","Other Value"};
    private String fileName = "pantryTest25";

    public static boolean orderInfiniteIngredientsLast = true;
    public static ArrayList<UnitConversion> conversions = new ArrayList<>();
    SearchFragment search = new SearchFragment();
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        readFile(context, true);
        if(recipes.size() == 0) {
            //examples of data
            initializeExamplesOfData();
        }

        initializeConversions();
        updateUniqueID();

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Fragment fragment = null;
                        switch (item.getItemId()) {
                            case R.id.nav_Home:
                                fragment = new ViewRecipesFragment();
                                break;
                            case R.id.nav_shopping_list:
                                fragment = new EditCartFragment();
                                break;
                            case R.id.nav_pantry:
                                fragment = new PantryFragment();
                                break;
                            case R.id.nav_search:
                                fragment = search;
                                break;
                    }
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, fragment).commit();
                        return true;
                    }
                });
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new ViewRecipesFragment()).commit();

        Toolbar myToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(myToolbar);

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.printAll: {
                Toast.makeText(context, "working", Toast.LENGTH_SHORT).show();
                printAllRecipes(context, true);
                break;
            }
            case R.id.contact_me:{
                Intent email = new Intent(Intent.ACTION_SEND);
                email.putExtra(Intent.EXTRA_EMAIL, new String[]{"RecipeHeroApp@gmail.com"});
                email.putExtra(Intent.EXTRA_SUBJECT, "Bug/Suggestion");
                email.putExtra(Intent.EXTRA_TEXT, "Whats wrong: \n\n\n Suggestions: \n");

                //need this to prompts email client only
                email.setType("message/rfc822");

                startActivity(Intent.createChooser(email, "Choose an Email client :"));
                break;
            }
            case R.id.donate_please:{
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                final View customLayout = getLayoutInflater().inflate(R.layout.donate, null);
                alertDialog.setView(customLayout);
                AlertDialog alert = alertDialog.create();
                alert.setCanceledOnTouchOutside(false);
                alert.show();
                Button close = customLayout.findViewById(R.id.button_donate_close);
                close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alert.cancel();
                    }
                });
                break;
            }
            default:{
                  break;
            }

            // case blocks for other MenuItems (if any)
        }
        return true;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    private void updateUniqueID(){ //set up for new recipe adds
        for (Recipe rec :
                recipes) {
            if (rec.getID() > UniqueID) UniqueID = (rec.getID() + 1);
        }
    }
    private void initializeExamplesOfData() {
        Ingredient ingred1 = new Ingredient("Chicken", 10, "oz");
        Ingredient ingred2 = new Ingredient("Turkey", 10, "oz");
        Ingredient ingred3 = new Ingredient("Chicken", 2, "lb");
        Ingredient ingred4 = new Ingredient("Milk", 10, "cup");
        Ingredient ingred5 = new Ingredient("Milk", 6, "fl oz");
        Ingredient ingred6 = new Ingredient("Parmesan", 2, "cup");
        Ingredient ingred7 = new Ingredient("Parmesan", 100, "g");
        Ingredient ingred8 = new Ingredient("Salt", 10, "tsp");
        Ingredient ingred9 = new Ingredient("Pepper", 10, "tsp");
        Ingredient pasta = new Ingredient("Pasta", 12, "oz");
        Ingredient sauce = new Ingredient("Tomato Sauce", 8, "oz");
        Ingredient grbe = new Ingredient("Green Beans", 12, "oz");
        Ingredient bull = new Ingredient("Bullion Cube", 1, "cube");
        ArrayList<Ingredient> ingredients1 = new ArrayList<>(Arrays.asList(ingred1, ingred2, ingred3, ingred4, ingred5));
        ArrayList<Ingredient> ingredients2 = new ArrayList<>(Arrays.asList(ingred2, ingred3, ingred7, ingred5, ingred6));
        ArrayList<Ingredient> ingredients3 = new ArrayList<>(Arrays.asList(ingred4, ingred5, ingred6, ingred7, ingred8));
        ArrayList<Ingredient> ingredients4 = new ArrayList<>(Arrays.asList(ingred5, ingred2, ingred6, ingred7, ingred9));
        ArrayList<Ingredient> ingredients5 = new ArrayList<>(Arrays.asList(ingred2, ingred3, ingred5, ingred7, ingred9));
        ArrayList<Ingredient> spag = new ArrayList<>(Arrays.asList(pasta, sauce));
        ArrayList<Ingredient> gb = new ArrayList<>(Arrays.asList(grbe, bull));
        addRecipe(0, new Recipe("Alfredo", ingredients1), 1);
        addRecipe(0, new Recipe("Pulled Pork", ingredients2), 2);
        addRecipe(0, new Recipe("Sweet and Sour Chicken", ingredients3), 3);
        addRecipe(0, new Recipe("Spaghetti", spag), 4);
        addRecipe(0, new Recipe("Green Beans", gb), 4);
        makeSummaryRecipeIngredients();
    }

    private void initializeConversions() {
        //FIRST NAME IS THE PREFERRED NAME - ANY VARIANT WILL CONVERT TO PREFERRED
        UnitConversion tbsp = new UnitConversion(1.0,"tbsp").addVariantNames(new ArrayList<String>(Arrays.asList("tablespoons","tablespoon","tbsp.","tblspn","tbsp.")));
        UnitConversion tspn = new UnitConversion(1.0,"tsp").addVariantNames(new ArrayList<String>(Arrays.asList("teaspoons","teaspoon","tspn.","tspn","tsp.")));
        UnitConversion cup = new UnitConversion(1.0,"cup").addVariantNames(new ArrayList<String>(Arrays.asList("cups","cup.")));
        UnitConversion pint = new UnitConversion(1.0,"pt").addVariantNames(new ArrayList<String>(Arrays.asList("pints","pint.","pint","pt.","Pnt")));
        UnitConversion quart = new UnitConversion(1.0,"qt").addVariantNames(new ArrayList<String>(Arrays.asList("quarts","quart.","quart")));
        UnitConversion gal = new UnitConversion(1.0,"gal").addVariantNames(new ArrayList<String>(Arrays.asList("gals","gal.","gallon","gall")));
        UnitConversion L = new UnitConversion(1.0,"L").addVariantNames(new ArrayList<String>(Arrays.asList("L.","Liter","lit.")));
        UnitConversion mL = new UnitConversion(1.0,"mL").addVariantNames(new ArrayList<String>(Arrays.asList("mL.","milliLiter")));
        UnitConversion ounce = new UnitConversion(1.0, "oz").addVariantNames(new ArrayList<String>(Arrays.asList("oz.","ounce","ounces")));
        UnitConversion fluidounce = new UnitConversion(1.0, "fl oz").addVariantNames(new ArrayList<String>(Arrays.asList("fl oz.","fluid ounce")));
        UnitConversion pound = new UnitConversion(1.0, "lb").addVariantNames(new ArrayList<String>(Arrays.asList("lbs","lb.","pound")));
        UnitConversion gram = new UnitConversion(1.0, "g").addVariantNames(new ArrayList<String>(Arrays.asList("gs","gram","g.")));
        UnitConversion dash = new UnitConversion(1.0, "dash").addVariantNames(new ArrayList<String>(Arrays.asList("dashes")));
        UnitConversion pinch = new UnitConversion(1.0, "pinch").addVariantNames(new ArrayList<String>(Arrays.asList("pinches")));
        dash.addEquivUnitConversion(new UnitConversion(1.0/8.0, tspn)).addEquivUnitConversion(new UnitConversion(1.0/24.0, tbsp));
        fluidounce.addEquivUnitConversion(new UnitConversion(30.0, mL)).addEquivUnitConversion(new UnitConversion(.125, cup));
        ounce.addEquivUnitConversion(new UnitConversion(28.0, gram));
        tspn.addEquivUnitConversion(new UnitConversion(5.0, mL)).addEquivUnitConversion(new UnitConversion(.005, L)).addEquivUnitConversion(new UnitConversion(3.0, tbsp));
        tspn.addEquivUnitConversion(new UnitConversion(8.0, dash));
        pound.addEquivUnitConversion(new UnitConversion(113.0*4, gram));
        tbsp.addEquivUnitConversion(new UnitConversion(3.0, tspn)).addEquivUnitConversion(new UnitConversion(15.0, mL)).addEquivUnitConversion(new UnitConversion(.015, L));
        tbsp.addEquivUnitConversion(new UnitConversion(1.0/16.0, cup)).addEquivUnitConversion(new UnitConversion(240.0, mL)).addEquivUnitConversion(new UnitConversion(.24,L));
        tbsp.addEquivUnitConversion(new UnitConversion(24.0, dash));
        L.addEquivUnitConversion(new UnitConversion(1000.0, mL));
        cup.addEquivUnitConversion(new UnitConversion(250.0, mL)).addEquivUnitConversion(new UnitConversion(.25, L)).addEquivUnitConversion(new UnitConversion(16.0, tbsp));
        pint.addEquivUnitConversion(new UnitConversion(500.0, mL)).addEquivUnitConversion(new UnitConversion(.5, L));
        quart.addEquivUnitConversion(new UnitConversion(.95, L)).addEquivUnitConversion(new UnitConversion(950.0, mL));
        gal.addEquivUnitConversion(new UnitConversion(3.8, L)).addEquivUnitConversion(new UnitConversion(3800.0, mL));
        pinch.addEquivUnitConversion(new UnitConversion(1.0/16.0, tspn)).addEquivUnitConversion(new UnitConversion(1.0/48.0, tbsp));
        conversions.addAll(new ArrayList<>(Arrays.asList(tbsp, tspn, cup, pint, quart, gal, L, mL, ounce, fluidounce, pound, gram, dash, pinch)));
    }

    private static void makeSummaryRecipeIngredients() {
        ArrayList<SameNameIngredients> ingredientSummary = new ArrayList<>();
        //add pantry from file
        for (SameNameIngredients sni: pantry
        ) {
            ArrayList<Ingredient> temp1 = sni.getIngredientList();
            boolean Added1 = false;
            for (int i = 0; i < temp1.size(); i++) {
                Ingredient ingred = temp1.get(i);
                if (ingred.getQuantity() <= 0) continue;
                for (int k = 0; k < ingredientSummary.size(); k++) {
                    if (ingredientSummary.size() == 0) {
                        ingredientSummary.add(new SameNameIngredients(ingred));
                        Added1 = true;
                        break;
                    } else if (Added1 = ingredientSummary.get(k).handleNewIngredient(ingred)) {
                        break;
                    }
                }
                if (!Added1) ingredientSummary.add(new SameNameIngredients(ingred));

                //pantry = ingredientSummary;
            }
        }
        //add ingredients from recipes
        for (Recipe rec: recipes
             ) {
            ArrayList<Ingredient> temp = rec.getIngredients();
            //allRecipeIngred.add(new )
            //ArrayList<Ingredient> temp = cartData.get(outer).getIngredientsTimesCart();
            boolean Added = false;
            for (int i = 0; i < temp.size(); i++) { //for EACH INGREDIENT PER CART ITEM
                Ingredient ingred = new Ingredient(temp.get(i).getName(), 0, temp.get(i).getMeasurementType(), temp.get(i).getAdditionalNote());

                for (int k = 0; k < ingredientSummary.size(); k++) {
                    if(ingredientSummary.size() == 0){
                        ingredientSummary.add(new SameNameIngredients(ingred));
                        Added = true;
                        break;
                    } else if(Added = ingredientSummary.get(k).handleNewIngredient(ingred)){ break;}
                }
                if(!Added) ingredientSummary.add(new SameNameIngredients(ingred));
            }
        }
        pantry = ingredientSummary;
        sortPantryData();
    }

    public static class SavePackage implements Serializable {
        private static final long serialVersionUID = 1234567L;
        public ArrayList<Recipe> recipes1 = new ArrayList<>();
        public ArrayList<SameNameIngredients> pantry1 = new ArrayList<>();
        public String Current_Sort;
        public SavePackage(ArrayList<Recipe> rec, ArrayList<SameNameIngredients> pant, Recipe.RecipeType current_Sort){
            recipes1.addAll(rec);
            pantry1.addAll(pant);
            Current_Sort = current_Sort.toString();
        }
    }
    public void Save(){
        //createFile(context, true);
        writeFile(context, true);
    }

    private void createFile(Context context, boolean isPersistent) {
        File file;
        if (isPersistent) {
            file = new File(context.getFilesDir(), fileName);
        } else {
            file = new File(context.getCacheDir(), fileName);
        }
        Toast.makeText(context, String.format("inputFile", file.getAbsolutePath()), Toast.LENGTH_SHORT).show();
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Toast.makeText(context, String.format("File %s creation failed", fileName), Toast.LENGTH_SHORT).show();
            }
        }
    }

    //src: https://github.com/learntodroid/FileIOTutorial/blob/master/app/src/main/java/com/learntodroid/fileiotutorial/InternalStorageActivity.java
    private void writeFile(Context context, boolean isPersistent) {
        try {
            //File dir = context.getDir("saveData", Context.MODE_PRIVATE);
            File outputFile = new File(context.getApplicationContext().getCacheDir(), "appSaveState.data");
            try {
                Log.e("The file path = ", outputFile.getAbsolutePath());
                outputFile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.e("abspath", outputFile.getAbsolutePath());
            FileOutputStream fos = new FileOutputStream(outputFile);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            SavePackage sp = new SavePackage(recipes, pantry, CURRENT_SORT);
            os.writeObject(sp);
            fos.flush();
            fos.close();
            os.flush();
            os.close();
            File inputFile = new File(context.getApplicationContext().getCacheDir(), "appSaveState.data");;
            Log.e("abspath", inputFile.getAbsolutePath());
            Log.e("can read?", String.valueOf(inputFile.canRead()));
            Log.e("exists?", String.valueOf(inputFile.exists()));
            Log.e("space?", String.valueOf(inputFile.getTotalSpace()));
            Log.e("length?", String.valueOf(inputFile.length()));
            Toast.makeText(context, String.format("Write to %s successful", outputFile.getAbsolutePath()), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            File inputFile = new File(context.getApplicationContext().getCacheDir(), "appSaveState.data");;
            Log.e("abspath", inputFile.getAbsolutePath());
            Log.e("can read?", String.valueOf(inputFile.canRead()));
            Log.e("exists?", String.valueOf(inputFile.exists()));
            Log.e("space?", String.valueOf(inputFile.getTotalSpace()));
            Log.e("length?", String.valueOf(inputFile.length()));
            e.printStackTrace();
            Toast.makeText(context, String.format("Write to file %s failed", fileName), Toast.LENGTH_SHORT).show();
        }

    }

    private void readFile(Context context, boolean isPersistent) {
        try {
            //File dir = context.getDir("saveData", Context.MODE_PRIVATE);
            File inputFile = new File(context.getApplicationContext().getCacheDir(), "appSaveState.data");;
            Log.e("abspath", inputFile.getAbsolutePath());
            Log.e("can read?", String.valueOf(inputFile.canRead()));
            Log.e("exists?", String.valueOf(inputFile.exists()));
            Log.e("space?", String.valueOf(inputFile.getTotalSpace()));
            Log.e("length?", String.valueOf(inputFile.length()));

            //Toast.makeText(context, String.format("inputFile", inputFile.getAbsolutePath()), Toast.LENGTH_SHORT).show();
            ObjectInputStream is = new ObjectInputStream(new FileInputStream(inputFile));
            SavePackage sp = (SavePackage) is.readObject();

            is.close();
            recipes = sp.recipes1;
            pantry = sp.pantry1;
            if(sp.Current_Sort != null) CURRENT_SORT = Recipe.RecipeType.valueOf(sp.Current_Sort);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, String.format("Read from file %s failed", fileName), Toast.LENGTH_SHORT).show();

        }
    }

    private void deleteFile(Context context, boolean isPersistent) {
        File file;
        if (isPersistent) {
            file = new File(context.getFilesDir(), fileName);
        } else {
            file = new File(context.getCacheDir(), fileName);
        }
        if (file.exists()) {
            file.delete();
            Toast.makeText(context, String.format("File %s has been deleted", fileName), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, String.format("File %s doesn't exist", fileName), Toast.LENGTH_SHORT).show();
        }
    }

    public static abstract class SortBar{
        View sortBar;
        Context context;
        TextView sort_rt_alcohol;
        TextView sort_rt_desert;
        TextView sort_rt_drink;
        TextView sort_rt_entree;
        TextView sort_rt_side;

        public SortBar(View view, Context context){
            sortBar = view;
            this.context = context;
            sort_rt_alcohol = sortBar.findViewById(R.id.sortby_beer);
            sort_rt_alcohol.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clicked(Recipe.RecipeType.AlcoholicDrink);;
                }
            });
            sort_rt_desert = sortBar.findViewById(R.id.sortby_desert);
            sort_rt_desert.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clicked(Recipe.RecipeType.Desert);
                }
            });
            sort_rt_drink = sortBar.findViewById(R.id.sortby_beverage);
            sort_rt_drink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clicked(Recipe.RecipeType.Drink);
                }
            });
            sort_rt_entree = sortBar.findViewById(R.id.sortby_entree);
            sort_rt_entree.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clicked(Recipe.RecipeType.Meal);
                }
            });
            sort_rt_side = sortBar.findViewById(R.id.sortby_side);
            sort_rt_side.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clicked(Recipe.RecipeType.Side);
                }
            });
            updateSelectedImage();
        }
        abstract void clicked(Recipe.RecipeType thisClick);
        /*        private void clicked(Recipe.RecipeType thisClick){
                    MainActivity.recipeSortPress(thisClick);
                    adapter = new RecipesAdapter(context, MainActivity.getRecipes(), thislistener);
                    recyclerView.setAdapter(adapter);
                    updateSelectedImage();
                }*/
        public void updateSelectedImage(){
            int selected = context.getResources().getColor(R.color.activeSort);
            int unselected = context.getResources().getColor(R.color.black);
            int listcolor = context.getResources().getColor(R.color.recipeListItem);
            int highlightlistcolor = context.getResources().getColor(R.color.pantryOtherTypes);
            switch(MainActivity.CURRENT_SORT){
                case AlcoholicDrink:
                    sort_rt_alcohol.setTextColor(selected);
                    sort_rt_desert.setTextColor(unselected);
                    sort_rt_drink.setTextColor(unselected);
                    sort_rt_entree.setTextColor(unselected);
                    sort_rt_side.setTextColor(unselected);
                    sort_rt_alcohol.setBackgroundColor(highlightlistcolor);
                    sort_rt_desert.setBackgroundColor(listcolor);
                    sort_rt_drink.setBackgroundColor(listcolor);
                    sort_rt_entree.setBackgroundColor(listcolor);
                    sort_rt_side.setBackgroundColor(listcolor);
                    break;
                case Drink:
                    sort_rt_alcohol.setTextColor(unselected);
                    sort_rt_desert.setTextColor(unselected);
                    sort_rt_drink.setTextColor(selected);
                    sort_rt_entree.setTextColor(unselected);
                    sort_rt_side.setTextColor(unselected);
                    sort_rt_alcohol.setBackgroundColor(listcolor);
                    sort_rt_desert.setBackgroundColor(listcolor);
                    sort_rt_drink.setBackgroundColor(highlightlistcolor);
                    sort_rt_entree.setBackgroundColor(listcolor);
                    sort_rt_side.setBackgroundColor(listcolor);
                    break;
                case Side:
                    sort_rt_alcohol.setTextColor(unselected);
                    sort_rt_desert.setTextColor(unselected);
                    sort_rt_drink.setTextColor(unselected);
                    sort_rt_entree.setTextColor(unselected);
                    sort_rt_side.setTextColor(selected);
                    sort_rt_alcohol.setBackgroundColor(listcolor);
                    sort_rt_desert.setBackgroundColor(listcolor);
                    sort_rt_drink.setBackgroundColor(listcolor);
                    sort_rt_entree.setBackgroundColor(listcolor);
                    sort_rt_side.setBackgroundColor(highlightlistcolor);
                    break;
                case Desert:
                    sort_rt_alcohol.setTextColor(unselected);
                    sort_rt_desert.setTextColor(selected);
                    sort_rt_drink.setTextColor(unselected);
                    sort_rt_entree.setTextColor(unselected);
                    sort_rt_side.setTextColor(unselected);
                    sort_rt_alcohol.setBackgroundColor(listcolor);
                    sort_rt_desert.setBackgroundColor(highlightlistcolor);
                    sort_rt_drink.setBackgroundColor(listcolor);
                    sort_rt_entree.setBackgroundColor(listcolor);
                    sort_rt_side.setBackgroundColor(listcolor);
                    break;
                case Meal:
                    sort_rt_alcohol.setTextColor(unselected);
                    sort_rt_desert.setTextColor(unselected);
                    sort_rt_drink.setTextColor(unselected);
                    sort_rt_entree.setTextColor(selected);
                    sort_rt_side.setTextColor(unselected);
                    sort_rt_alcohol.setBackgroundColor(listcolor);
                    sort_rt_desert.setBackgroundColor(listcolor);
                    sort_rt_drink.setBackgroundColor(listcolor);
                    sort_rt_entree.setBackgroundColor(highlightlistcolor);
                    sort_rt_side.setBackgroundColor(listcolor);
                    break;
                case NONE:
                    sort_rt_alcohol.setTextColor(unselected);
                    sort_rt_desert.setTextColor(unselected);
                    sort_rt_drink.setTextColor(unselected);
                    sort_rt_entree.setTextColor(unselected);
                    sort_rt_side.setTextColor(unselected);
                    sort_rt_alcohol.setBackgroundColor(listcolor);
                    sort_rt_desert.setBackgroundColor(listcolor);
                    sort_rt_drink.setBackgroundColor(listcolor);
                    sort_rt_entree.setBackgroundColor(listcolor);
                    sort_rt_side.setBackgroundColor(listcolor);
                    break;
                default:
                    break;
            }
        }
    }

    public static ArrayList<Recipe>  getRecipes(){
        return recipes;
    }
    public static ArrayList<SameNameIngredients>  getPantry(){
        return pantry;
    }
    @Override
    public void onPause() {
        Save();
        super.onPause();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    public static String getPrefferedMeasurementName(String currentName){
        for (UnitConversion uc :
                conversions) {
            if(uc.hasNameMatch(currentName)) {
                //Log.e("Changing", currentName + " TO " + uc.getNames().get(0));
                return uc.getNames().get(0);
            }
        }
        return currentName;
    };
    public static void removeRecipe(int index){
        recipes.remove(index);
        makeSummaryRecipeIngredients();
    }
    public static void recipeSortPress(Recipe.RecipeType pressed){
        if(pressed != CURRENT_SORT){
            CURRENT_SORT = pressed;
        } else {
            CURRENT_SORT = Recipe.RecipeType.NONE;
        }
        sortRecipeData();
    }
    private static void sortRecipeData(){
        ArrayList<Recipe> sortedList = new ArrayList<>();
        ArrayList<Recipe> offType = new ArrayList<>();
        if(MainActivity.CURRENT_SORT == Recipe.RecipeType.NONE) {
            Collections.sort(recipes, new Comparator<Recipe>() {
                @Override
                public int compare(Recipe recipe, Recipe t1) {
                    return recipe.getID() - t1.getID();
                }
            });
            return;
        }
        for (Recipe rec: recipes) {
            if(rec.getRecipeType() == MainActivity.CURRENT_SORT) sortedList.add(rec);
            else offType.add(rec);
        }
        sortedList.addAll(offType);
        recipes.clear();
        recipes.addAll(sortedList);
    }

    private static void setOrderInfiniteIngredients(boolean setTo){
        orderInfiniteIngredientsLast = setTo;
        sortPantryData();
    }
    public static void sortPantryData(){
        if(orderInfiniteIngredientsLast){
            ArrayList<SameNameIngredients> sortedPantry = new ArrayList<>();
            ArrayList<SameNameIngredients> rejects = new ArrayList<>();
            for (int i = 0; i < pantry.size(); i++){
                if(pantry.get(i).hasInfinite()) rejects.add(pantry.get(i));
                else sortedPantry.add(pantry.get(i));
            }
            sortedPantry.addAll(rejects);
            pantry = sortedPantry;
        }
    }
    public static void addRecipe(int index, Recipe rec){
        recipes.add(index, rec);
        makeSummaryRecipeIngredients();
    }
    public static void addRecipe(int index, Recipe rec, int cartQuant){
        recipes.add(index, rec);
        makeSummaryRecipeIngredients();
    }
    public static void addRecipes(int index, ArrayList<Recipe> rec, int cartQuant){
        for (Recipe recipe: rec
             ) {
        recipes.add(index, recipe);
        }
        makeSummaryRecipeIngredients();
    }

    public static String[] getTypes(){
        return types;
    }
    public static String[] getQuantities(){
        return quantities;
    }

    public static Bitmap loadImageFromStorage(String filename)
    {
        try {
            File f= new File(filename);
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            return b;
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

        return null;
    }
    public static String saveToInternalStorage(Bitmap bitmapImage, Context context){
        ContextWrapper cw = new ContextWrapper(context);
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        Date now = new Date();
        File mypath = new File(directory,now.getTime() + ".jpg");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return mypath.toString();
    }

    private static WebView mWebView;
    public static void printAllRecipes(Context context, boolean allRecipes) {
        // Create a WebView object specifically for printing
        WebView webView = new WebView(context);
        webView.setWebViewClient(new WebViewClient() {

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onPageFinished(WebView view, String url) {
                Log.e("doWebPrint", "page finished loading " + url);
                createWebPrintJob(view, context);
                mWebView = null;
            }
        });
        StringBuilder sb = new StringBuilder();
        for (Recipe rec :
                recipes) {
        StringBuilder sb2 = new StringBuilder();
        sb2.append("<h3>Ingredients: </h3>");
        sb2.append(rec.getIngredientsAsStringList().replace("\n", "<BR>")+"<h3>Instructions: </h3>");
        sb2.append(rec.getRecipeInstructions().replace("\n", "<BR>"));
        if(rec.getNutritionSummary() != null) sb2.append("<h6>Nutrition: </h6>" + rec.getNutritionSummary().toString());
        String body = sb2.toString();
        sb.append("<h1>" + rec.getRecipeTitle() + "</h1>");
        sb.append(body);
        }

        // Generate an HTML document on the fly:
        String htmlDocument = "<html><body>" + sb.toString() + "</body></html>";
        webView.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null);

        // Keep a reference to WebView object until you pass the PrintDocumentAdapter
        // to the PrintManager
        mWebView = webView;
    }
    public static void printToPrinter(Context context, String title, String body) {
        // Create a WebView object specifically for printing
        WebView webView = new WebView(context);
        webView.setWebViewClient(new WebViewClient() {

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onPageFinished(WebView view, String url) {
                Log.e("doWebPrint", "page finished loading " + url);
                createWebPrintJob(view, context);
                mWebView = null;
            }
        });

        // Generate an HTML document on the fly:
        String htmlDocument = "<html><body><h1>" + title + "</h1><p>" +
                body + "</p></body></html>";
        webView.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null);

        // Keep a reference to WebView object until you pass the PrintDocumentAdapter
        // to the PrintManager
        mWebView = webView;
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static void createWebPrintJob(WebView webView, Context context) {

        // Get a PrintManager instance
        PrintManager printManager = (PrintManager) context.getSystemService(Context.PRINT_SERVICE);

        String jobName = context.getString(R.string.app_name) + " Document";

        // Get a print adapter instance
        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter(jobName);

        // Create a print job with name and adapter instance
        PrintJob printJob = printManager.print(jobName, printAdapter,
                new PrintAttributes.Builder().build());

        // Save the job object for later status checking
       //printJobs.add(printJob);
    }

}