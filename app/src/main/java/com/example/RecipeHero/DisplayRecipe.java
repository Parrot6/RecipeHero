package com.example.RecipeHero;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import android.widget.Chronometer;
import android.os.Vibrator;

public class DisplayRecipe extends AppCompatActivity {
    private static int REQUEST_CODE = 100;
    TextView name;
    TextView ingredients;
    TextView instructions;
    TextView nutrition;
    Button edit;
    ImageView redownload;
    ImageButton export; //to do
    int layoutPosition; //if preview'd
    Button delete;
    Recipe recipe;
    String returnAction = "none";
    ImageView icon;
    ImageView recipeType;
    ImageView vidTutorial;
    ImageView gotoWebsite;
    Chronometer stopWatch;
    long timeWhenStopped = 0;
    long currentTimeLeft = 0;
    Button startStopWatch;
    Button stopStopWatch;
    ImageButton expandWidget;
    ImageButton openTimer;
    Button scaleUp;
    MediaPlayer player;
    private PendingIntent pendingIntent;
    private AlarmManager manager;
    boolean isTimerRunning = false;
    boolean preview = false;
    Intent intent;
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve a PendingIntent that will perform a broadcast
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);

        setContentView(R.layout.activity_display_recipe);
        expandWidget = findViewById(R.id.expand_widgets);
        openTimer = findViewById(R.id.widget_openTimer);
        openTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleStopwatchWidget();
            }
        });
        expandWidget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleStopwatchWidget();
            }
        });
        stopWatch = findViewById(R.id.Chronometer);
        stopWatch.setBase(SystemClock.elapsedRealtime() + 10011);
        stopWatch.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                if(chronometer.getText().equals("00:00")) {
                    stopWatch.setText(">>00:00<<");
                    startAlarmSounds();
                    stopStopWatch.callOnClick();
                } else if(chronometer.getText().toString().contains("−")){
                    stopWatch.setText(">>00:00<<");
                    stopStopWatch.callOnClick();
                }
                currentTimeLeft = stopWatch.getBase() - SystemClock.elapsedRealtime();
            }
        });
        stopWatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(DisplayRecipe.this);
                builder1.setTitle("Set Timer");
                builder1.setMessage("New timer for how long? (minutes)");
                builder1.setCancelable(true);
                // Set up the input
                final EditText input = new EditText(DisplayRecipe.this);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
                input.setGravity(Gravity.CENTER);
                input.setBackground(ContextCompat.getDrawable(DisplayRecipe.this,R.color.recipeListItem));
                builder1.setView(input);
                builder1.setPositiveButton(
                        "Confirm",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                try {
                                    int minutes = Integer.parseInt(input.getText().toString());
                                    //stopWatch.stop();
                                    stopWatch.setBase(SystemClock.elapsedRealtime() +  minutes*1000*60);
                                    stopStopWatch.callOnClick();
                                    //timeWhenStopped = stopWatch.getBase() - SystemClock.elapsedRealtime();
                                    dialog.cancel();
                                } catch (Exception e){
                                    dialog.cancel();
                                }
                            }
                        });
                builder1.setNegativeButton(
                        "Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert11 = builder1.create();
                alert11.show();
            }
        });
        stopWatch.setCountDown(true);
        startStopWatch = findViewById(R.id.timerStart);
        startStopWatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!stopWatch.getText().equals(">>00:00<<")) {
                    isTimerRunning = true;
                    stopWatch.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
                    stopWatch.start();
                    startStopWatch.setVisibility(View.GONE);
                    stopStopWatch.setVisibility(View.VISIBLE);
                    //startAlarm(SystemClock.elapsedRealtime() + timeWhenStopped);
                } else stopWatch.callOnClick();
            }
        });
        stopStopWatch = findViewById(R.id.timerStop);
        stopStopWatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //cancelAlarm();
                isTimerRunning = false;
                timeWhenStopped = stopWatch.getBase() - SystemClock.elapsedRealtime();
                stopWatch.stop();
                startStopWatch.setVisibility(View.VISIBLE);
                stopStopWatch.setVisibility(View.GONE);
                }
        });

        stopStopWatch.callOnClick();
        toggleStopwatchWidget(); //initially closed
        scaleUp = findViewById(R.id.button_display_recipe_scaleIngr);
        scaleUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(DisplayRecipe.this);
                builder1.setTitle("Scale Ingredients");
                builder1.setMessage("Enter number to scale all ingredient quantities by (1 is base scale)");
                builder1.setCancelable(true);
                // Set up the input
                final EditText input = new EditText(DisplayRecipe.this);

                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
                input.setGravity(Gravity.CENTER);
                input.setBackground(ContextCompat.getDrawable(DisplayRecipe.this,R.color.recipeListItem));
                builder1.setView(input);
                builder1.setPositiveButton(
                        "Confirm",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                try {
                                    double scale = Double.parseDouble(input.getText().toString());
                                    if(scale != 1.0) {
                                        ArrayList<Ingredient> clone = new ArrayList<>();
                                        for (Ingredient ing : recipe.getIngredients()) {
                                            clone.add(new Ingredient(ing.getName(),ing.getQuantity(),ing.getMeasurementType(),ing.getAdditionalNote()));
                                        }
                                        for(Ingredient ing : clone){
                                            ing.setQuantity(ing.getQuantity()*scale);
                                        }
                                        Recipe rec = new Recipe("shell", clone);
                                        ingredients.setText(rec.getIngredientsAsStringList());
                                    } else ingredients.setText(recipe.getIngredientsAsStringList());
                                    dialog.cancel();
                                } catch (Exception e){
                                    dialog.cancel();
                                }
                            }
                        });
                builder1.setNeutralButton("Reset", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ingredients.setText(recipe.getIngredientsAsStringList());
                        dialogInterface.cancel();
                    }
                });
                builder1.setNegativeButton(
                        "Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert11 = builder1.create();
                alert11.show();
            }
        });
        Intent intent = getIntent();
        if(getIntent().getExtras() != null) {
            recipe = (Recipe) intent.getSerializableExtra("Recipe");
            layoutPosition = (int) intent.getSerializableExtra("layoutPosition");
        }

        redownload = findViewById(R.id.button_redownload);
        redownload.setVisibility(View.GONE);
        redownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                redownload.setEnabled(false);
                //redownload.setVisibility(View.GONE);
                redownload.setColorFilter(R.color.cancellight);
                recipe = SearchFragment.reloadScrapeRecipeSwitch(DisplayRecipe.this, recipe.getSourceUrl(), recipe.getRecipeTitle(), recipe.getRecipeIcon(), recipe.sourceSite);
                initializeFields();
                redownload.setEnabled(true);
                redownload.setColorFilter(R.color.black);
            }
        });

        name = findViewById(R.id.text_RecipeTitle);
        ingredients = findViewById(R.id.text_Ingredients);
        Log.e("URL",recipe.getVideoTutorial());
        if(!recipe.getVideoTutorial().equals("")) {
            vidTutorial = findViewById(R.id.button_display_recipe_videoTut);
            vidTutorial.setVisibility(View.VISIBLE);
            vidTutorial.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(recipe.getVideoTutorial()));
                    startActivity(browserIntent);
                }
            });
        } else if(!recipe.getSourceUrl().equals("")) {
            Log.e("URL",recipe.getVideoTutorial());
            gotoWebsite = findViewById(R.id.button_display_recipe_sourceUrl);
            gotoWebsite.setVisibility(View.VISIBLE);
            gotoWebsite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.e("URL",recipe.getVideoTutorial());
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(recipe.getSourceUrl()));
                    startActivity(browserIntent);
                }
            });
        }
        icon = findViewById(R.id.displayrecipe_icon);
        if(recipe.getRecipeIcon() == null) icon.setVisibility(View.INVISIBLE);
        else {
            icon.setVisibility(View.VISIBLE);
            Bitmap b = recipe.getRecipeIcon();
            icon.setImageBitmap(b);
        }
        recipeType = findViewById(R.id.display_recipe_recipeType);
        if(recipe.getRecipeType() == Recipe.RecipeType.NONE) recipeType.setVisibility(View.INVISIBLE);
        else recipeType.setVisibility(View.VISIBLE);
        instructions = findViewById(R.id.Text_RecipeInstuctions);
        if(recipe.getRecipeInstructions() == "") instructions.setText("No Instructions");

        edit = findViewById(R.id.button_saveEdits);
        export = findViewById(R.id.button_export);
        delete = findViewById(R.id.button_cancelEdits);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edit();
            }
        });
        export.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(DisplayRecipe.this, export);
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
                                ClipboardManager clipboard = (ClipboardManager) DisplayRecipe.this.getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("label", printRecipe(true));
                                clipboard.setPrimaryClip(clip);
                                Toast.makeText(DisplayRecipe.this,"Copied to Clipboard!",Toast.LENGTH_SHORT).show();
                                return true;
                            case R.id.print:
                                MainActivity.printToPrinter(DisplayRecipe.this, recipe.getRecipeTitle(), printRecipe(false).replace("\n", "<BR>"));
                                return true;
                            case R.id.export:
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("text/plain");
                                intent.putExtra("sms_body", printRecipe(true));
                                if (intent.resolveActivity(getPackageManager()) != null) {
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
        if(intent.getAction() == "Preview"){
            preview = true;
            delete.setText("Go Back");
            edit.setText("Add To Recipes");
            layoutPosition = (int) intent.getExtras().get("layoutPosition");
        }
        nutrition = findViewById(R.id.layout_deisplay_recipe_nutritionHolder);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(preview){
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("passed_item", recipe);
                    returnIntent.putExtra("layoutPosition", layoutPosition);
                    returnAction = "Delete";
                    returnIntent.putExtra("Action", returnAction);
                    // setResult(RESULT_OK);
                    setResult(RESULT_OK, returnIntent);
                    finish();
                } else {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(DisplayRecipe.this);
                    builder1.setTitle("DELETE");
                    builder1.setMessage("Are you sure you want to delete this recipe?");
                    builder1.setCancelable(true);
                    // Set up the input
                    builder1.setPositiveButton(
                            "Confirm",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    try {
                                        Intent returnIntent = new Intent();
                                        returnIntent.putExtra("passed_item", recipe);
                                        returnIntent.putExtra("layoutPosition", layoutPosition);
                                        returnAction = "Delete";
                                        returnIntent.putExtra("Action", returnAction);
                                        // setResult(RESULT_OK);
                                        setResult(RESULT_OK, returnIntent);
                                        finish();
                                        dialog.cancel();
                                    } catch (Exception e) {
                                        dialog.cancel();
                                    }
                                }
                            });
                    builder1.setNegativeButton(
                            "Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                }
            }
        });


        initializeFields();

    }

    private void initializeFields() {
        if(recipe.getIngredients().size() == 0 || instructions.getText().toString().trim().length() == 0 || recipe.getRecipeInstructions().trim().equals("")){
            if(!recipe.sourceSite.equals("")){
                if(redownload.getVisibility() == View.VISIBLE) {
                    Toast.makeText(DisplayRecipe.this, "Reload not successful, Try again later :(", Toast.LENGTH_SHORT).show();
                    redownload.setVisibility(View.GONE);
                }
                else redownload.setVisibility(View.VISIBLE);
            } else redownload.setVisibility(View.GONE);
        }
        final Nutrition thisNut = recipe.getNutritionSummary();
        if(thisNut == null) {
            if(preview) nutrition.setText("Nutrition can be calculated after adding recipe");
            else nutrition.setText("Edit Recipe to load nutrition!");
        }
        else {
            nutrition.setText(recipe.getNutritionSummary().toString());
        }
        name.setText(recipe.getRecipeTitle());
        instructions.setText(recipe.getRecipeInstructions());
        recipeType.setImageResource(recipe.getRecipeType().getDrawable());
        ingredients.setText(recipe.getIngredientsAsStringList());
    }

    private void toggleStopwatchWidget(){
        if(findViewById(R.id.timerLabel).getVisibility() == View.GONE){
            findViewById(R.id.layout_displayRecipe_widgetHolder).setBackgroundResource(R.drawable.widgetbg);
            stopWatch.setVisibility(View.VISIBLE);
            stopWatch.setEnabled(true);
            expandWidget.setVisibility(View.VISIBLE);
            openTimer.setVisibility(View.GONE);
            findViewById(R.id.timerLabel).setVisibility(View.VISIBLE);
            findViewById(R.id.widget_toprightspacer).setVisibility(View.VISIBLE);
            if(!isTimerRunning) startStopWatch.setVisibility(View.VISIBLE);
            if(isTimerRunning) stopStopWatch.setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.widget_toprightspacer).setVisibility(View.GONE);
            if(isTimerRunning) stopWatch.setVisibility(View.VISIBLE);
            else stopWatch.setVisibility(View.INVISIBLE);
            stopWatch.setEnabled(false);
            expandWidget.setVisibility(View.GONE);
            openTimer.setVisibility(View.VISIBLE);
            findViewById(R.id.layout_displayRecipe_widgetHolder).setBackgroundColor(getResources().getColor(R.color.transparent));
            findViewById(R.id.timerLabel).setVisibility(View.GONE);
            startStopWatch.setVisibility(View.GONE);
            stopStopWatch.setVisibility(View.GONE);
        }
    }
    public void startAlarmSounds() {
        if (Build.VERSION.SDK_INT >= 26) {
            ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(6000, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(6000);
        }
        AssetFileDescriptor afd = null;
        player = new MediaPlayer();
        try {
            afd = getAssets().openFd("520200__latranz__industrial-alarm.mp3");
            player.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
            player.prepare();
            player.start();
            if(openTimer.getVisibility() == View.VISIBLE) toggleStopwatchWidget();
        } catch (IOException e) {
            e.printStackTrace();
        }
        AlertDialog.Builder builder1 = new AlertDialog.Builder(DisplayRecipe.this);
        builder1.setTitle("TIMER FINISHED");
        // Set up the input
        builder1.setPositiveButton(
                "STOP THE SIREN",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        player.stop();
                        dialog.cancel();
                    }
                });
        AlertDialog alert11 = builder1.create();
        alert11.show();
    }
    public void startAlarm(long howLongTilAlarmStart) {
        manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        intent = new Intent(DisplayRecipe.this, AlarmReceiver.class);
        intent.putExtra("extra","on");
        pendingIntent = PendingIntent.getBroadcast((DisplayRecipe) this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() +
                        howLongTilAlarmStart, pendingIntent);
    }
    public void cancelAlarm() {
        if (manager != null) {
            manager.cancel(pendingIntent);
            Toast.makeText(this, "Alarm Canceled", Toast.LENGTH_SHORT).show();
        }
    }
    private String printRecipe(boolean addTitle) {
        StringBuilder sb = new StringBuilder();
        if(addTitle)sb.append("**" + recipe.getRecipeTitle() + "**\n");
        sb.append("Ingredients: \n");
        sb.append(recipe.getIngredientsAsStringList()+"Instructions: \n");
        sb.append(recipe.getRecipeInstructions() + "\n");
        if(recipe.getNutritionSummary() != null) sb.append("Nutrition: \n" + nutrition.getText().toString());
        String s = sb.toString();
        return s;
    }

    @Override
    protected void onPause() {
        if(isTimerRunning) {
            startAlarm(currentTimeLeft);
            int seconds = (int) (currentTimeLeft/1000);
            int minutes = (int) (seconds/60);
            seconds -= minutes*60;
            Toast.makeText(DisplayRecipe.this, "Alarm set for " + minutes + "m" + seconds + "s from now", Toast.LENGTH_SHORT).show();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if(player!=null) player.stop();
        if (manager != null) {
            manager.cancel(pendingIntent);
            // Put extra string into my_intent, indicates off button pressed
            intent.putExtra("extra", "off");

            // Stop ringtone
            sendBroadcast(intent);
           // Toast.makeText(this, "Alarm Disabled!!",Toast.LENGTH_LONG).show();
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public boolean onOptionsItemSelected(MenuItem item){
        finish();
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            Recipe passedItem = (Recipe) data.getExtras().get("passed_item");
            layoutPosition = (int) data.getExtras().get("layoutPosition");
            returnAction = (String) data.getExtras().get("Action");

            // REINITIALIZE ALL FIELDS OIN RETURN
            recipe = passedItem;
            Log.e("Test", recipe.getIngredients().toString());
            name.setText(recipe.getRecipeTitle());
            ingredients.setText(recipe.getIngredientsAsStringList());
            instructions.setText(recipe.getRecipeInstructions());
            if(recipe.getNutritionSummary() != null)nutrition.setText(recipe.getNutritionSummary().toString());
            recipeType.setImageResource(recipe.getRecipeType().getDrawable());
            if(recipe.getRecipeIcon() == null) icon.setVisibility(View.INVISIBLE);
            else {
                icon.setVisibility(View.VISIBLE);
                Bitmap b = recipe.getRecipeIcon();
                icon.setImageBitmap(b);
            }
            if(recipe.getRecipeType() == Recipe.RecipeType.NONE) recipeType.setVisibility(View.INVISIBLE);
            else recipeType.setVisibility(View.VISIBLE);
            returnAction = "Save";
            Intent returnIntent = new Intent();
            returnIntent.putExtra("passed_item", recipe);
            returnIntent.putExtra("layoutPosition", layoutPosition);
            returnIntent.putExtra("Action", returnAction);
            setResult(RESULT_OK, returnIntent);
        }
    }

    public void edit(){
        if(preview){
            returnAction = "Save";
            Intent returnIntent = new Intent();
            returnIntent.putExtra("Action", returnAction);
            setResult(RESULT_OK, returnIntent);
            finish();
        } else {
            Intent newIntent = new Intent(this, EditRecipe.class);
            newIntent.putExtra("Recipe", recipe);
            newIntent.putExtra("layoutPosition", layoutPosition);
            startActivityForResult(newIntent, REQUEST_CODE);
        }
    }

}