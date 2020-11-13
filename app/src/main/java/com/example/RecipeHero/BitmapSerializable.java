package com.example.RecipeHero;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class BitmapSerializable implements Serializable {
    private transient Bitmap bitmap = null;
    public byte[] imageByteArray = null;
    public BitmapSerializable(Bitmap bm){
        if(bm == null) return;
        bitmap = bm;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
        imageByteArray = stream.toByteArray();
    }
    public Bitmap getBitmap(){
        if(bitmap == null){
            if(imageByteArray != null) bitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
        }
        return bitmap;
    }
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException{
        imageByteArray = (byte[]) in.readObject();
        //imageByteArray = bitmapDataObject.imageByteArray;
        if(imageByteArray != null) bitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
    }
}


