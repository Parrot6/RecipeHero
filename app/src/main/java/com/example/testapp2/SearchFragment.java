package com.example.testapp2;

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
import android.view.Gravity;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
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
public class  SearchFragment extends Fragment implements SearchAdapter.MyClickListener, SearchAdapterTasty.MyClickListener {

    private static final int REQUEST_CODE = 888;

    private TextView searchTerm;
    private Button search;
    RecyclerView rv;
    SearchAdapter adapter;
    Context context;
    SearchAdapter.MyClickListener listener;
    SearchAdapterTasty.MyClickListener listenerTasty;
    ArrayList<ArrayList<String>> dataPack = new ArrayList<>();
    List<Bitmap> imgs = Collections.synchronizedList( new ArrayList<>() );
    ImageView openRecipesAddButton = null;
    List<Boolean> isAdded;
    Spinner sortBy;
    Spinner whatSite;
    public ArrayList<String> sortByListAllRecipes = new ArrayList<>();
    public ArrayList<String> sortByListFoodNetwork = new ArrayList<>();
    private ProgressBar progressBar;

    public SearchFragment() {
        initImgs();
        sortByListAllRecipes.addAll(Arrays.asList("Best Match","Popularity","Newest"));
        sortByListFoodNetwork.addAll(Arrays.asList("Best Match","Rating"));
        // Required empty public constructor
    }
    public void initImgs(){
        imgs.clear();
        for (int i = 0; i < 20; i++) {
            imgs.add(null);
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
        listenerTasty = this;
        context = getActivity();
        if (getArguments() != null) {

        }
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
            super.getDropDownView(position, convertView, parent);
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
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        searchTerm = view.findViewById(R.id.text_search_input);
        search = view.findViewById(R.id.button_search_search);
        rv = view.findViewById(R.id.rv_scraping_search);
        rv.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        progressBar = view.findViewById(R.id.progressBar_search);
        progressBar.setVisibility(View.INVISIBLE);
        sortBy = view.findViewById(R.id.spinner_search_sortby);;
        final ArrayAdapter<String> adapterSpinnerAllRecipes = new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item, sortByListAllRecipes);
        final ArrayAdapter<String> adapterSpinnerFoodNetwork = new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item, sortByListFoodNetwork);
        adapterSpinnerAllRecipes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterSpinnerFoodNetwork.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortBy.setAdapter(adapterSpinnerAllRecipes);
        sortBy.setSelection(0);
        whatSite = view.findViewById(R.id.spinner_search_whatsite);
        SimpleImageArrayAdapter whatSiteAdapter = new SimpleImageArrayAdapter(context,
                new Integer[]{R.drawable.ic_allrecipes_icon, R.drawable.ic_food_network});
        whatSite.setAdapter(whatSiteAdapter);
        whatSite.setSelection(0);
        whatSite.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i == 0) sortBy.setAdapter(adapterSpinnerAllRecipes);
                if(i == 1) sortBy.setAdapter(adapterSpinnerFoodNetwork);
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
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        if(whatSite.getSelectedItemPosition() == 0){
                            scrapeQuery(searchTerm.getText().toString());
                        }
                        if(whatSite.getSelectedItemPosition() == 1){
                            scrapeQueryFoodNetwork(searchTerm.getText().toString());
                        }
                        //scrapeQuery(searchTerm.getText().toString());
                        //scrapeQueryTasty(searchTerm.getText().toString());
                        //scrapeQueryFoodNetwork(searchTerm.getText().toString());
                    }
                }).start();
            }
        });

        return view;
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void scrapeQueryTasty(String s) {
        long startTime = System.nanoTime();
        long imageDurationTimer = 0;
        ExecutorService imageFetchExecutor = Executors.newFixedThreadPool(20);
        ArrayList<String> imgurls = new ArrayList<String>();
        //ArrayList<String> ratings = new ArrayList<String>();
        //ArrayList<String> ratingVotes = new ArrayList<String>();
        ArrayList<String> titles = new ArrayList<String>();
        //ArrayList<String> descriptions = new ArrayList<String>();
        ArrayList<String> links = new ArrayList<String>();
        //ArrayList<String> videoLinks = new ArrayList<>();
        dataPack.clear();
        initImgs();
        Document doc = new Document(null);
        long startJsoupConnect = System.nanoTime();
        Log.e("startJsoupConnect", String.valueOf((System.nanoTime() - startJsoupConnect) / 1000000));
        try {
/*            String searchType = "";
            if (sortBy.getSelectedItem().toString().equals(sortByList.get(0)))
                searchType = "&sort=re";
            if (sortBy.getSelectedItem().toString().equals(sortByList.get(1)))
                searchType = "&sort=p";
            if (sortBy.getSelectedItem().toString().equals(sortByList.get(2)))
                searchType = "&sort=n";*/

            doc = Jsoup.connect("https://tasty.co/search?q=" + URLEncoder.encode(s, UTF_8.toString())).get();
            Log.e("connected", String.valueOf((System.nanoTime() - startJsoupConnect) / 1000000));
        } catch(UnsupportedEncodingException e){
            e.printStackTrace();
        } catch(IOException e){
            e.printStackTrace();
        }

        Elements linkElements = doc.select("#search-results-feed > div.feed__container > section > div > a");
        Log.e("linkElements", String.valueOf(linkElements.size()));
        //#search-results-feed > div:nth-child(2) > section > div > a:nth-child(1) > div.feed-item__img-wrapper > div > picture > img

        if (linkElements.size() != 0) {
            Elements titleElements = doc.select("div.feed-item__title");
            //#search-results-feed > div:nth-child(2) > section > div > a:nth-child(1) > div.feed-item__title
            Log.e("titleElements:", String.valueOf(titleElements.size()));

            long bindData = System.nanoTime();
            Elements imgElements = new Elements();
            for (Element link : linkElements
            ) {
                //get images
                imgurls.add(link.getElementsByTag("img").get(0).attr("src"));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Log.e("imgURLS", String.valueOf(link.getElementsByTag("img").get(0).attr("srcset")));
                }
                //get links
                links.add(link.attr("href"));
                Log.e("urlToRec",link.attr("href"));
            }
            Log.e("imgURLS", String.valueOf(imgurls.size()));

            for (Element title : titleElements
            ) {
                titles.add(title.text());
                Log.e("titles", title.text());
            }
/*            for (Element desc : descriptionElements
            ) {
                descriptions.add(desc.text());
            }
            for (Element vote : Votes
            ) {
                Element temp = vote.child(1);
                String deeper = temp.child(0).attr("number");
                ratingVotes.add(deeper);
            }*/


            imageDurationTimer = System.nanoTime();
            for (int i = 0; i < imgurls.size(); i++) {
                int finalI = i;
                imageFetchExecutor.submit(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
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
                            Log.e("malformedURL", String.valueOf(Base64.getDecoder().decode(imgurls.get(finalI))));
                            e.printStackTrace();
                        }
                    }
                });

            }



            dataPack.add(titles);
            //dataPack.add(descriptions);
            //dataPack.add(ratings);
            //dataPack.add(ratingVotes);
            dataPack.add(links);
            //dataPack.add(videoLinks);

            waitTilNoThreads(imageFetchExecutor);
        } else {
            titles.add("NO RESULTS");
            dataPack.add(titles);
           // dataPack.add(descriptions);
            //dataPack.add(ratings);
           // dataPack.add(ratingVotes);
            dataPack.add(links);
        }
        Log.e("picsAfter", String.valueOf((System.nanoTime() - imageDurationTimer)/1000000));
        new Thread(new Runnable() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        SearchAdapterTasty adapter = new SearchAdapterTasty(context, dataPack, imgs, listenerTasty);
                        rv.setAdapter(adapter);
                        //adapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
        Log.e("totalTime", String.valueOf((System.nanoTime() - startTime)/1000000));
/*        new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i < videoLinks.size(); i++){
                    parseVideoLink(i);
                }
            }
        }).start();*/
    };

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void scrapeQuery(String s) {
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
        dataPack.clear();
        initImgs();
        Document doc = new Document(null);
        long startJsoupConnect = System.nanoTime();
        Log.e("startJsoupConnect", String.valueOf((System.nanoTime() - startJsoupConnect) / 1000000));
            try {
                String searchType = "";
                if (sortBy.getSelectedItem().toString().equals(sortByListAllRecipes.get(0)))
                    searchType = "&sort=re";
                if (sortBy.getSelectedItem().toString().equals(sortByListAllRecipes.get(1)))
                    searchType = "&sort=p";
                if (sortBy.getSelectedItem().toString().equals(sortByListAllRecipes.get(2)))
                    searchType = "&sort=n";

                doc = Jsoup.connect("https://www.allrecipes.com/search/results/?wt=" + URLEncoder.encode(s, UTF_8.toString()) + searchType).get();
                Log.e("connected", String.valueOf((System.nanoTime() - startJsoupConnect) / 1000000));
            } catch(UnsupportedEncodingException e){
                e.printStackTrace();
            } catch(IOException e){
                e.printStackTrace();
            }
                Elements imgElements = doc.select("div.grid-card-image-container");
                if (imgElements.size() != 0) {
                    Elements titleElements = doc.getElementsByClass("fixed-recipe-card__h3");
                    Elements linkElements = doc.getElementsByClass("grid-card-image-container");
                    Elements ratingElements = doc.getElementsByClass("stars");
                    Elements Votes = doc.getElementsByClass("fixed-recipe-card__ratings");
                    Elements descriptionElements = doc.getElementsByClass("fixed-recipe-card__description");
                    long bindData = System.nanoTime();
                    for (Element rating : ratingElements
                    ) {
                        ratings.add(rating.dataset().get("ratingstars"));
                    }
                    for (Element link : linkElements
                    ) {
                        links.add(link.child(0).attr("href"));
                    }
                    for (Element div : imgElements
                    ) {
                        //GET VIDEO LINKS AT SAME TIME
                        if (div.children().size() == 2) {
                            String recipeVideoPage = div.child(1).attr("href");
                            if (recipeVideoPage.contains("/0/")) { //recipes with no video attached
                                videoLinks.add("");
                            } else {
                                videoLinks.add(recipeVideoPage);
                            }
                        } else videoLinks.add("");
                        //GET IMAGE
                        Element link = div.child(0);
                        Element pic = link.child(0);
                        String url = pic.dataset().get("original-src");
                        imgurls.add(url);
                    }
                    for (Element title : titleElements
                    ) {
                        titles.add(title.text());
                    }
                    for (Element desc : descriptionElements
                    ) {
                        descriptions.add(desc.text());
                    }
                    for (Element vote : Votes
                    ) {
                        Element temp = vote.child(1);
                        String deeper = temp.child(0).attr("number");
                        ratingVotes.add(deeper);
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
        Log.e("picsAfter", String.valueOf((System.nanoTime() - imageDurationTimer)/1000000));
        new Thread(new Runnable() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        adapter = new SearchAdapter(context, dataPack, imgs, listener);
                        rv.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
        Log.e("totalTime", String.valueOf((System.nanoTime() - startTime)/1000000));
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i < videoLinks.size(); i++){
                    parseVideoLink(i);
                }
            }
        }).start();
        };

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void scrapeQueryFoodNetwork(String s) {
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
        dataPack.clear();
        initImgs();
        Document doc = new Document(null);
        long startJsoupConnect = System.nanoTime();
        Log.e("startJsoupConnect", String.valueOf((System.nanoTime() - startJsoupConnect) / 1000000));
        try {
            String searchType = "";
            if (sortBy.getSelectedItem().toString().equals(sortByListAllRecipes.get(0)))
                searchType = "";
            if (sortBy.getSelectedItem().toString().equals(sortByListAllRecipes.get(1)))
                searchType = "/rating";

            doc = Jsoup.connect("https://www.foodnetwork.com/search/" + URLEncoder.encode(s, UTF_8.toString()) + "-" + searchType).get();
            Log.e("connected", String.valueOf((System.nanoTime() - startJsoupConnect) / 1000000));
        } catch(UnsupportedEncodingException e){
            e.printStackTrace();
        } catch(IOException e){
            e.printStackTrace();
        }
        Elements recipeBoxes = doc.getElementsByClass("o-RecipeResult");
        Log.e("recipeBoxes", String.valueOf(recipeBoxes.size()));
        if (recipeBoxes.size() != 0) {
            Elements titleElements = recipeBoxes.select("h3");
            Log.e("titleElements", String.valueOf(titleElements.size()));

            Elements imgElements = recipeBoxes.select("img");
            Log.e("imgElements", String.valueOf(imgElements.size()));
            Elements linkElements = recipeBoxes.select("div.m-MediaBlock__m-Rating > a");
            Log.e("linkElements", String.valueOf(linkElements.size()));
            Elements ratingElements = recipeBoxes.select("span.gig-rating-stars");
            Log.e("ratingElements", String.valueOf(ratingElements.size()));
            Elements votes = recipeBoxes.select("span.gig-rating-ratingsum");
            Log.e("votes", String.valueOf(votes.size()));
            Elements descriptionElements = recipeBoxes.select("section.o-RecipeInfo__o-Time");
            Elements descriptionElementsAuthor = recipeBoxes.select("span.m-Info__a-AssetInfo");
            Log.e("ElementsAuthor", String.valueOf(descriptionElementsAuthor.size()));
            long bindData = System.nanoTime();
            for (Element rating : ratingElements
            ) {
                String[] parts = rating.attr("title").split(" ", 4);
                double d = Double.parseDouble(parts[0]); // 5 of 5 stars
                ratings.add(String.valueOf(d));
                Log.e("rating:", String.valueOf(d));
            }
            for (Element link : linkElements
            ) {
                String url = "https:" + link.attr("href");
                links.add(url);
                Log.e("link:", url);
            }
            for (Element div : imgElements
            ) {
                //GET VIDEO LINKS AT SAME TIME
/*                if (div.children().size() == 2) {
                    String recipeVideoPage = div.child(1).attr("href");
                    if (recipeVideoPage.contains("/0/")) { //recipes with no video attached
                        videoLinks.add("");
                    } else {
                        videoLinks.add(recipeVideoPage);
                    }
                } else videoLinks.add("");*/
                //GET IMAGE
                String url = "https:" + div.attr("src");
                imgurls.add(url);
                Log.e("pic:", url);

            }
            for (Element title : titleElements
            ) {
                titles.add(title.text());
                Log.e("titles:", title.text());
            }
            for(int i = 0; i < descriptionElements.size(); i++) {
                    StringBuilder totalDescrip = new StringBuilder();
                    totalDescrip.append(descriptionElements.get(i).text() + "\n");
                    totalDescrip.append(descriptionElementsAuthor.get(i).text());
                    descriptions.add(totalDescrip.toString());
                    Log.e("descriptions:", totalDescrip.toString());
            }
            for (Element vote : votes
            ) {
                String[] parts = vote.text().split(" ",2);
                ratingVotes.add(parts[0]);
                Log.e("votes:", parts[0]);
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
        Log.e("picsAfter", String.valueOf((System.nanoTime() - imageDurationTimer)/1000000));
        new Thread(new Runnable() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        adapter = new SearchAdapter(context, dataPack, imgs, listener);
                        rv.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
        Log.e("totalTime", String.valueOf((System.nanoTime() - startTime)/1000000));
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i < videoLinks.size(); i++){
                    parseVideoLink(i);
                }
            }
        }).start();
    };

    public void waitTilNoThreads(ExecutorService imageFetchExecutor){
        try {
            Log.e("try","attempt to shutdown executor");
            imageFetchExecutor.shutdown();
            imageFetchExecutor.awaitTermination(15, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            Log.e("catch","tasks interrupted");
        }
        finally {
            if (!imageFetchExecutor.isTerminated()) {
                Log.e("finally","cancel non-finished tasks");
            }
            imageFetchExecutor.shutdownNow();
            Log.e("finally","shutdown finished");
        }
    }
    @Override
    public void onView(int layoutPosition, ImageView add, List<Boolean> isAdded) {
        openRecipesAddButton = add;
        this.isAdded = isAdded;
        Recipe rec = scrapeRecipe(dataPack.get(4).get(layoutPosition), dataPack.get(0).get(layoutPosition), imgs.get(layoutPosition));
        parseVideoLink(layoutPosition);
        rec.setVideoTutorial(dataPack.get(5).get(layoutPosition));
        Intent intent = new Intent(getActivity(), DisplayRecipe.class);
        intent.putExtra("Recipe", rec);
        intent.putExtra("Index", 0);
        intent.putExtra("layoutPosition", layoutPosition);
        intent.setAction("Preview");
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    public void quickAdd(int layoutPosition, ImageView toggleMe) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(toggleMe !=null){
                    toggleMe.setEnabled(false);
                    toggleMe.setImageResource(R.drawable.ic_iconmonstr_recipe_added);
                }

                parseVideoLink(layoutPosition); //FINALIZE LINK (work delayed til know its needed)

                Recipe rec = scrapeRecipe(dataPack.get(4).get(layoutPosition), dataPack.get(0).get(layoutPosition), imgs.get(layoutPosition));
                rec.setVideoTutorial(dataPack.get(5).

                get(layoutPosition));
                MainActivity.addRecipe(0,rec);
            }}).start();
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
    public void parseVideoLink(int layoutPosition){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!dataPack.get(5).get(layoutPosition).equals("") && !dataPack.get(5).get(layoutPosition).contains("player")) {
                     Thread nt = new Thread(() -> {

                        Document videoPage = null; //loadRecipeVideoPage then make Video url
                        try {
                            videoPage = Jsoup.connect(dataPack.get(5).get(layoutPosition)).timeout(10000).get();

                        Elements maybeLink = videoPage.getElementsByTag("video");
                        Map<String, String> data = maybeLink.get(0).dataset();
                        String account = data.get("account");
                        String vidId = data.get("video-id");
                        String player = data.get("player");
                        String urlConstructor = "https://players.brightcove.net/" + account + "/" + player + "_default/index.html?videoId=" + vidId;
                        dataPack.get(5).set(layoutPosition, urlConstructor);
                        } catch (IOException e) {
                            Log.e("FAILED VID FIND", dataPack.get(5).get(layoutPosition));
                            e.printStackTrace();
                        }
                    });
                    nt.start();
                    try {
                        nt.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
    }).start();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Recipe passedItem = (Recipe) data.getExtras().get("passed_item");
            int layoutPosition = (int) data.getExtras().get("layoutPosition");
            String action = (String) data.getExtras().get("Action");

            switch(action){
                case "Save":
                    isAdded.set(layoutPosition, true);
                    quickAdd(layoutPosition, openRecipesAddButton);
                    break;
                case "Delete":
                    openRecipesAddButton = null;
                    break;
                default:
                    break;
            }


        }
    }
    private Recipe scrapeRecipe(String recipeUrl, String recipeName, Bitmap recPic) {
        Recipe newRec = new Recipe("shell");
        ArrayList<Ingredient> ingreds = new ArrayList<>();
        newRec.setIcon(recPic, context);
        newRec.setRecipeTitle(recipeName);
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
                        s = s.replace("¼", ".25")
                                .replace("½", ".5")
                                .replace("¾", ".75")
                                .replace("⅐", String.valueOf(1.0/7.0))
                                .replace("⅑", String.valueOf(1.0/9.0))
                                .replace("⅒", String.valueOf(1.0/10.0))
                                .replace("⅓", String.valueOf(.33))
                                .replace("⅔", String.valueOf(2.0/3.0))
                                .replace("⅕", String.valueOf(1.0/5.0))
                                .replace("⅖", String.valueOf(2.0/5.0))
                                .replace("⅗", String.valueOf(3.0/5.0))
                                .replace("⅘", String.valueOf(4.0/5.0))
                                .replace("⅙", String.valueOf(1.0/6.0))
                                .replace("⅚", String.valueOf(5.0/6.0))
                                .replace("⅛", String.valueOf(1.0/8.0))
                                .replace("⅜", String.valueOf(3.0/8.0))
                                .replace("⅝", String.valueOf(5.0/8.0))
                                .replace("⅞", String.valueOf(7.0/8.0));
                        s = s.replaceAll("\\p{Zs}+", " "); //remove special spaces
                        s = s.replace(" .",".");

                        String[] parts = s.split(" ");
                        ArrayList<String> pieces = new ArrayList<>();
                        pieces.addAll(Arrays.asList(parts));
                        StringBuilder noteBuilder = new StringBuilder();
                        String[] commaSplit = s.split(",", 2);
                        if(commaSplit.length == 2){
                            int removeThisMany = commaSplit[1].split(" ").length - 1;
                            noteBuilder.append(commaSplit[1]);
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

                        int firstparen = -1;
                        int secondparen = -1;
                        for (int i = 0; i < pieces.size(); i++) {
                            boolean added = false;
                            if(pieces.get(i).contains("(")){
                                firstparen = i;
                                added = true;
                                noteBuilder.append(pieces.get(i).replace("(","")).append(" ");
                            }
                            if(pieces.get(i).contains(")")){
                                secondparen = i;
                                if(firstparen == i) {
                                    noteBuilder = new StringBuilder(noteBuilder.toString().replace(")",""));
                                } else {
                                    added = true;
                                    noteBuilder.append(pieces.get(i).replace(")",""));
                                }
                            } else if (firstparen >= 0 && secondparen == -1 && !added){
                                noteBuilder.append(pieces.get(i)).append(" ");
                            }
                        }

                        if(firstparen != -1 && secondparen != -1) { //remove (parenthesis phase)
                            for (int x = secondparen; x >= firstparen; x--) {
                                pieces.remove(x);
                            }
                        }

                        String note = noteBuilder.toString();
                        String measureType = pieces.get(0);
                        pieces.remove(0);

                        noteBuilder = new StringBuilder();
                        for (int i = 0; i < pieces.size(); i++) {
                            noteBuilder.append(pieces.get(i) + " ");
                        }
                        String ingredientName = noteBuilder.toString();
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
}