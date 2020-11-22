package com.example.RecipeHero;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import static android.view.View.GONE;

public class RecipesAdapter extends RecyclerView.Adapter<RecipesAdapter.ViewHolder> {

    final private MyClickListener mOnClickListener;
    private static ArrayList<Recipe> mData = new ArrayList<>();
    private LayoutInflater mInflater;

    // data is passed into the constructor
    public RecipesAdapter(Context context, ArrayList<Recipe> data, MyClickListener listener) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mData = data;
        this.mOnClickListener = listener;
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void searchRecipes(String search){
        mData = MainActivity.getRecipes();
        ArrayList<Recipe> clone = new ArrayList<>();
        for(Recipe rec: mData){
            if(rec.getRecipeTitle().toLowerCase().contains(search.toLowerCase())) clone.add(rec);
        }
        mData = clone;
        this.notifyDataSetChanged();
    }
    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recipe_listitem, parent, false);
        ViewHolder holder = new ViewHolder(view, mOnClickListener);

        return holder;
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Recipe recipe = mData.get(position);
        holder.myTextView.setText(recipe.getRecipeTitle());
        if(recipe.getRecipeIcon() != null) {
            holder.recipeIcon.setVisibility(View.VISIBLE);
            holder.recipeIcon.setImageBitmap(recipe.getRecipeIcon());
        }
        else holder.recipeIcon.setVisibility(GONE);

        if(recipe.getRecipeType() == Recipe.RecipeType.NONE){
            holder.recipeType.setVisibility(GONE);
        } else holder.recipeType.setVisibility(View.VISIBLE);
        holder.recipeType.setImageResource(recipe.getRecipeType().getDrawable());


    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView myTextView;
        //ImageView viewButton;
        ConstraintLayout wholeRow;
        MyClickListener listener;
        ImageView recipeType;
        ImageView recipeIcon;
        ViewHolder(View itemView, MyClickListener myClickListener) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.text_mainListRecipe);
            //viewButton = itemView.findViewById(R.id.button_edit_ingredient_delete);
            recipeIcon = itemView.findViewById(R.id.recipe_listitem_image);
            recipeType = itemView.findViewById(R.id.recips_listitem_type);
            wholeRow = itemView.findViewById(R.id.edit_ingredient_RelativeLayout);
            //recipeType.setColorFilter(itemView.getResources().getColor(R.color.recipeListItem));
            this.listener = myClickListener;
            wholeRow.setOnClickListener(this);
            //viewButton.setOnClickListener(this);
            //add more listeners here
        }

        @Override
            public void onClick(View view) {
            switch (view.getId()) {
                case R.id.edit_ingredient_RelativeLayout:
                        listener.onView(mData.get(this.getAdapterPosition()).getID(), this.getAdapterPosition());
                    break;
                default:
                    break;
            }
        }
    }

    // convenience method for getting data at click position
    Recipe getItem(int id) {
        return mData.get(id);
    }
    public void refreshRecipes(ArrayList<Recipe> newData){
        mData = newData;
        notifyDataSetChanged();
    }

    // parent activity will implement this method to respond to click events
    public interface MyClickListener {
        void onView(int recipeID, int layoutPosition);
    }
}