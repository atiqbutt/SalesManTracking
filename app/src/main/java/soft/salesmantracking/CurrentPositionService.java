package soft.salesmantracking;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Shah on 7/8/2017.
 */

public class CurrentPositionService extends Service {


    private static final String TAG = "BOOMBOOMTESTGPS";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 200f;
    double longitutde, latitude;
    public static final String LOGIN_URL = "http://www.swmapplication.com/API/update_location";
    public static final String CHECKOUT_URL = "http://www.swmapplication.com/API/check_out";
    public static final String DISTANCE_URL = "http://www.swmapplication.com/API/updateDistance";
    public static final String SEND_COORINATES_URL = "http://www.swmapplication.com/API/receiveTraveledCoordinates";
    public static final String SEND_Time_For_Online_URL = "http://swmapplication.com/API/isOnline";
    public static final String Distance_At_Tour_URL = "http://swmapplication.com/API/tourUpdateDistance";
    long previousTime;
    private Timer timer = new Timer();
    private boolean started = false;
    private Handler handler = new Handler();
    Runnable runnable;

    Location lastLocation = null;
    String endTiming = "17";

    boolean isNull = true;


    private class LocationListener implements android.location.LocationListener
    {
        Location mLastLocation;

        public LocationListener(String provider)
        {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location)
        {
            isNull = false;
            //String a = "Current Location: " + location.getLatitude() + ", " + location.getLongitude();
            //Toast.makeText(CurrentPositionService.this,a,Toast.LENGTH_SHORT).show();
            CheckinCheckOutTable checkinCheckOutTable = CheckinCheckOutTable.findById(CheckinCheckOutTable.class, Long.valueOf(1));

             checkinCheckOutTable.lat = String.valueOf(location.getLatitude());
            checkinCheckOutTable.lng = String.valueOf(location.getLongitude());

            checkinCheckOutTable.save();

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(CurrentPositionService.this);

            /*if(lastLocation != location){
                TraveledDistance traveledDistance = new TraveledDistance();
                traveledDistance.lat = location.getLatitude();
                traveledDistance.lng = location.getLongitude();
                traveledDistance.save();
            }*/
            TraveledDistance traveledDistance = new TraveledDistance();
            traveledDistance.lat = location.getLatitude();
            traveledDistance.lng = location.getLongitude();
            traveledDistance.save();

            List<TraveledDistance> distances = TraveledDistance.listAll(TraveledDistance.class);

            double totalDistance = 0, totalAllowance = 0;
            Location locationA = null, locationB = null;
            ArrayList<LatLng> latLngs = new ArrayList<LatLng>();
            if(distances.size() != 0){
                for(TraveledDistance obj : distances){
                    latLngs.add(new LatLng(obj.lat,obj.lng));
                }
                totalDistance = SphericalUtil.computeLength(latLngs);
            }

            double AllowanceRate = Double.parseDouble(preferences.getString("rate",""));


            if(haveNetworkConnection()){
                String lat = String.valueOf(location.getLatitude());
                String lng = String.valueOf(location.getLongitude());
                sendCurrentLocation(lat,lng);
                sendTraveledCoordinates();

                NumberFormat nf = NumberFormat.getInstance();
                nf.setMaximumFractionDigits(2); // set as you need
                String myStringmax = nf.format(totalDistance/1000);
                String allowance = nf.format((totalDistance/1000) * AllowanceRate);
                if(preferences.getString("isTour","").equalsIgnoreCase("1")){
                    TourUpdateDistance(myStringmax,allowance);
                }
                else {
                    updateDistance(myStringmax,allowance);

                }
            }
            lastLocation = location;

            mLastLocation.set(location);

        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(CurrentPositionService.this);
        SharedPreferences.Editor editor = preferences.edit();

        //Toast.makeText(this, "Current Service Starts", Toast.LENGTH_SHORT).show();


        previousTime = new Date().getTime();
        editor.putLong("isOnlineTime",new Date().getTime());
        editor.apply();

        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {

        //startForeground(1, new Notification());

        AndroidNetworking.initialize(getApplicationContext());
        Thread t = Thread.currentThread();
        t.setDefaultUncaughtExceptionHandler(new MyThreadUncaughtExceptionHandler());

        initializeLocationManager();





        /*try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {

            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }*/
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+05"));
        final String time = preferences.getString("OfficeTimeOut","");
        //Toast.makeText(CurrentPositionService.this,time,Toast.LENGTH_SHORT).show();
        final Date[] EndTime = {null};
        final Date[] CurrentTime = {null};
        final boolean[] isEnd = {false};
        final SharedPreferences.Editor editor = preferences.edit();
        final SimpleDateFormat dateFormat1 = new SimpleDateFormat("h:mm a");
        final SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd hh mm s");
        dateFormat1.setTimeZone(TimeZone.getTimeZone("GMT+05"));
        dateFormat2.setTimeZone(TimeZone.getTimeZone("GMT+05"));
        //Toast.makeText(this, "Entering in Loop", Toast.LENGTH_SHORT).show();
        new Thread(new Runnable(){
            @Override
            public void run() {
                while(true){
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    long currentTime = new Date().getTime();

                    long diff = currentTime - previousTime;


                    if(haveNetworkConnection() && diff >= 2 * 60 * 1000){
                        List<TraveledDistance> distances = TraveledDistance.listAll(TraveledDistance.class);

                        double totalDistance = 0, totalAllowance = 0;
                        Location locationA = null, locationB = null;
                        ArrayList<LatLng> latLngs = new ArrayList<LatLng>();
                        if(distances.size() != 0){
                            for(TraveledDistance obj : distances){
                                latLngs.add(new LatLng(obj.lat,obj.lng));
                            }
                            totalDistance = SphericalUtil.computeLength(latLngs);
                        }

                        double AllowanceRate = Double.parseDouble(preferences.getString("rate",""));


                        String lat = String.valueOf(lastLocation.getLatitude());
                        String lng = String.valueOf(lastLocation.getLongitude());
                        sendCurrentLocation(lat,lng);
                        sendTraveledCoordinates();

                        NumberFormat nf = NumberFormat.getInstance();
                        nf.setMaximumFractionDigits(2); // set as you need
                        String myStringmax = nf.format(totalDistance/1000);
                        String allowance = nf.format((totalDistance/1000) * AllowanceRate);
                        if(preferences.getString("isTour","").equalsIgnoreCase("1")){
                            TourUpdateDistance(myStringmax,allowance);
                        }
                        else {
                            updateDistance(myStringmax,allowance);

                        }
                        previousTime = new Date().getTime();
                        sendTime();
                    }

                    try {
                        //previousTime = preferences.getLong("isOnlineTime",0);


                        EndTime[0] = dateFormat.parse(time);
                        CurrentTime[0] = dateFormat.parse(dateFormat.format(new Date()));
                        if(CurrentTime[0].after(EndTime[0]) || CurrentTime[0].equals(EndTime[0])){
                            isEnd[0] = true;
                        }
                        else {
                            //isEnd[0] = true;
                            //Toast.makeText(CurrentPositionService.this, "Not Matched", Toast.LENGTH_SHORT).show();
                        }
                    }
                    catch (Exception e){
                        //isEnd[0] = true;
                        //Toast.makeText(CurrentPositionService.this, e.toString(), Toast.LENGTH_SHORT).show();
                    }

                    /*if(isEnd[0] && preferences.getString("isTour","").equalsIgnoreCase("0")){
                        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");
                        dayFormat.setTimeZone(TimeZone.getTimeZone("GMT+05"));
                        String lastLongitude, lastLatitude;
                        if(isNull){
                            lastLatitude = preferences.getString("checkInLat","");
                            lastLongitude = preferences.getString("checkInLng","");
                        }
                        else {
                            lastLatitude = String.valueOf(lastLocation.getLatitude());
                            lastLongitude = String.valueOf(lastLocation.getLongitude());
                        }



                        String checkOutTime = dateFormat1.format(new Date());

                        CheckinCheckOutTable obj = CheckinCheckOutTable.findById(CheckinCheckOutTable.class, Long.valueOf(1));
                        obj.isCheckOut = true;
                        obj.isCheckIn = false;
                        obj.checkOutLat = lastLatitude;
                        obj.CheckOutLng = lastLongitude;
                        obj.time = checkOutTime;
                        obj.save();

                        editor.putBoolean("IsCheckIn",false);
                        editor.putBoolean("IsCheckOut",true);
                        editor.putString("CheckOutTime",preferences.getString("OfficeTimeOut",""));
                        editor.putString("lastLatitude",lastLatitude);
                        editor.putString("lastLongitude",lastLongitude);
                        editor.putString("CheckOutDay", dayFormat.format(new Date()));
                        editor.apply();
                        if(haveNetworkConnection()){
                            obj.isSend = true;
                            obj.save();
                            try {



                                //sendTime("0:00 0");
                                UploadAutoCheckOut(checkOutTime,Double.parseDouble(lastLatitude),Double.parseDouble(lastLongitude));
                                List<TraveledDistance> distances = TraveledDistance.listAll(TraveledDistance.class);
                                //TraveledDistance.deleteAll(TraveledDistance.class);

                                AddLocationData.isCheckOut = true;

                                //broadcaster.sendBroadcast(intent);


                            }
                            catch (Exception e){
                        *//**//*
                            }
                        }
                        else {
                            obj.isSend = false;
                            obj.save();
                            CurrentPositionService.this.stopService(new Intent(CurrentPositionService.this,CurrentPositionService.class));
                            CurrentPositionService.this.stopService(new Intent(CurrentPositionService.this,TrackingService.class));
                        }


                    }*/


                }
            }
        }).start();
    }

    @Override
    public void onDestroy()
    {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
        CheckinCheckOutTable obj = CheckinCheckOutTable.findById(CheckinCheckOutTable.class, Long.valueOf(1));
        if(obj.isCheckIn){
            //Toast.makeText(this, "Not Stopable", Toast.LENGTH_SHORT).show();
            startService(new Intent(CurrentPositionService.this,CurrentPositionService.class));
            startService(new Intent(CurrentPositionService.this,TrackingService.class));
        }
       /* if(!AddLocationData.isCheckOut){
            startService(new Intent(CurrentPositionService.this,CurrentPositionService.class));
            startService(new Intent(CurrentPositionService.this,TrackingService.class));
        }*/
    }
    @Override
    public boolean stopService(Intent name) {
        //Toast.makeText(MyService.this,"Service stoped",Toast.LENGTH_SHORT).show();

        stop();
        CheckinCheckOutTable obj = CheckinCheckOutTable.findById(CheckinCheckOutTable.class, Long.valueOf(1));
        if(obj.isCheckIn){
            //Toast.makeText(this, "Not Stopable", Toast.LENGTH_SHORT).show();
            startService(new Intent(CurrentPositionService.this,CurrentPositionService.class));
            startService(new Intent(CurrentPositionService.this,TrackingService.class));
        }
        return super.stopService(name);
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
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

    public void sendCurrentLocation(final String lat, final String lng){
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(CurrentPositionService.this);

        AndroidNetworking.post(LOGIN_URL)
                .addBodyParameter("id", preferences.getString("userId", ""))
                .addBodyParameter("latitude", lat)
                .addBodyParameter("longitude", lng)
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


        /*StringRequest stringRequest = new StringRequest(Request.Method.POST, LOGIN_URL,
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
        RequestQueue requestQueue = Volley.newRequestQueue(CurrentPositionService.this);
        requestQueue.add(stringRequest);*/
    }

    public void UploadAutoCheckOut(final String Time, final double latitude, final double longitude){
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = preferences.edit();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+05"));

        AndroidNetworking.post(CHECKOUT_URL)
                .addBodyParameter("id", preferences.getString("userId", ""))
                .addBodyParameter("auto", "Yes")
                .addBodyParameter("latitude", String.valueOf(latitude))
                .addBodyParameter("longitude", String.valueOf(longitude))
                .addBodyParameter("checkOut", Time)
                .addBodyParameter("day", dateFormat.format(new Date()))
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



        /*StringRequest stringRequest = new StringRequest(Request.Method.POST, CHECKOUT_URL,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        editor.putString("isCheckOutSend","1");
                        editor.apply();

                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        editor.putString("isCheckOutSend","0");
                        editor.apply();
                        //Toast.makeText(CurrentPositionService.this,error.toString(), Toast.LENGTH_LONG ).show();//error.toString()
                        //pDialog.hide();
                    }
                }){

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> map = new HashMap<String,String>();

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+05"));

                map.put("checkOut",Time);
                map.put("auto","Yes");
                map.put("latitude",String.valueOf(latitude));
                map.put("longitude",String.valueOf(longitude));
                map.put("id",preferences.getString("userId",""));
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

    void updateDistance(final String distance, final String allowance){
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        AndroidNetworking.post(DISTANCE_URL)
                .addBodyParameter("id", preferences.getString("userId", ""))
                .addBodyParameter("distance", distance)
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

        /*StringRequest stringRequest = new StringRequest(Request.Method.POST, DISTANCE_URL,
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
                }){

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> map = new HashMap<String,String>();

                map.put("id",preferences.getString("userId",""));
                map.put("distance",distance);
                map.put("allowance",allowance);

                return map;
            }

        };

        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);*/

    }

    void TourUpdateDistance(final String distance, final String allowance){
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        AndroidNetworking.post(Distance_At_Tour_URL)
                .addBodyParameter("id", preferences.getString("userId", ""))
                .addBodyParameter("tour_id", preferences.getString("TourId",""))
                .addBodyParameter("distance", distance)
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

        /*StringRequest stringRequest = new StringRequest(Request.Method.POST, Distance_At_Tour_URL,
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
                }){

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> map = new HashMap<String,String>();

                map.put("id",preferences.getString("userId",""));
                map.put("tour_id",preferences.getString("TourId",""));
                map.put("distance",distance);
                map.put("allowance",allowance);

                return map;
            }

        };

        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);*/

    }

    void sendTraveledCoordinates(){

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        List<TraveledDistance> distances = TraveledDistance.listAll(TraveledDistance.class);
        for (final TraveledDistance obj : distances){

            if(!obj.isSend){
                TraveledDistance forSendTrue = TraveledDistance.findById(TraveledDistance.class,obj.getId());
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
                                TraveledDistance forSendTrue = TraveledDistance.findById(TraveledDistance.class,obj.getId());
                                forSendTrue.isSend = false;
                                forSendTrue.save();
                                // handle error
                            }
                        });
                /*StringRequest stringRequest = new StringRequest(Request.Method.POST, SEND_COORINATES_URL,
                        new Response.Listener<String>() {

                            @Override
                            public void onResponse(String response) {
//
                            }
                        },

                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                TraveledDistance forSendTrue = TraveledDistance.findById(TraveledDistance.class,obj.getId());
                                forSendTrue.isSend = false;
                                forSendTrue.save();

                                //Toast.makeText(CurrentPositionService.this,"updateDistance Error", Toast.LENGTH_LONG ).show();//error.toString()
                                //pDialog.hide();
                            }
                        }){

                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String,String> map = new HashMap<String,String>();
                        List<TraveledDistance> distances = TraveledDistance.listAll(TraveledDistance.class);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+05"));

                        map.put("id",preferences.getString("userId",""));
                        map.put("date",dateFormat.format(new Date()));
                        map.put("longitude", String.valueOf(obj.lng));
                        map.put("latitude", String.valueOf(obj.lat));

                        return map;
                    }

                };

                int socketTimeout = 30000;
                RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                stringRequest.setRetryPolicy(policy);
                RequestQueue requestQueue = Volley.newRequestQueue(this);
                requestQueue.add(stringRequest);*/
            }

        }


    }

    void sendTime() {

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        //Toast.makeText(this, "Time Sending", Toast.LENGTH_LONG).show();
        AndroidNetworking.post(SEND_Time_For_Online_URL)
                .addBodyParameter("id", preferences.getString("userId", ""))
                .setTag("test")
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        // do anything with response
                        //Toast.makeText(CurrentPositionService.this, "Response   " + response, Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onError(ANError error) {
                        //Toast.makeText(CurrentPositionService.this, error.toString(), Toast.LENGTH_SHORT).show();
                        // handle error
                    }
                });

        /*StringRequest stringRequest = new StringRequest(Request.Method.POST, SEND_Time_For_Online_URL,
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
                //map.put("time", time);
                       *//* map.put("longitude", String.valueOf(obj.lng));
                        map.put("latitude", String.valueOf(obj.lat));*//*

                return map;
            }

        };

        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);*/
    }


    public void stop() {
        started = false;
        handler.removeCallbacks(runnable);
    }

    public void start() {
        started = true;
        handler.postDelayed(runnable, 10000);
    }



}


