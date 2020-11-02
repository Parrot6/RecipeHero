package com.example.RecipeHero;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context arg0, Intent arg1) {
        // For our recurring task, we'll just display a message

        // Fetch extra strings from MainActivity on button intent
        // Determines whether user presses on or off
        String fetch_string = arg1.getExtras().getString("extra");
        // Log.e("What is the key?", fetch_string);
        // Create intent
        Intent service_intent = new Intent(arg0, AlarmPlayingService.class);

        // Pass extra string from receiver to RingtonePlayingService
        service_intent.putExtra("extra", fetch_string);

        // Start ringtone service
        arg0.startService(service_intent);

    }

}
