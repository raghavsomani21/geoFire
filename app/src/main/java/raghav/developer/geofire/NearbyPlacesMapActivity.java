package raghav.developer.geofire;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import org.imperiumlabs.geofirestore.GeoFirestore;
import org.imperiumlabs.geofirestore.GeoQuery;
import org.imperiumlabs.geofirestore.listeners.GeoQueryEventListener;

import java.util.List;
import java.util.Locale;

public class NearbyPlacesMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseAuth mAuth;
    Double curLat,curLong;
    LocationManager locationManager;
    LocationListener locationListener;
    Location lastKnownLocation;
    GeoQuery geoQuery;
    GeoFirestore geoFirestore;
    CollectionReference ref;

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
        setContentView(R.layout.activity_nearby_places_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mAuth = FirebaseAuth.getInstance();
        ref = FirebaseFirestore.getInstance().collection(mAuth.getUid());
        geoFirestore = new GeoFirestore(ref);

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

        locationManager =  (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mMap.clear();
                //if(location!=null) {
                curLat = location.getLatitude();
                curLong = location.getLongitude();
                //}
//                if(myLatitude==null)
//                    myLatitude=0.0;
//                if(myLongitude==null)
//                    myLongitude=0.0;
                    LatLng loc = new LatLng(curLat, curLong);
                mMap.addMarker(new MarkerOptions().position(loc).title("Your Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 8));

//                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
//                try {
//                    List<Address> addressList = geocoder.getFromLocation(curLat, curLong, 1);
//                    if (addressList != null && addressList.size() > 0) {
//                        Log.i("Places Info",addressList.get(0).toString());
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
                //GeoQuery to show nearby places

                geoQuery = geoQuery = geoFirestore.queryAtLocation(new GeoPoint(curLat, curLong), 2);
                geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                    @Override
                    public void onKeyEntered(String s, GeoPoint geoPoint) {
                        Double latitude,longitude;
                        latitude = geoPoint.getLatitude();
                        longitude = geoPoint.getLongitude();
                        LatLng loc = new LatLng(latitude,longitude);
                        mMap.addMarker(new MarkerOptions().position(loc).title("nearby").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                    }

                    @Override
                    public void onKeyExited(String s) {

                    }

                    @Override
                    public void onKeyMoved(String s, GeoPoint geoPoint) {
                        Log.i("Location Changed:","Still Nearby");
                    }

                    @Override
                    public void onGeoQueryReady() {
                        Log.i("Loading status:","Done");
                    }

                    @Override
                    public void onGeoQueryError(Exception e) {
                        Log.i("Error Occured",e.toString());
                        e.printStackTrace();
                    }
                });

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
