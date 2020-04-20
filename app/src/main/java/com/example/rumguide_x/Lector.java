package com.example.rumguide_x;

import android.content.Context;
import android.content.res.AssetManager;

import com.google.android.gms.maps.model.Marker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;

public class Lector {

    private Context mContext;

    public ArrayList<String> list;
    private File file;

    Lector(Context context){
        this.mContext = context;

    }

    ArrayList<String> getList(){
        return list;
    }

    void print(){

        try{
            Scanner sc= new Scanner(file);
            System.out.println(sc.nextLine());


            } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    public ArrayList<String> readLine(String path) {
        ArrayList<String> mLines = new ArrayList<>();

        AssetManager am = mContext.getAssets();

        try {
            InputStream is = am.open(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;

            while ((line = reader.readLine()) != null)
                mLines.add(line);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mLines;
    }

    public ArrayList<Marker> getMarkersFromFile(ArrayList<String> s){
        ArrayList<Marker> result= new ArrayList<>();


return result;
    }

    }

