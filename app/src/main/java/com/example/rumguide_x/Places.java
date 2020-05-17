package com.example.rumguide_x;

import com.google.android.gms.maps.model.LatLng;

public class Places {

    private String name;
    private String description;
    private LatLng cords;
    private int floor;
    private String type;
    private String building;

    public  Places(String name, LatLng cords){
this.name=name;
this.cords=cords;
    }

    public  Places(String name, LatLng cords,String type){
        this.type=type;
        this.name=name;
        this.cords=cords;
    }

    public Places(String name, LatLng cords, int floor, String type, String building,String description) {
        this.name = name;
        this.cords = cords;
        this.floor = floor;
        this.type = type;
        this.building=building;
        this.description=description;
    }

    //Contructor
    Places(){

    }

    public String getName() {
        return name;
    }

//    public String getLocation() {
//        return location;
//    }
    public String getDescription(){
        return description;
    }

    public LatLng getCords() {
        return cords;
    }

    public int getFloor() {
        return floor;
    }

//    public String getId() {
//        return id;
//    }

    public String getType() {
        return type;
    }


    public String getBuilding(){ return building;}

}
