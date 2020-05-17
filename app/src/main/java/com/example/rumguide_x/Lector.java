package com.example.rumguide_x;

import android.content.Context;
import android.content.res.AssetManager;

import com.google.android.gms.maps.model.LatLng;
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
import java.util.StringTokenizer;

public class Lector {
    private String mName;
    private String mType;
    private LatLng mLatLon;
    private int mFloor;
    private String mBuilding;
    private String mDescription;
    private float mLat;
    private float mLon;

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

    public ArrayList<Places> readLine(String path) {
        ArrayList<Places> mLines = new ArrayList<>();

        AssetManager am = mContext.getAssets();

        try {
            InputStream is = am.open(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;

            while ((line = reader.readLine()) != null) {
                StringTokenizer multiTokenizer = new StringTokenizer(line, ",:");
                //Resetear todas las variables de Places.
                String temp;
                mName=null;
                mDescription=null;
                mLat=0;
                mLon=0;
                mType=null;
                mFloor=-1;
                mBuilding=null;

                //Funciona como el parser por linea.
                while (multiTokenizer.hasMoreTokens()) {
                    temp=multiTokenizer.nextToken();
                    if(temp.compareTo("name")==0){
                        System.out.println("Se encontro un nombre");
                        if(multiTokenizer.hasMoreTokens()){ mName=multiTokenizer.nextToken(); }

                    }
                    else if(temp.compareTo("description")==0){
                        System.out.println("Se encontro un description");
                        if(multiTokenizer.hasMoreTokens()){ mDescription=multiTokenizer.nextToken(); }
                    }
                    else if(temp.compareTo("Lat")==0){
                        System.out.println("Se encontro una Lat");
                        if (multiTokenizer.hasMoreTokens()) {mLat=Float.parseFloat(multiTokenizer.nextToken());
                            System.out.println(mLat);}

                        }
                    else if(temp.compareTo("Lng")==0){
                        System.out.println("Se encontro un Lng");
                        if (multiTokenizer.hasMoreTokens()) {mLon=Float.parseFloat(multiTokenizer.nextToken());
                            System.out.println(mLat);}

                    }
                    else if(temp.compareTo("type")==0){
                        System.out.println("Se encontro un type");
                        if(multiTokenizer.hasMoreTokens()){ mType=multiTokenizer.nextToken(); }
                    }
                    else if(temp.compareTo("floor")==0){
                        System.out.println("Se encontro un floor");
                        if(multiTokenizer.hasMoreTokens()){ mFloor= Integer.parseInt(multiTokenizer.nextToken()); }
                    }
                    else if(temp.compareTo("building")==0){
                        System.out.println("Se encontro un building");
                        if(multiTokenizer.hasMoreTokens()){ mBuilding= multiTokenizer.nextToken(); }
                    }
                    System.out.println(mLon);
                }

                mLatLon= new LatLng(mLat,mLon);
                if(mFloor != -1){
                mLines.add(new Places(mName, mLatLon,mFloor,mType, mBuilding,mDescription));}
            }
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

