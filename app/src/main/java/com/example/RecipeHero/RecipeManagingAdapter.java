package com.example.RecipeHero;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import static android.view.View.GONE;

public class RecipeManagingAdapter extends RecyclerView.Adapter<RecipeManagingAdapter.ManagingViewHolder> {

    final private ManagingClickListener mOnClickListener;
    private static ArrayList<Recipe> mData = new ArrayList<>();
    private LayoutInflater mInflater;
    Context context;
    // data is passed into the constructor
    public RecipeManagingAdapter(Context context, ArrayList<Recipe> data, ManagingClickListener listener) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mData = data;
        this.context = context;
        this.mOnClickListener = listener;
    }

    // inflates the row layout from xml when needed
    @Override
    public ManagingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recipe_listitem_manage, parent, false);

        return new ManagingViewHolder(view, mOnClickListener);
    }

    // binds the data to the TextView in each row
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("RestrictedApi")
    @Override
    public void onBindViewHolder(ManagingViewHolder holder, int position) {
        Recipe recipe = mData.get(position);
        holder.myTextView.setText(recipe.getRecipeTitle());
        if(recipe.getRecipeIcon() != null) {
            holder.recipeIcon.setVisibility(View.VISIBLE);
            holder.recipeIcon.setImageBitmap(recipe.getRecipeIcon());
        }
        else holder.recipeIcon.setVisibility(GONE);
        holder.delete.setVisibility(View.VISIBLE);
        if(recipe.getRecipeType() == Recipe.RecipeType.NONE){
            holder.recipeType.setVisibility(View.VISIBLE);
        } else holder.recipeType.setVisibility(View.VISIBLE);
        holder.recipeType.setImageResource(recipe.getRecipeType().getDrawable());

    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public static class ManagingViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView myTextView;
        Button delete;
        ManagingClickListener listener;
        ImageView recipeType;
        ImageView recipeIcon;
        ManagingViewHolder(View itemView, ManagingClickListener myClickListener) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.text_managingRecipe);
            delete = itemView.findViewById(R.id.manage_delete);
            recipeIcon = itemView.findViewById(R.id.recipe_listitem_managing_image);
            recipeType = itemView.findViewById(R.id.recipe_managing_type);
            this.listener = myClickListener;
            delete.setOnClickListener(this);
            recipeType.setOnClickListener(this);
            //add more listeners here
        }

        @SuppressLint("RestrictedApi")
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.manage_delete:
                    //if (listener != null) {
                    if(getAdapterPosition() == RecyclerView.NO_POSITION) return;
                    listener.onDelete(getAdapterPosition());
                    // }
                    break;
                case R.id.recipe_managing_type:
                    Context wrapper = new ContextThemeWrapper(view.getContext(), R.style.AppTheme);
                    PopupMenu popup = new PopupMenu(wrapper, recipeType);

                    //Inflating the Popup using xml file
                    popup.getMenuInflater()
                            .inflate(R.menu.recipe_type_menu, popup.getMenu());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        popup.setForceShowIcon(true);
                    }

                    //registering popup with OnMenuItemClickListener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.rt_Alcohol: // Handle option1 Click
                                    mData.get(getAdapterPosition()).setRecipeType(Recipe.RecipeType.AlcoholicDrink);
                                    recipeType.setImageResource(Recipe.RecipeType.AlcoholicDrink.getDrawable());
                                    return true;
                                case R.id.rt_Desert: // Handle option2 Click
                                    mData.get(getAdapterPosition()).setRecipeType(Recipe.RecipeType.Desert);
                                    recipeType.setImageResource(Recipe.RecipeType.Desert.getDrawable());
                                    return true;
                                case R.id.rt_Drink: // Handle option2 Click
                                    mData.get(getAdapterPosition()).setRecipeType(Recipe.RecipeType.Drink);
                                    recipeType.setImageResource(Recipe.RecipeType.Drink.getDrawable());
                                    return true;
                                case R.id.rt_Meal: // Handle option2 Click
                                    mData.get(getAdapterPosition()).setRecipeType(Recipe.RecipeType.Meal);
                                    recipeType.setImageResource(Recipe.RecipeType.Meal.getDrawable());
                                    return true;
                                case R.id.rt_Side: // Handle option2 Click
                                    mData.get(getAdapterPosition()).setRecipeType(Recipe.RecipeType.Side);
                                    recipeType.setImageResource(Recipe.RecipeType.Side.getDrawable());
                                    return true;
                                default:
                                    mData.get(getAdapterPosition()).setRecipeType(Recipe.RecipeType.NONE);
                                    recipeType.setImageResource(Recipe.RecipeType.NONE.getDrawable());
                                    return false;
                            }
                        }
                    });

                    popup.show();
                    break;
                default:
                    break;
            }
        }
    }

    // parent activity will implement this method to respond to click events
    public interface ManagingClickListener {
        void onDelete(int layoutPosition);
    }
}