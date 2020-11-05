package com.example.RecipeHero;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.renderscript.ScriptIntrinsicBLAS;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import static com.example.RecipeHero.R.*;
import static java.util.Calendar.getInstance;


public class MainActivity extends AppCompatActivity{

    private static final int LOADFROMFILE_RESULT_CODE = 55;
    private static final int IMPORTFROMFILE_RESULT_CODE = 11;
    BottomNavigationView bottomNavigationView;

    static ArrayList<Recipe> recipes = new ArrayList<>();
    static ArrayList<SameNameIngredients> pantry = new ArrayList<>();
    public static int pantryLastNonInfinite = 0;
    Context context;
    //Fragments
    public static final String OTHER_VALUE = "Other Value";
    public static Recipe.RecipeType CURRENT_SORT = Recipe.RecipeType.NONE;
    public static int UniqueID = 0; //global id count
    private static final ArrayList<String> defaultTypes = new ArrayList<>(Arrays.asList("tsp","tbsp","cup","g",OTHER_VALUE,"fl oz","gal","mL","L"));
    private static ArrayList<String> defaultQuantities = new ArrayList<>(Arrays.asList("1","2","3","4",OTHER_VALUE));
    private static ArrayList<String> types = new ArrayList<>(Arrays.asList("tsp","tbsp","cup","g",OTHER_VALUE,"fl oz","gal","mL","L"));
    private static ArrayList<String> quantities = new ArrayList<>(Arrays.asList("1","2","3","4",OTHER_VALUE));
    private static String fileName = "pantryTest25";
    private final static String APP_TITLE = "Recipe Hero";// App Name
    private final static String APP_PNAME = "com.example.RecipeHero";// Package Name
    private final static int DAYS_UNTIL_PROMPT = 3;//Min number of days
    private final static int LAUNCHES_UNTIL_PROMPT = 3;//Min number of launches
    public static boolean orderInfiniteIngredientsLast = true;
    public static ArrayList<UnitConversion> conversions = new ArrayList<>();
    SearchFragment search = new SearchFragment();
    int mainFrame = id.fragmentContainer;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_main);
        context = this;
        rateAppChecker();

        readFile(context);
        if(recipes.size() == 0) {
            try {
                InputStream iis = getAssets().open("baseRecipes");
                ObjectInputStream ois = new ObjectInputStream(iis);
                SavePackage sp = (SavePackage) ois.readObject();
                iis.close();
                ois.close();
                loadInSavePackage(sp);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, String.format("Read from file %s failed", fileName), Toast.LENGTH_SHORT).show();
                initializeExamplesOfData();
            }
            openTutorial();
        }

        initializeConversions();
        updateUniqueID();

        bottomNavigationView = (BottomNavigationView) findViewById(id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Fragment fragment = null;
                        switch (item.getItemId()) {
                            case id.nav_Home:
                                fragment = new ViewRecipesFragment();
                                getSupportFragmentManager().beginTransaction().replace(id.fragmentContainer, fragment, "Recipes").commit();
                                break;
                            case id.nav_shopping_list:
                                fragment = new EditCartFragment();
                                getSupportFragmentManager().beginTransaction().replace(id.fragmentContainer, fragment).commit();
                                break;
                            case id.nav_pantry:
                                fragment = new PantryFragment();
                                getSupportFragmentManager().beginTransaction().replace(id.fragmentContainer, fragment).commit();
                                break;
                            case id.nav_search:
                                fragment = search;
                                getSupportFragmentManager().beginTransaction().replace(id.fragmentContainer, fragment).commit();
                                break;
                    }

                        return true;
                    }
                });
        getSupportFragmentManager().beginTransaction().replace(id.fragmentContainer, new ViewRecipesFragment(), "Recipes").commit();

        Toolbar myToolbar = (Toolbar) findViewById(id.main_toolbar);
        setSupportActionBar(myToolbar);

    }

    private boolean rateAppChecker() {
        SharedPreferences prefs = context.getSharedPreferences("apprater", 0);
        if (prefs.getBoolean("dontshowagain", false)) {
            return true;
        }
        SharedPreferences.Editor editor = prefs.edit();
        // Increment launch counter
        long launch_count = prefs.getLong("launch_count", 0) + 1;
        editor.putLong("launch_count", launch_count);

        // Get date of first launch
        long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong("date_firstlaunch", date_firstLaunch);
        }

        // Wait at least n days before opening
        if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= date_firstLaunch +
                    (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
                showRateDialog(context);
            }
        }
        editor.apply();
        return false;
    }

    private static void showRateDialog(Context context) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View customLayout =  li.inflate(layout.rate_my_app_popup, null);
        alertDialog.setView(customLayout);
        AlertDialog alert = alertDialog.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
        ConstraintLayout cl = customLayout.findViewById(id.constraintLayout_rateUs);
        cl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME)));
                alert.dismiss();
            }
        });
        Button rateus = customLayout.findViewById(id.button_rate_us);
        rateus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME)));
                alert.dismiss();
            }
        });
        Button close = customLayout.findViewById(R.id.button_rate_us_close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert.dismiss();
            }
        });
    }

    private void setValuesToBoxes(ArrayList<EditText> boxes, ArrayList<String> vals){
        for (int i = 0, x = 0; x < boxes.size(); i++) {
            if(i < vals.size()){
                if(!vals.get(i).equals(OTHER_VALUE)) {
                    boxes.get(x).setText(vals.get(i));
                    x++;
                }
            } else {
                boxes.get(x).setText("");
                x++;
            }
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case id.printAll: {
                printAllRecipes(context, true);
                break;
            }
            case id.contact_me:{
                Intent email = new Intent(Intent.ACTION_SEND);
                email.putExtra(Intent.EXTRA_EMAIL, new String[]{"RecipeHeroApp@gmail.com"});
                email.putExtra(Intent.EXTRA_SUBJECT, "Bug/Suggestion");
                email.putExtra(Intent.EXTRA_TEXT, "Whats wrong: \n\n\n Suggestions: \n");

                //need this to prompts email client only
                email.setType("message/rfc822");

                startActivity(Intent.createChooser(email, "Choose an Email client :"));
                break;
            }
            case id.donate_please:{
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                final View customLayout = getLayoutInflater().inflate(layout.donate, null);
                alertDialog.setView(customLayout);
                AlertDialog alert = alertDialog.create();
                alert.setCanceledOnTouchOutside(false);
                alert.show();
                Button close = customLayout.findViewById(id.button_donate_close);
                close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alert.cancel();
                    }
                });
                break;
            }
            case id.manage_recipes:{
                ViewRecipesFragment myFragment = (ViewRecipesFragment)getSupportFragmentManager().findFragmentByTag("Recipes");
                if (myFragment != null && myFragment.isVisible()) {
                    // add your code here
                } else Toast.makeText(context, "Try that on the Recipes page!", Toast.LENGTH_SHORT).show();
                return false;
            }
            case id.change_defaults:{
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                final View customLayout = getLayoutInflater().inflate(layout.default_selectors, null);
                alertDialog.setView(customLayout);
                AlertDialog alert = alertDialog.create();
                alert.setCanceledOnTouchOutside(false);
                ArrayList<EditText> quants = new ArrayList<>();
                EditText q1 = customLayout.findViewById(id.deafultQuantities);
                quants.add(q1);
                EditText q2 = customLayout.findViewById(id.deafultQuantities2);
                quants.add(q2);
                EditText q3 = customLayout.findViewById(id.deafultQuantities3);
                quants.add(q3);
                EditText q4 = customLayout.findViewById(id.deafultQuantities4);
                quants.add(q4);
                EditText q5 = customLayout.findViewById(id.deafultQuantities5);
                quants.add(q5);
                EditText q6 = customLayout.findViewById(id.deafultQuantities6);
                quants.add(q6);
                EditText q7 = customLayout.findViewById(id.deafultQuantities7);
                quants.add(q7);
                EditText q8 = customLayout.findViewById(id.deafultQuantities8);
                quants.add(q8);
                ArrayList<EditText> typesBoxes = new ArrayList<>();
                final EditText t1 = customLayout.findViewById(id.defaultTypes);
               typesBoxes.add(t1);
                EditText t2 = customLayout.findViewById(id.defaultTypes2);
               typesBoxes.add(t2);
                EditText t3 = customLayout.findViewById(id.defaultTypes3);
               typesBoxes.add(t3);
                EditText t4 = customLayout.findViewById(id.defaultTypes4);
               typesBoxes.add(t4);
                EditText t5 = customLayout.findViewById(id.defaultTypes5);
               typesBoxes.add(t5);
                EditText t6 = customLayout.findViewById(id.defaultTypes6);
               typesBoxes.add(t6);
                EditText t7 = customLayout.findViewById(id.defaultTypes7);
               typesBoxes.add(t7);
                EditText t8 = customLayout.findViewById(id.defaultTypes8);
               typesBoxes.add(t8);
               t1.setText("adawd");
               setValuesToBoxes(typesBoxes, types);
               setValuesToBoxes(quants, quantities);

                Button resetTypes = customLayout.findViewById(id.button_selectors_types);
                resetTypes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        types = defaultTypes;
                        setValuesToBoxes(typesBoxes, types);
                    }
                });
                Button resetQuants = customLayout.findViewById(id.button_selectors_quantities);
                resetQuants.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        quantities = defaultQuantities;
                        setValuesToBoxes(quants, quantities);
                    }
                });
                Button close = customLayout.findViewById(id.button_default_selectors_cancel);
                close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alert.cancel();
                    }
                });
                Button confirm = customLayout.findViewById(id.button_default_selectors_confirm);
                confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ArrayList<String> newQuants = new ArrayList<>();
                        for(int i = 0; i < quants.size(); i++){
                            if(!quants.get(i).getText().toString().equals("")) newQuants.add(quants.get(i).getText().toString());
                        }
                        quantities = newQuants;
                        quantities.add(quantities.size()/2, OTHER_VALUE);

                        ArrayList<String> newTypes = new ArrayList<>();
                        for(int i = 0; i < typesBoxes.size(); i++){
                            if(!typesBoxes.get(i).getText().toString().equals("")) newTypes.add(typesBoxes.get(i).getText().toString());
                        }
                        types = newTypes;
                        types.add(types.size()/2, OTHER_VALUE);

                        alert.cancel();
                    }
                });
                alert.show();
                break;
            }
            case id.save_load:{
                //load_save_screen.xml
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                final View customLayout = getLayoutInflater().inflate(layout.load_save_screen, null);
                alertDialog.setView(customLayout);
                AlertDialog alert = alertDialog.create();
                alert.setCanceledOnTouchOutside(false);
                alert.show();
                Button save = customLayout.findViewById(id.button_save);
                save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        writeFileExternallyDialog();
                        alert.cancel();
                    }
                });
                Button share = customLayout.findViewById(id.button_share_recipes);
                share.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        shareFileDialog();
                        alert.cancel();
                    }
                });
                Button shareCsv = customLayout.findViewById(R.id.button_share_csv);
                shareCsv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        shareCSV(printRecipesToCSV(context));
                        alert.cancel();
                    }
                });
                Button load = customLayout.findViewById(id.button_load);
                load.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        loadFileExternallyDialog();
                        alert.cancel();
                    }
                });
                Button importBut = customLayout.findViewById(id.button_import);
                importBut.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        importFileDialog();
                        alert.cancel();
                    }
                });
                Button close = customLayout.findViewById(id.button_load_save_close);
                close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alert.cancel();
                    }
                });
                break;
            }
            case id.tutorial:{
                openTutorial();
            }
            default:{
                  break;
            }

            // case blocks for other MenuItems (if any)
        }
        return true;
    }
    private void openTutorial(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        final View customLayout = getLayoutInflater().inflate(layout.tutorial, null);
        alertDialog.setView(customLayout);
        AlertDialog alert = alertDialog.create();
        alert.setCanceledOnTouchOutside(true);
        Button close = customLayout.findViewById(R.id.button_close_tutorial);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert.cancel();
            }
        });
        alert.show();
    }
    private void shareFile(File file) {
        Uri uri = FileProvider.getUriForFile(this, "com.example.RecipeHero.fileprovider", file);
        Intent intent = ShareCompat.IntentBuilder.from(this)
                .setType("application/octet-stream")
                .setText("Here is my Recipe Hero cookbook! Import them to your cookbook and check them out!")
                .setStream(uri)
                .setChooserTitle("Choose bar")
                .createChooserIntent()
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(intent);

    }
    private void shareCSV(File file) {
        Uri uri = FileProvider.getUriForFile(this, "com.example.RecipeHero.fileprovider", file);
        Intent intent = ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText("Here is my Recipe Hero cookbook CSV")
                .setStream(uri)
                .setChooserTitle("Choose bar")
                .createChooserIntent()
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(intent);

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
        addRecipe(0, new Recipe("Alfredo", ingredients1), context);
        addRecipe(0, new Recipe("Pulled Pork", ingredients2), context);
        addRecipe(0, new Recipe("Sweet and Sour Chicken", ingredients3), context);
        addRecipe(0, new Recipe("Spaghetti", spag), context);
        addRecipe(0, new Recipe("Green Beans", gb), context);
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
        public ArrayList<String> types1 = new ArrayList<>();
        public ArrayList<String> quantities1 = new ArrayList<>();
        public SavePackage(ArrayList<Recipe> rec, ArrayList<SameNameIngredients> pant, Recipe.RecipeType current_Sort,ArrayList<String> types1, ArrayList<String> quantities1){
            recipes1.addAll(rec);
            pantry1.addAll(pant);
            Current_Sort = current_Sort.toString();
            this.types1 = types1;
            this.quantities1 = quantities1;
        }

    }
    public static void Save(Context context){
        //createFile(context, true);
        writeFile(context, fileName);
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
    private static void writeFile(Context context, String fileName) {
        try {
            File outputFile = new File(context.getApplicationContext().getCacheDir(), fileName);
            try {
                outputFile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
            FileOutputStream fos = new FileOutputStream(outputFile);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            SavePackage sp = new SavePackage(recipes, pantry, CURRENT_SORT, types, quantities);
            os.writeObject(sp);
            fos.flush();
            fos.close();
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, String.format("Write to file %s failed", fileName), Toast.LENGTH_SHORT).show();
        }

    }
    private void writeFile(SavePackage sp, String filename) {
        try {
            //File dir = context.getDir("saveData", Context.MODE_PRIVATE);
            File outputFile = new File(context.getApplicationContext().getCacheDir(), filename);
            try {
                outputFile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
            FileOutputStream fos = new FileOutputStream(outputFile);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(sp);
            fos.flush();
            fos.close();
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, String.format("Write to file %s failed", filename), Toast.LENGTH_SHORT).show();
        }

    }
    private File writeFileExternally(SavePackage sp, String filename) {
        File outputFile = null;
        try {
            //File dir = context.getDir("saveData", Context.MODE_PRIVATE);
            outputFile = new File(context.getApplicationContext().getExternalFilesDir(null), filename);
            try {
                outputFile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
            FileOutputStream fos = new FileOutputStream(outputFile);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(sp);
            fos.flush();
            fos.close();
            os.flush();
            os.close();
            return outputFile;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, String.format("Write to file %s failed", filename), Toast.LENGTH_SHORT).show();
        }
        return outputFile;
    }
    private void writeCurrentFileExternally(Context context, String fileName) {
        try {
            File outputFile = new File(context.getApplicationContext().getExternalFilesDir(null), fileName);
            try {
                outputFile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
            FileOutputStream fos = new FileOutputStream(outputFile);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            SavePackage sp = new SavePackage(recipes, pantry, CURRENT_SORT, types, quantities);
            os.writeObject(sp);
            fos.flush();
            fos.close();
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private void shareFileDialog(){
        try {
            final String[] name = {""};
            //File dir = context.getDir("saveData", Context.MODE_PRIVATE);
            AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
            builder1.setTitle("Share a File!");
            builder1.setMessage("Choose your file name! Will be written to: \n" + context.getApplicationContext().getExternalFilesDir(null) + "/Your_Filename\nThen will ask you how you want to share it!");
            builder1.setCancelable(true);
            // Set up the input
            final EditText input = new EditText(MainActivity.this);
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setGravity(Gravity.CENTER);
            input.setBackground(ContextCompat.getDrawable(MainActivity.this, color.recipeListbg));
            input.setText(String.format("Share-%s", UniqueID));
            builder1.setView(input);
            builder1.setPositiveButton(
                    "Confirm",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            try {
                                name[0] = input.getText().toString();
                                File f = writeFileExternally(new SavePackage(recipes, pantry, CURRENT_SORT, types, quantities), name[0]);
                                shareFile(f);
                            } catch (Exception e) {
                                //File inputFile = new File(context.getApplicationContext().getCacheDir(), "appSaveState.data");;
                                Toast.makeText(context, "Failed to share",Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                            dialog.cancel();
                        }
                    });
            builder1.setNegativeButton(
                    "Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert11 = builder1.create();
            alert11.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void writeFileExternallyDialog(){
        try {
            final String[] name = {""};
            //File dir = context.getDir("saveData", Context.MODE_PRIVATE);
            AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
            builder1.setTitle("Save File Backup Externally");
            builder1.setMessage("Choose your file name! Will be written to: \n" + context.getApplicationContext().getExternalFilesDir(null) + "/Your_Filename");
            builder1.setCancelable(true);
            // Set up the input
            final EditText input = new EditText(MainActivity.this);
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setGravity(Gravity.CENTER);
            input.setBackground(ContextCompat.getDrawable(MainActivity.this, color.recipeListbg));
            input.setText(String.format("RH-Backup-%s", UniqueID));
            builder1.setView(input);
            builder1.setPositiveButton(
                    "Confirm",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            try {
                                name[0] = input.getText().toString();
                                writeCurrentFileExternally(context, name[0]);
                            } catch (Exception e) {
                                //File inputFile = new File(context.getApplicationContext().getCacheDir(), "appSaveState.data");;
                                Toast.makeText(context, "Failed to save externally",Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                                }
                            dialog.cancel();
                            }
                        });
            builder1.setNegativeButton(
                    "Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert11 = builder1.create();
            alert11.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void readFile(Context context) {
        try {
            File inputFile = new File(context.getApplicationContext().getCacheDir(), fileName);
            ObjectInputStream is = new ObjectInputStream(new FileInputStream(inputFile));
            SavePackage sp = (SavePackage) is.readObject();

            is.close();
            loadInSavePackage(sp);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, String.format("Read from file %s failed", fileName), Toast.LENGTH_SHORT).show();

        }
    }
    private void loadFileExternallyDialog() {
        String path = context.getApplicationContext().getExternalFilesDir(null).toString();

        try {
            final String[] name = {""};
            //File dir = context.getDir("saveData", Context.MODE_PRIVATE);
            AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
            builder1.setTitle("Load File From External Storage");
            builder1.setMessage("choose your file to Load from, this will wipe everything including settings, and replace with chosen save data");
            builder1.setCancelable(true);
            // Set up the input
            final Button directUrl = new Button(MainActivity.this);
            directUrl.setGravity(Gravity.CENTER);
            directUrl.setBackground(ContextCompat.getDrawable(MainActivity.this, drawable.rounded_corner));
            directUrl.setText("Choose a File");
            //directUrl.setWidth(0);
            LinearLayout.LayoutParams p = new
                    LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            directUrl.setLayoutParams(p);
            LinearLayout ll = new LinearLayout(context);
            ll.addView(directUrl);
            builder1.setView(ll);
            builder1.setNegativeButton(
                    "Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert11 = builder1.create();
            directUrl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                    chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
                    chooseFile.setType("*/*");
                    startActivityForResult(
                            Intent.createChooser(chooseFile, "Choose a file"),
                            LOADFROMFILE_RESULT_CODE
                    );
                    alert11.cancel();
                }
            });
            alert11.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void loadDataFromExternalFile(String filename, String directory) throws IOException, ClassNotFoundException {
        if(directory == null) directory = "";
        File inputFile = new File(context.getApplicationContext().getExternalFilesDir(null)+directory, filename);
        //Toast.makeText(context, String.format("inputFile", inputFile.getAbsolutePath()), Toast.LENGTH_SHORT).show();
        ObjectInputStream is = new ObjectInputStream(new FileInputStream(inputFile));
        SavePackage sp = (SavePackage) is.readObject();
        is.close();
        loadInSavePackage(sp);
        getSupportFragmentManager().beginTransaction().replace(mainFrame, new ViewRecipesFragment(), "Recipes").commit();
    }

    private void importFileDialog() {
        String path = context.getApplicationContext().getExternalFilesDir(null).toString();
        try {
            final String[] name = {""};
            //File dir = context.getDir("saveData", Context.MODE_PRIVATE);
            AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
            builder1.setTitle("Import Recipes From External Storage");
            builder1.setMessage("This will ADD the recipes from the chosen file but not change anything else");
            builder1.setCancelable(true);
            // Set up the input
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            final Button directUrl = new Button(MainActivity.this);
            directUrl.setGravity(Gravity.CENTER);
            directUrl.setBackground(ContextCompat.getDrawable(MainActivity.this, drawable.rounded_corner));
            directUrl.setText("Choose a File");
            LinearLayout.LayoutParams p = new
                    LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            directUrl.setLayoutParams(p);
            LinearLayout ll = new LinearLayout(context);
            ll.addView(directUrl);
            builder1.setView(ll);
            builder1.setNegativeButton(
                    "Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert11 = builder1.create();
            directUrl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                    chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
                    chooseFile.setType("*/*");
                    startActivityForResult(
                            Intent.createChooser(chooseFile, "Choose a file"),
                            IMPORTFROMFILE_RESULT_CODE
                    );
                    alert11.cancel();
                    Toast.makeText(context, "SUCCESSFULLY IMPORTED RECIPES", Toast.LENGTH_SHORT).show();
                }
            });
            alert11.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOADFROMFILE_RESULT_CODE && resultCode == Activity.RESULT_OK){
            DateFormat df = new SimpleDateFormat("dd-MM-yy@hh:mm");
            Date dateobj = new Date();
            Uri uri = data.getData();
            writeCurrentFileExternally(context,"Backup"+df.format(dateobj));
            SavePackage sp = importFromURI(uri);
            loadInSavePackage(sp);
            Toast.makeText(context, "Backup"+df.format(dateobj) + " created if need to undo later", Toast.LENGTH_LONG).show();
            //writeFileExternally(sp, "LoadedPackage"+df.format(dateobj));
        }
        if (requestCode == IMPORTFROMFILE_RESULT_CODE && resultCode == Activity.RESULT_OK){
            DateFormat df = new SimpleDateFormat("dd-MM-yy");
            Date dateobj = new Date();
            Uri uri = data.getData();
            //writeCurrentFileExternally(context, "BackupBeforeNewImport"+df.format(dateobj));
            SavePackage sp = importFromURI(uri);
            importInSavePackage(sp);
            //writeFileExternally(sp, "ImportedPackage"+df.format(dateobj));
        }
    }
    private SavePackage importFromURI(Uri uri){
        SavePackage sp = null;
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            ObjectInputStream ois = new ObjectInputStream(is);
            sp = (SavePackage) ois.readObject();
            is.close();
            ois.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return sp;
    };
    private void loadInSavePackage(SavePackage sp) {
        recipes = sp.recipes1;
        pantry = sp.pantry1;
        if (sp.Current_Sort != null) CURRENT_SORT = Recipe.RecipeType.valueOf(sp.Current_Sort);
        types = sp.types1;
        quantities = sp.quantities1;
        Save(context);
    }
    private void importInSavePackage(SavePackage sp) {
        addRecipes(recipes.size(), sp.recipes1, context);
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
            sort_rt_alcohol = sortBar.findViewById(id.sortby_beer);
            sort_rt_alcohol.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clicked(Recipe.RecipeType.AlcoholicDrink);;
                }
            });
            sort_rt_desert = sortBar.findViewById(id.sortby_desert);
            sort_rt_desert.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clicked(Recipe.RecipeType.Desert);
                }
            });
            sort_rt_drink = sortBar.findViewById(id.sortby_beverage);
            sort_rt_drink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clicked(Recipe.RecipeType.Drink);
                }
            });
            sort_rt_entree = sortBar.findViewById(id.sortby_entree);
            sort_rt_entree.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clicked(Recipe.RecipeType.Meal);
                }
            });
            sort_rt_side = sortBar.findViewById(id.sortby_side);
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
            int selected = context.getResources().getColor(color.activeSort);
            int unselected = context.getResources().getColor(color.black);
            Drawable listcolor = ResourcesCompat.getDrawable(context.getResources(), drawable.rounded_corner_unselected, null);
            Drawable highlightlistcolor = ResourcesCompat.getDrawable(context.getResources(), drawable.rounded_corner, null);
            switch(MainActivity.CURRENT_SORT){
                case AlcoholicDrink:
                    sort_rt_alcohol.setTextColor(selected);
                    sort_rt_desert.setTextColor(unselected);
                    sort_rt_drink.setTextColor(unselected);
                    sort_rt_entree.setTextColor(unselected);
                    sort_rt_side.setTextColor(unselected);
                    sort_rt_alcohol.setBackground(highlightlistcolor);
                    sort_rt_desert.setBackground(listcolor);
                    sort_rt_drink.setBackground(listcolor);
                    sort_rt_entree.setBackground(listcolor);
                    sort_rt_side.setBackground(listcolor);
                    break;
                case Drink:
                    sort_rt_alcohol.setTextColor(unselected);
                    sort_rt_desert.setTextColor(unselected);
                    sort_rt_drink.setTextColor(selected);
                    sort_rt_entree.setTextColor(unselected);
                    sort_rt_side.setTextColor(unselected);
                    sort_rt_alcohol.setBackground(listcolor);
                    sort_rt_desert.setBackground(listcolor);
                    sort_rt_drink.setBackground(highlightlistcolor);
                    sort_rt_entree.setBackground(listcolor);
                    sort_rt_side.setBackground(listcolor);
                    break;
                case Side:
                    sort_rt_alcohol.setTextColor(unselected);
                    sort_rt_desert.setTextColor(unselected);
                    sort_rt_drink.setTextColor(unselected);
                    sort_rt_entree.setTextColor(unselected);
                    sort_rt_side.setTextColor(selected);
                    sort_rt_alcohol.setBackground(listcolor);
                    sort_rt_desert.setBackground(listcolor);
                    sort_rt_drink.setBackground(listcolor);
                    sort_rt_entree.setBackground(listcolor);
                    sort_rt_side.setBackground(highlightlistcolor);
                    break;
                case Desert:
                    sort_rt_alcohol.setTextColor(unselected);
                    sort_rt_desert.setTextColor(selected);
                    sort_rt_drink.setTextColor(unselected);
                    sort_rt_entree.setTextColor(unselected);
                    sort_rt_side.setTextColor(unselected);
                    sort_rt_alcohol.setBackground(listcolor);
                    sort_rt_desert.setBackground(highlightlistcolor);
                    sort_rt_drink.setBackground(listcolor);
                    sort_rt_entree.setBackground(listcolor);
                    sort_rt_side.setBackground(listcolor);
                    break;
                case Meal:
                    sort_rt_alcohol.setTextColor(unselected);
                    sort_rt_desert.setTextColor(unselected);
                    sort_rt_drink.setTextColor(unselected);
                    sort_rt_entree.setTextColor(selected);
                    sort_rt_side.setTextColor(unselected);
                    sort_rt_alcohol.setBackground(listcolor);
                    sort_rt_desert.setBackground(listcolor);
                    sort_rt_drink.setBackground(listcolor);
                    sort_rt_entree.setBackground(highlightlistcolor);
                    sort_rt_side.setBackground(listcolor);
                    break;
                case NONE:
                    sort_rt_alcohol.setTextColor(unselected);
                    sort_rt_desert.setTextColor(unselected);
                    sort_rt_drink.setTextColor(unselected);
                    sort_rt_entree.setTextColor(unselected);
                    sort_rt_side.setTextColor(unselected);
                    sort_rt_alcohol.setBackground(listcolor);
                    sort_rt_desert.setBackground(listcolor);
                    sort_rt_drink.setBackground(listcolor);
                    sort_rt_entree.setBackground(listcolor);
                    sort_rt_side.setBackground(listcolor);
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
        Save(context);
        super.onPause();
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
            pantryLastNonInfinite = sortedPantry.size() - 1;
            sortedPantry.addAll(rejects);
            pantry = sortedPantry;
        }
    }
    public static void addRecipe(int index, Recipe rec, Context context){
        recipes.add(index, rec);
        makeSummaryRecipeIngredients();
        Save(context);
        //if(recipes.size() == 25 || recipes.size() == 75 || recipes.size() == 150) showRateDialog(context);
    }
    public static void addRecipes(int index, ArrayList<Recipe> rec, Context context){
        int i = index;
        for (Recipe recipe: rec
             ) {
        recipes.add(i, recipe);
        i++;
        }
        makeSummaryRecipeIngredients();
        Save(context);
    }

    public static ArrayList<String> getTypes(){
        return types;
    }
    public static ArrayList<String> getQuantities(){
        return quantities;
    }

    static WebView mWebView;
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
    public static File printRecipesToCSV(Context context){
        File file = null;
        StringBuilder myCSV = new StringBuilder();
        ArrayList<ArrayList<String>> csvLines = new ArrayList<>();
        ArrayList<String> header = new ArrayList<>();
        header.addAll(Arrays.asList("Name", "Ingredients", "Instructions","Nutrition", "Rating", "Source"));
        csvLines.add(header);
        for (Recipe rec :
                recipes) {
            ArrayList<String> lineBuilder = new ArrayList<>();
            StringBuilder sb2 = new StringBuilder();
            lineBuilder.add(rec.getRecipeTitle());
            lineBuilder.add(rec.getIngredientsAsStringList().replace("\n",";").replace(",","-"));
            lineBuilder.add(rec.getRecipeInstructions().replace("\n", ";").replace(",","-"));
            if(rec.getNutritionSummary() != null) lineBuilder.add(rec.getNutritionSummary().toString().replace("\n",";").replace(",","-"));
            else lineBuilder.add("");
            lineBuilder.add(String.valueOf(rec.getRating()));
            lineBuilder.add(rec.getSourceUrl());
            csvLines.add(lineBuilder);
        }
        for (ArrayList<String> line :
                csvLines) {
            for (String str :
                    line) {
                myCSV.append(str);
                myCSV.append(", ");
            }
            myCSV.append("\n");
        }
        FileOutputStream outputStream = null;
        File newCSV = new File(context.getApplicationContext().getExternalFilesDir(null), "RH-" + UniqueID + "-CSV.txt");
        try {
            outputStream = new FileOutputStream(newCSV);
            byte[] strToBytes = myCSV.toString().getBytes();
            outputStream.write(strToBytes);
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newCSV;
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

        String jobName = context.getString(string.app_name) + " Document";

        // Get a print adapter instance
        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter(jobName);

        // Create a print job with name and adapter instance
        PrintJob printJob = printManager.print(jobName, printAdapter,
                new PrintAttributes.Builder().build());

        // Save the job object for later status checking
       //printJobs.add(printJob);
    }

}