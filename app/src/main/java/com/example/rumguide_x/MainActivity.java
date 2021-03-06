package com.example.rumguide_x;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;


import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.simulator.BeaconSimulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;
import java.util.Set;


/**
 *On Map Ready Call back es para el google maps
 * Beacon Consumer es para manejar el consumo de la informacion de los beacons
 */

public class MainActivity extends FragmentActivity implements OnMapReadyCallback,BeaconConsumer, RangeNotifier, PopupMenu.OnMenuItemClickListener {

/** BEACON LAYOUT FORMATS
 *
 * ALTBEACON   "m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"
 *
 * EDDYSTONE  TLM  "x,s:0-1=feaa,m:2-2=20,d:3-3,d:4-5,d:6-7,d:8-11,d:12-15"
 *
 * EDDYSTONE  UID  "s:0-1=feaa,m:2-2=00,p:3-3:-41,i:4-13,i:14-19"
 *
 * EDDYSTONE  URL  "s:0-1=feaa,m:2-2=10,p:3-3:-41,i:4-20v"
 *
 * IBEACON  "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"
 *
 *
 *
 */

    private String EDDYSTONE_TLM= "s:0-1=feaa,m:2-2=20,d:3-3,d:4-5,d:6-7,d:8-11,d:12-15";
    private String EDDYSTONE= "s:0-1=feaa,m:2-2=00,p:3-3:-41,i:4-13,i:14-19";
    private static final String TAG = "MainActivity";


    GoogleMap map;
    GroundOverlayOptions stefani;
    GroundOverlay activeOverlay;

    private int styleNum = 0;
    //entero para ciclar atraves de los piso de un edificio.
    private int cycle;

    /**
     * Layout variables
     */
    private Button locateButton;
    private TextView txt;
    private TextView bannertxt;
    private LatLng lastLocation;
    private Button changeFloorButton;
    private Button layerButton;
    private Button lookForBeacons;
    private Button stopSearch;
    private SearchView searchBar;


    /**
     * Manager variables
     */
    //Maneja el location.
    private LocationManager locationManager;
    //esta pendiente del cambio en localizacion.
    private LocationListener locationListener;
    private BluetoothManager bluetoothManager;

    //Permite interactuar con los beacons
    private BeaconManager mBeaconManager =null;

    //Representa el criterio de campos con los que buscamos los beacons
    private Region mRegion;
    /**
     * Lists Variables
     */
    //Lista de los markers activos
    private ArrayList<Marker> currentMarkers = new ArrayList<>();
    //
//    private ArrayList<Marker> interestPoints = new ArrayList<>();
    private ArrayList<Places> celis1 = new ArrayList<>();
    private ArrayList<Places> celis2 = new ArrayList<>();
    private ArrayList<Places> celis3 = new ArrayList<>();
    private ArrayList<Places> allMarkers= new ArrayList<>();

    /**
     * GroundOverlay definitions
     */
    private  LatLng celis =new LatLng(18.2093646, -67.1409665);;
    private  LatLng quimica =new LatLng(18.2127023, -67.1408143);;
    //Overlays disponibles, floor plans

    GroundOverlayOptions celis_piso1;
    GroundOverlayOptions celis_piso2;
    GroundOverlayOptions celis_piso3;
    GroundOverlayOptions quim1;
    GroundOverlayOptions quim2;
    GroundOverlayOptions quim3;
    GroundOverlayOptions quim4;

    //Definir los GrounOverlays para usarse en Celis por ahora.



    /**
     * Al momento de correr la aplicacion se comienza por este metodo
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bannertxt= findViewById(R.id.banner);
        Typeface customFont = Typeface.createFromAsset(getAssets(),"fonts/Pacifico-Regular.ttf");
        bannertxt.setTypeface(customFont);

        //Asigna el mapa al fragment del view.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
                    mapFragment.getMapAsync(this);

        cycle = 0;//Variable para recorrer a traves de los pisos de stefani, solo para testing.
        //Actualmente no se esta utilizando.
        bluetoothManager= (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);


        searchBar = (SearchView)findViewById(R.id.searchView);
        searchBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBar.setIconified(false);
            }
        });

        searchBar.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        lookFor(query);
                        System.out.println(query);
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {

                        return false;
                    }
                }
        );

        // Maneja el uso del gps.
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //Codigo de prueba y debug, verificar error.
        if(locationManager!= null) Log.d("Loaction", "LocationManager no es nulo");
        else{Log.d("Loaction", "LocationManager es nulo");}

        //Manejador de Bluetooth beacons.
//        beaconManager =BeaconManager.getInstanceForApplication(this);
//        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(EDDYSTONE));
//        beaconManager.bind(this);
        //     ..\\ retrocede

        /**
         * Codigo en prueba, para obtener los marcadores desde text file.
         * Carga el archivo del test.txt y crea un arrayList de Places
         */
        loadAllMarkers();

        /**
         *Codigo encargado de inicializar el manejador de Beacons.
         */
        mBeaconManager = BeaconManager.getInstanceForApplication(this);

        //Fijamos el protocolo Beacon Eddystone, el formato que se estara buscando.
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));

        //Arreglo que contiene los identificadores que vamos a estar buscando en los beacons.
        //En este caso vamos a buscar sin criterio, por eso esta vacio.
        ArrayList<Identifier> identifiers =new ArrayList<>();

        //Entiendo que es la region que estaremos buscando de beacons. Para propositos de prueba escucharemos todos los beacons.
        //los identifiers se utilizan con beacon.getIdentifier, para seleccionar el que deseamos.
        mRegion = new Region("AllBeaconsRegion",identifiers);



        //variables de prueba, Points of interest
        //Places es una clase creada para formatear la data de los marcadores.
        //Lat=X Lng=Y
        celis1.add(new Places("C-100", new LatLng(18.2095799, -67.1412221)));
        celis1.add(new Places("C-101", new LatLng(18.2095064, -67.1413221)));
        celis1.add(new Places("C-102", new LatLng(18.2094404, -67.1412221)));
        celis1.add(new Places("C-103", new LatLng(18.2093860, -67.1410386)));
        celis1.add(new Places("C-104", new LatLng(18.2094305, -67.1411221)));
        celis1.add(new Places("C-105", new LatLng(18.2094205, -67.1410221)));
        celis1.add(new Places("Bathroom",new LatLng(18.2095799, -67.14106221),"bathroom"));


        celis2.add(new Places("C-200", new LatLng(18.2094385, -67.14100001)));
        celis2.add(new Places("C-201", new LatLng(18.2092365, -67.1412521)));
        celis2.add(new Places("C-202", new LatLng(18.2093345, -67.1410241)));
        celis2.add(new Places("C-203", new LatLng(18.2094325, -67.1413261)));
        celis2.add(new Places("C-204", new LatLng(18.2091305, -67.1412281)));
        celis2.add(new Places("C-205", new LatLng(18.2090405, -67.1410321)));
        celis2.add(new Places("Bathroom",new LatLng(18.2090799, -67.1410999),"bathroom"));


         celis_piso1 = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.celis_piso1))
                .position(celis, 90).bearing(15);

         celis_piso2 = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.celis_piso2))
                .position(celis, 90).bearing(15);

         celis_piso3 = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.celis_piso3))
                .position(celis, 90).bearing(15);
    /*
    Definir los GrounOverlays para usarse en Quimica por ahora.
    */

         quim1=new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.quim1))
                .position(quimica, 75).bearing(315);
         quim2=new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.quim2))
                .position(quimica, 75).bearing(315);
         quim3=new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.quim3))
                .position(quimica, 75).bearing(315);
         quim4=new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.quim4))
                .position(quimica, 75).bearing(315);

        /**
         * Manejando lo que hacen los botones.
         *         //Find view by id busca el elemento del view.
         *         //Para el boton en este caso es el id locateme.
         *         //Esto asocia la funcion con el boton.
         *
         *
         */

        locateButton = (Button) findViewById(R.id.locateme);
        txt = findViewById(R.id.TEXTOPRUEBA);
        txt.setVisibility(View.VISIBLE);
        txt.setTextColor(Color.WHITE);


        //Boton de simulacion de cambio de pisos.
        changeFloorButton = findViewById(R.id.floorChange);

        //Asigna el boton grafico a la varible del boton.
        layerButton = findViewById(R.id.layerButton);

        //Asigna funcionalidad al boton.
        layerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.setMapType(styleNum);
                styleNum++;
                if (styleNum > 3) {
                    styleNum = 0;
                }
            }
        });

        lookForBeacons= findViewById(R.id.beaconsearch);
        lookForBeacons.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                    System.out.println("No se tiene permiso");
                }
                else{
                    prepareDetection();
                }

//                TimedBeaconSimulator emul ;
//                emul = new TimedBeaconSimulator();
//                emul.createBasicSimulatedBeacons();
//                BeaconManager.setBeaconSimulator(emul);
//                mBeaconManager= BeaconManager.getInstanceForApplication(getBaseContext());
//                mBeaconManager.bind(bc);
//               System.out.println( BeaconManager.isAndroidLScanningDisabled());
                //onBeaconServiceConnect();

                //BeaconManager.getBeaconSimulator();
            }
        });

        stopSearch = findViewById(R.id.stopSearch);
        stopSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopDetectingBeacons();
            }
        });






        changeFloorButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {

                if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                // map.setMyLocationEnabled(true);
                //usara gps, se actualiza cada 5 segundos o cada 2 metros de diferencia, el listener es el ultimo.
//                    lastLocation = new LatLng(locationManager.getLastKnownLocation("gps").getLatitude(),
//                        locationManager.getLastKnownLocation("gps").getLongitude());

                    System.out.println("Se removeran los marcadores activos ");
                while (currentMarkers.size() != 0) {
                    currentMarkers.get(0).remove();
                    currentMarkers.remove(0);
                    // map.addGroundOverlay(stefani);
                }

                changeFloor();

            }
                else{
                    /**
                     * mensaje de alerta, Le falta polish, dar opcion de que encienda los servicios.
                     */
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setTitle("Location Services Disabled");
                    builder.setMessage("Please turn on location services to continue with this operation.");
                    builder.setIcon(R.drawable.common_google_signin_btn_icon_dark);
                    builder.show();
                }
        }
        });




        //Metodos ya creados por el listener.
        locationListener = new LocationListener() {

            //Verifica si el gps cambio de posicion.
            @Override
            public void onLocationChanged(Location location) {
                System.out.println("Se presiono el boton" + location.getLatitude() + " " + location.getLongitude());
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            //Verifica si el gps esta apagado.
            @Override
            public void onProviderDisabled(String provider) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);

            }
        };

//Codigo para verficar el permiso de usar GPS
//Verifica el permiso para acceder el gps del manifest, con el que se espera.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //SI NO SE TIENE PERMISO ESTE IF SE EJECUTA AL COMIENZO!!!!

            //my code, PIDE EL PERMISO DE LOCATION AL USER.
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.INTERNET}, 10);

            System.out.println("1");
            configureButton();
            return;

        } else {
            //SI YA TIENE PERMISO PASA POR AQUI.
            System.out.println("2");

            configureButton();
        }



    }
    //Termina el metodo inicial, onCreate.


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 10:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    configureButton();

                    map.setMyLocationEnabled(true);
                    //Me permite usar mi propio boton.
                    map.getUiSettings().setMyLocationButtonEnabled(false);

                    System.out.println("Se ejecuto On request permision");
                }
                return;
        }
    }

    //Se encarga de ejecutar la funcion cuando se preciona el boton.
    //Refactorizar luego, remover de la funcion configure button.
    public void configureButton() {
        locateButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                //usara gps, se actualiza cada 5 segundos o cada 2 metros de diferencia, el listener es el ultimo.
                if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.

                    requestPermissions(new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.INTERNET}, 10);

                    return;
                }

                /**
                 * is Provider Enabled verifica que el dispositivo no haya apagado los servicios de localizacion
                 * Verifica que este encendido, no es directamente relacionado a los permisos.
                 */
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                    if(locationManager==null){
                        locationManager= (LocationManager)getSystemService(Context.LOCATION_SERVICE);}
                            if(locationManager.getLastKnownLocation("gps")!=null) {
                                lastLocation = new LatLng(locationManager.getLastKnownLocation("gps").getLatitude(),
                                        locationManager.getLastKnownLocation("gps").getLongitude());
                                map.setMyLocationEnabled(true);
                                map.getUiSettings().setMyLocationButtonEnabled(false);
                                map.getUiSettings().setMapToolbarEnabled(false);
                            }
                            System.out.println("im here");
//              currentMarkers.add( map.addMarker(new MarkerOptions().position(lastLocation).title("PRUEBA")) );
                    try {
                            locationManager.requestLocationUpdates("gps", 5000, 2, locationListener);
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, 20));
                    } catch (Exception e) {
                        System.out.println("Se produjo error al ubicar");
                    }
                }

                else{
                    //Muestra un mensaje al usuario sobre falta de Location.
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setTitle("Location Services Disabled");
                    builder.setMessage("Please turn on location services to continue with this operation.");
                    builder.setIcon(R.drawable.common_google_signin_btn_icon_dark);
                    builder.show();
                    //
                    map.setMyLocationEnabled(false);
                    System.out.println("El usuario no tiene los servicios de localizacion encendido");
                }
            }
                                        });

    }

@Override
    public void onMapReady(GoogleMap googleMap){
        map = googleMap;

    LatLng cords= new LatLng(18.210136,-67.139733);
   // map.addMarker(new MarkerOptions().position(cords).title("PRUEBA'"));
    map.moveCamera(CameraUpdateFactory.newLatLngZoom(cords,14));
    map.getUiSettings().setMapToolbarEnabled(false);
    map.getUiSettings().setMyLocationButtonEnabled(false);
    map.getUiSettings().setCompassEnabled(false);
    map.setMaxZoomPreference(1000);



}

    /**
     * Simulates changing floor within a building structures, currenlty with a button
     * in the future this action will be triggered by bluetooth beacons
     */
    public void changeFloor() {

        if(cycle ==0){
            if(activeOverlay !=null){ activeOverlay.remove();}

            txt.setText("edificio celis, Piso 1");
            activeOverlay= map.addGroundOverlay(celis_piso1);
        cycle++;
        while(currentMarkers.size()!=0){
            currentMarkers.get(0).remove();
            currentMarkers.remove(0);

        }
        for(Places p : allMarkers) {
            System.out.println(p.getBuilding());
            if(p.getType().equals("bathroom_w") && p.getFloor()==1){
                currentMarkers.add(map.addMarker(new MarkerOptions().position(p.getCords()).title(p.getName())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.bathroom_women)).snippet(p.getDescription())));
            }
            else if(p.getType().equals("bathroom_m") && p.getFloor()==1){
                currentMarkers.add(map.addMarker(new MarkerOptions().position(p.getCords()).title(p.getName())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.bathroom_men)).snippet(p.getDescription())));
            }
            else if(p.getType().equals("stairs") && p.getFloor()==1){
                currentMarkers.add(map.addMarker(new MarkerOptions().position(p.getCords()).title(p.getName())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.stairs_w)).snippet(p.getDescription())));
            }
            else if(p.getType().equals("elevator") && p.getFloor()==1){
                currentMarkers.add(map.addMarker(new MarkerOptions().position(p.getCords()).title(p.getName())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.elevator)).snippet(p.getDescription())));
            }
            else if(p.getFloor()==1 && p.getBuilding().equals("Celis")){
                currentMarkers.add(map.addMarker(new MarkerOptions().position(p.getCords()).title(p.getName())));
            }
            }
        }
////////////////////////////
        else if(cycle==1){
            if(activeOverlay !=null){ activeOverlay.remove();}

            txt.setText("edificio celis, Piso 2");
            txt.setVisibility(View.VISIBLE);
            activeOverlay=map.addGroundOverlay(celis_piso2);
            cycle++;

            while(currentMarkers.size()!=0){
                currentMarkers.get(0).remove();
            }
            //Testing custom icons. BitmapDescriptorFactory.fromResource(R.drawable.celis_piso1)
            for(int i=0; i<celis2.size();i++) {
                if(celis2.get(i).getType() == "bathroom"){
                currentMarkers.add(map.addMarker(new MarkerOptions().position(celis2.get(i).getCords()).title(celis2.get(i).getName())
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.bathroom_marker)).snippet("Este bano no esta identificado, esto es una prueba vamos a ver cuando estapacion coge")));
                }

                else{
                    currentMarkers.add(map.addMarker(new MarkerOptions().position(celis2.get(i).getCords()).title(celis2.get(i).getName())));
                }

            }

        }
        else if(cycle==2){
            if(activeOverlay !=null){ activeOverlay.remove();}

            while(currentMarkers.size()!=0){
                currentMarkers.get(0).remove();
            }
            for(int i=0; i<celis3.size();i++) {
                currentMarkers.add(map.addMarker(new MarkerOptions().position(celis3.get(i).getCords()).title(celis3.get(i).getName())));
            }


            txt.setText("edificio celis, Piso 3");

            activeOverlay= map.addGroundOverlay(celis_piso3);
            cycle=0;

            // Elimina los markers relacionados al overlay presentado.
            while(currentMarkers.size()!=0){
                currentMarkers.get(0).remove();
            }
        }
    }

    // Metodos que provienen de la clase de BeaconManager, requisitos para su implementacion.
    public void onBeaconServiceConnect() {
        //Empezamos a buscar beacons.
        try {
            mBeaconManager.startRangingBeaconsInRegion(mRegion);
            Log.d("Beacons", "Se comenzo la busqueda de beacons");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        //Notifica luego que se acabe el periodo de lectura
        mBeaconManager.addRangeNotifier(this);

//        Region region = new Region("all-beacons-region", null, null, null);
//        try {
//            mBeaconManager.startRangingBeaconsInRegion(region);
//            mBeaconManager.bind(this);
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
//        mBeaconManager.addRangeNotifier(this);
    }

    /**
     * Este metodo se llama cada 6s, o mejor dicho los 6000ms que se pasaron como parametros en la funcion anterior
     *
     * @param beacons
     * @param region
     */
    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        ArrayList<String> beaconListF= new ArrayList<>();

        if(beacons.size()==0){
            Toast.makeText(getApplicationContext(),"No Beacons Found", Toast.LENGTH_SHORT).show();

        }
        for(Beacon beacon : beacons){
            beaconListF.add(beacon.getBluetoothName());
//            Toast.makeText(getApplicationContext(),beacon.getBluetoothName(),Toast.LENGTH_LONG).show();
        }
        Toast.makeText(getApplicationContext(),beaconListF.toString(),Toast.LENGTH_LONG).show();


//        for (Beacon beacon: beacons) {
//            if (beacon.getServiceUuid() == 0xfeaa && beacon.getBeaconTypeCode() == 0x00) {
//                // This is a Eddystone-UID frame
//                Identifier namespaceId = beacon.getId1();
//                Identifier instanceId = beacon.getId2();
//                Log.d(TAG, "I see a beacon transmitting namespace id: "+namespaceId+
//                        " and instance id: "+instanceId+
//                        " approximately "+beacon.getDistance()+" meters away.");
//
//                // Do we have telemetry data?
//                if (beacon.getExtraDataFields().size() > 0) {
//                    long telemetryVersion = beacon.getExtraDataFields().get(0);
//                    long batteryMilliVolts = beacon.getExtraDataFields().get(1);
//                    long pduCount = beacon.getExtraDataFields().get(3);
//                    long uptime = beacon.getExtraDataFields().get(4);
//
//                    Log.d(TAG, "The above beacon is sending telemetry version "+telemetryVersion+
//                            ", has been up for : "+uptime+" seconds"+
//                            ", has a battery level of "+batteryMilliVolts+" mV"+
//                            ", and has transmitted "+pduCount+" advertisements.");
//
//                }
//            }
//        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mBeaconManager.unbind(this);

    }

    private void prepareDetection(){
        //Comienza a correr el emulador de beacons.
        TimedBeaconSimulator simulator= new TimedBeaconSimulator();
        simulator.createTimedSimulatedBeacons();
        BeaconManager.setBeaconSimulator(simulator);

        BluetoothAdapter mBluetoothApadter = BluetoothAdapter.getDefaultAdapter();
        //Verificar informacion relacionada a la disponibilidad de Bluetooth y su acceso.
        if(mBluetoothApadter ==null){
            System.out.println("Este dispositivo no soporta bluetooth");
            Toast.makeText(getApplicationContext(),"No soporta Bluetooth",Toast.LENGTH_LONG).show();
        }
        else if(mBluetoothApadter.isEnabled()){
            Toast.makeText(getApplicationContext(),"Se comenzo la busqueda de beacons",Toast.LENGTH_LONG).show();
            startDetectingBeacons();
        }
        else{
            Toast.makeText(getApplicationContext(),"Verificar bluetooth",Toast.LENGTH_LONG).show();
            Intent enableBT = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
            startActivity(enableBT);
        }
    }
    private void startDetectingBeacons(){
        //Fijar un periodo de lectura de beacons, 6000 milisegundos
        mBeaconManager.setForegroundBetweenScanPeriod(10000);
        //Enlazar con el servicio de los beacons
        mBeaconManager.bind(this);


    }
    private void stopDetectingBeacons(){
        try{
            mBeaconManager.startRangingBeaconsInRegion(mRegion);
            Toast.makeText(this,"Deteniendo la busqueda",Toast.LENGTH_SHORT).show();
        }
        catch (RemoteException e){
            Toast.makeText(this,"Se produjo un error al detener la busqueda",Toast.LENGTH_SHORT).show();

        }
        //Remover los beacons encontrados en el region
        mBeaconManager.removeAllRangeNotifiers();
        mBeaconManager.unbind(this);
    }

    private void loadAllMarkers(){
        Lector lector;
        lector = new Lector(this);
        allMarkers =lector.readLine("test.txt");
        for (Places p : allMarkers) {
            Log.d("Places from text->", Integer.toString(p.getFloor()) );

        }
    }

    private void lookFor(String query){
        boolean match=false;
        searchBar.setIconified(true);
        searchBar.setIconified(true);

        while(currentMarkers.size()!=0){
            System.out.println((currentMarkers.get(0).getTitle()));
            currentMarkers.get(0).remove();
            currentMarkers.remove(0);
        }

        for(Places p : allMarkers){
            if((p.getName().toLowerCase()).equals(query.toLowerCase())){
                match=true;
                //currentMarkers.add(map.addMarker(new MarkerOptions().position(p.getCords()).title(p.getName())));
                if(p.getType().equals("bathroom_w") ){
                    currentMarkers.add(map.addMarker(new MarkerOptions().position(p.getCords()).title(p.getName())
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.bathroom_women)).snippet(p.getDescription().concat("Floor:"+p.getFloor()))));
                }
                else if(p.getType().equals("bathroom_m") ){
                    currentMarkers.add(map.addMarker(new MarkerOptions().position(p.getCords()).title(p.getName())
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.bathroom_men)).snippet(p.getDescription().concat("Floor:"+p.getFloor()))));
                }
                else if(p.getType().equals("stairs") ){
                    currentMarkers.add(map.addMarker(new MarkerOptions().position(p.getCords()).title(p.getName())
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.stairs_w)).snippet(p.getDescription().concat("Floor:"+p.getFloor()))));
                }
                else if(p.getType().equals("elevator") ){
                    currentMarkers.add(map.addMarker(new MarkerOptions().position(p.getCords()).title(p.getName())
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.elevator)).snippet(p.getDescription().concat("Floor:"+p.getFloor()))));
                }
                else {
                    currentMarkers.add(map.addMarker(new MarkerOptions().position(p.getCords()).title(p.getName().concat("Floor:"+p.getFloor()))));
                }
            }
        }
        if(match==false){ Toast.makeText(getApplicationContext(),"No Match Found", Toast.LENGTH_SHORT).show();}
    }
    public void showPopup(View v){
        PopupMenu popup = new PopupMenu(this,v);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.popup_menu);
        popup.show();
    }
    public void showHelp(View v){
        PopupMenu help = new PopupMenu(this,v);
        help.setOnMenuItemClickListener(this);
        help.inflate(R.menu.help_menu);
        help.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        if(item.getTitle().equals("Help")){
            Toast.makeText(getApplicationContext(),"Report any problems or bugs to: bernardo.sein@upr.edu", Toast.LENGTH_LONG).show();
        }
        System.out.println(item.getTitle());
        txt.setVisibility(TextView.VISIBLE);
        if(item.getTitle().equals("Quimica 1")){
            if(activeOverlay !=null){ activeOverlay.remove();}

            txt.setText("Edificio Quimica, Piso 1");
            activeOverlay= map.addGroundOverlay(quim1);
            showMarkers("Quimica",1);
        }
        else if(item.getTitle().equals("Quimica 2")){
            if(activeOverlay !=null){ activeOverlay.remove();}

            txt.setText("Edificio Quimica, Piso 2");
            activeOverlay= map.addGroundOverlay(quim2);
            showMarkers("Quimica",2);
        }
        else if(item.getTitle().equals("Quimica 3")){
            if(activeOverlay !=null){ activeOverlay.remove();}

            txt.setText("Edificio Quimica, Piso 3");
            activeOverlay= map.addGroundOverlay(quim3);
            showMarkers("Quimica",3);
        }
        else if(item.getTitle().equals("Quimica 4")){
            if(activeOverlay !=null){ activeOverlay.remove();}

            txt.setText("Edificio Quimica, Piso 4");
            activeOverlay= map.addGroundOverlay(quim4);
            showMarkers("Quimica",4);
        }
        else if(item.getTitle().equals("Celis 1")){
            if(activeOverlay !=null){ activeOverlay.remove();}

            txt.setText("Edificio Celis, Piso 1");
            activeOverlay= map.addGroundOverlay(celis_piso1);
            showMarkers("Celis",1);
        }
        else if(item.getTitle().equals("Celis 2")){
            if(activeOverlay !=null){ activeOverlay.remove();}

            txt.setText("Edificio Celis, Piso 2");
            activeOverlay= map.addGroundOverlay(celis_piso2);
            showMarkers("Celis",2);
        }
        else if(item.getTitle().equals("Celis 3")){
            if(activeOverlay !=null){ activeOverlay.remove();}

            txt.setText("Edificio Celis, Piso 3");
            activeOverlay= map.addGroundOverlay(celis_piso3);
            showMarkers("Celis",3);
        }
        return false;
    }

    private void showMarkers(String title, int floor) {

        while(currentMarkers.size()!=0){
            currentMarkers.get(0).remove();
            currentMarkers.remove(0);
        }
        for(Places p : allMarkers){
            if((p.getBuilding().toLowerCase()).equals(title.toLowerCase()) && p.getFloor()==floor){

                //currentMarkers.add(map.addMarker(new MarkerOptions().position(p.getCords()).title(p.getName())));
                if(p.getType().equals("bathroom_w") ){
                    currentMarkers.add(map.addMarker(new MarkerOptions().position(p.getCords()).title(p.getName())
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.bathroom_women)).snippet(p.getDescription())));
                }
                else if(p.getType().equals("bathroom_m") ){
                    currentMarkers.add(map.addMarker(new MarkerOptions().position(p.getCords()).title(p.getName())
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.bathroom_men)).snippet(p.getDescription())));
                }
                else if(p.getType().equals("bathroom") ){
                    currentMarkers.add(map.addMarker(new MarkerOptions().position(p.getCords()).title(p.getName())
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.bathroom_marker)).snippet(p.getDescription())));
                }
                else if(p.getType().equals("stairs") ){
                    currentMarkers.add(map.addMarker(new MarkerOptions().position(p.getCords()).title(p.getName())
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.stairs_w)).snippet(p.getDescription())));
                }
                else if(p.getType().equals("elevator") ){
                    currentMarkers.add(map.addMarker(new MarkerOptions().position(p.getCords()).title(p.getName())
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.elevator)).snippet(p.getDescription())));
                }
                else {
                    currentMarkers.add(map.addMarker(new MarkerOptions().position(p.getCords()).title(p.getName()).snippet(p.getDescription())));
                }
            }
        }
    }
}
