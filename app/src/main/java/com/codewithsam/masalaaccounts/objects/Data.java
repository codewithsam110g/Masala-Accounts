package com.codewithsam.masalaaccounts.objects;

import java.util.Date;

public class Data {
    public String name;
    public float amount;
    public String date;

    public Data(){
        name="NAN";
        amount = 0;
        date = "NAN";
    }

    public Data(String name, float amount, String date){
        this.name = name;
        this.amount = amount;
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

}
