package com.example.RecipeHero;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class NutritionAdapter extends RecyclerView.Adapter<NutritionAdapter.ViewHolderIng> {
    public static DecimalFormat df = new DecimalFormat("0.00");

    private static ArrayList<Ingredient> mData;
    private static ArrayList<viewState> currentState = new ArrayList<>();
    private LayoutInflater mInflater;
    Context context;
    private static int cursorSpot = 0;
    private Nutrition totalRecNut;
    Recipe recipe;
    // data is passed into the constructor
    public NutritionAdapter(Context context, ArrayList<Ingredient> data, Recipe recipe) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        mData = data;
        for (Ingredient ingredient : mData
             ) {
            currentState.add(new viewState());
        }
        currentState.add(new viewState()); //for total recipe
        this.recipe = recipe;
        totalRecNut = recipe.getNutritionSummary();
       // this.mOnClickListener = listener;
    }
    // inflates the row layout from xml when needed
    @NonNull
    @Override
    public ViewHolderIng onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.nutrition_info_item, parent, false);

        return new ViewHolderIng(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolderIng holder, int position) {
        Ingredient ingr;
        Nutrition thisNut;
        String temp;
        if(position < getItemCount()-1){
            ingr = mData.get(position);

            holder.ingredientName.setText(ingr.getName());
            holder.amtAndType.setText(String.format("%s %s", ingr.getQuantity(), ingr.getMeasurementType()));
            thisNut = ingr.getNutrition();
            holder.resultSuccess.setVisibility(View.VISIBLE);
            if(ingr.getNutrition() == null) {
                holder.resultSuccess.setText(Nutrition.QueryResults.NOT_QUERIED.asString());
                return;
            }
            holder.Lock.setVisibility(View.GONE);
            temp = "Nutrition based on " + df.format(thisNut.nf_serving_size_qty) + " " + thisNut.nf_serving_size_unit + " of "+ thisNut.item_name;
            holder.resultSuccess.setText(thisNut.getQueryResults().asString());
        } else {
            Nutrition recipeSummary = null;
            holder.resultSuccess.setVisibility(View.GONE);
            holder.Lock.setChecked(recipe.nutritionSummaryLocked);
            holder.Lock.setVisibility(View.VISIBLE);
            holder.ingredientName.setText("");
            holder.amtAndType.setText("Total Recipe");
            holder.resultSuccess.setText("");
            temp = "Nutrition based on summary of all ingredients. Lock Total Recipe to Scale All & prevent individual ingredients from updating it when changed.";
            //if(!recipe.nutritionSummaryLocked) updateRecipeTotalNutrition();
            thisNut = totalRecNut;
        }
        //holder.nutrientHolder.setVisibility(View.VISIBLE);
        if(currentState.get(position).isExpanded) holder.nutrientHolder.setVisibility(View.VISIBLE);
        else holder.nutrientHolder.setVisibility(View.GONE);
        holder.editNutrition.setTag(position);



        holder.scaleAll.setTag(position);
        holder.scaleAll.setChecked(currentState.get(position).isScaleAllChecked);




        holder.nutritionBasedOn.setText(temp);
        holder.et_calories.setText(df.format(thisNut.kcal_calories));
        holder.et_calories.setTag(position);
        holder.et_totalFat.setText(df.format(thisNut.g_total_fat));
        holder.et_totalFat.setTag(position);
        holder.et_cholesterol.setText(df.format(thisNut.mg_cholesterol));
        holder.et_cholesterol.setTag(position);
        holder.et_sodium.setText(df.format(thisNut.mg_sodium));
        holder.et_sodium.setTag(position);
        holder.et_totalCarbs.setText(df.format(thisNut.g_total_carbohydrate));
        holder.et_totalCarbs.setTag(position);
        holder.et_sugars.setText(df.format(thisNut.g_sugars));
        holder.et_sugars.setTag(position);
        holder.et_protein.setText(df.format(thisNut.g_protein));
        holder.et_protein.setTag(position);
        holder.et_servingWeight.setText(df.format(thisNut.g_serving_weight_grams));
        holder.et_servingWeight.setTag(position);
        holder.et_servingSize.setText(df.format(thisNut.nf_serving_size_qty));
        holder.et_servingSize.setTag(position);
    }

    private void scaleAll(Nutrition nut, double multiplier, int position){
        nut.scaleNutrition(multiplier);
        notifyItemChanged(position);
    };

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size() + 1;
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolderIng extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView amtAndType;
        TextView ingredientName;
        TextView resultSuccess;
        Button editNutrition;
        View itemView;
        ConstraintLayout nutrientHolder;
        TextView nutritionBasedOn;
        Switch scaleAll;
        TextView calories;
        TextView totalFat;
        TextView cholesterol;
        TextView sodium;
        TextView totalCarbs;
        TextView sugars;
        TextView protein;
        TextView servingWeight;
        TextView servingSize;
        EditText et_calories;
        EditText et_totalFat;
        EditText et_cholesterol;
        EditText et_sodium;
        EditText et_totalCarbs;
        EditText et_sugars;
        EditText et_protein;
        EditText et_servingWeight;
        EditText et_servingSize;
        Switch Lock;

        ViewHolderIng(View itemView) {
            super(itemView);
            Lock = itemView.findViewById(R.id.switch_nutrition_lockrecipe);
            Lock.setVisibility(View.GONE);
            Lock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    recipe.nutritionSummaryLocked = b;
                }
            });
            this.itemView = itemView;
            nutrientHolder = itemView.findViewById(R.id.layout_nutrition_details);
            nutrientHolder.setVisibility(View.GONE);
            ingredientName = itemView.findViewById(R.id.text_nutrition_info_name);
            editNutrition = itemView.findViewById(R.id.button_nutrition_info_edit);
            editNutrition.setOnClickListener(this);
            amtAndType = itemView.findViewById(R.id.text_nutrtition_info_qtyAndMeas);
            resultSuccess = itemView.findViewById(R.id.text_nutrition_info_result);
            editNutrition.setText("edit");
            //editNutrition.setOnClickListener(this);
            nutritionBasedOn = itemView.findViewById(R.id.text_nutrition_info_basedOn);
            scaleAll = itemView.findViewById(R.id.switch_scaleNutrition);
            scaleAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                   if(scaleAll.getTag() != null) currentState.get((Integer) scaleAll.getTag()).isScaleAllChecked = b; //so databind remembers
                }
            });
            calories = itemView.findViewById(R.id.text_nutrition_title_calories);
            totalFat = itemView.findViewById(R.id.text_nutrition_title_total_fat);
            cholesterol = itemView.findViewById(R.id.text_nutrition_title_cholesterol);
            sodium = itemView.findViewById(R.id.text_nutrition_title_sodium);
            totalCarbs = itemView.findViewById(R.id.text_nutrition_title_totalCarbs);
            sugars = itemView.findViewById(R.id.text_nutrition_title_sugars);
            protein = itemView.findViewById(R.id.text_nutrition_title_protein);
            servingWeight = itemView.findViewById(R.id.text_nutrition_title_servingWeight);
            servingSize = itemView.findViewById(R.id.text_nutrition_title_servingSize);

            et_calories = itemView.findViewById(R.id.et_nutrition_info_calories);
            et_calories.addTextChangedListener(new CustomTextWatcher(et_calories){
                @Override
                Nutrition.command mySetter() {
                    if(getAdapterPosition() == getItemCount() - 1) return recipe.getNutritionSummary()::setKcal_calories;
                    return mData.get((Integer) et_calories.getTag()).getNutrition()::setKcal_calories;
                }
            });
            et_totalFat = itemView.findViewById(R.id.et_nutrition_info_totalFat);
            et_totalFat.addTextChangedListener(new CustomTextWatcher(et_totalFat){
                @Override
                Nutrition.command mySetter() {
                    if(getAdapterPosition() == getItemCount() - 1) return recipe.getNutritionSummary()::setG_total_fat;
                    return mData.get((Integer) et_totalFat.getTag()).getNutrition()::setG_total_fat;
                }
            });
            et_cholesterol = itemView.findViewById(R.id.et_nutrition_info_cholesterol);
            et_cholesterol.addTextChangedListener(new CustomTextWatcher(et_cholesterol){
                @Override
                Nutrition.command mySetter() {
                    if(getAdapterPosition() == getItemCount() - 1) return recipe.getNutritionSummary()::setMg_cholesterol;
                    return mData.get((Integer) et_cholesterol.getTag()).getNutrition()::setMg_cholesterol;
                }
            });
            et_sodium = itemView.findViewById(R.id.et_nutrition_info_sodium);
            et_sodium.addTextChangedListener(new CustomTextWatcher(et_sodium){
                @Override
                Nutrition.command mySetter() {
                    if(getAdapterPosition() == getItemCount() - 1) return recipe.getNutritionSummary()::setMg_sodium;
                    return mData.get((Integer) et_sodium.getTag()).getNutrition()::setMg_sodium;
                }
            });
            et_totalCarbs = itemView.findViewById(R.id.et_nutrition_info_totalCarbs);
            et_totalCarbs.addTextChangedListener(new CustomTextWatcher(et_totalCarbs){
                @Override
                Nutrition.command mySetter() {
                    if(getAdapterPosition() == getItemCount() - 1) return recipe.getNutritionSummary()::setG_total_carbohydrate;
                    return mData.get((Integer) et_totalCarbs.getTag()).getNutrition()::setG_total_carbohydrate;
                }
            });
            et_sugars = itemView.findViewById(R.id.et_nutrition_info_sugars);
            et_sugars.addTextChangedListener(new CustomTextWatcher(et_sugars){
                @Override
                Nutrition.command mySetter() {
                    if(getAdapterPosition() == getItemCount() - 1) return recipe.getNutritionSummary()::setG_sugars;
                    return mData.get((Integer) et_sugars.getTag()).getNutrition()::setG_sugars;
                }
            });
            et_protein = itemView.findViewById(R.id.et_nutrition_info_protein);
            et_protein.addTextChangedListener(new CustomTextWatcher(et_protein){
                @Override
                Nutrition.command mySetter() {
                    if(getAdapterPosition() == getItemCount() - 1) return recipe.getNutritionSummary()::setG_protein;
                    return mData.get((Integer) et_protein.getTag()).getNutrition()::setG_protein;
                }
            });
            et_servingWeight = itemView.findViewById(R.id.et_nutrition_info_servingWeight);
            et_servingWeight.addTextChangedListener(new CustomTextWatcher(et_servingWeight){
                @Override
                Nutrition.command mySetter() {
                    if(getAdapterPosition() == getItemCount() - 1) return recipe.getNutritionSummary()::setG_serving_weight_grams;
                    return mData.get((Integer) et_servingWeight.getTag()).getNutrition()::setG_serving_weight_grams;
                }
            });
            et_servingSize = itemView.findViewById(R.id.et_nutrition_info_servingSize);
            et_servingSize.addTextChangedListener(new CustomTextWatcher(et_servingSize){
                @Override
                Nutrition.command mySetter() {
                    if(getAdapterPosition() == getItemCount() - 1) return recipe.getNutritionSummary()::setNf_serving_size_qty;
                    return mData.get((Integer) et_servingSize.getTag()).getNutrition()::setNf_serving_size_qty;
                }
            });

        }
        private class CustomTextWatcher implements TextWatcher {
            private EditText et;
            boolean ignoreRecursiveCalls = false;
            String beforeValue;

            CustomTextWatcher(EditText et){
                this.et = et;
            }
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                beforeValue = charSequence.toString();
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(et.hasFocus()) {
                    if (!ignoreRecursiveCalls) {
                        ignoreRecursiveCalls = true;
                        cursorSpot = i + i2;
                        if (et.getTag() != null) {
                            if ((int) et.getTag() == getItemCount()-1) {
                                afterTextChangedMethod(et, beforeValue, mySetter(), true);
                            } else afterTextChangedMethod(et, beforeValue, mySetter(), false);
                        }

                        /*if(et_servingSize.hasFocus()) */
                        ignoreRecursiveCalls = false; //any calls between flags will not make it into here
                        //et_servingSize.setSelection(Math.min(cursorSpot, charSequence.toString().length()));
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
                et.setSelection(Math.min(cursorSpot, editable.toString().length()));
            }
            Nutrition.command mySetter() {
                return null;
            }
        }
        public void afterTextChangedMethod(EditText et, String beforeValue, Nutrition.command setter, boolean isTotalRec){
                if (!et.hasFocus()) return; //stop setText from triggering loop
                if (scaleAll.isChecked()) {
                    if (beforeValue.equals(et.getText().toString())) {
                        return; //do nothing if no change
                    }
                    double before = Double.parseDouble(beforeValue);
                    double differenceMultiplier = 1.0;
                    if (before != 0.0)
                        differenceMultiplier = Double.parseDouble(et.getText().toString()) / before;
                    else
                        differenceMultiplier = Double.parseDouble(et.getText().toString()) / 1;
                    if (differenceMultiplier > 0.001 && differenceMultiplier != 1.0) {
                        setter.set(Double.parseDouble(beforeValue)); //revert to original value and then scale all values
                        if(isTotalRec) scaleAll(recipe.getNutritionSummary(), differenceMultiplier, (Integer) et.getTag());
                        else scaleAll(mData.get((Integer) et.getTag()).getNutrition(), differenceMultiplier, (Integer) et.getTag());
                        updateRecipeTotalNutrition();
                    }
                } else {
                    setter.set(Double.parseDouble(et.getText().toString()));
                    updateRecipeTotalNutrition();
                }
        }
       @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.button_nutrition_info_edit:
                    if(view.getTag() != null) {
                        int pos = (int) view.getTag();
                        if (!currentState.get(pos).isExpanded) {
                            currentState.get(pos).isExpanded = true;
                            editNutrition.setText("hide");
                            nutrientHolder.setVisibility(View.VISIBLE);
                        } else {
                            currentState.get(pos).isExpanded = false;
                            editNutrition.setText("edit");
                            nutrientHolder.setVisibility(View.GONE);
                        }
                    }
                    break;
                default:
                    break;
            }

        }
    }

    private void updateRecipeTotalNutrition() {
        if(!recipe.nutritionSummaryLocked) return;
        Nutrition totalNutritionSoFar = new Nutrition();
        for (Ingredient ing: mData
        ) {
            if(ing.getNutrition() == null) continue;
            totalNutritionSoFar.getCombined(ing.getNutrition());
        }
        totalRecNut = totalNutritionSoFar;
        recipe.setNutritionSummary(totalNutritionSoFar);
        notifyItemChanged(getItemCount()-1);
    }

    private class viewState{
        public boolean isExpanded = false;
        public boolean isScaleAllChecked = false;
        viewState(){

        }
    }
    // convenience method for getting data at click position
    Ingredient getItem(int id) {
        return mData.get(id);
    }

}