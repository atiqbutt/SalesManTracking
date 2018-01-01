package soft.salesmantracking;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class Attendance extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, ConnectivityReceiver.ConnectivityReceiverListener {


    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    static final Integer LOCATION = 0x1;
    double startlongi, endlongi;
    double startlati, endlati, testLat,testLng;
    TextView textView;
    LatLng latLng;
    double dLat, dLong;
    GoogleMap mGoogleMap;
    SupportMapFragment mFragment;
    Marker currLocationMarker;
    LocationManager locationManager;
    Location mLastLocation;
    ArrayList<LatLng> markerPoints;
    String location;
    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);



        askForPermission(android.Manifest.permission.ACCESS_FINE_LOCATION,LOCATION);
        statusCheck();

////
        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Plaese Wait, Loading Map.");
        pDialog.setCancelable(false);
        //pDialog.show();


        mFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.googleMapnav);
        mFragment.getMapAsync(this);


        final PlaceAutocompleteFragment places= (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        places.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

//                locationSearch = (EditText) findViewById(R.id.editTextnav);
                //place.getName().toString();
                location = place.getAddress().toString();



                List<android.location.Address> addressList = null;


                    Geocoder geocoder = new Geocoder(Attendance.this);
                    try {

                        addressList = geocoder.getFromLocationName(location, 1);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    android.location.Address address = addressList.get(0);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    mGoogleMap.addMarker(new MarkerOptions().position(latLng).title(location));
                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));





                    endlati = address.getLatitude();
                    endlongi = address.getLongitude();

                    Location startPoint = new Location("locationA");

                    startPoint.setLatitude(testLat);
                    startPoint.setLongitude(testLng);


                    Location endPoint = new Location("locationA");
                    endPoint.setLatitude(endlati);
                    endPoint.setLongitude(endlongi);

                    LatLng origin = new LatLng(testLat,testLng);
                    LatLng dest = new LatLng(endlati,endlongi);

//                DrawRouteMaps.getInstance(this)
//                        .draw(origin, dest, mGoogleMap);
//
//
//                LatLngBounds bounds = new LatLngBounds.Builder()
//                        .include(origin)
//                        .include(dest).build();
//                Point displaySize = new Point(500,100);
//                getWindowManager().getDefaultDisplay().getSize(displaySize);
//                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, displaySize.x, 5000, 70));

                    String url = getUrl(origin, dest);
                    //Log.d("onMapClick", url.toString());
                    FetchUrl FetchUrl = new FetchUrl();

                    // Start downloading json data from Google Directions API
                    FetchUrl.execute(url);

                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(origin));
                    mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(12));


                    double distance = startPoint.distanceTo(endPoint) / 1000;

                    NumberFormat nf = NumberFormat.getInstance();
                    nf.setMaximumFractionDigits(1); // set as you need
                    String myStringmax = nf.format(distance);
                    // int finald = (int) distance;
                    InputMethodManager imm = (InputMethodManager) getSystemService(
                            Activity.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

//


                    Snackbar snackbar = Snackbar
                            .make(findViewById(android.R.id.content), "Distance is  " + myStringmax + " K.m ", Snackbar.LENGTH_LONG)
                            .setAction("Save", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    saveLocation(location,endlati,endlongi);
                                    Toast.makeText(Attendance.this, "Saved Successfully.", Toast.LENGTH_SHORT).show();

                                }
                            });
//        View view = snackbar.getView();


                    snackbar.setDuration(10000);
                    View sbView = snackbar.getView();
//                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) sbView.getLayoutParams();
//                params.gravity = Gravity.TOP;
//                sbView.setLayoutParams(params);
                    TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextColor(Color.WHITE);
                    snackbar.show();

                Toast.makeText(getApplicationContext(),place.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Status status) {

                Toast.makeText(getApplicationContext(),"Check Network Connection or Enter Location",Toast.LENGTH_SHORT).show();//status.toString()

            }
        });



                pDialog.dismiss();





    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.attendance, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.attendance) {
            finish();
            startActivity(new Intent(this,NavBarAttendance.class));
            overridePendingTransition(0,0);
            // Handle the camera action
        } else if (id == R.id.details) {

        }
        else if (id == R.id.MarkedList) {
            finish();
            startActivity(new Intent(this,ListOfMarkedPlaces.class));
            overridePendingTransition(0,0);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



    @Override
    public void onMapReady(GoogleMap gMap) {
        mGoogleMap = gMap;
        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading Map.");
        pDialog.setCancelable(false);


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        mGoogleMap.setMyLocationEnabled(true);
//
        buildGoogleApiClient();

        mGoogleApiClient.connect();

       // Toast.makeText(this, "lll", Toast.LENGTH_SHORT).show();
//
        List<AddLocaton> addLocations = AddLocaton.listAll(AddLocaton.class);

        //ArrayList<MarkerOptions> marketOptions = new ArrayList<MarkerOptions>();

        for(AddLocaton obj : addLocations){
            MarkerOptions MarkObj = new MarkerOptions();
            LatLng latLng = new LatLng(obj.LocationLat,obj.LocationLng);
            MarkObj.position(latLng).title(obj.LocationName);
            //marketOptions.add(MarkObj);
            mGoogleMap.addMarker(MarkObj);
        }



    }

    protected synchronized void buildGoogleApiClient() {
        Toast.makeText(this, "buildGoogleApiClient", Toast.LENGTH_SHORT).show();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

    }


//    public boolean isNetworkAvaliable(final Context ctx) {
//        ConnectivityManager connectivityManager = (ConnectivityManager) ctx
//                .getSystemService(Context.CONNECTIVITY_SERVICE);
//        if ((connectivityManager
//                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE) != null && connectivityManager
//                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED)
//                || (connectivityManager
//                .getNetworkInfo(ConnectivityManager.TYPE_WIFI) != null && connectivityManager
//                .getNetworkInfo(ConnectivityManager.TYPE_WIFI)
//                .getState() == NetworkInfo.State.CONNECTED)) {
//
//            EditText locationSearch = (EditText) findViewById(R.id.editTextnav);
//            location = locationSearch.getText().toString();
//            List<android.location.Address> addressList = null;
//
//            if (!TextUtils.isEmpty(locationSearch.getText().toString())) {
//                Geocoder geocoder = new Geocoder(this);
//                try {
//
//                    addressList = geocoder.getFromLocationName(location, 1);
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                android.location.Address address = addressList.get(0);
//                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
//                mGoogleMap.addMarker(new MarkerOptions().position(latLng).title(location));
//                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
//
//
//
//
//
//                endlati = address.getLatitude();
//                endlongi = address.getLongitude();
//
//                Location startPoint = new Location("locationA");
//
//                startPoint.setLatitude(testLat);
//                startPoint.setLongitude(testLng);
//
//
//                Location endPoint = new Location("locationA");
//                endPoint.setLatitude(endlati);
//                endPoint.setLongitude(endlongi);
//
//                LatLng origin = new LatLng(testLat,testLng);
//                LatLng dest = new LatLng(endlati,endlongi);
//
////                DrawRouteMaps.getInstance(this)
////                        .draw(origin, dest, mGoogleMap);
////
////
////                LatLngBounds bounds = new LatLngBounds.Builder()
////                        .include(origin)
////                        .include(dest).build();
////                Point displaySize = new Point(500,100);
////                getWindowManager().getDefaultDisplay().getSize(displaySize);
////                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, displaySize.x, 5000, 70));
//
//                String url = getUrl(origin, dest);
//                //Log.d("onMapClick", url.toString());
//                FetchUrl FetchUrl = new FetchUrl();
//
//                // Start downloading json data from Google Directions API
//                FetchUrl.execute(url);
//
//                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(origin));
//                mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(12));
//
//
//                double distance = startPoint.distanceTo(endPoint) / 1000;
//
//                NumberFormat nf = NumberFormat.getInstance();
//                nf.setMaximumFractionDigits(1); // set as you need
//                String myStringmax = nf.format(distance);
//                // int finald = (int) distance;
//                InputMethodManager imm = (InputMethodManager) getSystemService(
//                        Activity.INPUT_METHOD_SERVICE);
//                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
//
////
//
//
//                Snackbar snackbar = Snackbar
//                        .make(findViewById(android.R.id.content), "Distance is  " + myStringmax + " K.m ", Snackbar.LENGTH_LONG)
//                        .setAction("Save", new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//
//                                saveLocation(location,endlati,endlongi);
//                                Toast.makeText(ctx, "Saved Successfully.", Toast.LENGTH_SHORT).show();
//
//                            }
//                        });
////        View view = snackbar.getView();
//
//
//                snackbar.setDuration(10000);
//                View sbView = snackbar.getView();
////                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) sbView.getLayoutParams();
////                params.gravity = Gravity.TOP;
////                sbView.setLayoutParams(params);
//                TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
//                textView.setTextColor(Color.WHITE);
//                snackbar.show();
//
//            }
//
//
//            else {
//
//                Toast.makeText(ctx, "Enter Location..", Toast.LENGTH_SHORT).show();
//
////                Snackbar snackbar = Snackbar
////                        .make(findViewById(android.R.id.content), "Enter Location", Snackbar.LENGTH_LONG);
////                snackbar.setDuration(10000);
////                View sbView = snackbar.getView();
////                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) sbView.getLayoutParams();
////                params.gravity = Gravity.TOP;
////                sbView.setLayoutParams(params);
////                TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
////                textView.setTextColor(Color.WHITE);
////                snackbar.show();
//
//            }
//            return true;
//
//        } else {
//            Snackbar snackbar = Snackbar
//                    .make(findViewById(android.R.id.content), "No Internet Connection", Snackbar.LENGTH_LONG);
////        View view = snackbar.getView();
//
//
//            snackbar.setDuration(10000);
//            View sbView = snackbar.getView();
////            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) sbView.getLayoutParams();
////            params.gravity = Gravity.TOP;
////            sbView.setLayoutParams(params);
//            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
//            textView.setTextColor(Color.WHITE);
//            snackbar.show();
//            return false;
//        }
//    }

    public void onMapSearchNav(View view) {

        /*EditText locationSearch = (EditText) findViewById(R.id.editTextnav);

        LatLng dest = getLatitude(this,locationSearch.getText().toString());
        LatLng origin = new LatLng(testLat,testLng);

        String url = getUrl(origin, dest);
        //Log.d("onMapClick", url.toString());
        FetchUrl FetchUrl = new FetchUrl();

        // Start downloading json data from Google Directions API
        FetchUrl.execute(url);

        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(origin));
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(12));*/


        //isNetworkAvaliable(this);



        // checkConnection();

    }


    @Override
    public void onConnected(Bundle bundle) {
        //Toast.makeText(this, "onConnected", Toast.LENGTH_SHORT).show();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            //place marker at current position
            //mGoogleMap.clear();
            testLat = mLastLocation.getLatitude();
            testLng = mLastLocation.getLongitude();
            latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("Current Position");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
            currLocationMarker = mGoogleMap.addMarker(markerOptions);
        }

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(50000); //5 seconds
        mLocationRequest.setFastestInterval(30000); //3 seconds
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        //mLocationRequest.setSmallestDisplacement(0.1F); //1/10 meter

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);



    }

    @Override
    public void onConnectionSuspended(int i) {
        // Toast.makeText(this,"onConnectionSuspended",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Toast.makeText(this,"onConnectionFailed",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {



        mLastLocation = location;
        if (currLocationMarker != null) {
            currLocationMarker.remove();
        }


        //Showing Current Location Marker on Map
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);


        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        String provider = locationManager.getBestProvider(new Criteria(), true);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location locations = locationManager.getLastKnownLocation(provider);
        List<String> providerList = locationManager.getAllProviders();
        if (null != locations && null != providerList && providerList.size() > 0) {
            double longitude = locations.getLongitude();
            double latitude = locations.getLatitude();
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            try {
                List<android.location.Address> listAddresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (null != listAddresses && listAddresses.size() > 0) {

//                    Here we are finding , whatever we want our marker to show when clicked
                    String state = listAddresses.get(0).getAdminArea();
                    String country = listAddresses.get(0).getCountryName();
                    String subLocality = listAddresses.get(0).getSubLocality();
                    markerOptions.title("" + latLng + "," + subLocality + "," + state + "," + country);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            startlati =location.getLatitude();
            startlongi = location.getLongitude();
        }

        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        currLocationMarker = mGoogleMap.addMarker(markerOptions);

        //move map camera
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        //this code stops location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }



    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED) {

            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            }


            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();

        }
    }


    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(Attendance.this, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(Attendance.this, permission)) {
                ActivityCompat.requestPermissions(Attendance.this, new String[]{permission}, requestCode);

            }
            else {

                ActivityCompat.requestPermissions(Attendance.this, new String[]{permission}, requestCode);
            }

        } else {
//            if(permission== android.Manifest.permission.CALL_PHONE){
//                Intent callIntent = new Intent(Intent.ACTION_CALL);
//                callIntent.setData(Uri.parse("tel:" + "{+92 333-6386454 }"));
//                startActivity(callIntent);
//            }
//            else{
//                Intent map = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=lahore leads university,lahore pakistan"));
//                map.setPackage("com.google.android.apps.maps");
//                startActivity(map);
//            }
           // Toast.makeText(this, "" + permission + " is already granted.", Toast.LENGTH_SHORT).show();
        }
    }

    public void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();

        }
    }
    //
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.LightDialogTheme);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        finish();
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        overridePendingTransition(0,0);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }


    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showSnack(isConnected);

    }

    private void checkConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        showSnack(isConnected);
    }

    private void showSnack(boolean isConnected) {
        String message;
        int color;
        if (isConnected) {

            Snackbar snackbar = Snackbar
                    .make(findViewById(android.R.id.content), "Distance is Km", Snackbar.LENGTH_LONG);

            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(Color.WHITE);
            snackbar.show();


            // Toast.makeText(this, "Distence is"+distance+"Km", Toast.LENGTH_SHORT).show();

//            message = "Good! Connected to Internet";
//            color = Color.WHITE;
        } else {

            Snackbar snackbar = Snackbar
                    .make(findViewById(android.R.id.content), "Sorry! Not connected to internet", Snackbar.LENGTH_LONG);

            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(Color.RED);
            snackbar.show();

            // message = "Sorry! Not connected to internet";
            //color = Color.RED;
        }


    }


    private String getUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;


        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;


        return url;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            Log.d("downloadUrl", data.toString());
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }



    // Fetches data from url passed
    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }


    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask",jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask","Executing routes");
                Log.d("ParserTask",routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask",e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(7);
                lineOptions.color(Color.RED);

                Log.d("onPostExecute","onPostExecute lineoptions decoded");

            }

            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null) {
                mGoogleMap.addPolyline(lineOptions);
            }
            else {
                Log.d("onPostExecute","without Polylines drawn");
            }
        }
    }



    public  LatLng getLatitude(Context context, String city) {
        Geocoder geocoder = new Geocoder(context,context.getResources().getConfiguration().locale);
        List<Address> addresses = null;
        LatLng latLng = null;
        try {
            addresses = geocoder.getFromLocationName(city, 1);
            Address address = addresses.get(0);
            latLng = new LatLng(address.getLatitude(), address.getLongitude());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return latLng;
    }


    public void saveLocation(String SearchLocation, double lat, double lng){
        AddLocaton addLocaton = new AddLocaton();
        addLocaton.LocationName = SearchLocation;
        addLocaton.LocationLat = lat;
        addLocaton.LocationLng = lng;
        addLocaton.save();
    }






}
