package com.example.rumguide_x;

import com.google.android.gms.maps.model.LatLng;

public class Places {

    private String name;
    private String location;
    private LatLng cords;
    private String floor;
    private String id;
    private String type;

    public  Places(String name, LatLng cords){
this.name=name;
this.cords=cords;
    }

    public  Places(String name, LatLng cords,String type){
        this.type=type;
        this.name=name;
        this.cords=cords;
    }

    public Places(String name, String location, LatLng cords, String floor, String id, String type) {
        this.name = name;
        this.location = location;
        this.cords = cords;
        this.floor = floor;
        this.id = id;
        this.type = type;
    }

    //Contructor
    Places(){

    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public LatLng getCords() {
        return cords;
    }

    public String getFloor() {
        return floor;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }
}
