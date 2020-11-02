package com.example.RecipeHero;

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.io.IOException;

public class AlarmPlayingService extends Service {

    MediaPlayer alarm;
    boolean isRunning;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Log.i("LocalService", "Received start id " + startId + ": " + intent);

        // Fetch extra strings (on/off)
        String state = intent.getExtras().getString("extra");
        if(state.equals("off")){
            alarm.stop();
            alarm.reset();
            return START_NOT_STICKY;
        }
        // Log.e("Ringtone state: extra ", state);
        // Log.e("Sound choice is ", sound_id.toString());
        Log.e("inService", "here");
        // Notification service setup, set intent to link to MainActivity

            if (Build.VERSION.SDK_INT >= 26) {
                ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(6000, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(6000);
            }
            AssetFileDescriptor afd = null;
            alarm = new MediaPlayer();
            try {
                afd = getAssets().openFd("520200__latranz__industrial-alarm.mp3");
                alarm.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                alarm.prepare();
                alarm.start();
            } catch (IOException e) {
                e.printStackTrace();
            }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        // Tell user we stopped
        // Log.e("onDestroy called", "ta da");
        super.onDestroy();
        this.isRunning = false;
    }

}
