package com.example.RecipeHero;

import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static java.nio.charset.StandardCharsets.UTF_8;

public class NutritionQuery {

    private static final String API_URL = "https://reqres.in/api/users/2";
    private static final String Nutritionix_API_KEY = "7a33fb023dmsh26f191c59816e64p1168dfjsn5e1e67646390";
    private static final String Nutritionix_API_URL = "nutritionix-api.p.rapidapi.com";
    ArrayList<Nutrition> lastQuery = new ArrayList<>();
    int saveXqueries = 3;
    boolean finished;
    boolean failed = false;
    Ingredient ing;
    public boolean isFinished(){
        return finished;
    }
    public ArrayList<Nutrition> getNutrition(){
        return lastQuery;
    }

    //check if any have successful conversion
    public Nutrition getBestResult(){
        for (Nutrition nut :
                lastQuery) {
            if(nut.getQueryResults() == Nutrition.QueryResults.SUCCESS) return nut;
        }
        return lastQuery.get(0);
    }
    public NutritionQuery(Ingredient ingredient){
        finished = false;
        ing = ingredient;
        OkHttpHandler okHttpHandler = new OkHttpHandler(ingredient.getName());
        okHttpHandler.execute(API_URL);
    }
    public void queryAnother(String ingredient){
        finished = false;
        failed = false;
        OkHttpHandler okHttpHandler = new OkHttpHandler(ingredient);
        okHttpHandler.execute(API_URL);
    }

    public class OkHttpHandler extends AsyncTask {

        OkHttpClient client = new OkHttpClient();
        String query = "";

        public OkHttpHandler(String toQuery) {
            query = URLEncoder.encode(toQuery);
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected Object doInBackground(Object[] objects) {

            String encoded = "";
            try {
                encoded = URLEncoder.encode("nf_ingredient_statement," +
                        "item_name," +
                        "nf_calories," +
                        "nf_calories_from_fat," +
                        "nf_total_fat," +
                        "nf_saturated_fat," +
                        "nf_monounsaturated_fat," +
                        "nf_polyunsaturated_fat," +
                        "nf_trans_fatty_acid," +
                        "nf_cholesterol," +
                        "nf_sodium," +
                        "nf_total_carbohydrate," +
                        "nf_dietary_fiber," +
                        "nf_sugars," +
                        "nf_protein," +
                        "nf_vitamin_a_dv," +
                        "nf_vitamin_c_dv," +
                        "nf_calcium_dv," +
                        "nf_iron_dv," +
                        "nf_potassium," +
                        "nf_servings_per_container," +
                        "nf_serving_size_qty," +
                        "nf_serving_size_unit," +
                        "nf_serving_weight_grams," +
                        "metric_qty," +
                        "metric_uom", UTF_8.toString());
            } catch (UnsupportedEncodingException e) {
                failed = true;
                e.printStackTrace();
            }
            Request request = new Request.Builder()
                    .url("https://nutritionix-api.p.rapidapi.com/v1_1/search/" + query + "?fields=" + encoded)
                    .get()
                    .addHeader("x-rapidapi-host", Nutritionix_API_URL)
                    .addHeader("x-rapidapi-key", Nutritionix_API_KEY)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                String s;
                if (response.body() == null) throw new AssertionError();
                s = response.body().string();

                JSONObject json = new JSONObject(s);
                JSONArray jsonArray = json.getJSONArray("hits");
                if(jsonArray.length() == 0) {
                    finished = true;
                    lastQuery.add(new Nutrition().setQueryResults(Nutrition.QueryResults.NO_RESULTS));
                    return s;
                }
                for(int i = 0; i < saveXqueries && i < jsonArray.length(); i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i); // top result

                    JSONObject fields = jsonObject.getJSONObject("fields");
                    lastQuery.add(new Nutrition(fields, ing));
                }
                finished = true;

                return s;

            } catch (Exception e) {
                failed = true;

                Log.e("API CALL", "EXCEPTION");
                e.printStackTrace();
                finished = true;
            }
            return null;
        }
    }
}