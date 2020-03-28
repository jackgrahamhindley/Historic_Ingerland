package com.historicingerland.myapplication;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static java.util.Locale.filter;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener, GeoQueryEventListener {

    private GoogleMap mMap;
    private static final String TAG = MapsActivity.class.getSimpleName();
    private static final float DEFAULT_ZOOM = 10f;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private DatabaseReference myLocationRef;
    private GeoFire geoFire;
    private List<LatLng> battlesite;
    private LocationRequest locationRequest;

    private Boolean FINE_LOCATION_GRANTED;
    private Boolean COARSE_LOCATION_GRANTED;
    private Boolean BACKGROUND_LOCATION_GRANTED;

    // vars
    private Boolean mLocationPermissionGranted = false;

    private void getLocationPermission() {
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
        };

        Log.d(TAG, "MA_APP STARTED");

        Context context = this.getApplicationContext();
        int granted = PackageManager.PERMISSION_GRANTED;
        List<String> deniedPermissions =
                Arrays.asList(Arrays.stream(permissions)
                        .filter(p -> ContextCompat.checkSelfPermission(context, p) != granted)
                        .toArray(size -> new String[size]));

        String debugLogMessage = "MA_denied permissions: " + String.join(", ", deniedPermissions);
        Log.d(TAG, debugLogMessage);

        if (deniedPermissions.size() > 0) {
            ActivityCompat.requestPermissions(this,
                    (String[]) deniedPermissions.toArray(),
                    getLocationRequestCode(deniedPermissions));
        } else {
            mLocationPermissionGranted = true;
        }
    }

    private int getLocationRequestCode(List<String> deniedPermissions) {
        int result = 0;
        if (deniedPermissions.contains(Manifest.permission.ACCESS_FINE_LOCATION)) {
            result += 1;
        }
        if (deniedPermissions.contains(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            result += 10;
        }
        if (deniedPermissions.contains(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
            result += 100;
        }

        Log.d(TAG, "MA_permissions CODE: " + result);
        return result;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions , @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionGranted = false;

        char[] requestCodeString = String.format("%03d%n", requestCode).toCharArray();
        FINE_LOCATION_GRANTED = requestCodeString[0] == '1';
        COARSE_LOCATION_GRANTED = requestCodeString[1] == '1';
        BACKGROUND_LOCATION_GRANTED = requestCodeString[2] == '1';

        Log.d(TAG, "MA_permissions FINE: " + FINE_LOCATION_GRANTED);
        Log.d(TAG, "MA_permissions COARSE: " + COARSE_LOCATION_GRANTED);
        Log.d(TAG, "MA_permissions BACKGROUND: " + BACKGROUND_LOCATION_GRANTED);

        mLocationPermissionGranted = FINE_LOCATION_GRANTED || COARSE_LOCATION_GRANTED || BACKGROUND_LOCATION_GRANTED;
    }

    private void initArea() {
        battlesite = new ArrayList<>();
        battlesite.add(new LatLng(51.900754, -0.199123));
        battlesite.add(new LatLng(51.535097, -0.10545));
        battlesite.add(new LatLng(51.698223, -0.185469));

        battlesite.add(new LatLng(52.22329313,-0.88526021));
        battlesite.add(new LatLng(55.23967675,-2.19193996));
        battlesite.add(new LatLng(51.38374443,-1.97212147));
        battlesite.add(new LatLng(53.16128417,-2.81514374));
        battlesite.add(new LatLng(54.77659855,-1.60133246));
        battlesite.add(new LatLng(54.97834296,-1.74551715));
        battlesite.add(new LatLng(51.39079173,-1.34960991));
        battlesite.add(new LatLng(54.37655527,-1.44413171));
        battlesite.add(new LatLng(53.9659982,-1.26005943));
        battlesite.add(new LatLng(54.09974188,-1.34512774));
        battlesite.add(new LatLng(53.07800572,-2.54530663));
        battlesite.add(new LatLng(52.41060626,-0.99797611));
        battlesite.add(new LatLng(51.04458638,-2.79846552));
        battlesite.add(new LatLng(51.43823885,-2.39823684));
        battlesite.add(new LatLng(50.88180584,-0.01479996));
        battlesite.add(new LatLng(51.71770667,0.70212723));
        battlesite.add(new LatLng(55.79255058,-2.0544988));
        battlesite.add(new LatLng(50.91201318,0.48466479));
        battlesite.add(new LatLng(55.55464597,-2.05371881));
        battlesite.add(new LatLng(52.83493027,-2.06757953));
        battlesite.add(new LatLng(52.11121148,-1.30095827));
        battlesite.add(new LatLng(52.14083037,-1.48082265));
        battlesite.add(new LatLng(52.10617289,-1.94394579));
        battlesite.add(new LatLng(55.62699054,-2.16703783));
        battlesite.add(new LatLng(52.58588933,-1.42490218));
        battlesite.add(new LatLng(50.4387642,-4.57081285));
        battlesite.add(new LatLng(51.6759895,-1.06737897));
        battlesite.add(new LatLng(51.0606848,-1.14853004));
        battlesite.add(new LatLng(53.75634567,-1.67500975));
        battlesite.add(new LatLng(51.66281103,-0.20032423));
        battlesite.add(new LatLng(52.9138191,-2.42668797));
        battlesite.add(new LatLng(54.09893388,-1.39061957));
        battlesite.add(new LatLng(50.41594812,-4.64981312));
        battlesite.add(new LatLng(50.37027359,-4.66753139));
        battlesite.add(new LatLng(52.11788515,-1.2487676));
        battlesite.add(new LatLng(53.84000207,-1.27265955));
        battlesite.add(new LatLng(53.19986964,-0.03465949));
        battlesite.add(new LatLng(52.17070842,-2.21868579));
        battlesite.add(new LatLng(53.43886578,-2.60422295));
        battlesite.add(new LatLng(53.03360369,-0.89460949));
        battlesite.add(new LatLng(51.95268271,-1.7325032));
        battlesite.add(new LatLng(50.83610582,-4.52058161));
        battlesite.add(new LatLng(51.981857,-2.16351318));
        battlesite.add(new LatLng(51.11839141,-2.92816513));
        battlesite.add(new LatLng(52.75019629,-2.72936804));
        battlesite.add(new LatLng(54.99952101,-2.963934));
        battlesite.add(new LatLng(53.98685028,-0.90336887));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        Log.d(TAG, "MA_attempting location permission" );
        getLocationPermission();
    }

    private void getDeviceLocation(){
        Log.d(TAG, "getDeviceLocation: getting the current device location" );
        FusedLocationProviderClient mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try{
            if(mLocationPermissionGranted){
                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "on complete: found");
                            Location currentLocation = (Location) task.getResult();
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                    DEFAULT_ZOOM);
                            geoFire.setLocation("Stefan", new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude()));

                        }else{
                            Log.d(TAG,"on completion: current location is null");
                            Toast.makeText(MapsActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        }catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: SecurityException" + e.getMessage());
        }
    }
    private void moveCamera(LatLng latLng, float zoom){
        Log.d(TAG, "Moving the camera to: lat: " + latLng.latitude + ", long: " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "MA_loading onMapReady" );

        mMap = googleMap;

        settingGeoFire();

        if (mLocationPermissionGranted) {
            getDeviceLocation();
            mMap.setMyLocationEnabled(true);
        }

        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(MapsActivity.this));
        mMap.setOnInfoWindowClickListener(this);


        // World heritage sites

        mMap.addMarker(new MarkerOptions().position(new LatLng(50.282, -5.036)).title("Cornwall and West Devon Mining Landscape").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000105").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(52.96, -3.116)).title("Pontcysyllte Aqueduct and Canal").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000106").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(52.956, -3.098)).title("Pontcysyllte Aqueduct and Canal").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000106").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(54.47, -3.084)).title("The English Lake District").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1452615").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51.478, -0.297)).title("Royal Botanic Gardens, Kew").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000102").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51.38, -2.367)).title("City of Bath").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000103").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(53.408, -2.993)).title("Liverpool - Maritime Mercantile City").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000104").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(53.41, -2.993)).title("Liverpool - Maritime Mercantile City").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000104").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(53.052, -1.506)).title("Derwent Valley Mills").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000100").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(53.022, -1.493)).title("Derwent Valley Mills").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000100").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(50.646, -2.671)).title("Dorset and East Devon Coast").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000101").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51.473, -0.307)).title("Royal Botanic Gardens, Kew").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000102").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(54.966, -2.622)).title("Frontiers of the Roman Empire (Hadrian's Wall)").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000098").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(54.981, -2.445)).title("Frontiers of the Roman Empire (Hadrian's Wall)").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000098").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(53.839, -1.79)).title("Saltaire").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000099").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(53.839, -1.79)).title("Saltaire").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000099").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51.499, -0.126)).title("Palace of Westminster, Westminster Abbey and St. Margaret's Church").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000095").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51.475, 0.003)).title("Maritime Greenwich").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000096").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51.478, -0.001)).title("Maritime Greenwich").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000096").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51.299, -1.836)).title("Stonehenge, Avebury and Associated Sites").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000097").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51.279, 1.087)).title("Canterbury Cathedral, St. Augustine's Abbey and St. Martin's Church").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000093").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51.279, 1.086)).title("Canterbury Cathedral, St. Augustine's Abbey and St. Martin's Church").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000093").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(54.121, -1.574)).title("Studley Royal Park including the ruins of Fountains Abbey").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000094").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(54.118, -1.573)).title("Studley Royal Park including the ruins of Fountains Abbey").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000094").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(54.774, -1.576)).title("Durham Castle and Cathedral").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000089").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(52.627, -2.47)).title("Ironbridge Gorge").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000090").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51.848, -1.373)).title("Blenheim Palace").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000091").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51.508, -0.076)).title("Tower of London").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000092").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(53.235, -2.305)).title("Jodrell Bank Observatory").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1466112").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));


        // Protected wrecks


        mMap.addMarker(new MarkerOptions().position(new LatLng(50, -5)).title("HANOVER").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000072").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(52, 2)).title("DUNWICH BANK").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000073").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(50, -4)).title("SALCOMBE CANNON SITE").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000074").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51, -1)).title("MARY ROSE").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000075").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(50, -5)).title("ROYAL ANNE").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000068").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(50, -4)).title("CORONATION OFFSHORE").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000069").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(50, -4)).title("CORONATION INSHORE").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000070").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(50, -4)).title("ERME ESTUARY").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000071").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51, -3)).title("CHURCH ROCKS").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000064").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(50, -4)).title("CATTEWATER").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000065").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(50, -6)).title("BARTHOLOMEW LEDGES").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000066").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(50, -5)).title("ST ANTHONY").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000067").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51, 1)).title("ANNE").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000060").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51, -1)).title("GRACE DIEU and the possible site of the HOLIGOST").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000061").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51, 2)).title("ADMIRAL GARDNER").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000062").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(50, -6)).title("TEARING LEDGE").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000063").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51, 2)).title("STIRLING CASTLE").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000056").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51, 2)).title("RESTORATION").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000057").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51, 2)).title("NORTHUMBERLAND").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000058").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51, 1)).title("Langdon Bay").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000059").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51, -1)).title("HMS INVINCIBLE").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000052").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51, -5)).title("GULL ROCK").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000053").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(50, -4)).title("ERME INGOT").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000054").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51, 1)).title("AMSTERDAM").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000055").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51, -1)).title("The Hazardous (formerly Le Hazardeux)").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000048").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(50, -5)).title("SCHIEDAM").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000049").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(50, -4)).title("MOOR SAND").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000050").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51, -5)).title("IONA II").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000051").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51, -1)).title("HMS/m A1").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000043").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51, -1)).title("YARMOUTH ROADS").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000044").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51, -2)).title("STUDLAND BAY WRECK").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000045").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(50, -5)).title("RILL COVE").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000046").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(55, -1)).title("SM UC-70").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1446103").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51, 0)).title("Unknown Wreck Site").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1464317").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51, -2)).title("HM submarine A3").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1422537").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51, 1)).title("SM U-8").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1430265").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51, -2)).title("Wreck of HMT Arfon").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1432595").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51, -3)).title("Unknown Wreck: Chesil Beach (Cannon Site)").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1433972").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51, 1)).title("LONDON").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000088").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51, 1)).title("Unknown Wreck (GAD8; previously known as the 'Goodwins Cannon Site')").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1401982").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51, -1)).title("Unknown Wreck off Thorness Bay").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1402103").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(50, -6)).title("Association").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1419276").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51, 0)).title("NORMAN'S BAY").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000084").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51, 2)).title("The Rooswijk").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000085").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(50, -6)).title("WHEEL WRECK").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000086").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51, -2)).title("THE NEEDLES SITE").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000087").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(54, 0)).title("FILEY BAY WRECK").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000080").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51, 1)).title("HOLLAND NO. 5").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000081").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51, -2)).title("SWASH CHANNEL WRECK").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000082").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51, -3)).title("WEST BAY").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000083").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(50, -5)).title("LOE BAR WRECK").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000076").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(55, -1)).title("SEATON CAREW").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000077").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(50, -6)).title("HMS Colossus").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000078").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(52, 1)).title("SOUTH EDINBURGH CHANNEL").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000079").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));


        // Battlefield sites


        mMap.addMarker(new MarkerOptions().position(new LatLng(53.75634567, -1.67500975)).title("Battle of Adwalton Moor 1643").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000000"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51.66281103, -0.20032423)).title("Battle of Barnet 1471").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000001"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(52.9138191, -2.42668797)).title("Battle of Blore Heath 1459").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000002"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(54.09893388, -1.39061957)).title("Battle of Boroughbridge 1322").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000003"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(52.58588933, -1.42490218)).title("Battle of Bosworth (Field) 1485").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000004"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(50.4387642, -4.57081285)).title("Battle of Braddock Down 1643").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000005"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51.6759895, -1.06737897)).title("Battle of Chalgrove 1643").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000006"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51.0606848, -1.14853004)).title("Battle of Cheriton 1644").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000007"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(52.11121148, -1.30095827)).title("Battle of Cropredy Bridge 1644").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000008"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(52.14083037, -1.48082265)).title("Battle of Edgehill 1642").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000009"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(52.10617289, -1.94394579)).title("Battle of Evesham 1265").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000010"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(55.62699054, -2.16703783)).title("Battle of Flodden 1513").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000011"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(55.79255058, -2.0544988)).title("The Site of The Battle of Halidon Hill 1333").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000012"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(50.91201318, 0.48466479)).title("Battle of Hastings 1066").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000013"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(55.55464597, -2.05371881)).title("Battle of Homildon Hill 1402").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000014"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(52.83493027, -2.06757953)).title("Battle of Hopton Heath 1643").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000015"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51.04458638, -2.79846552)).title("Battle of Langport 1645").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000016"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51.43823885, -2.39823684)).title("Battle of Lansdown (Hill) 1643").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000017"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(50.88180584, -0.01479996)).title("Battle of Lewes 1264").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000018"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51.71770667, 0.70212723)).title("Battle of Maldon 991").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000019"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(53.9659982, -1.26005943)).title("Battle of Marston Moor 1644").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000020"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(54.09974188, -1.34512774)).title("Battle of Myton 1319").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000021"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(53.07800572, -2.54530663)).title("Battle of Nantwich 1644").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000022"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(52.41060626, -0.99797611)).title("Battle of Naseby 1645").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000023"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(54.77659855, -1.60133246)).title("Battle of Neville's Cross 1346").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000024"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(54.97834296, -1.74551715)).title("Battle of Newburn Ford 1640").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000025"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51.39079173, -1.34960991)).title("Battle of Newbury 1643").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000026"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(54.37655527, -1.44413171)).title("Battle of Northallerton 1138").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000027"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(52.22329313, -0.88526021)).title("Battle of Northampton 1460").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000028"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(55.23967675, -2.19193996)).title("Battle of Otterburn 1388").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000029"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51.38374443, -1.97212147)).title("Battle of Roundway Down 1643").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000030"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(53.16128417, -2.81514374)).title("Battle of Rowton Heath 1645").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000031"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51.11839141, -2.92816513)).title("Battle of Sedgemoor 1685").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000032"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(52.75019629, -2.72936804)).title("Battle of Shrewsbury 1403").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000033"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(54.99952101, -2.963934)).title("Battle of Solway Moss 1542").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000034"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(53.98685028, -0.90336887)).title("Battle of Stamford Bridge 1066").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000035"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(53.03360369, -0.89460949)).title("Battle of Stoke (Field) 1487").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000036"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51.95268271, -1.7325032)).title("Battle of Stow (-on-the-Wold) 1646").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000037"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(50.83610582, -4.52058161)).title("Battle of Stratton 1643").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000038"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(51.981857, -2.16351318)).title("Battle of Tewkesbury 1471").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000039"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(53.84000207, -1.27265955)).title("Battle of Towton, 1461").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000040"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(53.19986964, -0.03465949)).title("Battle of Winceby 1643").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000041"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(52.17070842, -2.21868579)).title("Battle of Worcester 1651 with Powick Bridge 1642").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1000042"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(53.43886578, -2.60422295)).title("Battle of Winwick (also known as Battle of Red Bank) 1648").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1412878"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(50.41594812, -4.64981312)).title("Battle of Lostwithiel 21 August 1644").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1413619"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(50.37027359, -4.66753139)).title("Battle of Lostwithiel 31 August - 1 September 1644").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1413762"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(52.11788515, -1.2487676)).title("Battle of Edgcote 1469").snippet("https://historicengland.org.uk/listing/the-list/list-entry/1413782").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));



        initArea();

        for (LatLng latLng : battlesite){
            mMap.addCircle(new CircleOptions().center(latLng)
            .radius(15000)
                    .strokeColor(Color.BLUE)
                    .fillColor(0x220000FF)
                    .strokeWidth(5.0f)
            );

            GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(latLng.latitude, latLng.longitude), 15.0f);
            geoQuery.addGeoQueryEventListener(MapsActivity.this);
        }


    }





    private void settingGeoFire() {
        FirebaseDatabase firebaseDb = FirebaseDatabase.getInstance("https://historic-ingerland.firebaseio.com");
        myLocationRef = firebaseDb.getReference("MyLocation/Stefan");
        geoFire = new GeoFire(myLocationRef);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        String snippetStr = marker.getSnippet();
        Uri webpage = Uri.parse(snippetStr);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }


    @Override
    public void onKeyEntered(String key, GeoLocation location) {
        sendNotification("Battle site near", String.format("You are near to a famous battle site"));
    }

    @Override
    public void onKeyExited(String key) {

    }



    @Override
    public void onKeyMoved(String key, GeoLocation location) {

    }

    @Override
    public void onGeoQueryReady() {

    }

    @Override
    public void onGeoQueryError(DatabaseError error) {
        Toast.makeText(this, ""+error.getMessage(),Toast.LENGTH_SHORT).show();

    }

    private void sendNotification(String title, String content) {
            String NOTIFICATION_CHANNEL_ID = "battle_multiplelocation";
        NotificationManager notificationmanager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notification",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0,1000,500,1000});
            notificationChannel.enableVibration(true);
            notificationmanager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(false)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher));
        Notification notification = builder.build();
        notificationmanager.notify(new Random().nextInt(),notification);

    }

}
