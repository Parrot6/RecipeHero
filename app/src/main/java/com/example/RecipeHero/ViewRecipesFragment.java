package com.example.RecipeHero;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.session.MediaController;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import static android.content.Context.INPUT_METHOD_SERVICE;


public class ViewRecipesFragment extends Fragment implements RecipesAdapter.MyClickListener, RecipeManagingAdapter.ManagingClickListener  {


    //private ArrayList<Recipe> recipes;
    private static int REQUEST_CODE = 50;
    private RecipesAdapter adapter;
    RecipeManagingAdapter managingAdapter;
    private RecyclerView recyclerView;
    private RecipesAdapter.MyClickListener thislistener;
    private RecipeManagingAdapter.ManagingClickListener managingListener;
    private Context context;
    boolean isBeingManaged = false;
    EditText tv;
    ImageView clearText;
    public ViewRecipesFragment() {
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
            context = getActivity().getApplicationContext();
            thislistener = this;
            managingListener = this;
        setHasOptionsMenu(true);
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void switchToManageMode(){
        if(isBeingManaged){
            isBeingManaged = false;
            recyclerView.setAdapter(adapter);
            adapter.searchRecipes(tv.getText().toString());
        } else {
            isBeingManaged = true;
            managingAdapter = new RecipeManagingAdapter(context, MainActivity.getRecipes(), this);
            recyclerView.setAdapter(managingAdapter);
            managingAdapter.searchRecipes(tv.getText().toString());
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_recipes, container, false);

        adapter = new RecipesAdapter(context, MainActivity.getRecipes(), this);
        recyclerView = view.findViewById(R.id.mainList);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        //adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
        Button addRecipe = view.findViewById(R.id.button_addRecipe);
        addRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addRecipe(view);
            }
        });
        View sortBarView = view.findViewById(R.id.view_recipes_sortby);
        clearText = view.findViewById(R.id.button_fragmentrecipes_cleartext);
        clearText.setVisibility(View.GONE);
        clearText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv.setText("");
                clearText.setVisibility(View.GONE);
            }
        });
        tv = (EditText) view.findViewById(R.id.EditText_RecipeName);
        tv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(tv.getText().toString().equals("")) clearText.setVisibility(View.GONE);
                else clearText.setVisibility(View.VISIBLE);
                if(isBeingManaged) managingAdapter.searchRecipes(charSequence.toString());
                else adapter.searchRecipes(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        MainActivity.SortBar sortBar = new MainActivity.SortBar(sortBarView, context) {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            void clicked(Recipe.RecipeType thisClick) {
                MainActivity.recipeSortPress(thisClick);
                if(isBeingManaged){
                    managingAdapter = new RecipeManagingAdapter(context, MainActivity.getRecipes(), managingListener);
                    recyclerView.setAdapter(managingAdapter);
                    managingAdapter.searchRecipes(tv.getText().toString());
                } else {
                    adapter = new RecipesAdapter(context, MainActivity.getRecipes(), thislistener);
                    recyclerView.setAdapter(adapter);
                    adapter.searchRecipes(tv.getText().toString());
                }
                updateSelectedImage();
            }
        };
        return view;
    }

   @RequiresApi(api = Build.VERSION_CODES.N)
   @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.manage_recipes:{
                switchToManageMode();
            }
            default:{
                break;
            }

            // case blocks for other MenuItems (if any)
        }
        return true;
    }
    @Override
    public void onView(int recID, int layoutPosition) {
        Intent intent = new Intent(getActivity(), DisplayRecipe.class);
        Recipe recToSend = MainActivity.getRecipeByID(recID);
        intent.putExtra("Recipe", recToSend);
        intent.putExtra("layoutPosition", layoutPosition);
        startActivityForResult(intent, REQUEST_CODE);
    }

    public void addRecipe(View view) {
        String temp = tv.getText().toString();
        if(temp.equals("")) temp = "NewRecipeName";
        Recipe rec = new Recipe(temp);
        MainActivity.addRecipe(0, rec, context);
        //SaveRecipes(recipes);
        tv.setText("");
        adapter.notifyItemInserted(0);
        try {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            // TODO: handle exception
        }
        recyclerView.scrollToPosition(0);
        Intent newIntent = new Intent(context, EditRecipe.class);
        newIntent.putExtra("Recipe", rec);
        newIntent.putExtra("layoutPosition", 0);
        startActivityForResult(newIntent, REQUEST_CODE);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Recipe passedItem = (Recipe) data.getExtras().get("passed_item");
            String action = (String) data.getExtras().get("Action");
            int index = (int) data.getExtras().get("layoutPosition");
            switch(action){
                case "Save":
                    MainActivity.removeRecipeById(passedItem.getID());
                    MainActivity.addRecipe(index, passedItem, context);
                    recyclerView.scrollToPosition(index);
                    //recyclerView.notifyAll();
                    break;
                case "Delete":
                    MainActivity.removeRecipeById(passedItem.getID());
                    adapter.notifyDataSetChanged();
                    recyclerView.scrollToPosition(0);
                    break;
                default:
                    break;
            }


        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onResume(){
        super.onResume();
        adapter = new RecipesAdapter(context, MainActivity.getRecipes(), this);
        adapter.searchRecipes(tv.getText().toString());
        recyclerView.setAdapter(adapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onDelete(int recipeId, int layoutPosition) {
        MainActivity.removeRecipeById(recipeId);
        managingAdapter.searchRecipes(tv.getText().toString());
        //managingAdapter.notifyDataSetChanged();
    }
}