package raghav.developer.geofire;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;


import org.imperiumlabs.geofirestore.GeoFirestore;

import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseAuth mAuth;
    Double myLatitude=0.0;
    Double myLongitude=0.0;
    LocationManager locationManager;
    LocationListener locationListener;
    Location lastKnownLocation;
    List<Address> listAddress;
    Button saveLocButton,cancelButton;
    GeoFirestore geoFirestore;
    CollectionReference ref;
    Double saveLoc;
    Double saveLong;
    DocumentReference locationRef;
    LatLng saveLatLng;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 15000, 0, locationListener);
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        saveLocButton = findViewById(R.id.saveLocButtonId);
        cancelButton = findViewById(R.id.cancelButtonId);


        mAuth = FirebaseAuth.getInstance();
        ref = FirebaseFirestore.getInstance().collection(mAuth.getUid());
        geoFirestore = new GeoFirestore(ref);
        locationRef = ref.document(mAuth.getUid()+"savedLocations");

        saveLocButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listAddress==null)
                    Toast.makeText(MapsActivity.this, "No location selected", Toast.LENGTH_SHORT).show();
                else{

                    geoFirestore.setLocation(mAuth.getUid()+"SAVED_LOC" +Double.toString(saveLoc+saveLong), new GeoPoint(saveLoc, saveLong), new GeoFirestore.CompletionCallback() {
                        @Override
                        public void onComplete(Exception exception) {
                            if (exception == null) {
                                Toast.makeText(MapsActivity.this, "Uploaded", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(MapsActivity.this,HomeActivity.class));
                            } else {
                                Log.i("Error:",exception.toString());
                                exception.printStackTrace();
                                Toast.makeText(MapsActivity.this, "Error. Try again!!", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
//                    locationRef.set(saveLatLng).addOnCompleteListener(new OnCompleteListener<Void>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Void> task) {
//                            if(task.isSuccessful()){
//                                Toast.makeText(MapsActivity.this, "Uploaded", Toast.LENGTH_LONG).show();
//                                startActivity(new Intent(MapsActivity.this,HomeActivity.class));
//                            }
//                            else{
//                                Toast.makeText(MapsActivity.this, "Error. Try again!!", Toast.LENGTH_LONG).show();
//                            }
//                        }
//                    });
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MapsActivity.this,HomeActivity.class));
            }
        });

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
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(listAddress!=null)
                    listAddress.clear();
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(latLng));

                Geocoder geocoder = new Geocoder(getApplicationContext(),Locale.getDefault());

                try{
                    listAddress = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
                    if(listAddress!=null)
                        Toast.makeText(MapsActivity.this,listAddress.get(0).getAddressLine(0),Toast.LENGTH_LONG).show();

                    saveLoc = latLng.latitude;
                    saveLong = latLng.longitude;
                    saveLatLng = new LatLng(saveLoc,saveLong);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                mMap.clear();
                //if(location!=null) {
                    myLatitude = location.getLatitude();
                    myLongitude = location.getLongitude();
                //}
//                if(myLatitude==null)
//                    myLatitude=0.0;
//                if(myLongitude==null)
//                    myLongitude=0.0;
                LatLng loc = new LatLng(myLatitude, myLongitude);
                mMap.addMarker(new MarkerOptions().position(loc).title("Your Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 8));

                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                try {
                    List<Address> addressList = geocoder.getFromLocation(myLatitude, myLongitude, 1);
                    if (addressList != null && addressList.size() > 0) {
                        Log.i("Places Info",addressList.get(0).toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        // Add a marker in Sydney and move the camera
//        if(Build.VERSION.SDK_INT < 23){
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
//        }
       // else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 15000, 0, locationListener);
                lastKnownLocation=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        //}


        mMap.clear();
        Double x = 0.0;
        Double y=0.0;
        //if(lastKnownLocation!=null){
            x = lastKnownLocation.getLatitude();
            y = lastKnownLocation.getLongitude();
        //}
        LatLng loc = new LatLng(x,y);
        mMap.addMarker(new MarkerOptions().position(loc).title("").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc,20));

    }
}
