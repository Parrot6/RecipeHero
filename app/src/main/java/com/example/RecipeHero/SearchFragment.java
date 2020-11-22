package com.example.RecipeHero;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


import static java.nio.charset.StandardCharsets.*;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class  SearchFragment extends Fragment implements SearchAdapter.MyClickListener {

    private static final int REQUEST_CODE = 888;
    private final int MAXRESULTS = 25;
    private TextView searchTerm;
    private Button search;
    RecyclerView rv;
    SearchAdapter adapter;
    Context context;
    SearchAdapter.MyClickListener listener;
    ArrayList<ArrayList<String>> dataPack = new ArrayList<>();
    List<Boolean> isAdded = new ArrayList<>();
    List<Boolean> isViewed = new ArrayList<>();
    List<Bitmap> imgs = Collections.synchronizedList( new ArrayList<>() );
    List<Boolean> isVideoParsed = new ArrayList<>();
    List<Boolean> isRecipeParsed = new ArrayList<>();
    List<Recipe> recipesParsed = new ArrayList<>();
    Spinner sortBy;
    Spinner whatSite;
    ExecutorService threadPool = Executors.newFixedThreadPool(MAXRESULTS);
    int returnToPosition = 0;
    private static int currentSearchPage = 1;
    public ArrayList<String> sortByListAllRecipes = new ArrayList<>();
    public ArrayList<String> sortByListFoodNetwork = new ArrayList<>();
    public ArrayList<String> sortByListBigOven = new ArrayList<>();
    public ArrayList<String> sortByListKingArthur = new ArrayList<>();
    private ProgressBar progressBar;
    private CurrentState currentState = CurrentState.AllRecipes;
    public enum CurrentState{
        FoodNetwork, AllRecipes, BigOven
    }
    public SearchFragment() {
        reinitializeValsForAdapter();
        sortByListAllRecipes.addAll(Arrays.asList("Best Match","Popularity","Newest"));
        sortByListFoodNetwork.addAll(Arrays.asList("Best Match","Rating"));
        sortByListBigOven.addAll(Arrays.asList("Best Match","Rating","Quality"));
        sortByListKingArthur.addAll(Arrays.asList("Best Match","Rating","Quality"));
        // Required empty public constructor
    }
    public void reinitializeValsForAdapter(){
        dataPack.clear();
        imgs.clear();
        isAdded.clear();
        isViewed.clear();
        isRecipeParsed.clear();
        recipesParsed.clear();
        for (int i = 0; i < MAXRESULTS; i++) {
            imgs.add(null);
            recipesParsed.add(null);
        }
        for(int i = 0; i < MAXRESULTS; i++){
            isRecipeParsed.add(false);
            isAdded.add(false);
            isViewed.add(false);
        }
    }
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchFragment newInstance(String param1, String param2) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listener = this;
        context = getActivity();
        if (getArguments() != null) {

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        rv.setAdapter(new SearchAdapter(context, dataPack, imgs, listener, isAdded, isViewed, currentSearchPage));
    }

    public class SimpleImageArrayAdapter extends ArrayAdapter<Integer> {
        private Integer[] images;

        public SimpleImageArrayAdapter(Context context, Integer[] images) {
            super(context, android.R.layout.simple_spinner_item, images);
            this.images = images;
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getImageForPosition(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getImageForPosition(position);
        }

        private View getImageForPosition(int position) {
            ImageView imageView = new ImageView(getContext());
            imageView.setBackgroundResource(images[position]);
            imageView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return imageView;
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)  {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        searchTerm = view.findViewById(R.id.text_search_input);
        search = view.findViewById(R.id.button_search_search);
        rv = view.findViewById(R.id.rv_scraping_search);
        rv.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        progressBar = view.findViewById(R.id.progressBar_search);
        progressBar.setVisibility(View.INVISIBLE);
        sortBy = view.findViewById(R.id.spinner_search_sortby);
        final ArrayAdapter<String> adapterSpinnerAllRecipes = new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item, sortByListAllRecipes);
        final ArrayAdapter<String> adapterSpinnerFoodNetwork = new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item, sortByListFoodNetwork);
        final ArrayAdapter<String> adapterSpinnerBigOven = new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item, sortByListBigOven);
        adapterSpinnerBigOven.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterSpinnerAllRecipes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterSpinnerFoodNetwork.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortBy.setAdapter(adapterSpinnerAllRecipes);
        sortBy.setSelection(0);
        whatSite = view.findViewById(R.id.spinner_search_whatsite);
        SimpleImageArrayAdapter whatSiteAdapter = new SimpleImageArrayAdapter(context,
                new Integer[]{R.drawable.ic_allrecipes_icon, R.drawable.ic_food_network, R.drawable.ic_bigoven});
        whatSite.setAdapter(whatSiteAdapter);
        whatSite.setSelection(0);

        whatSite.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i == 0){
                    currentState = CurrentState.AllRecipes;
                    sortBy.setAdapter(adapterSpinnerAllRecipes);
                }
                if(i == 1) {
                    currentState = CurrentState.FoodNetwork;
                    sortBy.setAdapter(adapterSpinnerFoodNetwork);
                }
                if(i == 2){
                    currentState = CurrentState.BigOven;
                    sortBy.setAdapter(adapterSpinnerBigOven);
                }
                sortBy.setSelection(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        search.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {
                showLoadingBar();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        currentSearchPage = 1;
                        scrapeQuerySwitch(searchTerm.getText().toString());
                    }
                }).start();
            }
        });
        whatSite.performClick();
        return view;
    }

    private void showLoadingBar() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getActivity().findViewById(R.id.progressBar_search).setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.VISIBLE);
                    }
                });
            }
        }).start();
    }


    public void waitTilNoThreads(ExecutorService imageFetchExecutor){
        try {
            imageFetchExecutor.shutdown();
            imageFetchExecutor.awaitTermination(15, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
        }
        finally {
            if (!imageFetchExecutor.isTerminated()) {
            }
            imageFetchExecutor.shutdownNow();
        }
    }

    @Override
    public void onView(int layoutPosition) {
        Recipe rec = scrapeRecipeSwitch(dataPack.get(4).get(layoutPosition), dataPack.get(0).get(layoutPosition), imgs.get(layoutPosition), layoutPosition);
        parseVideoLink(layoutPosition);
        if(currentState == CurrentState.AllRecipes){
            rec.setVideoTutorial(dataPack.get(5).get(layoutPosition));
        } else rec.setVideoTutorial("");
        returnToPosition = layoutPosition;
        Intent intent = new Intent(getActivity(), DisplayRecipe.class);
        intent.putExtra("Recipe", rec);
        intent.putExtra("Index", 0);
        intent.putExtra("layoutPosition", layoutPosition);
        intent.setAction("Preview");
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    public void quickAdd(int layoutPosition) {
        threadPool.submit(new Runnable() {
            @Override
            public void run() {

                parseVideoLink(layoutPosition); //FINALIZE LINK (work delayed til know its needed)
                Recipe rec = scrapeRecipeSwitch(dataPack.get(4).get(layoutPosition), dataPack.get(0).get(layoutPosition), imgs.get(layoutPosition), layoutPosition);
                if(currentState == CurrentState.AllRecipes){
                    rec.setVideoTutorial(dataPack.get(5).get(layoutPosition));
                } else rec.setVideoTutorial("");
                MainActivity.addRecipe(0,rec, context);
            }});
    }

    @Override
    public void playVideo(int layoutPosition) {
        new Thread(() -> {
            if (!dataPack.get(5).get(layoutPosition).equals("") && !dataPack.get(5).get(layoutPosition).contains("player")) {
                Document videoPage = null; //loadRecipeVideoPage then make Video url
                try {
                    videoPage = Jsoup.connect(dataPack.get(5).get(layoutPosition)).timeout(10000).get();
                } catch (IOException e) {
                    Toast.makeText(context, "FAILED TO FIND VIDEO", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                Elements maybeLink = videoPage.getElementsByTag("video");
                Map<String, String> data = maybeLink.get(0).dataset();
                String account = data.get("account");
                String vidId = data.get("video-id");
                String player = data.get("player");
                String urlConstructor = "https://players.brightcove.net/" + account + "/" + player + "_default/index.html?videoId=" + vidId;
                dataPack.get(5).remove(layoutPosition);
                dataPack.get(5).add(layoutPosition, urlConstructor);
            }
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(dataPack.get(5).get(layoutPosition)));
            startActivity(browserIntent);
            //VideoPlayerHolder.setVisibility(View.VISIBLE);
            //videoPlayer.setSource(dataPack.get(5).get(layoutPosition));
        }).start();

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void nextPage(int incrementPageBy) {
            if(currentSearchPage + incrementPageBy <= 0) return;
            showLoadingBar();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //adapter = null;
                    currentSearchPage += incrementPageBy;
                    scrapeQuerySwitch(searchTerm.getText().toString(), currentSearchPage);

                }
            }).start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        rv.scrollToPosition(returnToPosition);
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            String action = (String) data.getExtras().get("Action");
            switch(action){
                case "Save":
                    isAdded.set(returnToPosition, true);
                    quickAdd(returnToPosition);
                    break;
                case "Delete":
                    //do nothing
                    break;
                default:
                    break;
            }


        }
    }

    private Recipe scrapeRecipeSwitch(String recipeUrl, String recipeName, Bitmap recPic, int layoutPosition){
        if(currentState == CurrentState.FoodNetwork) return scrapeRecipeFoodNetwork(recipeUrl, recipeName, recPic, layoutPosition);
        if(currentState == CurrentState.AllRecipes) return scrapeRecipeAllRecipes(recipeUrl, recipeName, recPic, layoutPosition);
        if(currentState == CurrentState.BigOven) return scrapeRecipeBigOven(recipeUrl, recipeName, recPic, layoutPosition);
        return null;
    }
    public static Recipe reloadScrapeRecipeSwitch(Context context, String recipeUrl, String recipeName, Bitmap recPic, String state){
        CurrentState currentState = CurrentState.valueOf(state);
        if(currentState == CurrentState.FoodNetwork) return getFoodNetworkRecipe(recipeUrl, recipeName, recPic, context, currentState);
        if(currentState == CurrentState.AllRecipes) return getAllRecipesRecipe(recipeUrl, recipeName, recPic, context, currentState);
        if(currentState == CurrentState.BigOven) return getBigOvenRecipe(recipeUrl, recipeName, recPic, context, currentState);
        Recipe rec = new Recipe(recipeName);
        rec.setIcon(recPic, context);
        rec.sourceSite = state;
        return rec;
    }
    private Recipe scrapeRecipeAllRecipes(String recipeUrl, String recipeName, Bitmap recPic, int layoutPosition) {
        if(isVideoParsed.get(layoutPosition)) return recipesParsed.get(layoutPosition);
        Recipe newRec = getAllRecipesRecipe(recipeUrl, recipeName, recPic, context, currentState);
        recipesParsed.set(layoutPosition, newRec);
        return newRec;
    }
    private static Recipe getAllRecipesRecipe(String recipeUrl, String recipeName, Bitmap recPic, Context context, CurrentState currentState) {
        Recipe newRec = new Recipe("shell");
        ArrayList<Ingredient> ingreds = new ArrayList<>();
        newRec.setIcon(recPic, context);
        newRec.setRecipeTitle(recipeName);
        newRec.sourceSite = currentState.toString();
        Thread nt = new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                try {
                    Document doc = Jsoup.connect(recipeUrl).get();
                    Elements ingredientsElements = doc.getElementsByClass("ingredients-item");
                    for (Element ingred :
                            ingredientsElements) {
                        String s = ingred.text();
                        s = s.replaceAll("\\p{Zs}+", " "); //remove special spaces
                        s = removeLiteralFractionsWithLeadingSpace(s); //1 1/3 -> 1.33
                        s = removeUnicodeFractions(s); //1/3 -> .33
                        s = s.replace(" .",".");

                        String[] parts = s.split(" ");
                        ArrayList<String> pieces = new ArrayList<>();
                        pieces.addAll(Arrays.asList(parts));
                        StringBuilder noteBuilder = new StringBuilder();
                        int dashmarker = -1;
                        for(int i = 0; i < pieces.size(); i++){
                            if(pieces.get(i).equals("-")) dashmarker = i;
                            else if(dashmarker != -1) noteBuilder.append(pieces.get(i)+" ");
                        }
                        if(dashmarker != -1){
                            for(int i = pieces.size() - 1; i >= dashmarker; i--){
                                pieces.remove(i);
                            }
                        }
                        s = s.split("-",2)[0];
                        /*String[] dashSplit = s.split("-", 2);
                        if(dashSplit.length == 2){
                            int removeThisMany = dashSplit[1].split(" ").length - 1;
                            noteBuilder.append(dashSplit[1].trim());
                            for(int i = removeThisMany; i > 0; i--) {
                                pieces.remove(pieces.size() - 1);
                            }
                            //String lastWordNoComma = pieces.get(pieces.size()-1).replace("-","");
                            //pieces.remove(pieces.size()-1);
                            //pieces.add(lastWordNoComma);
                            s = dashSplit[0];
                        }*/
                        String[] commaSplit = s.split(",", 2);
                        if(commaSplit.length == 2){
                            int removeThisMany = commaSplit[1].split(" ").length - 1;
                            noteBuilder.append(commaSplit[1].trim());
                            for(int i = removeThisMany; i > 0; i--) {
                                pieces.remove(pieces.size() - 1);
                            }
                            String lastWordNoComma = pieces.get(pieces.size()-1).replace(",","");
                            pieces.remove(pieces.size()-1);
                            pieces.add(lastWordNoComma);
                        }


                        Double quant;
                        try {
                            quant = Double.parseDouble(pieces.get(0));
                            pieces.remove(0);
                        } catch (NumberFormatException e){
                            quant = 0.0;
                        }

                        String note = moveParenthesizedToNote(pieces, noteBuilder);
                        String measureType = pieces.get(0);
                        pieces.remove(0);

                        noteBuilder = new StringBuilder();
                        for (int i = 0; i < pieces.size(); i++) {
                            noteBuilder.append(pieces.get(i) + " ");
                        }
                        String ingredientName = noteBuilder.toString().trim();
                        if(pieces.size() == 0){
                            ingredientName = measureType;
                            measureType = "whole";
                        }

                        Ingredient newIng = new Ingredient(ingredientName, quant, measureType, note);
                        ingreds.add(newIng);
                    }
                    newRec.setIngredients(ingreds);
                    Elements instructionElements = doc.select("div.section-body > div.paragraph");
                    StringBuilder instructs = new StringBuilder();
                    for (int i = 0; i < instructionElements.size(); i++) {
                        //ingreds.add(new Ingredient());
                        instructs.append(instructionElements.get(i).text());
                        if(i < ingredientsElements.size()-1) instructs.append("\n\n");
                        //Log.e("instructions", instructionElements.get(i).text());
                    }
                    newRec.setRecipeInstructions(instructs.toString());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });
        nt.start();
        while(nt.isAlive()){

        }
        return newRec;
    }


    private static String moveParenthesizedToNote(ArrayList<String> pieces, StringBuilder noteBuilder) {
        int firstparen = -1;
        int secondparen = -1;
        for (int i = 0; i < pieces.size(); i++) {
            boolean added = false;
            if (pieces.get(i).contains("(")) {
                firstparen = i;
                added = true;
                noteBuilder.append(pieces.get(i).replace("(", "")).append(" ");
            }
            if (pieces.get(i).contains(")")) {
                secondparen = i;
                if (firstparen == i) {
                    noteBuilder = new StringBuilder(noteBuilder.toString().replace(")", ""));
                } else {
                    added = true;
                    noteBuilder.append(pieces.get(i).replace(")", ""));
                    break;
                }
            } else if (firstparen >= 0 && secondparen == -1 && !added) {
                noteBuilder.append(pieces.get(i)).append(" ");
            }
        }

        if (firstparen != -1 && secondparen != -1) { //remove (parenthesis phase)
            for (int x = secondparen; x >= firstparen; x--) {
                pieces.remove(x);
            }
        }
        if(pieces.get(0).contains("-")) {
            noteBuilder.append("(" + pieces.get(0) + ")");
            pieces.remove(0);
        }

        return noteBuilder.toString();
    }

    private Recipe scrapeRecipeFoodNetwork(String recipeUrl, String recipeName, Bitmap recPic, int layoutPosition) {
        if(isVideoParsed.get(layoutPosition)) return recipesParsed.get(layoutPosition);
        Recipe newRec = getFoodNetworkRecipe(recipeUrl, recipeName, recPic, context, currentState);
        recipesParsed.set(layoutPosition, newRec);
        return newRec;
    }

    private static Recipe getFoodNetworkRecipe(String recipeUrl, String recipeName, Bitmap recPic, Context context, CurrentState currentState) {
        Recipe newRec = new Recipe("shell");
        newRec.setSourceUrl(recipeUrl);
        ArrayList<Ingredient> ingreds = new ArrayList<>();
        newRec.setIcon(recPic, context);
        newRec.setRecipeTitle(recipeName);
        newRec.sourceSite = currentState.toString();
        Thread nt = new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                try {
                    Document doc = Jsoup.connect(recipeUrl).get();
                    Elements ingredElements = doc.select("p.o-Ingredients__a-Ingredient");
                    //Log.e("ingredElements", String.valueOf(ingredElements.size()));
                    ingredElements.remove(0); //REMOVE "Deselect All" ingredient
                    for (Element ingr :
                            ingredElements) {
                        //Log.e("ingre", ingr.text());
                        String s = ingr.text();
                        s = removeUnicodeFractions(s);
                        s = s.replaceAll("\\p{Zs}+", " "); //remove special spaces
                        s = s.replace(" .",".");
                        s = removeWrittenOutQuantities(s);
                        s = removeLiteralFractionsWithLeadingSpace(s);
                        s = removeLiteralFractionsWithoutLeadingSpace(s);
                        String[] parts = s.split(" ");
                        ArrayList<String> pieces = new ArrayList<>();
                        pieces.addAll(Arrays.asList(parts));
                        StringBuilder noteBuilder = new StringBuilder();
                        String[] commaSplit = s.split(",", 2);
                        if(commaSplit.length == 2){
                            int removeThisMany = commaSplit[1].split(" ").length - 1;
                            noteBuilder.append(commaSplit[1].trim());
                            for(int i = removeThisMany; i > 0; i--) {
                                pieces.remove(pieces.size() - 1);
                            }
                            String lastWordNoComma = pieces.get(pieces.size()-1).replace(",","");
                            pieces.remove(pieces.size()-1);
                            pieces.add(lastWordNoComma);
                        }
                        if(pieces.get(1).contains("or")){ //2 or 3 type Ingredient
                            noteBuilder.append("("+ pieces.get(0) +" or " + pieces.get(2) +")");
                            pieces.remove(2); //3
                            pieces.remove(1); //or
                        } else if(pieces.get(1).contains("to")){
                            noteBuilder.append("("+ pieces.get(0) +" to " + pieces.get(2) +")");
                            pieces.remove(2); //3
                            pieces.remove(1); //or
                        }

                        Double quant;
                        try {
                            quant = Double.parseDouble(pieces.get(0));
                            pieces.remove(0);
                        } catch (NumberFormatException e){
                            quant = 0.0;
                        }
                        String note = moveParenthesizedToNote(pieces, noteBuilder);
                        String measureType = pieces.get(0);
                        char firstLetter = measureType.charAt(0);
                        if(String.valueOf(firstLetter).equals(String.valueOf(firstLetter).toLowerCase())) { //Probably means its a proper name, not a ingredient measurement
                            pieces.remove(0);
                        } else {
                            measureType = "n/a";
                        }
                        noteBuilder = new StringBuilder();
                        for (int i = 0; i < pieces.size(); i++) {
                            noteBuilder.append(pieces.get(i) + " ");
                        }
                        String ingredientName = noteBuilder.toString().trim();
                        if(pieces.size() == 0){ //if there were no words left for ingredient
                            ingredientName = measureType;
                            measureType = "whole";
                        }

                        Ingredient newIng = new Ingredient(ingredientName, quant, measureType, note);
                        //Log.e("STRUCT", newIng.toString());
                        ingreds.add(newIng);
                    }
                    newRec.setIngredients(ingreds);

                    Elements instrElements = doc.select("li.o-Method__m-Step");
                    StringBuilder instructs = new StringBuilder();
                    for (int i = 0; i < instrElements.size(); i++) {
                        instructs.append(instrElements.get(i).text());
                        if(i < instrElements.size()-1) instructs.append("\n\n");
                    }
                    newRec.setRecipeInstructions(instructs.toString());
                } catch (Exception e) {

                }
            }
        });
        nt.start();
        while(nt.isAlive()){

        }
        return newRec;
    }

    private Recipe scrapeRecipeBigOven(String recipeUrl, String recipeName, Bitmap recPic, int layoutPosition) {
        if(isVideoParsed.get(layoutPosition)) return recipesParsed.get(layoutPosition);
        Recipe newRec = getBigOvenRecipe(recipeUrl, recipeName, recPic, context, currentState);
        recipesParsed.set(layoutPosition, newRec);
        return newRec;
    }

    private static Recipe getBigOvenRecipe(String recipeUrl, String recipeName, Bitmap recPic, Context context, CurrentState currentState) {
        Recipe newRec = new Recipe("shell");
        newRec.setSourceUrl(recipeUrl);
        ArrayList<Ingredient> ingreds = new ArrayList<>();
        newRec.setIcon(recPic, context);
        newRec.setRecipeTitle(recipeName);
        newRec.sourceSite = currentState.toString();
        Thread nt = new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                try {
                    Document doc = Jsoup.connect(recipeUrl).get();
                    Elements ingredElements = doc.select("span.ingredient:not(.ingHeading)");
                    for (Element ingr :
                            ingredElements) {
                        String s = ingr.text();
                        s = removeUnicodeFractions(s);
                        s = s.replaceAll("\\p{Zs}+", " "); //remove special spaces
                        s = s.replace(" .",".");
                        s = removeWrittenOutQuantities(s);
                        s = removeLiteralFractionsWithLeadingSpace(s);
                        s = removeLiteralFractionsWithoutLeadingSpace(s);
                        String[] parts = s.split(" ");
                        ArrayList<String> pieces = new ArrayList<>();
                        pieces.addAll(Arrays.asList(parts));
                        StringBuilder noteBuilder = new StringBuilder();
                        String[] semicolonSplit = s.split(";", 2);
                        if(semicolonSplit.length == 2){
                            int removeThisMany = semicolonSplit[1].split(" ").length - 1;
                            noteBuilder.append(semicolonSplit[1].trim());
                            for(int i = removeThisMany; i > 0; i--) {
                                pieces.remove(pieces.size() - 1);
                            }
                            pieces.remove(pieces.size()-1); //remove standalone semicolon
                        }

                        Double quant;





                        try {
                            quant = Double.parseDouble(pieces.get(0));
                            pieces.remove(0);
                        } catch (NumberFormatException e){
                            quant = 0.0;
                        }

                        String note = moveParenthesizedToNote(pieces, noteBuilder);
                        String measureType = pieces.get(0).trim();
                        pieces.remove(0);
                        StringBuilder nameBuilder = new StringBuilder();
                        for (int i = 0; i < pieces.size(); i++) {
                            nameBuilder.append(pieces.get(i) + " ");
                        }
                        String ingredientName = nameBuilder.toString().trim();
                        if(pieces.size() == 0){ //if there were no words left for ingredient
                            ingredientName = measureType;
                            measureType = "whole";
                        }

                        Ingredient newIng = new Ingredient(ingredientName, quant, measureType, note);
                        //Log.e("STRUCT", newIng.toString());
                        ingreds.add(newIng);
                    }
                    newRec.setIngredients(ingreds);

                    Elements instrElements = doc.select("div#instr > p");
                    StringBuilder instructs = new StringBuilder();
                    for (int i = 0; i < instrElements.size(); i++) {
                        instructs.append(instrElements.get(i).text());
                        if(i < instrElements.size()-1) instructs.append("\n\n");
                    }
                    newRec.setRecipeInstructions(instructs.toString());
                } catch (Exception e) {

                }
            }
        });
        nt.start();
        while(nt.isAlive()){

        }
        return newRec;
    }

    private static String removeLiteralFractionsWithoutLeadingSpace(String s) {
        DecimalFormat df = new DecimalFormat(".##");
        s = s.replace("1/2",".5").replace("1/4",".25").replace("3/4",".75").replace("1/7", df.format(1.0/7.0))
                .replace("1/9", df.format(1.0/9.0))
                .replace("1/10", df.format(1.0/10.0))
                .replace("1/3", df.format(.33))
                .replace("2/3", df.format(.66))
                .replace("2/5", df.format(2.0/5.0))
                .replace("1/5", df.format(1.0/5.0))
                .replace("2/5", df.format(2.0/5.0))
                .replace("3/5", df.format(3.0/5.0))
                .replace("4/5", df.format(4.0/5.0))
                .replace("1/6", df.format(1.0/6.0))
                .replace("5/6", df.format(5.0/6.0))
                .replace("1/8", df.format(1.0/8.0))
                .replace("3/8", df.format(3.0/8.0))
                .replace("5/8", df.format(5.0/8.0))
                .replace("7/8", df.format(7.0/8.0));
        return s;
    }
    private static String removeLiteralFractionsWithLeadingSpace(String s) {
        DecimalFormat df = new DecimalFormat(".##");
        s = s.replace(" 1/2",".5").replace(" 1/4",".25").replace(" 3/4", ".75").replace(" 1/7", df.format(1.0/7.0))
                .replace(" 1/9", df.format(1.0/9.0))
                .replace(" 1/10", df.format(1.0/10.0))
                .replace(" 1/3", df.format(.33))
                .replace(" 2/3", df.format(.66))
                .replace(" 2/5", df.format(2.0/5.0))
                .replace(" 1/5", df.format(1.0/5.0))
                .replace(" 2/5", df.format(2.0/5.0))
                .replace(" 3/5", df.format(3.0/5.0))
                .replace(" 4/5", df.format(4.0/5.0))
                .replace(" 1/6", df.format(1.0/6.0))
                .replace(" 5/6", df.format(5.0/6.0))
                .replace(" 1/8", df.format(1.0/8.0))
                .replace(" 3/8", df.format(3.0/8.0))
                .replace(" 5/8", df.format(5.0/8.0))
                .replace(" 7/8", df.format(7.0/8.0));
        return s;
    }
    private static String removeUnicodeFractions(String s) {
        DecimalFormat df = new DecimalFormat(".##");
        s = s.replace("¼", ".25")
                .replace("½", ".5")
                .replace("¾", ".75")
                .replace("⅐", String.valueOf(df.format(1.0/7.0)))
                .replace("⅑", String.valueOf(df.format(1.0/9.0)))
                .replace("⅒", String.valueOf(df.format(1.0/10.0)))
                .replace("⅓", ".33")
                .replace("⅔", String.valueOf(df.format(2.0/3.0)))
                .replace("⅕", String.valueOf(df.format(1.0/5.0)))
                .replace("⅖", String.valueOf(df.format(2.0/5.0)))
                .replace("⅗", String.valueOf(df.format(3.0/5.0)))
                .replace("⅘", String.valueOf(df.format(4.0/5.0)))
                .replace("⅙", String.valueOf(df.format(1.0/6.0)))
                .replace("⅚", String.valueOf(df.format(5.0/6.0)))
                .replace("⅛", String.valueOf(df.format(1.0/8.0)))
                .replace("⅜", String.valueOf(df.format(3.0/8.0)))
                .replace("⅝", String.valueOf(df.format(5.0/8.0)))
                .replace("⅞", String.valueOf(df.format(7.0/8.0)));
        return s;
    }
    private static String removeWrittenOutQuantities(String s) {
        s = s.replace("One", "1")
                .replace("Two", "2")
                .replace("Three", "3").replace("Four", "4").replace("Five", "5").replace("Six", "6");
        return s;
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void scrapeQuerySwitch(String s, int pageNum){
        if(currentState == CurrentState.FoodNetwork) scrapeQueryFoodNetwork(s, pageNum);
        if(currentState == CurrentState.AllRecipes) scrapeQueryAllRecipes(s, pageNum);
        if(currentState == CurrentState.BigOven) scrapeQueryBigOven(s, pageNum);
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void scrapeQuerySwitch(String s){
        if(currentState == CurrentState.FoodNetwork) scrapeQueryFoodNetwork(s, 1);
        if(currentState == CurrentState.AllRecipes) scrapeQueryAllRecipes(s, 1);
        if(currentState == CurrentState.BigOven) scrapeQueryBigOven(s, 1);
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void scrapeQueryAllRecipes(String s, int pageNum) {
        long startTime = System.nanoTime();
        long imageDurationTimer = 0;
        ExecutorService imageFetchExecutor = Executors.newFixedThreadPool(20);
        ArrayList<String> imgurls = new ArrayList<String>();
        ArrayList<String> ratings = new ArrayList<String>();
        ArrayList<String> ratingVotes = new ArrayList<String>();
        ArrayList<String> titles = new ArrayList<String>();
        ArrayList<String> descriptions = new ArrayList<String>();
        ArrayList<String> links = new ArrayList<String>();
        ArrayList<String> videoLinks = new ArrayList<>();

        reinitializeValsForAdapter();
        Document doc = new Document(null);
        long startJsoupConnect = System.nanoTime();
        //Log.e("startJsoupConnect", String.valueOf((System.nanoTime() - startJsoupConnect) / 1000000));
        try {
            String searchType = "";
            if (sortBy.getSelectedItem().toString().equals(sortByListAllRecipes.get(0)))
                searchType = "&sort=re";
            if (sortBy.getSelectedItem().toString().equals(sortByListAllRecipes.get(1)))
                searchType = "&sort=p";
            if (sortBy.getSelectedItem().toString().equals(sortByListAllRecipes.get(2)))
                searchType = "&sort=n";
            String searchPageText = "&page=" + pageNum;
            //Log.e("ATTEMPTING", "https://www.allrecipes.com/search/results/?wt=" + URLEncoder.encode(s.trim(), UTF_8.toString()) + searchType+searchPageText);
            doc = Jsoup.connect("https://www.allrecipes.com/search/results/?wt=" + URLEncoder.encode(s.trim(), UTF_8.toString()) + searchType+searchPageText).get();
            //Log.e("connected", String.valueOf((System.nanoTime() - startJsoupConnect) / 1000000));
        } catch(UnsupportedEncodingException e){
            e.printStackTrace();
        } catch(IOException e){
            e.printStackTrace();
        }
        Elements recipeBoxes = doc.select("div.grid-card-image-container");
        if (recipeBoxes.size() != 0) {
            initiallyFillArrays(imgurls, ratings, ratingVotes, titles, descriptions, links, videoLinks, recipeBoxes);
            Elements titleElements = doc.getElementsByClass("fixed-recipe-card__h3");
            Elements linkElements = doc.getElementsByClass("grid-card-image-container");
            Elements ratingElements = doc.getElementsByClass("stars");
            Elements Votes = doc.getElementsByClass("fixed-recipe-card__ratings");
            Elements descriptionElements = doc.getElementsByClass("fixed-recipe-card__description");
            long bindData = System.nanoTime();
            for(int i = 0; i < ratingElements.size(); i++) {
                    ratings.set(i, ratingElements.get(i).dataset().get("ratingstars"));
            }
            for(int i = 0; i < linkElements.size(); i++) {
                links.set(i,linkElements.get(i).child(0).attr("href"));
            }
            for(int i = 0; i < recipeBoxes.size(); i++) {
                //GET VIDEO LINKS AT SAME TIME
                if (recipeBoxes.get(i).children().size() == 2) {
                    String recipeVideoPage = recipeBoxes.get(i).child(1).attr("href");
                    if (recipeVideoPage.contains("/0/")) { //recipes with no video attached
                        videoLinks.set(i,"");
                    } else {
                        videoLinks.set(i,recipeVideoPage);
                    }
                } else videoLinks.add("");
                //GET IMAGE
                Element link = recipeBoxes.get(i).child(0);
                Element pic = link.child(0);
                String url = pic.dataset().get("original-src");
                imgurls.set(i, url);
            }
            for(int i = 0; i < titleElements.size(); i++) {
                titles.set(i, titleElements.get(i).text());
            }
            for(int i = 0; i < descriptionElements.size(); i++) {
                descriptions.set(i, descriptionElements.get(i).text());
            }
            for(int i = 0; i < Votes.size(); i++) {
                Element temp = Votes.get(i).child(1);
                String deeper = temp.child(0).attr("number");
                ratingVotes.set(i, deeper);
            }

            imageDurationTimer = System.nanoTime();
            for (int i = 0; i < imgurls.size(); i++) {
                int finalI = i;
                imageFetchExecutor.submit(new Runnable() {
                    @Override
                    public void run() {
                        String name = Thread.currentThread().getName();
                        try {
                            URL url = new URL(imgurls.get(finalI));
                            HttpURLConnection con = (HttpURLConnection) new URL(imgurls.get(finalI)).openConnection();
                            con.setConnectTimeout(3000);
                            con.setReadTimeout(3000);
                            Bitmap bm = BitmapFactory.decodeStream(con.getInputStream());
                            imgs.set(finalI, bm);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

            }
            dataPack.add(titles);
            dataPack.add(descriptions);
            dataPack.add(ratings);
            dataPack.add(ratingVotes);
            dataPack.add(links);
            dataPack.add(videoLinks);
            waitTilNoThreads(imageFetchExecutor);
        } else {
            titles.add("NO RESULTS");
            descriptions.add("Try searching something else!");
            dataPack.add(titles);
            dataPack.add(descriptions);
            dataPack.add(ratings);
            dataPack.add(ratingVotes);
            dataPack.add(links);
            dataPack.add(videoLinks);
        }
        finalizeQueryScrape(videoLinks);
    };
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void scrapeQueryFoodNetwork(String s, int pageNum) {
        long startTime = System.nanoTime();
        long imageDurationTimer = 0;
        ExecutorService imageFetchExecutor = Executors.newFixedThreadPool(20);
        ArrayList<String> imgurls = new ArrayList<String>();
        ArrayList<String> ratings = new ArrayList<String>();
        ArrayList<String> ratingVotes = new ArrayList<String>();
        ArrayList<String> titles = new ArrayList<String>();
        ArrayList<String> descriptions = new ArrayList<String>();
        ArrayList<String> links = new ArrayList<String>();
        ArrayList<String> videoLinks = new ArrayList<>();

        reinitializeValsForAdapter();
        Document doc = new Document(null);
        long startJsoupConnect = System.nanoTime();
        try {
            String searchType = "";
            if (sortBy.getSelectedItem().toString().equals(sortByListFoodNetwork.get(0)))
                searchType = "";
            if (sortBy.getSelectedItem().toString().equals(sortByListFoodNetwork.get(1)))
                searchType = "/rating";
            String pageNumCode = "/p/"+pageNum;
            if(pageNum == 1) pageNumCode = "";
            doc = Jsoup.connect("https://www.foodnetwork.com/search/" + URLEncoder.encode(s.trim(), UTF_8.toString()) + "-" + pageNumCode + searchType).get();
        } catch(UnsupportedEncodingException e){
            e.printStackTrace();
        } catch(IOException e){
            e.printStackTrace();
        }
        Elements recipeBoxes = doc.getElementsByClass("o-RecipeResult");
        //Log.e("recipeBoxes", String.valueOf(recipeBoxes.size()));
        if (recipeBoxes.size() != 0) {
            Elements titleElements = recipeBoxes.select("h3");
            //Log.e("titleElements", String.valueOf(titleElements.size()));

            Elements imgElements = recipeBoxes.select("img");
            //Log.e("imgElements", String.valueOf(imgElements.size()));
            Elements linkElements = recipeBoxes.select("div.m-MediaBlock__m-Rating > a");
            //Log.e("linkElements", String.valueOf(linkElements.size()));
            Elements ratingElements = recipeBoxes.select("span.gig-rating-stars");
            //Log.e("ratingElements", String.valueOf(ratingElements.size()));
            Elements votes = recipeBoxes.select("span.gig-rating-ratingsum");
            //Log.e("votes", String.valueOf(votes.size()));
            Elements descriptionElements = recipeBoxes.select("section.o-RecipeInfo__o-Time");
            Elements descriptionElementsAuthor = recipeBoxes.select("span.m-Info__a-AssetInfo");
            //Log.e("ElementsAuthor", String.valueOf(descriptionElementsAuthor.size()));
            long bindData = System.nanoTime();
            initiallyFillArrays(imgurls, ratings, ratingVotes, titles, descriptions, links, videoLinks, recipeBoxes);
            for(int i = 0; i < ratingElements.size(); i++){
                String[] parts = ratingElements.get(i).attr("title").split(" ", 4);
                double d = Double.parseDouble(parts[0]); // 5 of 5 stars
                ratings.set(i,String.valueOf(d));
                //Log.e("rating:", String.valueOf(d));
            }
            for(int i = 0; i < votes.size(); i++){
                String[] parts = votes.get(i).text().split(" ",2);
                ratingVotes.set(i, parts[0]);
                //Log.e("votes:", parts[0]);
            }
            for(int i = 0; i < linkElements.size(); i++) {
                    String url = "https:" + linkElements.get(i).attr("href");
                    links.set(i, url);
            }
            for(int i = 0; i < imgElements.size(); i++) {
                    //GET IMAGE
                    String url = "https:" + imgElements.get(i).attr("src");
                    imgurls.set(i,url);
                    //Log.e("pic:", url);
            }
            for(int i = 0; i < titleElements.size(); i++) {
                    titles.set(i, titleElements.get(i).text());
            }
            for(int i = 0; i < descriptionElements.size(); i++) {
                StringBuilder totalDescrip = new StringBuilder();
                totalDescrip.append(descriptionElements.get(i).text() + "\n");
                if(i < descriptionElementsAuthor.size()) totalDescrip.append(descriptionElementsAuthor.get(i).text());
                descriptions.set(i,totalDescrip.toString());
            }



            imageDurationTimer = System.nanoTime();
            for (int i = 0; i < imgurls.size(); i++) {
                int finalI = i;
                imageFetchExecutor.submit(new Runnable() {
                    @Override
                    public void run() {
                        String name = Thread.currentThread().getName();
                        try {
                            URL url = new URL(imgurls.get(finalI));
                            HttpURLConnection con = (HttpURLConnection) new URL(imgurls.get(finalI)).openConnection();
                            con.setConnectTimeout(3000);
                            con.setReadTimeout(3000);
                            Bitmap bm = BitmapFactory.decodeStream(con.getInputStream());
                            imgs.set(finalI, bm);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            dataPack.add(titles);
            dataPack.add(descriptions);
            dataPack.add(ratings);
            dataPack.add(ratingVotes);
            dataPack.add(links);
            dataPack.add(videoLinks);
            waitTilNoThreads(imageFetchExecutor);
        } else {
            titles.add("NO RESULTS");
            descriptions.add("Try searching something else!");
            dataPack.add(titles);
            dataPack.add(descriptions);
            dataPack.add(ratings);
            dataPack.add(ratingVotes);
            dataPack.add(links);
            dataPack.add(videoLinks);
        }
        finalizeQueryScrape(videoLinks);
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void scrapeQueryBigOven(String s, int pageNum) {
        long startTime = System.nanoTime();
        long imageDurationTimer = 0;
        ExecutorService imageFetchExecutor = Executors.newFixedThreadPool(20);
        ArrayList<String> imgurls = new ArrayList<String>();
        ArrayList<String> ratings = new ArrayList<String>();
        ArrayList<String> ratingVotes = new ArrayList<String>();
        ArrayList<String> titles = new ArrayList<String>();
        ArrayList<String> descriptions = new ArrayList<String>();
        ArrayList<String> links = new ArrayList<String>();
        ArrayList<String> videoLinks = new ArrayList<>();

        reinitializeValsForAdapter();
        Document doc = new Document(null);
        long startJsoupConnect = System.nanoTime();
        try {
            String searchType = "";
            if (sortBy.getSelectedItem().toString().equals(sortByListBigOven.get(1)))
                searchType = "&sort=rating";
            if (sortBy.getSelectedItem().toString().equals(sortByListBigOven.get(2)))
                searchType = "&sort=quality";
            String pageNumCode = "/page/"+pageNum;
            if(pageNum == 1) pageNumCode = "";

            if(!searchType.equals(""))doc = Jsoup.connect("https://www.bigoven.com/recipes/search" + pageNumCode + "?any_kw=" + URLEncoder.encode(s.trim(), UTF_8.toString()) + searchType).get();
            else doc = Jsoup.connect("https://www.bigoven.com/recipes/" + URLEncoder.encode(s.trim(), UTF_8.toString()) + "/best" + pageNumCode).get(); //default search
        } catch(UnsupportedEncodingException e){
            e.printStackTrace();
        } catch(IOException e){
            e.printStackTrace();
        }

        Elements recipeBoxes = doc.getElementsByClass("recipe-tile-full");
        //Log.e("recipeBoxes", String.valueOf(recipeBoxes.size()));
        if (recipeBoxes.size() != 0) {
            Elements titleElements = recipeBoxes.select("li.list-group-recipetile-1");
            //Log.e("titleElements", String.valueOf(titleElements.size()));

            Elements imgElements = recipeBoxes.select("img");
            //Log.e("imgElements", String.valueOf(imgElements.size()));

            Elements linkElements = recipeBoxes.select("div.panel-body > a:nth-child(1)");
            //Log.e("linkElements", String.valueOf(linkElements.size()));

            Elements ratingElements = recipeBoxes.select("div.icons-2");
            //Log.e("ratingElements", String.valueOf(ratingElements.size()));

            Elements votes = recipeBoxes.select("span.rating-counter");
            //Log.e("votes", String.valueOf(votes.size()));
            //Elements descriptionElements = recipeBoxes.select("section.o-RecipeInfo__o-Time");
            //Elements descriptionElementsAuthor = recipeBoxes.select("span.m-Info__a-AssetInfo");
            //Log.e("ElementsAuthor", String.valueOf(descriptionElementsAuthor.size()));
            long bindData = System.nanoTime();
            initiallyFillArrays(imgurls, ratings, ratingVotes, titles, descriptions, links, videoLinks, recipeBoxes);
            for(int i = 0; i < ratingElements.size(); i++){
                Elements fullStars = ratingElements.get(i).select("i.fa.fa-star");
                Elements halfStars = ratingElements.get(i).select("i.fa.fa-star-half");
                double d = fullStars.size() + halfStars.size()/2.0;
                ratings.set(i,String.valueOf(d));
                //Log.e("rating:", String.valueOf(d));
            }
            for(int i = 0; i < votes.size(); i++){
                String numVotes = votes.get(i).text();
                ratingVotes.set(i, numVotes);
                //Log.e("votes:", parts[0]);
            }
            for(int i = 0; i < linkElements.size(); i++) {
                String url = linkElements.get(i).attr("href");
                links.set(i, url);
            }
            for(int i = 0; i < imgElements.size(); i++) {
                //GET IMAGE
                String url = imgElements.get(i).attr("src");
                imgurls.set(i,url);
                //Log.e("pic:", url);
            }
            for(int i = 0; i < titleElements.size(); i++) {
                titles.set(i, titleElements.get(i).text());
            }
            for(int i = 0; i < recipeBoxes.size(); i++) {
                try {
                    String str = recipeBoxes.get(i).getElementsByClass("upvote-counter").first().text();
                    descriptions.set(i, recipeBoxes.get(i).dataset().get("servings") + " servings, " + str + " likes");
                } catch (NullPointerException e){
                    descriptions.set(i, recipeBoxes.get(i).dataset().get("servings") + " servings, " + "no likes");
                }

            }
            imageDurationTimer = System.nanoTime();
            for (int i = 0; i < imgurls.size(); i++) {
                int finalI = i;
                imageFetchExecutor.submit(new Runnable() {
                    @Override
                    public void run() {
                        String name = Thread.currentThread().getName();
                        try {
                            URL url = new URL(imgurls.get(finalI));
                            HttpURLConnection con = (HttpURLConnection) new URL(imgurls.get(finalI)).openConnection();
                            con.setConnectTimeout(3000);
                            con.setReadTimeout(3000);
                            Bitmap bm = BitmapFactory.decodeStream(con.getInputStream());
                            imgs.set(finalI, bm);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            dataPack.add(titles);
            dataPack.add(descriptions);
            dataPack.add(ratings);
            dataPack.add(ratingVotes);
            dataPack.add(links);
            dataPack.add(videoLinks);
            waitTilNoThreads(imageFetchExecutor);
        } else {
            titles.add("NO RESULTS");
            descriptions.add("Try searching something else!");
            dataPack.add(titles);
            dataPack.add(descriptions);
            dataPack.add(ratings);
            dataPack.add(ratingVotes);
            dataPack.add(links);
            dataPack.add(videoLinks);
        }
        finalizeQueryScrape(videoLinks);
    }


    private void finalizeQueryScrape(ArrayList<String> videoLinks){
        updateUIafterScrape();
        startVideoLoading(videoLinks);
        if(dataPack.size() > 0) startRecipesParsing(dataPack.get(4));
    }
    private void startRecipesParsing(ArrayList<String> links) {
        for(int i = 0; i < links.size(); i++) {
            if(dataPack.size() != 0) try {
                scrapeRecipeSwitch(dataPack.get(4).get(i), dataPack.get(0).get(i), imgs.get(i), i);
            } catch (IndexOutOfBoundsException e){
            }
        }
    }

    private void initiallyFillArrays(ArrayList<String> imgurls, ArrayList<String> ratings, ArrayList<String> ratingVotes, ArrayList<String> titles, ArrayList<String> descriptions, ArrayList<String> links, ArrayList<String> videoLinks, Elements recipeBoxes) {
        isVideoParsed.clear();
        for(int i = 0; i < recipeBoxes.size(); i++){
            isVideoParsed.add(false);
            titles.add("No Title");
            imgurls.add("");
            descriptions.add("No description");
            links.add("");
            videoLinks.add("");
            ratings.add("0");
            ratingVotes.add("0");
        }
    }

    private void startVideoLoading(ArrayList<String> videoLinks) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < videoLinks.size(); i++) {
                    if(dataPack.size() != 0) parseVideoLink(i);
                }
            }
        }).start();
    }
    public void parseVideoLink(int layoutPosition) throws IndexOutOfBoundsException{
        if (dataPack.get(5).size() > layoutPosition - 1 && !dataPack.get(5).get(layoutPosition).equals("") && isVideoParsed.get(layoutPosition)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Thread nt = new Thread(() -> {
                        Document videoPage = null; //loadRecipeVideoPage then make Video url
                        try {
                            if(dataPack.size() != 0) videoPage = Jsoup.connect(dataPack.get(5).get(layoutPosition)).timeout(10000).get();
                            Elements maybeLink = videoPage.getElementsByTag("video");
                            Map<String, String> data = maybeLink.get(0).dataset();
                            String account = data.get("account");
                            String vidId = data.get("video-id");
                            String player = data.get("player");
                            String urlConstructor = "https://players.brightcove.net/" + account + "/" + player + "_default/index.html?videoId=" + vidId;
                            if(dataPack.size() != 0) dataPack.get(5).set(layoutPosition, urlConstructor);
                        } catch (IOException e) {
                            e.printStackTrace();
                            return;
                        }
                        isVideoParsed.set(layoutPosition, true);
                    });
                    nt.start();
                    try {
                        nt.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }).start();
        }
    }

    private void updateUIafterScrape() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        adapter = new SearchAdapter(context, dataPack, imgs, listener, isAdded, isViewed, currentSearchPage);
                        rv.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }


}