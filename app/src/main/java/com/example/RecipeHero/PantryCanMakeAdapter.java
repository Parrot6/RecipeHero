package com.example.RecipeHero;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;

public class PantryCanMakeAdapter extends RecyclerView.Adapter<PantryCanMakeAdapter.ViewHolderPantryCanMake> {
    private LayoutInflater mInflater;
    private Context context;
    ArrayList<Recipe> mdata = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.N)
    public PantryCanMakeAdapter(Context mcontext, ArrayList<Recipe> data) {
        context = mcontext;
        ArrayList<RecipeStockProfile> percents = new ArrayList<>();
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (int i = 0; i < data.size(); i++) {
            RecipeStockProfile recProfile = new RecipeStockProfile(data.get(i), i);
            percents.add(recProfile);
        }
        percents.sort(new Comparator<RecipeStockProfile>() {
            @Override
            public int compare(RecipeStockProfile recipeStockProfile, RecipeStockProfile t1) {
                return (int) (t1.percentStocked - recipeStockProfile.percentStocked);
            }
        });
        for(int i = 0; i < percents.size(); i++){
            mdata.add(data.get(percents.get(i).parentArraylistPos));
        }
    }

    @NonNull
    @Override
    public ViewHolderPantryCanMake onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pantry_recipe_canmake, parent, false);
        ViewHolderPantryCanMake holder = new ViewHolderPantryCanMake(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderPantryCanMake holder, int position) {
        double x = 0;
        Recipe data = mdata.get(position);
        holder.recipeName.setText(data.getRecipeTitle());
        RecipeStockProfile recProfile = new RecipeStockProfile(data, position);

        holder.ingredientsHave.setText(recProfile.getStockedText());
        holder.ingredientsDontHave.setText(recProfile.getUnstockedText());
        if(data.getRecipeType() == Recipe.RecipeType.NONE) holder.recipeType.setVisibility(View.GONE);
        else {
            holder.recipeType.setVisibility(View.VISIBLE);
            holder.recipeType.setImageResource(data.getRecipeType().getDrawable());
        }
        holder.recipeCompletion.setText(String.valueOf((int)recProfile.percentStocked) + "%");
    }

    @Override
    public int getItemCount() {
        return mdata.size();
    }


    public static class ViewHolderPantryCanMake extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView recipeName;
        TextView recipeCompletion;
        TextView ingredientsHaveTitle;
        TextView ingredientsHave;
        TextView getIngredientsDontHaveTitle;
        TextView ingredientsDontHave;
        Button viewButton;
        ConstraintLayout viewmoreblock;
        ArrayList<Ingredient> allIngred;
        ImageView recipeType;
        ViewHolderPantryCanMake(View itemView) {
            super(itemView);
            recipeName = itemView.findViewById(R.id.text_pantry_canmake_recipeName);
            recipeCompletion = itemView.findViewById(R.id.pantry_canmake_numMissingIngr);
            ingredientsHave = itemView.findViewById(R.id.text_pantry_canmake_ingredientHave);
            ingredientsDontHave = itemView.findViewById(R.id.text_pantry_canmake_ingredientDontHave);
            viewButton = itemView.findViewById(R.id.button_pantry_canmake_view);
            viewmoreblock = itemView.findViewById(R.id.pantry_canmake_viewmoreblock);
            viewmoreblock.setVisibility(View.GONE);
            viewButton.setOnClickListener(this);
            recipeType = itemView.findViewById(R.id.pantry_canmake_recipeType);
            //PantryViewMoreAdapter adapter = new PantryViewMoreAdapter(itemView.getContext(), null, this);
            //nestedRV.setAdapter(adapter);
            //add more listeners here
        }
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.button_pantry_canmake_view:
                    if(viewButton.getText().equals("view")) {
                        viewmoreblock.setVisibility(View.VISIBLE);
                        viewButton.setText("hide");
                    } else {
                        viewmoreblock.setVisibility(View.GONE);
                        viewButton.setText("view");
                    }
                break;
            }
            }
    }

    public static class RecipeStockProfile {
        ArrayList<Ingredient> stocked;
        ArrayList<Ingredient> unstocked;
        double percentStocked;
        int parentArraylistPos;
        RecipeStockProfile(Recipe rec, int arrayPosition){
            parentArraylistPos = arrayPosition;
            stocked = PantryFragment.getRecipeStocked(rec);
            unstocked = PantryFragment.getRecipeUnstocked(rec);
            double stockD = stocked.size();
            double unstockD = unstocked.size();
            percentStocked = (stockD / (unstockD + stockD));
            if(stockD + unstockD == 0) percentStocked = 1;
            percentStocked = percentStocked * 100;
        }
        public String getStockedText(){
            String textSoFar = "";
            for(int i = 0; i < stocked.size(); i++){
                textSoFar += stocked.get(i).toString();
                if((i + 1) < stocked.size()) textSoFar += " \n";
            }
            if(textSoFar.equals("")) textSoFar = "None";
            return textSoFar;
        }
        public String getUnstockedText(){
            String textSoFar = "";
            DecimalFormat format = new DecimalFormat("0.#");
            for(int i = 0; i < unstocked.size(); i++){
                textSoFar += unstocked.get(i).toString();
                Double havePartialStock = PantryFragment.checkIngredientStock(unstocked.get(i));
                if(havePartialStock > 0) textSoFar += " (have " + format.format(havePartialStock) + ")";
                if((i + 1) < unstocked.size()) textSoFar += " \n";
            }
            if(textSoFar.equals("")) textSoFar = "None";
            return textSoFar;
        }
    }

    public static String ingredArraytoString(ArrayList<Ingredient> ings){
        String ingredientsSoFar = "";
        for(int i = 0; i < ings.size(); i++){
            ingredientsSoFar += ings.get(i).toString();
            if((i + 1) < ings.size()) ingredientsSoFar += " \n";
        }
        if(ingredientsSoFar.equals("")) ingredientsSoFar = "None";
        return ingredientsSoFar;
    }
}
