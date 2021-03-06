package com.example.RecipeHero;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class EditCartAdapter extends RecyclerView.Adapter<EditCartAdapter.ViewHolderShop> {

    final private CartClickListener mOnClickListener;
    private ArrayList<Recipe> mData = new ArrayList<>();
    private LayoutInflater mInflater;

    // data is passed into the constructor
    public EditCartAdapter(Context context, ArrayList<Recipe> data, CartClickListener listener) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mData = data;
        this.mOnClickListener = listener;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolderShop onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.edit_cart_item, parent, false);
        ViewHolderShop holder = new ViewHolderShop(view, mOnClickListener);

        return holder;
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolderShop holder, int position) {
        Recipe recipe = mData.get(position);
        //if(recipe.getRecipeTitle() != null) {
            holder.quantity.setText(String.valueOf(mData.get(position).getCartQuantity()));
            //if(holder.quantity.getTag() != null) { //ONLY ADD ONE LISTENER
             //   holder.quantity.addTextChangedListener(new MyCustomEditCartTextListener(position));
                holder.quantity.setTag(position);
            //}
            holder.recipeName.setText(recipe.getRecipeTitle());
            String recIngs = recipe.getIngredientsAsStringList();

            if(recipe.getRecipeType() == Recipe.RecipeType.NONE) holder.recipeType.setVisibility(View.GONE);
            else {
                holder.recipeType.setVisibility(View.VISIBLE);
                holder.recipeType.setImageResource(recipe.getRecipeType().getDrawable());
            }

            if(recIngs.length() != 0) {
                holder.ingredients.setText(recIngs);
            } else {
                holder.ingredientsTitle.setText("No Ingredients");
                holder.ingredients.setVisibility(View.GONE);
            }

            String recInstruc = recipe.getRecipeInstructions();
            if(recInstruc.length() != 0) {
                holder.instructions.setText(recInstruc);
            } else {
                holder.instructionsTitle.setText("No Instructions");
                holder.instructions.setVisibility(View.GONE);
            }

            holder.recipePrev.setVisibility(View.GONE);
        //}
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolderShop extends RecyclerView.ViewHolder implements View.OnClickListener, TextWatcher {
        ImageButton minus;
        EditText quantity;
        ImageButton plus;
        TextView recipeName;
        ImageView recipeType;
        Button expand;
        TextView ingredientsTitle;
        TextView instructionsTitle;
        TextView ingredients;
        TextView instructions;
        ScrollView recipePrev;
        CartClickListener listener;

        ViewHolderShop(View itemView, CartClickListener myClickListener) {
            super(itemView);
            minus = itemView.findViewById(R.id.button_shoppingMinus);
            quantity = itemView.findViewById(R.id.editTextNumber);
            quantity.addTextChangedListener(this);
            plus = itemView.findViewById(R.id.button_shoppingPlus);
            recipeName = itemView.findViewById(R.id.edit_cart_IngredientName);
            recipeType = itemView.findViewById(R.id.shoppingitem_recipeType);
            expand = itemView.findViewById(R.id.button_edit_ingredient_delete);
            ingredientsTitle = itemView.findViewById(R.id.Text_expandedIngredientsTitle);
            ingredients = itemView.findViewById(R.id.text_expandedIngredients);
            instructionsTitle = itemView.findViewById(R.id.text_expandedStaticIngredientsTitle);
            instructions = itemView.findViewById(R.id.text_expandedRecipeInstructions);
            recipePrev = itemView.findViewById(R.id.scrollview_recipePreview);
            this.listener =  myClickListener;
            minus.setOnClickListener(this);
            plus.setOnClickListener(this);
            expand.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.button_edit_ingredient_delete:
                    //if (listener != null) {
                    //listener.deleteIngredient(this.getLayoutPosition());
                    if(recipePrev.getVisibility() == View.VISIBLE){
                        expand.setText("peek");
                        recipePrev.setVisibility(View.GONE);
                    } else {
                        expand.setText("hide");
                        recipePrev.setVisibility(View.VISIBLE);
                    }
                    // }
                    break;
                case R.id.button_shoppingMinus:
                    int xx = Integer.parseInt(quantity.getText().toString());
                    if(xx >= 1) {
                        listener.minusQuantity(this.getLayoutPosition());
                        xx--;
                        String z = xx + "";
                        quantity.setText(z);
                    } else quantity.setText("0");
                    break;
                case R.id.button_shoppingPlus:
                    listener.plusQuantity(this.getLayoutPosition());
                   int x = Integer.parseInt(quantity.getText().toString()) + 1;
                    String y = x + "";
                    quantity.setText(y);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String s = charSequence.toString();
            int d;
            if(s.equals("")) {
                d = 0;
            }else d = Integer.parseInt(charSequence.toString());
            if(d < 0) d = 0;
            mData.get(getAdapterPosition()).setCartQuantity(d);
            listener.refresh();
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }
    private class MyCustomEditCartTextListener implements TextWatcher {
        private int position;

        MyCustomEditCartTextListener(int position) {
            this.position = position;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            // no op
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            String s = charSequence.toString();
            int d;
            if(s.equals("")) {
                d = 0;
            }else d = Integer.parseInt(charSequence.toString());
            if(d < 0) d = 0;
            mData.get(position).setCartQuantity(d);
            mOnClickListener.refresh();
        }

        @Override
        public void afterTextChanged(Editable editable) {
            // no op
        }
    }

    // parent activity will implement this method to respond to click events
    public interface CartClickListener {
        void minusQuantity(int layoutPosition);
        void plusQuantity(int layoutPosition);
        void refresh();
    }
}