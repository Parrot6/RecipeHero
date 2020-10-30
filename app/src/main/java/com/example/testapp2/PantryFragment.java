package com.example.testapp2;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class PantryFragment extends Fragment implements PantryAdapter.PantryClickListener, AdapterView.OnItemSelectedListener {

    PantryAdapter adapter;
    PantryCanMakeAdapter canMakeAdapter;
    RecyclerView recyclerView;
    RecyclerView recyclerViewCanMake;
    TextView newIng;
    Spinner quantity;
    Spinner measureType;
    Button addIngred;
    Button canMake;
    Button showAll;
    LinearLayout sortBarHolder;
    Boolean showAllRecipes = false;
    public ArrayList<String> quantitiesArr = new ArrayList<>();
    public ArrayList<String> typesArr = new ArrayList<>();
    private final static double shownRecipesThresholdPercent = 50.0;
    // TODO: Rename and change types of parameters
    private ArrayList<Recipe> recipes;
    private static ArrayList<SameNameIngredients> pantry  = MainActivity.getPantry();
    Context context;

    public PantryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context mcontext) {
        super.onAttach(mcontext);
        context = mcontext;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recipes = MainActivity.getRecipes();
        context = getActivity().getApplicationContext();
        pantry = MainActivity.getPantry();
        quantitiesArr.addAll(Arrays.asList(MainActivity.getQuantities()));
        typesArr.addAll(Arrays.asList(MainActivity.getTypes()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_pantry, container, false);
        recipes = MainActivity.getRecipes();
        MainActivity.sortPantryData();


        recyclerViewCanMake = view.findViewById(R.id.rv_pantry_canMake);
        newIng = view.findViewById(R.id.text_pantryNewIngredient);
        sortBarHolder = view.findViewById(R.id.pantry_canmake_sortbarHolder);
        canMake = view.findViewById(R.id.button_pantry_canMake);
        canMake.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                if(canMake.getText().equals("Back to View Pantry")){
                    recyclerView.setVisibility(View.VISIBLE);
                    recyclerViewCanMake.setVisibility(View.GONE);
                    sortBarHolder.setVisibility(View.GONE);
                    showAll.setText("show all");
                    showAll.setVisibility(View.GONE);
                    canMake.setText("What Can I Make?");
                } else {
                    recyclerView.setVisibility(View.INVISIBLE);
                    sortBarHolder.setVisibility(View.VISIBLE);
                    recyclerViewCanMake.setVisibility(View.VISIBLE);
                    canMakeAdapter = new PantryCanMakeAdapter(context, recipesIcanMake()); //refresh data
                    recyclerViewCanMake.setAdapter(canMakeAdapter);
                    showAll.setVisibility(View.VISIBLE);
                    canMake.setText("Back to View Pantry");
                }
            }
        });

        quantity = view.findViewById(R.id.spinner_pantryQuantity);
        final ArrayAdapter<String> adapterSpinnerQuantity = new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item, quantitiesArr);
        adapterSpinnerQuantity.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        quantity.setAdapter(adapterSpinnerQuantity);
        quantity.setOnItemSelectedListener((AdapterView.OnItemSelectedListener) this);

        measureType = view.findViewById(R.id.spinner_pantryMeasurement);
        ArrayAdapter<String> adapterSpinnerType = new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item, typesArr);
        adapterSpinnerType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        measureType.setAdapter(adapterSpinnerType);
        measureType.setOnItemSelectedListener(this);
        addIngred = view.findViewById(R.id.button_pantryAdd);
        addIngred.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //currentIngredients.add(new Ingredient("Test"));
                if(newIng.getText().toString().length() == 0) return;
                Ingredient ing = new Ingredient(newIng.getText().toString() , Double.parseDouble(quantity.getSelectedItem().toString()), measureType.getSelectedItem().toString());
                if(handleIngredient(ing)){
                   //
                    Log.e("handled", ing.toString());
                    PantryAdapter refreshAdapter = adapter;
                    recyclerView.setAdapter(refreshAdapter);
                } else {
                    Log.e("adding", ing.toString());
                    MainActivity.pantry.add(0, new SameNameIngredients(ing));
                    //adapter.notifyItemInserted(0);
                    PantryAdapter refreshAdapter = adapter.notifyAdded(0);
                    recyclerView.setAdapter(refreshAdapter);
                }
                //.scrollToPosition(currentIngredients.size()-1);
                newIng.setText("");
                quantity.setSelection(0);
                measureType.setSelection(0);
            }
        });
        showAll = view.findViewById(R.id.button_pantry_showAll);
        showAll.setVisibility(View.GONE);
        showAll.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                if(showAll.getText().toString().toLowerCase().equals("show all")) {
                    PantryCanMakeAdapter showAllAdapter = new PantryCanMakeAdapter(context, recipes);
                    showAllRecipes = true;
                    recyclerViewCanMake.setAdapter(showAllAdapter);
                    showAll.setText("undo");
                } else {
                    showAll.setText("show all");
                    showAllRecipes = false;
                    canMakeAdapter = new PantryCanMakeAdapter(context, recipesIcanMake());
                    recyclerViewCanMake.setAdapter(canMakeAdapter);
                }

            }
        });

        //canMakeAdapter = new PantryCanMakeAdapter(context, recipesIcanMake());
        recyclerViewCanMake.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        recyclerViewCanMake.setAdapter(canMakeAdapter);
        recyclerViewCanMake.setVisibility(View.GONE);

        View pantrySortBar = view.findViewById(R.id.pantry_canmake_sortbar);
        MainActivity.SortBar sortBar = new MainActivity.SortBar(pantrySortBar, context) {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            void clicked(Recipe.RecipeType thisClick) {
                MainActivity.recipeSortPress(thisClick);
                if(showAllRecipes) canMakeAdapter = new PantryCanMakeAdapter(context, recipes);
                else canMakeAdapter = new PantryCanMakeAdapter(context, recipesIcanMake());
                recyclerViewCanMake.setAdapter(canMakeAdapter);
                updateSelectedImage();
            }
        };


        adapter = new PantryAdapter(context, this);
        recyclerView = view.findViewById(R.id.rv_pantry_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);
        return view;
    }
    public boolean handleIngredient(Ingredient ing){
            if(ing.getName().equals("")) return false;
                for (int k = 0; k < pantry.size(); k++) {
                    if(pantry.get(k).handleNewIngredient(ing)) return true;
                }
                return false;
    }
    @Override
    public void pantryDeleteIngredient(int layoutPosition) {

    }

    @Override
    public void pantryUpdateIngredient(int parent, int layoutPosition, int increment) {
            SameNameIngredients ing = pantry.get(parent);
            ArrayList<Ingredient> list = ing.getIngredientList();
        list.get(layoutPosition).setQuantity(list.get(layoutPosition).getQuantity()+increment);
        PantryAdapter refreshAdapter = adapter;
        recyclerView.setAdapter(refreshAdapter);
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if(adapterView.getId() == R.id.spinner_pantryQuantity) {
            if(adapterView.getSelectedItem().toString().equals("Other Value")){
                AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
                builder1.setTitle("Quantity");
                builder1.setMessage("Write your custom quantity below");
                builder1.setCancelable(true);
// Set up the input
                final EditText input = new EditText(context);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
                builder1.setView(input);

                builder1.setPositiveButton(
                        "Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String inputText = input.getText().toString();
                                quantitiesArr.add(0, inputText);

                                quantity.setSelection(0);
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
        if(adapterView.getId() == R.id.spinner_pantryMeasurement) {
            if( adapterView.getSelectedItem().toString().equals("Other Value")){
                AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
                builder1.setTitle("Measurement");
                builder1.setMessage("Write your custom measurement below");
                builder1.setCancelable(true);
// Set up the input
                final EditText input = new EditText(context);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_TEXT);
                builder1.setView(input);

                builder1.setPositiveButton(
                        "Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String inputText = input.getText().toString();
                                typesArr.add(0, inputText);

                                measureType.setSelection(0);
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

    public ArrayList<Recipe> recipesIcanMake(){
        ArrayList<Recipe> sortedByStock = new ArrayList<>();
        ArrayList<Recipe> recipesToShow = new ArrayList<>();
        ArrayList<PantryCanMakeAdapter.RecipeStockProfile> percents = new ArrayList<>();
        for(int i = 0; i < recipes.size(); i++) {
            percents.add(new PantryCanMakeAdapter.RecipeStockProfile(recipes.get(i), i));
        }
        for(int y = 0; y < percents.size(); y++){
            sortedByStock.add(recipes.get(percents.get(y).parentArraylistPos));
        }
        for(int i = 0; i < sortedByStock.size(); i++) {
            boolean addToCanMake = true;
            if (percents.get(i).percentStocked >= shownRecipesThresholdPercent) recipesToShow.add(sortedByStock.get(i));
        }
        return recipesToShow;
    }
    public static boolean isIngredientStocked(Ingredient ing){
        for (SameNameIngredients sni: pantry
             ) {
            if(sni.isStocked(ing)) return true;
        }
        return false;
    }
    public static double checkIngredientStock(Ingredient ing){
        for (SameNameIngredients sni: pantry
        ) {
            if(sni.checkNameMatch(ing.getName())) return sni.getStocked(ing);
        }
        return 0;
    }

    public static ArrayList<Ingredient> getRecipeStocked(Recipe thisrec){
        ArrayList<Ingredient> haveStockedSoFar = new ArrayList<>();
        for (Ingredient ing: thisrec.getIngredients()
             ) {
            if(isIngredientStocked(ing)) haveStockedSoFar.add(ing);
        }
        return haveStockedSoFar;
    }
    public static ArrayList<Ingredient> getRecipeUnstocked(Recipe thisrec){
        ArrayList<Ingredient> haveStockedSoFar = new ArrayList<>();
        for (Ingredient ing: thisrec.getIngredients()
        ) {
            if(!isIngredientStocked(ing)) haveStockedSoFar.add(ing);
        }
        return haveStockedSoFar;
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}