package com.example.RecipeHero;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import static android.content.Context.INPUT_METHOD_SERVICE;


public class ViewRecipesFragment extends Fragment implements RecipesAdapter.MyClickListener, RecipeManagingAdapter.ManagingClickListener  {


    //private ArrayList<Recipe> recipes;
    private static int REQUEST_CODE = 50;
    private RecipesAdapter adapter;
    RecipeManagingAdapter managingAdapter;
    private RecyclerView recyclerView;
    private RecipesAdapter.MyClickListener thislistener;
    private Context context;
    boolean isBeingManaged = false;
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
        setHasOptionsMenu(true);
    }
    public void switchToManageMode(){
        if(isBeingManaged){
            isBeingManaged = false;
            recyclerView.setAdapter(adapter);
        } else {
            isBeingManaged = true;
            managingAdapter = new RecipeManagingAdapter(context, MainActivity.getRecipes(), this);
            recyclerView.setAdapter(managingAdapter);
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
        MainActivity.SortBar sortBar = new MainActivity.SortBar(sortBarView, context) {
            @Override
            void clicked(Recipe.RecipeType thisClick) {
                MainActivity.recipeSortPress(thisClick);
                adapter = new RecipesAdapter(context, MainActivity.getRecipes(), thislistener);
                recyclerView.setAdapter(adapter);
                updateSelectedImage();
            }
        };

        return view;
    }
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
    public void onView(int layoutPosition) {
        Intent intent = new Intent(getActivity(), DisplayRecipe.class);
        Log.e("layoutPositiononView", String.valueOf(layoutPosition));
        Log.e("layoutPositiononCont", String.valueOf(MainActivity.getRecipes().get(layoutPosition).getRecipeTitle()));
        Recipe recToSend = MainActivity.getRecipes().get(layoutPosition);
        Log.e("layoutPositiononCont", String.valueOf(recToSend.getRecipeTitle()));

        intent.putExtra("Recipe", recToSend);
        intent.putExtra("layoutPosition", layoutPosition);
        startActivityForResult(intent, REQUEST_CODE);
    }

    public void addRecipe(View view) {
        EditText tv = (EditText) getActivity().findViewById(R.id.EditText_RecipeName);
        String temp = tv.getText().toString();
        if(temp.equals("")) return;
        MainActivity.addRecipe(0, new Recipe(temp), context);
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
                    MainActivity.removeRecipe(index);
                    MainActivity.addRecipe(index, passedItem, context);
                    recyclerView.scrollToPosition(index);
                    //recyclerView.notifyAll();
                    break;
                case "Delete":
                    MainActivity.removeRecipe(index);
                    adapter.notifyDataSetChanged();
                    recyclerView.scrollToPosition(0);
                    break;
                default:
                    break;
            }


        }
    }

    @Override
    public void onResume(){
        super.onResume();
        adapter = new RecipesAdapter(context, MainActivity.getRecipes(), this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onDelete(int layoutPosition) {
        MainActivity.removeRecipe(layoutPosition);
        managingAdapter.notifyItemRemoved(layoutPosition);
    }
}