package com.example.RecipeHero;

import java.io.Serializable;
import java.text.DecimalFormat;

public class Ingredient implements Serializable
{
    private static final long serialVersionUID = 1234567L;
    private String name = "";
    transient DecimalFormat df = new DecimalFormat("0.##");
    private double quantity = 0;
    private boolean isInfinitelyStocked = false;
    private String measurementType = "none";
    private String additionalNote;
    private Nutrition nutrition = null;

    public Ingredient(String name, double quantity, String measurementType){
        this.name = formatName(name);
        this.quantity = Double.parseDouble(df.format(quantity));
        this.measurementType =  MainActivity.getPrefferedMeasurementName(measurementType);
        this.additionalNote = "";
    }
    public Ingredient(String name){
        this.name = formatName(name);
        this.quantity = 1;
        this.measurementType = "Unknown";
        this.additionalNote = "";
    }
    public Ingredient(String name, double quantity){
        this.name = formatName(name);
        this.quantity = Double.parseDouble(df.format(quantity));

        this.measurementType = "Unknown";
        this.additionalNote = "";
    }
    public Ingredient(String name, double quantity, String measurementType, String additionalNote){
        this.name = formatName(name);
        this.quantity = Double.parseDouble(df.format(quantity));
        this.measurementType =  MainActivity.getPrefferedMeasurementName(measurementType);
        if(additionalNote.equals("Note")){
            this.additionalNote = "";
        } else this.additionalNote = additionalNote;
    }
    public String formatName(String s){
        return capitolizeFirst(s);
    };
    public String capitolizeFirst(String s){
        StringBuilder sb = new StringBuilder();
        sb.append(s.substring(0,1).toUpperCase());
        sb.append(s.substring(1));
        return sb.toString();
    };
    public void setName(String name) {
        this.name = formatName(name);
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getMeasurementType() {
        return measurementType;
    }

    public void setMeasurementType(String measurementType) {
        this.measurementType = measurementType;
    }

    public String getAdditionalNote() {
        return additionalNote;
    }

    public void setAdditionalNote(String additionalNote) {
        this.additionalNote = additionalNote;
    }

    public String toString(){
        DecimalFormat df = new DecimalFormat("0.##");
        String str;
        if(quantity == 0.0){
            if(measurementType == "n/a") str = this.name;
            else str = this.measurementType + " " +  this.name;
        } else {
            str = df.format(this.quantity) + " " + this.measurementType + " " +  this.name;
        }
        if(additionalNote != "") str += " -" + this.additionalNote;
        return str;
    }

    public String getName() {
        return name;
    }

    public Nutrition getNutrition() {
        return nutrition;
    }

    public void setNutrition(Nutrition nutrition) {
        this.nutrition = nutrition;
    }

    public boolean isInfinitelyStocked() {
        return isInfinitelyStocked;
    }

    public void setInfinitelyStocked(boolean infinitelyStocked) {
        isInfinitelyStocked = infinitelyStocked;
    }
}
