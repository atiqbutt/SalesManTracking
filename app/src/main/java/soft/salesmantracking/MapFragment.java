package soft.salesmantracking;

import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

/**
 * Created by Malik on 14/07/2017.
 */

public class MapFragment extends Fragment implements
        OnMapReadyCallback{
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    static final Integer LOCATION = 0x1;
    double startlongi,endlongi;
    double startlati, endlati;
    TextView textView;
    LatLng latLng;
    double dLat,dLong;
    GoogleMap mGoogleMap;
    SupportMapFragment mFragment;
    Marker currLocationMarker;
    LocationManager locationManager;
    Location mLastLocation;
    ArrayList<LatLng> markerPoints;
    String location;
    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

                final View view = inflater.inflate(R.layout.mapfragment,container,false);

                mFragment = (SupportMapFragment) getFragmentManager().findFragmentById(R.id.googleMapfragment);
                mFragment.getMapAsync(this);
                return view;
        }

        @Override
        public void onMapReady(GoogleMap googleMap) {

        }
/*@Override
public void onMapReady(GoogleMap gMap) {

//

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
        // pDialog.dismiss();
        mGoogleApiClient.connect();
        Toast.makeText(this, "lll", Toast.LENGTH_SHORT).show();
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

public boolean isNetworkAvaliable(final Context ctx) {
        ConnectivityManager connectivityManager = (ConnectivityManager) ctx
        .getSystemService(Context.CONNECTIVITY_SERVICE);
        if ((connectivityManager
        .getNetworkInfo(ConnectivityManager.TYPE_MOBILE) != null && connectivityManager
        .getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED)
        || (connectivityManager
        .getNetworkInfo(ConnectivityManager.TYPE_WIFI) != null && connectivityManager
        .getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        .getState() == NetworkInfo.State.CONNECTED)) {

        EditText locationSearch = (EditText) findViewById(R.id.editTextFragment);
        location = locationSearch.getText().toString();
        List<android.location.Address> addressList = null;

        if (!TextUtils.isEmpty(locationSearch.getText().toString())) {
        Geocoder geocoder = new Geocoder(this);
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

        startPoint.setLatitude(startlati);
        startPoint.setLongitude(startlongi);


        Location endPoint = new Location("locationA");
        endPoint.setLatitude(endlati);
        endPoint.setLongitude(endlongi);

        LatLng origin = new LatLng(startlati,startlongi);
        LatLng dest = new LatLng(endlati,endlongi);

        DrawRouteMaps.getInstance(this)
        .draw(origin, dest, mGoogleMap);


        LatLngBounds bounds = new LatLngBounds.Builder()
        .include(origin)
        .include(dest).build();
        Point displaySize = new Point(500,100);
        getWindowManager().getDefaultDisplay().getSize(displaySize);
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, displaySize.x, 5000, 70));

        double distance = startPoint.distanceTo(endPoint) / 1000;

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(1); // set as you need
        String myStringmax = nf.format(distance);
        // int finald = (int) distance;
        InputMethodManager imm = (InputMethodManager) getSystemService(
        Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);



        Snackbar snackbar = Snackbar
        .make(findViewById(android.R.id.content), "Distance is  " + myStringmax + " K.m ", Snackbar.LENGTH_LONG)
        .setAction("Save", new View.OnClickListener() {
@Override
public void onClick(View v) {

        saveLocation(location,endlati,endlongi);
        Toast.makeText(ctx, "Saved Successfully.", Toast.LENGTH_SHORT).show();

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

        }


        else {

        Toast.makeText(ctx, "Enter Location..", Toast.LENGTH_SHORT).show();

//

        }
        return true;

        } else {
        Snackbar snackbar = Snackbar
        .make(findViewById(android.R.id.content), "No Internet Connection", Snackbar.LENGTH_LONG);
//        View view = snackbar.getView();


        snackbar.setDuration(10000);
        View sbView = snackbar.getView();
//
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackbar.show();
        return false;
        }
        }



public void onMapSearchFragment(View view) {

        isNetworkAvaliable(this);



        // checkConnection();

        }


@Override
public void onConnected(Bundle bundle) {
        Toast.makeText(this, "onConnected", Toast.LENGTH_SHORT).show();
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





public void statusCheck() {
final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
        buildAlertMessageNoGps();

        }
        }
//


@Override
public void onNetworkConnectionChanged(boolean isConnected) {
        showSnack(isConnected);

        }

private void checkConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        showSnack(isConnected);
        }





public void onMapSearch(View view) {

        isNetworkAvaliable(this);



        // checkConnection();

        }



@Override
public void onConnectionSuspended(int i) {
        // Toast.makeText(this,"onConnectionSuspended",Toast.LENGTH_SHORT).show();
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
        if (ContextCompat.checkSelfPermission(MapFragment.this, permission) != PackageManager.PERMISSION_GRANTED) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MapFragment.this, permission)) {
        ActivityCompat.requestPermissions(MapFragment.this, new String[]{permission}, requestCode);

        }
        else {

        ActivityCompat.requestPermissions(MapFragment.this, new String[]{permission}, requestCode);
        }

        } else {
//
        Toast.makeText(this, "" + permission + " is already granted.", Toast.LENGTH_SHORT).show();
        }
        }


//
private void buildAlertMessageNoGps() {
final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
        .setCancelable(false)
        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
public void onClick(final DialogInterface dialog, final int id) {
        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
        })
        .setNegativeButton("No", new DialogInterface.OnClickListener() {
public void onClick(final DialogInterface dialog, final int id) {
        dialog.cancel();
        }
        });
final AlertDialog alert = builder.create();
        alert.show();
        }*/






/*private void showSnack(boolean isConnected) {
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

public void saveLocation(String SearchLocation, double lat, double lng){
        AddLocaton addLocaton = new AddLocaton();
        addLocaton.LocationName = SearchLocation;
        addLocaton.LocationLat = lat;
        addLocaton.LocationLng = lng;
        addLocaton.save();
        }
        */



}
