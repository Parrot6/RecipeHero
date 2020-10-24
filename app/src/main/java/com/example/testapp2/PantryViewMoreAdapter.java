package com.example.testapp2;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PantryViewMoreAdapter extends RecyclerView.Adapter<PantryViewMoreAdapter.ViewHolderNestedPantry> {

    private final LayoutInflater mInflater;
    Context context;
    SameNameIngredients parent;
    ArrayList<Ingredient> mdata = new ArrayList<>();
    private int parentPosition;
    boolean hasInfinitelyStockedMember = false;
    public PantryViewMoreAdapter(Context mcontext, SameNameIngredients parent){
        context = mcontext;
        this.parent = parent;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                //thisSameNameIngredient.remove(0);

        for(int i = 1; i < parent.getIngredientList().size(); i++){
            mdata.add(parent.getIngredientList().get(i));
            //Log.e("subItem", mdata.get(mdata.size()-1).toString());
        }
        //this.mdata = thisSameNameIngredient;
    }

    @NonNull
    @Override
    public ViewHolderNestedPantry onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pantry_item_subitem, parent, false);
        ViewHolderNestedPantry holder = new ViewHolderNestedPantry(view);

        return holder;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(@NonNull ViewHolderNestedPantry holder, int position) {
        holder.quantity.setText(String.valueOf(mdata.get(position).getQuantity()));
        holder.quantity.setTag(position);
        holder.ingredient.setText(mdata.get(position).getName());
        holder.measurementType.setText(mdata.get(position).getMeasurementType());

        holder.infinity.setTag(parentPosition);
        if (mdata.get(position).isInfinitelyStocked()) {
            holder.infinity.setImageResource(R.drawable.ic_iconmonstr_infinity_2);
            holder.pantryMinus.setVisibility(View.GONE);
            holder.pantryPlus.setVisibility(View.GONE);
            holder.quantity.setVisibility(View.GONE);
        } else {
            if(hasInfinitelyStockedMember) holder.infinity.setImageResource(R.drawable.ic_iconmonstr_infinity_sibling);
            else holder.infinity.setImageResource(R.drawable.ic_iconmonstr_infinity_1);
            holder.pantryMinus.setVisibility(View.VISIBLE);
            holder.pantryPlus.setVisibility(View.VISIBLE);
            holder.quantity.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mdata.size();
    }
    public class ViewHolderNestedPantry extends RecyclerView.ViewHolder implements View.OnClickListener, TextWatcher {
        ImageButton pantryMinus;
        TextView quantity;
        ImageButton pantryPlus;
        ImageButton infinity;
        TextView ingredient;
        TextView measurementType;

        ViewHolderNestedPantry(View itemView) {
            super(itemView);
            pantryMinus = (ImageButton) itemView.findViewById(R.id.button_pantryNestedMinus);
            pantryMinus.setOnClickListener(this);
            quantity = itemView.findViewById(R.id.text_pantryNestedQuantity);
            quantity.addTextChangedListener(this);
            pantryPlus = (ImageButton) itemView.findViewById(R.id.button_pantryNestedPlus);
            pantryPlus.setOnClickListener(this);
            ingredient = itemView.findViewById(R.id.text_pantryNestedIngredientName);
            measurementType = itemView.findViewById(R.id.pantryNested_item_measurement_Type);
            infinity = itemView.findViewById(R.id.button_pantry_subitem_infinite);
            infinity.setOnClickListener(this);

        }


        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.button_pantryNestedMinus:
                    if(mdata.get(this.getAdapterPosition()).getQuantity() > 0) {
                        mdata.get(this.getAdapterPosition()).setQuantity(mdata.get(this.getAdapterPosition()).getQuantity() - 1);
                        quantity.setText(String.valueOf(mdata.get(this.getAdapterPosition()).getQuantity()));
                    }
                    break;
                case R.id.button_pantryNestedPlus:
                    mdata.get(this.getAdapterPosition()).setQuantity(mdata.get(this.getAdapterPosition()).getQuantity()+1);
                    quantity.setText(String.valueOf(mdata.get(this.getAdapterPosition()).getQuantity()));
                    break;
                case R.id.button_pantry_subitem_infinite:
                    mdata.get(this.getAdapterPosition()).setInfinitelyStocked(!mdata.get(0).isInfinitelyStocked());
                    if(mdata.get(this.getAdapterPosition()).isInfinitelyStocked()){
                        pantryMinus.setVisibility(View.GONE);
                        pantryPlus.setVisibility(View.GONE);
                        quantity.setVisibility(View.GONE);
                        infinity.setImageResource(R.drawable.ic_iconmonstr_infinity_2);
                    } else {
                        pantryMinus.setVisibility(View.VISIBLE);
                        pantryPlus.setVisibility(View.VISIBLE);
                        quantity.setVisibility(View.VISIBLE);
                        if(MainActivity.pantry.get((Integer) view.getTag()).hasInfinite()) infinity.setImageResource(R.drawable.ic_iconmonstr_infinity_sibling);
                        else infinity.setImageResource(R.drawable.ic_iconmonstr_infinity_1);                    }
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
            double d;
            if(s.equals("")) {
                d = 0;
            }else d = Double.parseDouble(s);
            if(d < 0) d = 0;
            PantryViewMoreAdapter.this.mdata.get(getAdapterPosition()).setQuantity(d);
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }

}
