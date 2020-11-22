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

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class IngredientsAdapter extends RecyclerView.Adapter<IngredientsAdapter.ViewHolderIng> {

    final private MyClickListener mOnClickListener;
    private static ArrayList<Ingredient> mData;
    private LayoutInflater mInflater;
    private static int beingEdited = -1;
    private static ViewHolderIng beingEditedViewHolder;
    // data is passed into the constructor
    public IngredientsAdapter(Context context, ArrayList<Ingredient> data, MyClickListener listener) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mData = data;
        this.mOnClickListener = listener;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolderIng onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.edit_ingredient, null);
        ViewHolderIng holder = new ViewHolderIng(view, mOnClickListener);

        return holder;
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolderIng holder, int position) {
        Ingredient ingr = mData.get(position);
        //if(!ingr.getAdditionalNote().trim().equals("")) holder.note.setText(String.format("%s", ingr.getAdditionalNote()));
        //else holder.note.setText("");
        if(beingEdited != position) {
            holder.ingredientName.setEnabled(false);
            holder.amt.setEnabled(false);
            holder.type.setEnabled(false);
            holder.note.setEnabled(false);
            holder.itemView.findViewById(R.id.ConstraintLayout).setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.recipeListItem));
            holder.editIngredient.setVisibility(View.VISIBLE);
            holder.deleteIngredient.setImageResource(R.drawable.ic_iconmonstr_x_close);
        } else {
            holder.deleteIngredient.setImageResource(R.drawable.ic_iconmonstr_check_mark_4);
            holder.ingredientName.setEnabled(true);
            holder.amt.setEnabled(true);
            holder.type.setEnabled(true);
            holder.note.setEnabled(true);
            holder.itemView.findViewById(R.id.ConstraintLayout).setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.pantryOtherTypes));
            holder.editIngredient.setVisibility(View.GONE);
        }
        holder.ingredientName.setTag(position);
        holder.amt.setTag(position);
        holder.type.setTag(position);
        holder.note.setTag(position);
        holder.ingredientName.setText(ingr.getName().trim());
        holder.amt.setText(String.valueOf(ingr.getQuantity()));
        holder.type.setText(String.valueOf(ingr.getMeasurementType()));
        holder.note.setText(String.valueOf(ingr.getAdditionalNote()));
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public static class ViewHolderIng extends RecyclerView.ViewHolder implements View.OnClickListener {
        EditText amt;
        EditText type;
        EditText ingredientName;
        EditText note;
        ImageView deleteIngredient;
        MyClickListener listener;
        ImageButton editIngredient;
        ViewHolderIng(View itemView, MyClickListener myClickListener) {
            super(itemView);
            editIngredient = itemView.findViewById(R.id.imageButton_editThisIngredient);
            editIngredient.setOnClickListener(this);
            ingredientName = itemView.findViewById(R.id.text_mainListRecipe);
            ingredientName.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                      if(ingredientName.getTag() != null)  mData.get((int) ingredientName.getTag()).setName(charSequence.toString());
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
            deleteIngredient = itemView.findViewById(R.id.button_edit_ingredient_delete);
            amt = itemView.findViewById(R.id.editText_edit_Ingredient_quantity);
            amt.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if(amt.getTag() != null) mData.get((int) amt.getTag()).setQuantity(Double.parseDouble(charSequence.toString()));
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
            type = itemView.findViewById(R.id.editText_editIngredMeasureType);
            type.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if(type.getTag() != null) mData.get((int) type.getTag()).setMeasurementType(charSequence.toString());
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
            note = itemView.findViewById(R.id.text_edit_ingredient_note);
            note.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if(note.getTag() != null) mData.get((int) note.getTag()).setAdditionalNote(charSequence.toString());
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
            this.listener = myClickListener;
            deleteIngredient.setOnClickListener(this);
            //add more listeners here
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.button_edit_ingredient_delete:
                        if(this.getLayoutPosition() == beingEdited){
                            finishEditingThisIngred();
                        } else listener.deleteIngredient(this.getLayoutPosition());
                   // }
                    break;
                case R.id.imageButton_editThisIngredient:
                    //turn previous one back to normal first
                    finishEditingThisIngred();
                    beingEdited = getAdapterPosition();
                    beingEditedViewHolder = this;
                    deleteIngredient.setImageResource(R.drawable.ic_iconmonstr_check_mark_4);
                    amt.setEnabled(true);
                    type.setEnabled(true);
                    ingredientName.setEnabled(true);
                    note.setEnabled(true);
                    itemView.findViewById(R.id.ConstraintLayout).setBackgroundColor(itemView.getContext().getResources().getColor(R.color.pantryOtherTypes));
                    editIngredient.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
        }


    }
    private static void finishEditingThisIngred() {
        if(beingEditedViewHolder != null) {
            beingEdited = -1;
            beingEditedViewHolder.deleteIngredient.setImageResource(R.drawable.ic_iconmonstr_x_close);
            beingEditedViewHolder.amt.setEnabled(false);
            beingEditedViewHolder.type.setEnabled(false);
            beingEditedViewHolder.ingredientName.setEnabled(false);
            beingEditedViewHolder.note.setEnabled(false);
            beingEditedViewHolder.itemView.findViewById(R.id.ConstraintLayout).setBackgroundColor(beingEditedViewHolder.itemView.getContext().getResources().getColor(R.color.recipeListItem));
            beingEditedViewHolder.editIngredient.setVisibility(View.VISIBLE);
        }
    }
    // convenience method for getting data at click position
    Ingredient getItem(int id) {
        return mData.get(id);
    }

    // parent activity will implement this method to respond to click events
    public interface MyClickListener {

        void deleteIngredient(int layoutPosition);
        void updateIngredient(int layoutPosition);
    }
}