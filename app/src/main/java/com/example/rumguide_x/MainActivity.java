package com.example.rumguide_x;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.widget.Button;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 *On Map Ready Call back es para el google maps
 * Beacon Consumer es para manejar el consumo de la informacion de los beacons
 */

public class MainActivity extends FragmentActivity implements OnMapReadyCallback,BeaconConsumer, RangeNotifier {
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


    private Button locateButton;
    private TextView txt;
    private LatLng lastLocation;
    private Button changeFloorButton;
    private Button layerButton;




    //Maneja el location.
    private LocationManager locationManager;
    //esta pendiente del cambio en localizacion.
    private LocationListener locationListener;

    private BluetoothManager bluetoothManager;

    private BeaconManager mBeaconManager =null;



    //Lista de los markers activos
    private ArrayList<Marker> currentMarkers = new ArrayList<>();
    //
//    private ArrayList<Marker> interestPoints = new ArrayList<>();
    private ArrayList<Places> celis1 = new ArrayList<>();
    private ArrayList<Places> celis2 = new ArrayList<>();
    private ArrayList<Places> celis3 = new ArrayList<>();


    //Overlays disponibles, floor plans
    GroundOverlayOptions celis_piso1;
    GroundOverlayOptions celis_piso2;
    GroundOverlayOptions celis_piso3;

//Maneja los dispositivos Bluetooth, Aun no se utiliza.
    private BluetoothAdapter BA;
    private Set<BluetoothDevice> pairedDevices;

    //Manejador de Beacons.



    //Al momento de correr la aplicacion
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Asigna el mapa al fragment del view.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
                    mapFragment.getMapAsync(this);


        cycle = 0;

        //Manejador de Bluetooth beacons.
//        beaconManager =BeaconManager.getInstanceForApplication(this);
//        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(EDDYSTONE));
//        beaconManager.bind(this);

        mBeaconManager = BeaconManager.getInstanceForApplication(this.getApplicationContext());
        // Detect the main Eddystone-UID frame:
        mBeaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
        // Detect the telemetry Eddystone-TLM frame:
        mBeaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_TLM_LAYOUT));
        mBeaconManager.bind(this);




        //variables de prueba, Points of interest
        celis1.add(new Places("C-100", new LatLng(18.2095799, -67.1412221)));
        celis1.add(new Places("C-101", new LatLng(18.2095064, -67.1413221)));
        celis1.add(new Places("C-102", new LatLng(18.2094404, -67.1412221)));
        celis1.add(new Places("C-103", new LatLng(18.2093860, -67.1410386)));
        celis1.add(new Places("C-104", new LatLng(18.2094305, -67.1411221)));
        celis1.add(new Places("C-105", new LatLng(18.2094205, -67.1410221)));


        celis2.add(new Places("C-200", new LatLng(18.2094385, -67.14100001)));
        celis2.add(new Places("C-201", new LatLng(18.2092365, -67.1412521)));
        celis2.add(new Places("C-202", new LatLng(18.2093345, -67.1410241)));
        celis2.add(new Places("C-203", new LatLng(18.2094325, -67.1413261)));
        celis2.add(new Places("C-204", new LatLng(18.2091305, -67.1412281)));
        celis2.add(new Places("C-205", new LatLng(18.2090405, -67.1410321)));


        //Manejando lo que hacen los botones.
        //Find view by id busca el elemento del view.
        //Para el boton en este caso es el id locateme.
        //Esto asocia la funcion con el boton.
        locateButton = (Button) findViewById(R.id.locateme);
        txt = findViewById(R.id.TEXTOPRUEBA);

        //Boton de simulacion de cambio de pisos.
        changeFloorButton = findViewById(R.id.floorChange);

        layerButton = findViewById(R.id.layerButton);
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


        //Localizacion del edificio de Celis
        LatLng celis = new LatLng(18.2093602, -67.1408958);

        //Definir los GrounOverlays para usarse en Celis por ahora.
        celis_piso1 = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.celis_piso1))
                .position(celis, 100).bearing(15);

        celis_piso2 = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.celis_piso2))
                .position(celis, 100).bearing(15);

        celis_piso3 = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.celis_piso3))
                .position(celis, 100).bearing(15);


        changeFloorButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {

                if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                // map.setMyLocationEnabled(true);
                //usara gps, se actualiza cada 5 segundos o cada 2 metros de diferencia, el listener es el ultimo.
                    lastLocation = new LatLng(locationManager.getLastKnownLocation("gps").getLatitude(),
                        locationManager.getLastKnownLocation("gps").getLongitude());

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

        bluetoothManager= (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);

        // Maneja el uso del gps.
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);


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

        BA = BluetoothAdapter.getDefaultAdapter();

    }




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
    public void configureButton() {
        locateButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                System.out.println(Manifest.permission.ACCESS_FINE_LOCATION);

                System.out.println(PackageManager.PERMISSION_GRANTED);


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
                            lastLocation = new LatLng(locationManager.getLastKnownLocation("gps").getLatitude(),
                            locationManager.getLastKnownLocation("gps").getLongitude());
                            map.setMyLocationEnabled(true);
                            map.getUiSettings().setMyLocationButtonEnabled(false);
                            System.out.println("im here");
//              currentMarkers.add( map.addMarker(new MarkerOptions().position(lastLocation).title("PRUEBA")) );
                    try {
                            locationManager.requestLocationUpdates("gps", 5000, 2, locationListener);
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, 20));
                    } catch (Exception e) {
                        System.out.println("Algo raro paso");
                    }
                }

                else{

                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setTitle("Location Services Disabled");
                    builder.setMessage("Please turn on location services to continue with this operation.");
                    builder.setIcon(R.drawable.common_google_signin_btn_icon_dark);
                    builder.show();


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

    map.setMaxZoomPreference(500);

}

    /**
     * Simulates changing floor within a building structures, currenlty with a button
     * in the future this action will be triggered by bluetooth beacons
     */
    public void changeFloor() {

        if(cycle ==0){
            if(activeOverlay !=null){
            activeOverlay.remove();}

            txt.setText("edificio celis, Piso 1");
            activeOverlay= map.addGroundOverlay(celis_piso1);
        cycle++;
        while(currentMarkers.size()!=0){
            currentMarkers.get(0).remove();
        }
        for(int i=0; i<celis1.size();i++) {
            currentMarkers.add(map.addMarker(new MarkerOptions().position(celis1.get(i).getCords()).title(celis1.get(i).getName())));
            }
        }

////////////////////////////
        else if(cycle==1){
            if(activeOverlay !=null){
                activeOverlay.remove();}


            txt.setText("edificio celis, Piso 2");
            activeOverlay=map.addGroundOverlay(celis_piso2);
            cycle++;

            while(currentMarkers.size()!=0){
                currentMarkers.get(0).remove();
            }
            for(int i=0; i<celis2.size();i++) {
                currentMarkers.add(map.addMarker(new MarkerOptions().position(celis2.get(i).getCords()).title(celis2.get(i).getName())));

            }

        }
        else if(cycle==2){
            if(activeOverlay !=null){
                activeOverlay.remove();}

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
        Region region = new Region("all-beacons-region", null, null, null);
        try {
            mBeaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        mBeaconManager.addRangeNotifier(this);
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        for (Beacon beacon: beacons) {
            if (beacon.getServiceUuid() == 0xfeaa && beacon.getBeaconTypeCode() == 0x00) {
                // This is a Eddystone-UID frame
                Identifier namespaceId = beacon.getId1();
                Identifier instanceId = beacon.getId2();
                Log.d(TAG, "I see a beacon transmitting namespace id: "+namespaceId+
                        " and instance id: "+instanceId+
                        " approximately "+beacon.getDistance()+" meters away.");

                // Do we have telemetry data?
                if (beacon.getExtraDataFields().size() > 0) {
                    long telemetryVersion = beacon.getExtraDataFields().get(0);
                    long batteryMilliVolts = beacon.getExtraDataFields().get(1);
                    long pduCount = beacon.getExtraDataFields().get(3);
                    long uptime = beacon.getExtraDataFields().get(4);

                    Log.d(TAG, "The above beacon is sending telemetry version "+telemetryVersion+
                            ", has been up for : "+uptime+" seconds"+
                            ", has a battery level of "+batteryMilliVolts+" mV"+
                            ", and has transmitted "+pduCount+" advertisements.");

                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mBeaconManager.unbind(this);

    }



}
