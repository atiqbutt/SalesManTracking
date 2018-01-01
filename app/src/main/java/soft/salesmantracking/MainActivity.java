package soft.salesmantracking;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
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
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.maps.android.SphericalUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import cc.cloudist.acplibrary.ACProgressConstant;
import cc.cloudist.acplibrary.ACProgressFlower;

//import com.ahmadrosid.lib.drawroutemap.DrawRouteMaps;


public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, ConnectivityReceiver.ConnectivityReceiverListener
{


    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    static final Integer LOCATION = 0x1;
    double startlongi, endlongi;
    double startlati, endlati, testLat,testLng;
    TextView textView, distanceText, allowanceText, title;
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
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    boolean isOfficeRadius = false;
    public static final String Update_Location_Url = "http://www.swmapplication.com/API/update_location";
    public static final String LOGIN_URL = "http://www.swmapplication.com/API/check_in";
    public static final String CHECKOUT_URL = "http://www.swmapplication.com/API/check_out";
    public static final String CLEAR_CHECKOUT_URL = "http://www.swmapplication.com/API/clear_check_out";
    public static final String DISTANCE_URL = "http://www.swmapplication.com/API/distanceAtCheckIn";
    public static final String Update_Info_URL = "http://www.swmapplication.com/API/getUpdateInfo";
    public static final String Update_token_URL = "http://swmapplication.com/API/updateToken";
    public static final String SEND_Time_For_Online_URL = "http://swmapplication.com/API/isOnline";
    public static final String Tour_Check_In_URL = "http://swmapplication.com/API/tour_check_in";
    public static final String Tour_Check_Out_URL = "http://swmapplication.com/API/tour_check_out";
    public static final String Distance_At_Tour_CheckIn_URL = "http://swmapplication.com/API/tourDistanceAtCheckIn";
    public static final String EDIT_TOUR_URL = "http://swmapplication.com/API/edit_tour";
    public static final String UPDATE_DISTANCE_URL = "http://www.swmapplication.com/API/updateDistance";
    public static final String UPDATE_Distance_At_Tour_URL = "http://swmapplication.com/API/tourUpdateDistance";
    public static final String SEND_COORINATES_URL = "http://www.swmapplication.com/API/receiveTraveledCoordinates";

    Handler handler;

    private static final int CAMERA_REQUEST = 1888;
    ImageView dots;
    ACProgressFlower dialog;



    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }


        TextView checkInText = (TextView) findViewById(R.id.CheckInTime);
        TextView checkOutText = (TextView) findViewById(R.id.textView4);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();

        if(preferences.getString("isTour","").equalsIgnoreCase("0")){
            checkInText.setText("Checkin Time: "+preferences.getString("OfficeTimeIn",""));
            checkOutText.setText("Checkout Time: " + preferences.getString("OfficeTimeOut",""));
        }
        else {
            checkInText.setText("Start Date: "+preferences.getString("tourStart",""));
            checkOutText.setText("End Date: " + preferences.getString("tourEnd",""));
        }

        FirebaseMessaging.getInstance().subscribeToTopic("test");
        FirebaseInstanceId.getInstance().getToken();

        /*List<CheckinCheckOutTable> data = CheckinCheckOutTable.listAll(CheckinCheckOutTable.class);
        Toast.makeText(this, String.valueOf(data.size()), Toast.LENGTH_SHORT).show();
        for(CheckinCheckOutTable obj : data){
            Toast.makeText(this, String.valueOf(obj.getId()), Toast.LENGTH_SHORT).show();
        }*/

        //Toast.makeText(this,preferences.getString("TourId", ""),Toast.LENGTH_SHORT).show();
        UpdateToken(FirebaseInstanceId.getInstance().getToken());
        distanceText = (TextView) findViewById(R.id.CalDistance);
        allowanceText = (TextView) findViewById(R.id.CalAllowance);
        dots = (ImageView) findViewById(R.id.menu_dots);
        title = (TextView) findViewById(R.id.Title);
        /*if(isMyServiceRunning(CurrentPositionService.class)){
            Toast.makeText(this, "Current Position is Runing", Toast.LENGTH_SHORT).show();
        }if(isMyServiceRunning(TrackingService.class)){
            Toast.makeText(this, "Tracking Position is Runing", Toast.LENGTH_SHORT).show();
        }*/


        /*List<TraveledDistance> informations = TraveledDistance.listAll(TraveledDistance.class);
        if(informations.size() == 0){
            Toast.makeText(this, "No Stay", Toast.LENGTH_SHORT).show();
        }
        else {
            double total = 0;
            for(TraveledDistance obj : informations){
                //String Msg = obj.startTime + "\n" + obj.EndTime + "\n" + obj.distance + "\n" + obj.isSend;

                Toast.makeText(this, String.valueOf(obj.lat), Toast.LENGTH_SHORT).show();

            }

            //Toast.makeText(this, String.valueOf(total/1000), Toast.LENGTH_LONG).show();
        }*/


        dialog = new ACProgressFlower.Builder(this)
                .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                .themeColor(Color.WHITE)
                .text("Loading Map")
                .fadeColor(Color.DKGRAY).build();
        dialog.show();




        askForPermission(android.Manifest.permission.ACCESS_FINE_LOCATION,LOCATION);
        statusCheck();


////        mToolbar = (Toolbar) findViewById(R.id.toolbar);
////        setSupportActionBar(mToolbar);
////        Intent intent=new Intent("android.location.GPS_ENABLED_CHANGE");
////        intent.putExtra("enabled", true);
////        sendBroadcast(intent);
        try {

            mFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.googleMapf);
            mFragment.getMapAsync(this);
            final PlaceAutocompleteFragment places= (PlaceAutocompleteFragment)
                    getFragmentManager().findFragmentById(R.id.place_autocomplete_fragmentM);
            places.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(Place place) {

//                locationSearch = (EditText) findViewById(R.id.editTextnav);
                    //place.getName().toString();
                    location = place.getAddress().toString();





                    List<android.location.Address> addressList = null;


                    Geocoder geocoder = new Geocoder(MainActivity.this);
                    try {

                        addressList = geocoder.getFromLocationName(location, 1);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //android.location.Address address = addressList.get(0);
                    //LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    LatLng latLng = place.getLatLng();
                    mGoogleMap.addMarker(new MarkerOptions().position(latLng).title(location));
                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));





                    endlati = latLng.latitude; //address.getLatitude();
                    endlongi = latLng.longitude; //address.getLongitude();

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
                    String Aurl = AgetUrl(origin, dest);
                    //Log.d("onMapClick", url.toString());
                    MainActivity.FetchUrl FetchUrl = new MainActivity.FetchUrl();
                    MainActivity.AFetchUrl aFetchUrl = new MainActivity.AFetchUrl();

                    // Start downloading json data from Google Directions API
                    FetchUrl.execute(url);
                    aFetchUrl.execute(Aurl);

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
                                    Drawable drawable;
                                    Toast.makeText(MainActivity.this, "Saved Successfully.", Toast.LENGTH_SHORT).show();

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

        }catch (Exception e){
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }

        distanceAndAllowance();
        title.setText(preferences.getString("username",""));



        dots.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CheckinCheckOutTable attendance = CheckinCheckOutTable.findById(CheckinCheckOutTable.class, (long) 1);
                        PopupMenu popup = new PopupMenu(MainActivity.this,dots, Gravity.RIGHT);

                        AttendanceData obj = AttendanceData.findById(AttendanceData.class, (long) 1);

                        SimpleDateFormat dateFormat1 = new SimpleDateFormat("h:mm a");
                        boolean IsEnableTime = false;
                        SimpleDateFormat day = new SimpleDateFormat("yyyy-MM-dd");
                        day.setTimeZone(TimeZone.getTimeZone("GMT+05"));

                        /*if (preferences.getString("isTour","").equalsIgnoreCase("1")) {

                            popup.getMenuInflater().inflate(R.menu.menumarklist, popup.getMenu());
                        } else */if (attendance.isCheckIn && isOfficeRadius) {
                            popup.getMenuInflater().inflate(R.menu.menucheckout, popup.getMenu());

                        } else if(isOfficeRadius) {
                            editor.putString("CheckInTime", "");
                            editor.putString("CheckOutTime", "");
                            editor.putBoolean("IsCheckOut", false);
                            editor.putBoolean("IsCheckIn",false);
                            editor.apply();
                            popup.getMenuInflater().inflate(R.menu.menucheckin, popup.getMenu());
                            //Toast.makeText(this,"3",Toast.LENGTH_SHORT).show();

                        }
                        else if(!isOfficeRadius) {
                            popup.getMenuInflater().inflate(R.menu.menumarklist, popup.getMenu());
                        }


                        popup.setOnMenuItemClickListener(
                                new PopupMenu.OnMenuItemClickListener() {
                                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                                    @Override
                                    public boolean onMenuItemClick(MenuItem item) {
                                        List<TraveledDistance> distances = TraveledDistance.listAll(TraveledDistance.class);
                                        switch (item.getItemId()) {
                                            // action with ID action_refresh was selected
                                            case R.id.inCheckin:

                                                if(preferences.getString("isTour","").equalsIgnoreCase("0")){
                                                    setAutoCheckOutAlarm();
                                                }

                                                IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                                                Intent batteryStatus = MainActivity.this.registerReceiver(null, ifilter);

                                                int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                                                int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                                                float batteryPct = (level * 100) / (float)scale;

                                                final CheckinCheckOutTable obj = CheckinCheckOutTable.findById(CheckinCheckOutTable.class, Long.valueOf(1));
                                                obj.isCheckIn = true;
                                                obj.isCheckOut = false;
                                                obj.save();
                                                editor.putBoolean("IsCheckIn", true);
                                                editor.putBoolean("IsCheckOut", false);
                                                editor.putBoolean("isFirst", true);
                                                editor.putString("checkInLat", String.valueOf(testLat));
                                                editor.putString("checkInLng", String.valueOf(testLng));
                                                editor.putString("isSend","1");
                                                editor.putString("battery",String.valueOf(batteryPct));
                                                editor.apply();
                                                SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a");
                                                SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
                                                dateFormat1.setTimeZone(TimeZone.getTimeZone("GMT+05"));
                                                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+05"));
                                                String checkInTime = dateFormat.format(new Date());
                                                AddLocationData.isCheckOut = false;
                                                editor.putString("CheckInTime", checkInTime);
                                                editor.apply();

                                                if(!preferences.getString("CheckInDay","").equalsIgnoreCase(dateFormat1.format(new Date()))){

                                                    TraveledDistance.deleteAll(TraveledDistance.class);
                                                }
                                                if(haveNetworkConnection()){
                                                    editor.putString("isSend","1");
                                                    editor.apply();




                                                    if(preferences.getString("isTour","").equalsIgnoreCase("1")){
                                                    /*if(!preferences.getString("CheckInDay","").equalsIgnoreCase(dateFormat1.format(new Date()))){
                                                        UploadTourCheckIn(checkInTime);
                                                        TourDistanceOnCheckIn();
                                                    }*/
                                                        UploadTourCheckIn(checkInTime);
                                                        TourDistanceOnCheckIn();

                                                    }
                                                    else {
                                                        if(!preferences.getString("CheckInDay","").equalsIgnoreCase(dateFormat1.format(new Date()))){
                                                            UploadCheckIn(checkInTime);
                                                            DistanceOnCheckIn();
                                                            //TraveledDistance.deleteAll(TraveledDistance.class);
                                                        }
                                                        else{
                                                            ClearCheckOut();
                                                        }

                                                    }
                                                    if(!preferences.getString("CheckInDay","").equalsIgnoreCase(dateFormat1.format(new Date()))){
                                                        editor.putString("CheckInDay",dateFormat1.format(new Date()));
                                                        editor.apply();
                                                    }
                                                    sendCurrentLocation(String.valueOf(testLat),String.valueOf(testLng));

                                                    UpdateInfo();

                                                    obj.isSend = true;
                                                    obj.save();
                                                    editor.putBoolean("isSend",true);
                                                    editor.apply();


                                                    startService(new Intent(MainActivity.this, CurrentPositionService.class));
                                                    startService(new Intent(MainActivity.this, TrackingService.class));
                                                }
                                                else {
                                                    CheckinCheckOutTable isSend = CheckinCheckOutTable.findById(CheckinCheckOutTable.class, (long) 1);
                                                    isSend.isSend = false;
                                                    isSend.save();
                                                    startService(new Intent(MainActivity.this, CurrentPositionService.class));
                                                    startService(new Intent(MainActivity.this, TrackingService.class));
                                                    editor.putBoolean("isSend",false);
                                                    editor.apply();
                                                    //Toast.makeText(MainActivity.this, "Connect Internet For Check In", Toast.LENGTH_SHORT).show();
                                                }


                                                break;
                                            // action with ID action_settings was selected
                                            case R.id.inMarkList:
                                                startActivity(new Intent(MainActivity.this,ListOfMarkedPlaces.class));
                                                break;
                                            case R.id.outMarkList:
                                                startActivity(new Intent(MainActivity.this,ListOfMarkedPlaces.class));
                                                break;
                                            case R.id.outCheckout:
                                                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                                                alertDialog.setTitle("Check Out");
                                                alertDialog.setMessage("Do You Want To Check Out");
                                                alertDialog.setPositiveButton("Yes",
                                                        new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {


                                                                final CheckinCheckOutTable obj1 = CheckinCheckOutTable.findById(CheckinCheckOutTable.class, Long.valueOf(1));
                                                                obj1.isCheckOut = true;
                                                                obj1.isCheckIn = false;
                                                                obj1.save();
                                                                SimpleDateFormat dateFormat2 = new SimpleDateFormat("h:mm a");
                                                                final SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");
                                                                dayFormat.setTimeZone(TimeZone.getTimeZone("GMT+05"));
                                                                final String checkOutTime = dateFormat2.format(new Date());
                                                                editor.putBoolean("IsCheckIn", false);
                                                                editor.putBoolean("IsCheckOut", true);
                                                                editor.putString("CheckOutTime", checkOutTime);
                                                                editor.putString("checkoutLat", String.valueOf(testLat));
                                                                editor.putString("checkoutLng", String.valueOf(testLng));
                                                                editor.putString("CheckOutDay", dayFormat.format(new Date()));
                                                                editor.apply();

                                                                obj1.checkOutLat = String.valueOf(testLat);
                                                                obj1.CheckOutLng = String.valueOf(testLng);
                                                                obj1.time = checkOutTime;
                                                                obj1.save();


                                                                if(haveNetworkConnection()){



                                                                    sendTime();
                                                                    String TourId = "";
                                                                    //TraveledDistance.deleteAll(TraveledDistance.class);
                                                                    if(preferences.getString("isTour","").equalsIgnoreCase("1")){
                                                                        TourUpdateDistance();
                                                                        UploadTourCheckOut(checkOutTime);
                                                                        sendTraveledCoordinates();

                                                                        edit_tour(preferences.getString("TourId",""));
                                                                        TourId = preferences.getString("TourId","");
                                                                        editor.putString("CheckInDay","0");
                                                                        editor.apply();

                                                                        List<Tour> tours = Tour.listAll(Tour.class);
                                                                        Tour delObj = null;
                                                                        for (Tour obj : tours){
                                                                            if(obj.identity.equalsIgnoreCase(TourId)){
                                                                                delObj = obj;
                                                                            }
                                                                        }
                                                                        delObj.delete();

                                                                    }
                                                                    else {
                                                                        updateDistance();
                                                                        UploadCheckOut(checkOutTime);
                                                                        sendTraveledCoordinates();
                                                                    }
                                                                    AddLocationData.isCheckOut = true;

                                                                    editor.putString("isTour", "0");

                                                                    editor.putString("tourEnd", "0");
                                                                    editor.putString("TourId", "0");


                                                                    editor.apply();



                                                                    editor.putBoolean("isSend",true);
                                                                    editor.apply();
                                                                    obj1.isSend = true;
                                                                    obj1.save();



                                                                    //updateDistance();
                                                                    stopService(new Intent(MainActivity.this, CurrentPositionService.class));
                                                                    stopService(new Intent(MainActivity.this, TrackingService.class));
                                                                }
                                                                else {
                                                                    CheckinCheckOutTable obj2 = CheckinCheckOutTable.findById(CheckinCheckOutTable.class, (long) 1);
                                                                    obj2.isSend =false;
                                                                    obj2.save();
                                                                    editor.putBoolean("isSend",false);
                                                                    editor.apply();
                                                                    stopService(new Intent(MainActivity.this, CurrentPositionService.class));
                                                                    stopService(new Intent(MainActivity.this, TrackingService.class));
                                                                }


                                                            }
                                                        });
                                                alertDialog.setNegativeButton("No",
                                                        new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                dialogInterface.cancel();
                                                            }
                                                        });
                                                alertDialog.show();



                                                break;
                                            case R.id.markMarkedList:
                                                startActivity(new Intent(MainActivity.this,ListOfMarkedPlaces.class));
                                                break;
                                            case R.id.inTagCurrent:
                                                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                                                startActivityForResult(cameraIntent, CAMERA_REQUEST);
                                                break;
                                            case R.id.outTagCurrent:
                                                Intent cameraIntent1 = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                                                startActivityForResult(cameraIntent1, CAMERA_REQUEST);

                                                break;
                                            case R.id.markTagCurrent:
                                                CheckinCheckOutTable checkinCheckOutTable = CheckinCheckOutTable.findById(CheckinCheckOutTable.class, (long) 1);
                                                boolean isMarked = false;
                                                long markedPlaceId = 1;
                                                List<AddLocaton> locatons = AddLocaton.listAll(AddLocaton.class);
                                                for(AddLocaton addLocaton : locatons){
                                                    Location locationA = new Location("A");
                                                    locationA.setLatitude(Double.parseDouble(checkinCheckOutTable.lat));
                                                    locationA.setLongitude(Double.parseDouble(checkinCheckOutTable.lng));
                                                    Location locationB = new Location("B");
                                                    locationB.setLatitude(Double.parseDouble(String.valueOf(addLocaton.LocationLat)));
                                                    locationB.setLongitude(Double.parseDouble(String.valueOf(addLocaton.LocationLng)));
                                                    double distance = locationA.distanceTo(locationB);
                                                    if(distance <= 50){
                                                        isMarked = true;
                                                        markedPlaceId = addLocaton.getId();
                                                    }

                                                }
                                                if(isMarked){
                                                    final AlertDialog.Builder markedAlertDialog = new AlertDialog.Builder(MainActivity.this);
                                                    markedAlertDialog.setTitle("Already Marked");
                                                    markedAlertDialog.setMessage("You have already marked at this place \n If you still mark new previous place will be replaced");

                                                    final long finalMarkedPlaceId = markedPlaceId;
                                                    markedAlertDialog.setPositiveButton("Yes",
                                                            new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                                    AddLocaton locaton = AddLocaton.findById(AddLocaton.class, finalMarkedPlaceId);
                                                                    locaton.delete();
                                                                    Intent cameraIntent2 = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                                                                    startActivityForResult(cameraIntent2, CAMERA_REQUEST);
                                                                }
                                                            });
                                                    markedAlertDialog.setNegativeButton("No",
                                                            new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                                    dialogInterface.cancel();
                                                                }
                                                            });

                                                    markedAlertDialog.show();
                                                }
                                                else {
                                                    Intent cameraIntent2 = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                                                    startActivityForResult(cameraIntent2, CAMERA_REQUEST);
                                                }


                                                break;
                                            case R.id.markPath:

                                                if(distances.size() == 0){
                                                    Toast.makeText(MainActivity.this,"You did't travel yet", Toast.LENGTH_LONG).show();
                                                }
                                                else {
                                                    Intent Path = new Intent(MainActivity.this,PathMap.class);
                                                    startActivity(Path);
                                                }


                                                break;
                                            case R.id.inPath:
                                                if(distances.size() == 0){
                                                    Toast.makeText(MainActivity.this,"You did't travel yet", Toast.LENGTH_LONG).show();
                                                }
                                                else {
                                                    Intent Path1 = new Intent(MainActivity.this,PathMap.class);
                                                    startActivity(Path1);
                                                }


                                                break;
                                            case R.id.outPath:
                                                if(distances.size() == 0){
                                                    Toast.makeText(MainActivity.this,"You did't travel yet", Toast.LENGTH_LONG).show();
                                                }
                                                else {
                                                    Intent Path2 = new Intent(MainActivity.this,PathMap.class);
                                                    startActivity(Path2);
                                                }
                                                break;
                                            default:
                                                break;
                                        }
                                        return true;
                                    }
                                }
                        );
                        popup.show();
                    }
                }
        );

        /*final Handler handler = new Handler();
        Runnable refresh = null;

        final Runnable finalRefresh = refresh;
        refresh = new Runnable() {
            public void run() {
                // Do something
//                distanceAndAllowance();
                Toast.makeText(MainActivity.this, "Refrshing...", Toast.LENGTH_SHORT).show();
                handler.postDelayed(finalRefresh, 2000);
            }
        };
        handler.post(refresh);*/



        final Handler handler = new Handler();
        final int delay = 1000; //milliseconds

        handler.postDelayed(new Runnable(){
            public void run(){
                //do something
                distanceAndAllowance();
                handler.postDelayed(this, delay);
            }
        }, delay);


    }



   /* @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        menu.clear();

        SimpleDateFormat dateFormat1 = new SimpleDateFormat("h:mm a");
        boolean IsEnableTime = false;
        String time = "4:00 AM";
        try {
            Date EnableTime = dateFormat1.parse(time);
            if (dateFormat1.parse(dateFormat1.format(new Date())).after(EnableTime)) {
                IsEnableTime = true;
            }
        } catch (Exception e) {

        }

        if (preferences.getBoolean("IsCheckOut", false) && !IsEnableTime) {

            inflater.inflate(R.menu.menumarklist, menu);
        } else if (preferences.getBoolean("IsCheckIn", false) && isOfficeRadius) {
            inflater.inflate(R.menu.menucheckout, menu);

        } else if(isOfficeRadius) {
            editor.putString("CheckInTime", "");
            editor.putString("CheclOutTime", "");
            editor.putBoolean("IsCheckOut", false);
            editor.putBoolean("IsCheckIn",false);
            editor.apply();
            inflater.inflate(R.menu.menucheckin, menu);
            //Toast.makeText(this,"3",Toast.LENGTH_SHORT).show();

        }
        else {
            inflater.inflate(R.menu.menumarklist, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.inCheckin:
                editor.putBoolean("IsCheckIn", true);
                editor.putBoolean("IsCheckOut", false);
                editor.apply();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String checkInTime = dateFormat.format(new Date());

                editor.putString("CheckInTime", checkInTime);
                editor.apply();

                UploadCheckIn(checkInTime);
                DistanceOnCheckIn();

                startService(new Intent(this, CurrentPositionService.class));
                startService(new Intent(this, TrackingService.class));

                break;
            // action with ID action_settings was selected
            case R.id.inMarkList:
                startActivity(new Intent(this,ListOfMarkedPlaces.class));
                break;
            case R.id.outMarkList:
                startActivity(new Intent(this,ListOfMarkedPlaces.class));
                break;
            case R.id.outCheckout:

                SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String checkOutTime = dateFormat1.format(new Date());
                editor.putBoolean("IsCheckIn", false);
                editor.putBoolean("IsCheckOut", true);
                editor.putString("CheckOutTime", checkOutTime);
                editor.apply();

                List<TraveledDistance> distances = TraveledDistance.listAll(TraveledDistance.class);
                TraveledDistance.deleteAll(TraveledDistance.class);
                UploadCheckOut(checkOutTime);
                //updateDistance();
                stopService(new Intent(this, CurrentPositionService.class));
                stopService(new Intent(this, TrackingService.class));
                break;
            case R.id.markMarkedList:
                startActivity(new Intent(this,ListOfMarkedPlaces.class));
                break;
            case R.id.inTagCurrent:
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
                break;
            case R.id.outTagCurrent:
                Intent cameraIntent1 = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent1, CAMERA_REQUEST);

                break;
            case R.id.markTagCurrent:
                Intent cameraIntent2 = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent2, CAMERA_REQUEST);

                break;
            default:
                break;
        }


        return true;
    }*/

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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            final Bitmap photo = (Bitmap) data.getExtras().get("data");

            final EditText input = new EditText(this);
            input.setHint("Enter Location Name");
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.MarginLayoutParams.WRAP_CONTENT);
            lp.setMargins(10,10,10,10);


            input.setLayoutParams(lp);
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("Location Name");
            alertDialog.setView(input);

            alertDialog.setPositiveButton("YES",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                String password = input.getText().toString();
                                AddLocaton addLocaton = new AddLocaton();
                                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                photo.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
                                final byte[] imageBytes = byteArrayOutputStream.toByteArray();
                                final String StringPhoto = Base64.encodeToString(imageBytes,Base64.DEFAULT);

                                if(TextUtils.isEmpty(password)){
                                    input.setError("Enter Location Name");
                                }
                                else {
                                    CheckinCheckOutTable checkinCheckOutTable = CheckinCheckOutTable.findById(CheckinCheckOutTable.class, (long) 1);
                                    addLocaton.LocationName = password;
                                    addLocaton.Image = StringPhoto;
                                    addLocaton.LocationLng = Double.parseDouble(checkinCheckOutTable.lng);
                                    addLocaton.LocationLat = Double.parseDouble(checkinCheckOutTable.lat);
                                    addLocaton.save();

                                    checkinCheckOutTable.isMarked = true;
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd h-mm-ss");
                                    checkinCheckOutTable.markTime = new Date().getTime();
                                    checkinCheckOutTable.markedPlcaeId = addLocaton.getId();
                                    checkinCheckOutTable.save();

                                    /*MarkerOptions markerOptions = new MarkerOptions();
                                    markerOptions.position(new LatLng(addLocaton.LocationLat,addLocaton.LocationLng));
                                    markerOptions.title(addLocaton.LocationName);
                                    mGoogleMap.addMarker(markerOptions);*/
                                }
                            }catch (Exception e){
                                Toast.makeText(MainActivity.this, "Capture From Simple Camera", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

            alertDialog.setNegativeButton("NO",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

            alertDialog.show();

        }
    }

    protected synchronized void buildGoogleApiClient() {
        //Toast.makeText(this, "buildGoogleApiClient", Toast.LENGTH_SHORT).show();
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
            String coordinates = preferences.getString("OfficeCoordinates","");
            String separated[] = coordinates.split(",");
            float[] results = new float[4];
            Location.distanceBetween(Double.parseDouble(separated[0]), Double.parseDouble(separated[1]), mLastLocation.getLatitude(), mLastLocation.getLongitude(), results);

            if(preferences.getString("isOutdoor","").equalsIgnoreCase("Any") || preferences.getString("isTour","").equalsIgnoreCase("1")){
                isOfficeRadius = true;
            }
            else {
                if(results[0] <= 150){

                    isOfficeRadius = true;

                }
                else {
                    isOfficeRadius = false;
                }
            }
            /*testLat = mLastLocation.getLatitude();
            testLng = mLastLocation.getLongitude();*/
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

        dialog.dismiss();
        //move map camera
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        endlati = testLat = location.getLatitude();
        endlongi = testLng = location.getLongitude();

        distanceAndAllowance();
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
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission)) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);

            }
            else {

                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
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

    private String AgetUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;


        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&alternatives=true&mode-driving";

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

    public void refreshClick(View view) {
        UpdateInfo();
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

            MainActivity.ParserTask parserTask = new MainActivity.ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }

    private class AFetchUrl extends AsyncTask<String, Void, String> {

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

            MainActivity.AParserTask aparserTask = new MainActivity.AParserTask();

            // Invokes the thread for parsing the JSON data
            aparserTask.execute(result);

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

    private class AParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

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
                lineOptions.color(Color.GREEN);

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
        addLocaton.Image = "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";
        addLocaton.save();
    }

    public void distanceAndAllowance() {

        final CheckinCheckOutTable obj1 = CheckinCheckOutTable.findById(CheckinCheckOutTable.class, Long.valueOf(1));

        if(obj1.isCheckOut){
            distanceText.setText("0");
            allowanceText.setText("0");
        }
        else {
            List<TraveledDistance> distances = TraveledDistance.listAll(TraveledDistance.class);
            List<StayInformation> Stay = StayInformation.listAll(StayInformation.class);

            double totalDistance = 0;//totalAllowance = 0;
            Location locationA = null, locationB = null;
            ArrayList<LatLng> latLngs = new ArrayList<LatLng>();
            for(TraveledDistance obj : distances){
                latLngs.add(new LatLng(obj.lat,obj.lng));
            }

            totalDistance = SphericalUtil.computeLength(latLngs);



            NumberFormat nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits(2); // set as you need
            String myStringmax = nf.format(totalDistance / 1000);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            double AllowanceRate = Double.parseDouble(preferences.getString("rate", ""));
            String allowance = nf.format((totalDistance / 1000) * AllowanceRate);

            distanceText.setText(myStringmax);
            allowanceText.setText(String.valueOf(allowance +" "+ preferences.getString("currency", "")));
        }

    }

    public void UploadCheckIn(final String Time) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);


        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+05"));




        AndroidNetworking.post(LOGIN_URL)
                .addBodyParameter("id", preferences.getString("userId", ""))
                .addBodyParameter("checkIn", Time)
                .addBodyParameter("checkInLat",String.valueOf(testLat))
                .addBodyParameter("checkInLng",String.valueOf(testLng))
                .addBodyParameter("day",dateFormat.format(new Date()))
                .addBodyParameter("battery",preferences.getString("battery",""))
                .setTag("test")
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {

                        // do anything with response
                    }

                    @Override
                    public void onError(ANError error) {
                        CheckinCheckOutTable obj = CheckinCheckOutTable.findById(CheckinCheckOutTable.class, (long) 1);

                        obj.isSend = false;
                        obj.save();
                        // handle error
                    }
                });

        /*StringRequest stringRequest = new StringRequest(Request.Method.POST, LOGIN_URL,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        editor.putString("isCheckInSend","1");
                        editor.apply();


//
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        editor.putString("isCheckInSend","0");
                        editor.apply();
                        //pDialog.hide();
                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+05"));

                map.put("day",dateFormat.format(new Date()));
                map.put("checkIn", Time);
                map.put("checkInLat",String.valueOf(testLat));
                map.put("checkInLng",String.valueOf(testLng));
                map.put("id", preferences.getString("userId", ""));


                return map;
            }

        };

        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);*/
    }


    public void UploadCheckOut(final String Time) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);


        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+05"));


        AndroidNetworking.post(CHECKOUT_URL)
                .addBodyParameter("id", preferences.getString("userId", ""))
                .addBodyParameter("checkOut", Time)
                .addBodyParameter("auto", "No")
                .addBodyParameter("latitude", String.valueOf(testLat))
                .addBodyParameter("longitude", String.valueOf(testLng))
                .addBodyParameter("day",dateFormat.format(new Date()))
                .setTag("test")
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {

                        // do anything with response
                    }

                    @Override
                    public void onError(ANError error) {
                        CheckinCheckOutTable obj = CheckinCheckOutTable.findById(CheckinCheckOutTable.class, (long) 1);

                        obj.isSend = false;
                        obj.save();
                        // handle error
                    }
                });


        /*StringRequest stringRequest = new StringRequest(Request.Method.POST, CHECKOUT_URL,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        editor.putString("isCheckOutSend","1");
                        editor.apply();



//
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        editor.putString("isCheckOutSend","0");
                        editor.apply();
                        //pDialog.hide();
                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+05"));

                String coordinates = preferences.getString("OfficeCoordinates","");
                String separated[] = coordinates.split(",");
                map.put("checkOut", Time);
                map.put("auto", "No");
                map.put("latitude", String.valueOf(testLat));
                map.put("longitude", String.valueOf(testLng));
                map.put("id", preferences.getString("userId", ""));

                map.put("day",dateFormat.format(new Date()));


                return map;
            }

        };

        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);*/
    }


    public void UploadTourCheckIn(final String Time) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+05"));



        AndroidNetworking.post(Tour_Check_In_URL)
                .addBodyParameter("id", preferences.getString("userId", ""))
                .addBodyParameter("checkIn", Time)
                .addBodyParameter("checkInLat",String.valueOf(testLat))
                .addBodyParameter("checkInLng",String.valueOf(testLng))
                .addBodyParameter("tour_id", preferences.getString("TourId", ""))
                .addBodyParameter("day",dateFormat.format(new Date()))
                .setTag("test")
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {

                        // do anything with response
                    }

                    @Override
                    public void onError(ANError error) {

                        // handle error
                    }
                });

        /*StringRequest stringRequest = new StringRequest(Request.Method.POST, Tour_Check_In_URL,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        editor.putString("isCheckInSend","1");
                        editor.apply();
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            JSONObject jsonObject = jsonArray.getJSONObject(0);


                        } catch (JSONException e) {


                            e.printStackTrace();
                        }


//
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        editor.putString("isCheckInSend","0");
                        editor.apply();
                        //pDialog.hide();
                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+05"));

                map.put("day",dateFormat.format(new Date()));
                map.put("checkIn", Time);
                map.put("checkInLat",String.valueOf(testLat));
                map.put("checkInLng",String.valueOf(testLng));
                map.put("tour_id", preferences.getString("TourId", ""));
                map.put("id", preferences.getString("userId", ""));


                return map;
            }

        };

        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);*/
    }


    public void UploadTourCheckOut(final String Time) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+05"));

        AndroidNetworking.post(Tour_Check_Out_URL)
                .addBodyParameter("id", preferences.getString("userId", ""))
                .addBodyParameter("checkOut", Time)
                .addBodyParameter("tour_id", preferences.getString("TourId", ""))
                .addBodyParameter("auto", "No")
                .addBodyParameter("latitude", String.valueOf(testLat))
                .addBodyParameter("longitude", String.valueOf(testLng))
                .addBodyParameter("day",dateFormat.format(new Date()))
                .setTag("test")
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {

                        // do anything with response
                    }

                    @Override
                    public void onError(ANError error) {

                        // handle error
                    }
                });

        /*StringRequest stringRequest = new StringRequest(Request.Method.POST, Tour_Check_Out_URL,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        editor.putString("isCheckOutSend","1");
                        editor.apply();
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            JSONObject jsonObject = jsonArray.getJSONObject(0);


                        } catch (JSONException e) {


                            e.printStackTrace();
                        }


//
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        editor.putString("isCheckOutSend","0");
                        editor.apply();
                        //pDialog.hide();
                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+05"));

                String coordinates = preferences.getString("OfficeCoordinates","");
                String separated[] = coordinates.split(",");
                map.put("checkOut", Time);
                map.put("tour_id", preferences.getString("TourId", ""));
                map.put("auto", "No");
                map.put("latitude", String.valueOf(testLat));
                map.put("longitude", String.valueOf(testLng));
                map.put("id", preferences.getString("userId", ""));

                map.put("day",dateFormat.format(new Date()));


                return map;
            }

        };

        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);*/
    }

    public void DistanceOnCheckIn() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        AndroidNetworking.post(DISTANCE_URL)
                .addBodyParameter("id", preferences.getString("userId", ""))
                .addBodyParameter("tour_id", preferences.getString("TourId", ""))
                .setTag("test")
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {

                        // do anything with response
                    }

                    @Override
                    public void onError(ANError error) {
                        CheckinCheckOutTable obj = CheckinCheckOutTable.findById(CheckinCheckOutTable.class, (long) 1);

                        obj.isSend = false;
                        obj.save();
                        // handle error
                    }
                });

        /*StringRequest stringRequest = new StringRequest(Request.Method.POST, DISTANCE_URL,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        //pDialog.hide();
                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();

                map.put("id", preferences.getString("userId", ""));


                return map;
            }

        };

        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);*/
    }

    public void TourDistanceOnCheckIn() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);



        AndroidNetworking.post(Distance_At_Tour_CheckIn_URL)
                .addBodyParameter("id", preferences.getString("userId", ""))
                .addBodyParameter("tour_id", preferences.getString("TourId", ""))
                .setTag("test")
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {

                        // do anything with response
                    }

                    @Override
                    public void onError(ANError error) {

                        // handle error
                    }
                });

        /*StringRequest stringRequest = new StringRequest(Request.Method.POST, Distance_At_Tour_CheckIn_URL,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            JSONObject jsonObject = jsonArray.getJSONObject(0);


                        } catch (JSONException e) {


                            e.printStackTrace();
                        }


//
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        //pDialog.hide();
                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();

                map.put("id", preferences.getString("userId", ""));
                map.put("tour_id", preferences.getString("TourId", ""));
                return map;
            }

        };

        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);*/
    }


    public void UpdateInfo(){
        final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
        dialog.setMessage("Updating Profile...");
        dialog.setCancelable(false);
        dialog.show();

        AndroidNetworking.post(Update_Info_URL)
                .addBodyParameter("id", preferences.getString("userId", ""))
                .setTag("test")
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {

                        try{
                            JSONArray jsonArray = new JSONArray(response);
                            JSONObject jsonObject = jsonArray.getJSONObject(0);

                            String msg, coordinates, timeIn, timeOut, rate, currency, rateCurrency, username, isActive, isOutDoor;

                            //Toast.makeText(LogIn.this,"true",Toast.LENGTH_SHORT).show();
                            msg = jsonObject.getString("msg");
                            if(msg.equalsIgnoreCase("true")){
                                coordinates = jsonObject.getString("coordinates");
                                timeIn = jsonObject.getString("time_in");
                                timeOut = jsonObject.getString("time_out");
                                username = jsonObject.getString("username");
                                rateCurrency = jsonObject.getString("rate");
                                isActive = jsonObject.getString("isActive");
                                isOutDoor = jsonObject.getString("isOutdoor");
                                String[] seperated = rateCurrency.split(" ");
                                rate = seperated[0];
                                currency = seperated[1];
                                final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("OfficeCoordinates",coordinates);
                                editor.putString("OfficeTimeIn",timeIn);
                                editor.putString("OfficeTimeOut",timeOut);
                                editor.putString("rate",rate);
                                editor.putString("currency",currency);
                                editor.putString("username",username);
                                editor.putString("isOutdoor",isOutDoor);
                                editor.apply();
                                dialog.dismiss();
                                if(isActive.equalsIgnoreCase("1")){
                                    editor.putString("isActive","1");
                                    editor.apply();
                                    stopService(new Intent(MainActivity.this, CurrentPositionService.class));
                                    stopService(new Intent(MainActivity.this, TrackingService.class));
                                    finish();
                                    startActivity(new Intent(MainActivity.this,DeactiveScreen.class));

                                }
                                else {
                                    editor.putString("isActive","0");
                                    editor.apply();
                                    Toast.makeText(MainActivity.this,"Profile Updated",Toast.LENGTH_LONG).show();
                                    finish();
                                    startActivity(new Intent(MainActivity.this,MainActivity.class));
                                    overridePendingTransition(0,0);
                                }
                            }
                            else {
                                TraveledDistance.deleteAll(TraveledDistance.class);
                                AddLocaton.deleteAll(AddLocaton.class);
                                StayInformation.deleteAll(StayInformation.class);
                                editor.putString("isActive","0");
                                editor.putBoolean("ïsSignIn",false);
                                editor.apply();
                                stopService(new Intent(MainActivity.this, CurrentPositionService.class));
                                stopService(new Intent(MainActivity.this, TrackingService.class));
                                Toast.makeText(MainActivity.this,"Your Profile is Deleted",Toast.LENGTH_LONG).show();
                                finish();
                                startActivity(new Intent(MainActivity.this,LogIn.class));
                            }





                        }catch (JSONException e){

                            // Toast.makeText(LogIn.this,"Json Error",Toast.LENGTH_SHORT).show();

                            //Toast.makeText(LogIn.this,e.toString(),Toast.LENGTH_SHORT).show();

                            e.printStackTrace();
                        }


                    }


                    @Override
                    public void onError(ANError error) {

                        // handle error
                    }
                });

        /*StringRequest stringRequest = new StringRequest(Request.Method.POST, Update_Info_URL,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {


                        try{
                            JSONArray jsonArray = new JSONArray(response);
                            JSONObject jsonObject = jsonArray.getJSONObject(0);

                            String msg, coordinates, timeIn, timeOut, rate, currency, rateCurrency, username, isActive, isOutDoor;

                            //Toast.makeText(LogIn.this,"true",Toast.LENGTH_SHORT).show();
                            msg = jsonObject.getString("msg");
                            if(msg.equalsIgnoreCase("true")){
                                coordinates = jsonObject.getString("coordinates");
                                timeIn = jsonObject.getString("time_in");
                                timeOut = jsonObject.getString("time_out");
                                username = jsonObject.getString("username");
                                rateCurrency = jsonObject.getString("rate");
                                isActive = jsonObject.getString("isActive");
                                isOutDoor = jsonObject.getString("isOutdoor");
                                String[] seperated = rateCurrency.split(" ");
                                rate = seperated[0];
                                currency = seperated[1];
                                final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("OfficeCoordinates",coordinates);
                                editor.putString("OfficeTimeIn",timeIn);
                                editor.putString("OfficeTimeOut",timeOut);
                                editor.putString("rate",rate);
                                editor.putString("currency",currency);
                                editor.putString("username",username);
                                editor.putString("isOutdoor",isOutDoor);
                                editor.apply();
                                dialog.dismiss();
                                if(isActive.equalsIgnoreCase("1")){
                                    editor.putString("isActive","1");
                                    editor.apply();
                                    stopService(new Intent(MainActivity.this, CurrentPositionService.class));
                                    stopService(new Intent(MainActivity.this, TrackingService.class));
                                    finish();
                                    startActivity(new Intent(MainActivity.this,DeactiveScreen.class));

                                }
                                else {
                                    editor.putString("isActive","0");
                                    editor.apply();
                                    Toast.makeText(MainActivity.this,"Profile Updated",Toast.LENGTH_LONG).show();
                                    finish();
                                    startActivity(new Intent(MainActivity.this,MainActivity.class));
                                    overridePendingTransition(0,0);
                                }
                            }
                            else {
                                TraveledDistance.deleteAll(TraveledDistance.class);
                                AddLocaton.deleteAll(AddLocaton.class);
                                StayInformation.deleteAll(StayInformation.class);
                                editor.putString("isActive","0");
                                editor.putBoolean("ïsSignIn",false);
                                editor.apply();
                                stopService(new Intent(MainActivity.this, CurrentPositionService.class));
                                stopService(new Intent(MainActivity.this, TrackingService.class));
                                Toast.makeText(MainActivity.this,"Your Profile is Deleted",Toast.LENGTH_LONG).show();
                                finish();
                                startActivity(new Intent(MainActivity.this,LogIn.class));
                            }





                        }catch (JSONException e){

                            // Toast.makeText(LogIn.this,"Json Error",Toast.LENGTH_SHORT).show();

                            //Toast.makeText(LogIn.this,e.toString(),Toast.LENGTH_SHORT).show();

                            e.printStackTrace();
                        }


                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dialog.dismiss();
                        Toast.makeText(MainActivity.this,"Connection Failed", Toast.LENGTH_LONG ).show();

                    }
                }){

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> map = new HashMap<String,String>();

                map.put("id", preferences.getString("userId", ""));


                return map;
            }

        };

        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);*/
    }

    public void UpdateToken(final String token){

        AndroidNetworking.post(Update_token_URL)
                .addBodyParameter("token",token)
                .addBodyParameter("lng", preferences.getString("userId", ""))
                .setTag("test")
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {

                        // do anything with response
                    }

                    @Override
                    public void onError(ANError error) {

                        // handle error
                    }
                });

        /*StringRequest stringRequest = new StringRequest(Request.Method.POST, Update_token_URL,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }){

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> map = new HashMap<String,String>();

                map.put("lng", preferences.getString("userId", ""));
                map.put("token", token);

                return map;
            }

        };

        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);*/
    }


    void sendTime() {

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, SEND_Time_For_Online_URL,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
//
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Toast.makeText(CurrentPositionService.this,"updateDistance Error", Toast.LENGTH_LONG ).show();//error.toString()
                        //pDialog.hide();
                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+05"));

                map.put("id", preferences.getString("userId", ""));
                map.put("time", "0:00 0");
                       /* map.put("longitude", String.valueOf(obj.lng));
                        map.put("latitude", String.valueOf(obj.lat));*/

                return map;
            }

        };

        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }


    public void sendCurrentLocation(final String lat, final String lng){
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        AndroidNetworking.post(Update_Location_Url)
                .addBodyParameter("id", preferences.getString("userId", ""))
                .addBodyParameter("latitude",lat)
                .addBodyParameter("longitude",lng)
                .setTag("test")
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {

                        // do anything with response
                    }

                    @Override
                    public void onError(ANError error) {

                        // handle error
                    }
                });
        /*StringRequest stringRequest = new StringRequest(Request.Method.POST, Update_Location_Url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        //Toast.makeText(CurrentPositionService.this,"Current Location Sended", Toast.LENGTH_LONG ).show();//error.toString()
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        //Toast.makeText(CurrentPositionService.this,error.toString(), Toast.LENGTH_LONG ).show();//error.toString()
                        //pDialog.hide();
                    }
                }){

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> map = new HashMap<String,String>();

                map.put("latitude",lat);
                map.put("longitude",lng);
                map.put("id",preferences.getString("userId",""));

                return map;
            }

        };

        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(stringRequest);*/
    }


    public void ClearCheckOut() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+05"));


        AndroidNetworking.post(CLEAR_CHECKOUT_URL)
                .addBodyParameter("id", preferences.getString("userId", ""))
                .addBodyParameter("checkOut", "")
                .addBodyParameter("auto", "")
                .addBodyParameter("latitude", "")
                .addBodyParameter("longitude", "")
                .addBodyParameter("day",dateFormat.format(new Date()))
                .setTag("test")
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {

                        // do anything with response
                    }

                    @Override
                    public void onError(ANError error) {

                        // handle error
                    }
                });
        /*StringRequest stringRequest = new StringRequest(Request.Method.POST, CLEAR_CHECKOUT_URL,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        editor.putString("isCheckOutSend","1");
                        editor.apply();



//
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        editor.putString("isCheckOutSend","0");
                        editor.apply();
                        //pDialog.hide();
                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+05"));

                String coordinates = preferences.getString("OfficeCoordinates","");
                String separated[] = coordinates.split(",");
                map.put("checkOut", "");
                map.put("auto", "");
                map.put("latitude", "");
                map.put("longitude", "");
                map.put("id", preferences.getString("userId", ""));

                map.put("day",dateFormat.format(new Date()));


                return map;
            }

        };

        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);*/
    }

    public void edit_tour(final String tourId){
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        AndroidNetworking.post(EDIT_TOUR_URL)
                .addBodyParameter("id", preferences.getString("userId", ""))
                .addBodyParameter("tour_id", tourId)
                .setTag("test")
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {

                        // do anything with response
                    }

                    @Override
                    public void onError(ANError error) {

                        // handle error
                    }
                });

        /*StringRequest stringRequest = new StringRequest(Request.Method.POST, EDIT_TOUR_URL,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        *//*editor.putString("isCheckOutSend","1");
                        editor.apply();*//*
//
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                       *//* editor.putString("isCheckOutSend","0");
                        editor.apply();*//*
                        //pDialog.hide();
                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();

                map.put("id", preferences.getString("userId", ""));
                map.put("tour_id", tourId);

                return map;
            }

        };

        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);*/

    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }



    void updateDistance(){
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        List<TraveledDistance> distances = TraveledDistance.listAll(TraveledDistance.class);
        List<StayInformation> Stay = StayInformation.listAll(StayInformation.class);

        double totalDistance = 0;//totalAllowance = 0;
        Location locationA = null, locationB = null;
        ArrayList<LatLng> latLngs = new ArrayList<LatLng>();
        for(TraveledDistance obj : distances){
            latLngs.add(new LatLng(obj.lat,obj.lng));
        }

        totalDistance = SphericalUtil.computeLength(latLngs);



        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2); // set as you need
        String myStringmax = nf.format(totalDistance / 1000);
        //SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        double AllowanceRate = Double.parseDouble(preferences.getString("rate", ""));
        String allowance = nf.format((totalDistance / 1000) * AllowanceRate);

        AndroidNetworking.post(UPDATE_DISTANCE_URL)
                .addBodyParameter("id", preferences.getString("userId", ""))
                .addBodyParameter("distance", myStringmax)
                .addBodyParameter("allowance", allowance)
                .setTag("test")
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {

                        // do anything with response
                    }
                    @Override
                    public void onError(ANError error) {

                        // handle error
                    }
                });

    }


    void TourUpdateDistance() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        List<TraveledDistance> distances = TraveledDistance.listAll(TraveledDistance.class);
        List<StayInformation> Stay = StayInformation.listAll(StayInformation.class);

        double totalDistance = 0;//totalAllowance = 0;
        Location locationA = null, locationB = null;
        ArrayList<LatLng> latLngs = new ArrayList<LatLng>();
        for(TraveledDistance obj : distances){
            latLngs.add(new LatLng(obj.lat,obj.lng));
        }

        totalDistance = SphericalUtil.computeLength(latLngs);



        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2); // set as you need
        String myStringmax = nf.format(totalDistance / 1000);
        //SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        double AllowanceRate = Double.parseDouble(preferences.getString("rate", ""));
        String allowance = nf.format((totalDistance / 1000) * AllowanceRate);

        AndroidNetworking.post(UPDATE_Distance_At_Tour_URL)
                .addBodyParameter("id", preferences.getString("userId", ""))
                .addBodyParameter("tour_id", preferences.getString("TourId", ""))
                .addBodyParameter("distance", myStringmax)
                .addBodyParameter("allowance", allowance)
                .setTag("test")
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {

                        // do anything with response
                    }

                    @Override
                    public void onError(ANError error) {

                        // handle error
                    }
                });
    }

    void sendTraveledCoordinates() {

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        List<TraveledDistance> distances = TraveledDistance.listAll(TraveledDistance.class);
        for (final TraveledDistance obj : distances) {

            if (!obj.isSend) {
                TraveledDistance forSendTrue = TraveledDistance.findById(TraveledDistance.class, obj.getId());
                forSendTrue.isSend = true;
                forSendTrue.save();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+05"));

                AndroidNetworking.post(SEND_COORINATES_URL)
                        .addBodyParameter("id", preferences.getString("userId", ""))
                        .addBodyParameter("date", dateFormat.format(new Date()))
                        .addBodyParameter("longitude", String.valueOf(obj.lng))
                        .addBodyParameter("latitude", String.valueOf(obj.lat))
                        .setTag("test")
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsString(new StringRequestListener() {
                            @Override
                            public void onResponse(String response) {

                                // do anything with response
                            }

                            @Override
                            public void onError(ANError error) {
                                TraveledDistance forSendTrue = TraveledDistance.findById(TraveledDistance.class, obj.getId());
                                forSendTrue.isSend = false;
                                forSendTrue.save();
                                // handle error
                            }
                        });
            }
        }
    }

    public void setAutoCheckOutAlarm(){
        String checkoutTime = preferences.getString("OfficeTimeOut","");
        String[] sepertedOne = checkoutTime.split(":");
        int hour = Integer.parseInt(sepertedOne[0]);
        String[] seperatedTwo = sepertedOne[1].split(" ");
        int min = Integer.parseInt(seperatedTwo[0]);

        if(seperatedTwo[1].equalsIgnoreCase("PM")){
            if(hour != 12){
                hour += 12;
            }
        }
        else {
            if(hour == 12){
                hour = 00;
            }
        }

        AlarmManager alarmMgr0 = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

//Create pending intent & register it to your alarm notifier class
        Intent intent0 = new Intent(this, AutoCheckOutReceiver.class);
        PendingIntent pendingIntent0 = PendingIntent.getBroadcast(this, 0, intent0, PendingIntent.FLAG_CANCEL_CURRENT);

        alarmMgr0.cancel(pendingIntent0);
//set timer you want alarm to work (here I have set it to 7.20pm)
        //Intent intent0 = new Intent(this, OldEntryRemover.class);
        Calendar timeOff9 = Calendar.getInstance();
        timeOff9.setTimeInMillis(System.currentTimeMillis());


        timeOff9.set(Calendar.HOUR_OF_DAY, hour);
        timeOff9.set(Calendar.MINUTE, min);
        timeOff9.set(Calendar.SECOND, 0);

        alarmMgr0.setRepeating(AlarmManager.RTC_WAKEUP, timeOff9.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pendingIntent0);
        //alarmMgr0.set(AlarmManager.RTC_WAKEUP, timeOff9.getTimeInMillis(), pendingIntent0);
    }


    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }




}