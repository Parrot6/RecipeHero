package com.example.testapp2;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jsoup.helper.HttpConnection;

import java.io.StringWriter;
import java.util.ArrayList;

public class EditCartFragment extends Fragment implements EditCartAdapter.CartClickListener  {

    RecyclerView rv;
    ImageButton but2;
    EditCartAdapter adapter;
    ArrayList<Recipe> cartData;
    ArrayList<SameNameIngredients> ingredientSummary = new ArrayList<>();
    TextView outputTextView;
    Button resetCart;
    EditCartAdapter.CartClickListener thisListener = this;
    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context mcontext) {
        super.onAttach(mcontext);
        context=mcontext;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the
        View view = inflater.inflate(R.layout.fragment_editcart, parent, false);

        cartData = MainActivity.getRecipes();

        adapter = new EditCartAdapter(context, cartData, this);
        rv = view.findViewById(R.id.recycleView_EditCart);
        rv.setLayoutManager(new LinearLayoutManager(context));
        rv.setAdapter(adapter);
        resetCart = view.findViewById(R.id.button_editcart_reset);
        resetCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(Recipe rec: cartData){
                    rec.setCartQuantity(0);
                    adapter.notifyDataSetChanged();
                }
            }
        });
        outputTextView = view.findViewById(R.id.text_RESULTTEST);
        outputTextView.setMovementMethod(new ScrollingMovementMethod());


        but2 = view.findViewById(R.id.button_EditCart2);
        but2.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(context, but2);
                MenuInflater infl = popupMenu.getMenuInflater();
                infl.inflate(R.menu.copy_print_export, popupMenu.getMenu());
                popupMenu.setGravity(Gravity.CENTER);
                popupMenu.setForceShowIcon(true);
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.clipboard:
                                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("label", printCart());
                                clipboard.setPrimaryClip(clip);
                                Toast.makeText(context,"Copied to Clipboard!",Toast.LENGTH_SHORT).show();
                                return true;
                            case R.id.print:
                                MainActivity.printToPrinter(context, "SHOPPING LIST", printCart().replace("\n", "<BR>").replace(")", " in pantry)"));
                                return true;
                            case R.id.export:
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("text/plain");
                                intent.putExtra("sms_body", "SHOPPING LIST\n" + printCart());
                                if (intent.resolveActivity(context.getPackageManager()) != null) {
                                    startActivity(intent);
                                }
                                return true;
                            default:
                                return false;
                        }
                    }
                });


            }
        });

        View sortBarHolder = view.findViewById(R.id.editcart_sortbarholder);
        MainActivity.SortBar editcartSortBar = new MainActivity.SortBar(sortBarHolder, context) {
            @Override
            void clicked(Recipe.RecipeType thisClick) {
                MainActivity.recipeSortPress(thisClick);
                adapter = new EditCartAdapter(context, cartData, thisListener);
                rv.setAdapter(adapter);
                updateSelectedImage();
            }
        };
        updateSummary();
        return view;
    }
    private void updateSummary(){
        makeSummaryFromCartData();
        outputTextView.setText(printCart());
    }
    private void makeSummaryFromCartData() {
        boolean Added = false;
        ingredientSummary = new ArrayList<>();
        for(int outer = 0; outer < cartData.size(); outer++) { //for EACH CART ITEM
            if(cartData.get(outer).getCartQuantity() != 0) {
                ArrayList<Ingredient> temp = cartData.get(outer).getIngredientsTimesCart();
                for (int i = 0; i < temp.size(); i++) { //for EACH INGREDIENT PER CART ITEM
                    Ingredient ingred = temp.get(i);
                    for (int k = 0; k < ingredientSummary.size(); k++) {
                        if (ingredientSummary.size() == 0) {
                            ingredientSummary.add(new SameNameIngredients(ingred));
                            Added = true;
                            break;
                        } else if (Added = ingredientSummary.get(k).handleNewIngredient(ingred)) {
                            break;
                        }
                    }
                    if (!Added) ingredientSummary.add(new SameNameIngredients(ingred));
                }
            }
        }
        Log.e("makeSummaryCartData", ingredientSummary.toString());
        }

    public String printCart(){
        StringWriter notInfinite = new StringWriter();
        StringWriter infiniteAddLast = new StringWriter();
        for(int i = 0; i < ingredientSummary.size(); i++){
            boolean addToEnd = false;
            StringWriter sm = new StringWriter();
            ArrayList<Ingredient> temp = new ArrayList<>();
            temp.addAll(ingredientSummary.get(i).getIngredientList());
            sm.write(ingredientSummary.get(i).getIngredName());
            StringBuffer sb = new StringBuffer();

            for(int h = 0; h < ingredientSummary.get(i).getIngredName().length() + 3; h++){sb.append(" ");} //make spaces equal to ingredient name length

            for(int k = 0; k < temp.size(); k++){
                if(k > 0) sm.write(sb.toString());
                double stocked = PantryFragment.checkIngredientStock(temp.get(k));
                String ingred = " " + temp.get(k).getQuantity() + " " + temp.get(k).getMeasurementType();
                if(stocked > 0) {
                    if(stocked == SameNameIngredients.INFINITE){
                        addToEnd = true;
                        sm.write(ingred + " (infinite)");
                    } else sm.write(ingred + " (have " + PantryFragment.checkIngredientStock(temp.get(k)) + " " + temp.get(k).getMeasurementType() + ")");
                } else sm.write(ingred);
                sm.write("\n");
            }
            if(addToEnd) infiniteAddLast.write(sm.toString());
            else notInfinite.write(sm.toString());
        }
        notInfinite.write(infiniteAddLast.toString());
        return notInfinite.toString();
    }

    @Override
    public void minusQuantity(int layoutPosition) {
        cartData.get(layoutPosition).decrementQuant();
        updateSummary();
    }

    @Override
    public void plusQuantity(int layoutPosition) {
        cartData.get(layoutPosition).incrementQuant();
        updateSummary();
    }

    @Override
    public void refresh() {
        updateSummary();
    }

}
