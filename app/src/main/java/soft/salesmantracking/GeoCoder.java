package soft.salesmantracking;

import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class GeoCoder extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private Integer THRESHOLD = 2;
    private DelayAutoCompleteTextView geo_autocomplete;
    private ImageView geo_autocomplete_clear;
    private GoogleMap mMap;
    LocationManager locationManager;
    double startlongi,endlongi;
    double startlati, endlati;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo_coder);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync((OnMapReadyCallback) GeoCoder.this);
    }

    public void onMapSearch(View view) {
        EditText locationSearch = (EditText) findViewById(R.id.editText);
        String location = locationSearch.getText().toString();
        List<android.location.Address> addressList = null;

        if (location != null || !location.equals("")) {
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(location, 1);

            } catch (IOException e) {
                e.printStackTrace();
            }
            android.location.Address address = addressList.get(0);
            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
            mMap.addMarker(new MarkerOptions().position(latLng).title("Current Location"));
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            endlati = address.getLatitude();
            endlongi = address.getLongitude();
        }


        Location startPoint=new Location("locationA");

        startPoint.setLatitude(startlati);
        startPoint.setLongitude(startlongi);


        Location endPoint  =  new Location("locationA");

        endPoint.setLatitude(endlati);
        endPoint.setLongitude(endlongi);


        double distance = startPoint.distanceTo(endPoint)/1000;


        Toast.makeText(this, "Distence "+distance, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
//        Location location = new Location("Current");
//        LatLng sydney = new LatLng(location.getLatitude(), location.getLongitude());
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Your Current Location"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,11));
//        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
//        mMap.setMyLocationEnabled(true);
//        startlati = location.getLatitude();
//        startlongi = location.getLongitude();


    }

    @Override
    public void onLocationChanged(Location location) {




    }


//        geo_autocomplete_clear = (ImageView) findViewById(R.id.geo_autocomplete_clear);
//
//        geo_autocomplete = (DelayAutoCompleteTextView) findViewById(R.id.geo_autocomplete);
//        geo_autocomplete.setThreshold(THRESHOLD);
//        geo_autocomplete.setAdapter(new GeoAutoCompleteAdapter(this)); // 'this' is Activity instance
//
//        geo_autocomplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
//                GeoSearchResult result = (GeoSearchResult) adapterView.getItemAtPosition(position);
//                //geo_autocomplete.setText(result.getAddress());
//
//                String location = geo_autocomplete.getText().toString();
//                List<android.location.Address> addressList = null;
//
//                if (location != null || !location.equals("")) {
//                    Geocoder geocoder = new Geocoder(GeoCoder.this);
//                    try {
//                        addressList = geocoder.getFromLocationName(location, 1);
//
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    android.location.Address address = addressList.get(0);
//                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
//                    mMap.addMarker(new MarkerOptions().position(latLng).title("Marker"));
//                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
//                }
//            }
//        });
//
//        geo_autocomplete.addTextChangedListener(new TextWatcher() {
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//            }
//
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                if(s.length() > 0)
//                {
//                    geo_autocomplete_clear.setVisibility(View.VISIBLE);
//                }
//                else
//                {
//                    geo_autocomplete_clear.setVisibility(View.GONE);
//                }
//            }
//        });
//
//        geo_autocomplete_clear.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v) {
//                // TODO Auto-generated method stub
//                geo_autocomplete.setText("");
//            }
//        });





    }
