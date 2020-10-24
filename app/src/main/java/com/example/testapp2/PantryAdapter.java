package com.example.testapp2;

import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.ViewHolderPantry> {
    final private PantryClickListener mOnClickListener;
    private ArrayList<SameNameIngredients> sameNameIngredients;
    private List<Boolean> isExpanded = new ArrayList<>();
    private LayoutInflater mInflater;
    private Context context;

    public PantryAdapter(Context mcontext, PantryClickListener listener) {
        context = mcontext;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        sameNameIngredients = MainActivity.pantry;
        for(int i = 0; i < getItemCount(); i++){
            isExpanded.add(false);

        }
        this.mOnClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolderPantry onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pantry_item, parent, false);
        ViewHolderPantry holder = new ViewHolderPantry(view, mOnClickListener);

        return holder;
    }
    public PantryAdapter notifyAdded(int pos){
        isExpanded.add(pos, false);
        return this;
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(@NonNull ViewHolderPantry holder, int position) {
        SameNameIngredients thisSameNameIngredient = sameNameIngredients.get(position);
        holder.allIngred = thisSameNameIngredient.getIngredientList();
        holder.quantity.setText(String.valueOf(thisSameNameIngredient.getIngredientList().get(0).getQuantity()));
        holder.ingredient.setText(thisSameNameIngredient.getIngredientList().get(0).getName());
        holder.measurementType.setText(thisSameNameIngredient.getIngredientList().get(0).getMeasurementType());


       /* ArrayList<Ingredient> childNameIngredients = new ArrayList<>();
        for (int i = 1; i < sameNameIngredients.get(position).size(); i++) {
            childNameIngredients.add(thisSameNameIngredient.get(i));
        }*/
        if (thisSameNameIngredient.getIngredientList().size() <= 1) {
            holder.viewMoreTypes.setVisibility(View.GONE);
            holder.nestedRV.setVisibility(View.GONE);
        } else {
            holder.viewMoreTypes.setVisibility(View.VISIBLE);
            holder.nestedRV.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
            if(isExpanded.get(position).booleanValue()) {
                holder.nestedRV.setVisibility(View.VISIBLE);
                holder.viewMoreTypes.setImageResource(holder.viewMoreOpen);
            }
            else {
                holder.viewMoreTypes.setImageResource(holder.viewMoreClosed);
                holder.nestedRV.setVisibility(View.GONE);
            }
            holder.nestedRV.setAdapter(new PantryViewMoreAdapter(context, sameNameIngredients.get(position)));
        }
        if (thisSameNameIngredient.getIngredientList().get(0).isInfinitelyStocked()) {
            holder.infinite.setImageResource(R.drawable.ic_iconmonstr_infinity_2);
            holder.pantryMinus.setVisibility(View.GONE);
            holder.pantryPlus.setVisibility(View.GONE);
            holder.quantity.setVisibility(View.GONE);
        } else {
            if(sameNameIngredients.get(position).hasInfinite()) holder.infinite.setImageResource(R.drawable.ic_iconmonstr_infinity_sibling);
            else holder.infinite.setImageResource(R.drawable.ic_iconmonstr_infinity_1);
            holder.pantryMinus.setVisibility(View.VISIBLE);
            holder.pantryPlus.setVisibility(View.VISIBLE);
            holder.quantity.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return MainActivity.pantry.size();
    }

    public class ViewHolderPantry extends RecyclerView.ViewHolder implements View.OnClickListener, TextWatcher {
        ImageButton pantryMinus;
        TextView quantity;
        ImageButton pantryPlus;
        ImageButton infinite;
        TextView ingredient;
        TextView measurementType;
        ImageView viewMoreTypes;
        RecyclerView nestedRV;
        PantryClickListener listener;
        LinearLayout Layout;
        ArrayList<Ingredient> allIngred;
        final int viewMoreClosed = R.drawable.ic_iconmonstr_arrow_moretypes;
        final int viewMoreOpen = R.drawable.ic_iconmonstr_x_uparrow;
        ViewHolderPantry(View itemView, PantryClickListener myClickListener) {
            super(itemView);
            pantryMinus = (ImageButton) itemView.findViewById(R.id.button_pantryMinus);
            pantryMinus.setOnClickListener(this);
            quantity = itemView.findViewById(R.id.text_pantryQuantity);
            quantity.addTextChangedListener(this);
            pantryPlus = (ImageButton) itemView.findViewById(R.id.button_pantryPlus);
            pantryPlus.setOnClickListener(this);
            ingredient = itemView.findViewById(R.id.text_pantryIngredientName);
            measurementType = itemView.findViewById(R.id.pantry_item_measurement_Type);
            viewMoreTypes = itemView.findViewById(R.id.button_viewMoreTypes);
            this.listener = myClickListener;
            viewMoreTypes.setOnClickListener(this);
            nestedRV = itemView.findViewById(R.id.pantry_subItems);
            Layout = itemView.findViewById(R.id.pantry_linearLayout);
            infinite = itemView.findViewById(R.id.button_pantry_infinite);
            infinite.setOnClickListener(this);

            //add more listeners here
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.button_viewMoreTypes:
                    if(nestedRV.getVisibility() == View.VISIBLE){
                        viewMoreTypes.setImageResource(viewMoreClosed);
                        nestedRV.setVisibility(View.GONE);
                        isExpanded.set(getAdapterPosition(), false);
                    } else {
                        viewMoreTypes.setImageResource(viewMoreOpen);
                        nestedRV.setVisibility(View.VISIBLE);
                        isExpanded.set(getAdapterPosition(), true);
                        //Layout.setBackgroundColor(Color.parseColor("#f0f0f0"));
                    }
                    break;
                case R.id.button_pantryMinus:
                    if(allIngred.get(0).getQuantity() > 0) {
                        //quantity.setText(Integer.parseInt((String) quantity.getText())-1);
                        allIngred.get(0).setQuantity(allIngred.get(0).getQuantity()-1);
                        quantity.setText(String.valueOf(allIngred.get(0).getQuantity()));

                        //listener.pantryUpdateIngredient(this.getLayoutPosition(), 0, -1);
                    }
                    break;
                case R.id.button_pantryPlus:
                    //quantity.setText(Integer.parseInt((String) quantity.getText())+1);
                    allIngred.get(0).setQuantity(allIngred.get(0).getQuantity()+1);
                    quantity.setText(String.valueOf(allIngred.get(0).getQuantity()));
                    //listener.pantryUpdateIngredient(this.getLayoutPosition(),0, 1);
                    break;
                case R.id.button_pantry_infinite:
                    allIngred.get(0).setInfinitelyStocked(!allIngred.get(0).isInfinitelyStocked());
                    if(allIngred.get(0).isInfinitelyStocked()){
                        infinite.setImageResource(R.drawable.ic_iconmonstr_infinity_2);
                        pantryMinus.setVisibility(View.GONE);
                        pantryPlus.setVisibility(View.GONE);
                        quantity.setVisibility(View.GONE);
                    } else {
                        infinite.setImageResource(R.drawable.ic_iconmonstr_infinity_1);
                        pantryMinus.setVisibility(View.VISIBLE);
                        pantryPlus.setVisibility(View.VISIBLE);
                        quantity.setVisibility(View.VISIBLE);
                    }
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
                d = 0.0;
            }else d = Double.parseDouble(charSequence.toString());
            if(d < 0) d = 0.0;
            sameNameIngredients.get(getAdapterPosition()).getIngredientList().get(0).setQuantity(d);
            //Log.e("updated:", getAdapterPosition() + " " + sameNameIngredients.get(getAdapterPosition()).getIngredientList().get(0).getQuantity());
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }

    public interface PantryClickListener {
        void pantryDeleteIngredient(int layoutPosition);
        void pantryUpdateIngredient(int layoutPosition, int secondPosition, int incrementBy);
    }
}
