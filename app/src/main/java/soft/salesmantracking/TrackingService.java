package soft.salesmantracking;

import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.WindowManager;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by Shah on 7/5/2017.
 */

public class TrackingService extends Service{
    private static final String TAG = "BOOMBOOMTESTGPS";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 60000;
    private static final float LOCATION_DISTANCE = 200f;
    boolean isStay = false;
    long StayStartTime;
    public static final String LOGIN_URL = "http://www.swmapplication.com/API/stay_info";
    public static final String Marked_Location_URL = "http://www.swmapplication.com/API/sendMarkedLocations";
    public static final String Marked_Location_Stay_URL = "http://swmapplication.com/API/marked_stay_info";
    Date StartTime, EndTime;
    String place;
    double stayedPlaceLatitude, stayedPlaceLongitude;
    String startTime,endTime;
    boolean isDialog = false;
    long previousTime;


    private class LocationListener implements android.location.LocationListener
    {
        Location mLastLocation;

        public LocationListener(String provider)
        {
            //Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location)
        {
            CheckinCheckOutTable checkinCheckOutTable = CheckinCheckOutTable.findById(CheckinCheckOutTable.class, (long) 1);
            if(isStay && place != null){

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(TrackingService.this);
                SharedPreferences.Editor editor = preferences.edit();
                String DurationStayed = "";
                /*Toast.makeText(TrackingService.this,place + "\n",Toast.LENGTH_SHORT).show();
                Toast.makeText(TrackingService.this,"Start Time: " + new Date(StayStartTime).toString() + "\n",Toast.LENGTH_SHORT).show();
                Toast.makeText(TrackingService.this,"End Time" + new Date(new Date().getTime()).toString() + "\n",Toast.LENGTH_SHORT).show();
*/
                SimpleDateFormat day = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm:s a");
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+05"));
                day.setTimeZone(TimeZone.getTimeZone("GMT+05"));

                Date date = new Date();
                long currentTime = date.getTime();
                long markedPlaceStayTime;
                if(checkinCheckOutTable.isMarked){
                    try {
                        markedPlaceStayTime = currentTime - checkinCheckOutTable.markTime;
                        AddLocaton obj = AddLocaton.findById(AddLocaton.class,checkinCheckOutTable.markedPlcaeId);
                        MarkedStayTime markedStayTime = new MarkedStayTime();
                        markedStayTime.LocationLat = obj.LocationLat;
                        markedStayTime.LocationLng = obj.LocationLng;
                        markedStayTime.Image = obj.Image;
                        markedStayTime.LocationName = obj.LocationName;

                        long diff = currentTime - checkinCheckOutTable.markTime;
                        int days = (int) (diff / (1000 * 60 * 60 * 24));
                        int hours = (int) ((diff - (1000 * 60 * 60 * 24 * days)) / (1000 * 60 * 60));
                        int min = (int) (diff - (1000 * 60 * 60 * 24 * days) - (1000 * 60 * 60 * hours))
                                / (1000 * 60);
                        min = min %60;
                        int sec = (int)((diff/(1000))%60);

                        if(hours != 0){
                            DurationStayed = String.valueOf(hours) + " hours";
                        }
                        if(min != 0){
                            DurationStayed = DurationStayed + "  " + String.valueOf(min) + " min";
                        }
                        DurationStayed = DurationStayed + "  " + String.valueOf(sec) + " sec";

                        markedStayTime.Duration = DurationStayed;
                        markedStayTime.Day = day.format(new Date());
                        markedStayTime.MarkedStaySartTime = dateFormat.format(new Date(checkinCheckOutTable.markTime));
                        markedStayTime.startTime = dateFormat.format(new Date(StayStartTime));
                        markedStayTime.EndTime = dateFormat.format(new Date());

                        DurationStayed = "";
                        diff = currentTime - StayStartTime;
                        days = (int) (diff / (1000 * 60 * 60 * 24));
                        hours = (int) ((diff - (1000 * 60 * 60 * 24 * days)) / (1000 * 60 * 60));
                        min = (int) (diff - (1000 * 60 * 60 * 24 * days) - (1000 * 60 * 60 * hours))
                                / (1000 * 60);
                        min = min %60;
                        sec = (int)((diff/(1000))%60);

                        if(hours != 0){
                            DurationStayed = String.valueOf(hours) + " hours";
                        }
                        if(min != 0){
                            DurationStayed = DurationStayed + "  " + String.valueOf(min) + " min";
                        }
                        DurationStayed = DurationStayed + "  " + String.valueOf(sec) + " sec";

                        markedStayTime.TotalStayDuration = DurationStayed;

                        markedStayTime.save();

                        checkinCheckOutTable.isMarked = false;
                        checkinCheckOutTable.markedPlcaeId = 0;
                        checkinCheckOutTable.markTime = 0;
                        checkinCheckOutTable.save();
                    }catch (Exception e){

                    }



                }
                else {
                    DurationStayed = "";
                    long diff = currentTime - StayStartTime;
                    int days = (int) (diff / (1000 * 60 * 60 * 24));
                    int hours = (int) ((diff - (1000 * 60 * 60 * 24 * days)) / (1000 * 60 * 60));
                    int min = (int) (diff - (1000 * 60 * 60 * 24 * days) - (1000 * 60 * 60 * hours))
                            / (1000 * 60);
                    min = min %60;
                    int sec = (int)((diff/(1000))%60);

                    if(hours != 0){
                        DurationStayed = String.valueOf(hours) + " hours";
                    }
                    if(min != 0){
                        DurationStayed = DurationStayed + "  " + String.valueOf(min) + " min";
                    }
                    DurationStayed = DurationStayed + "  " + String.valueOf(sec) + " sec";
                    //Toast.makeText(TrackingService.this,"Stayed Duration: " + DurationStayed,Toast.LENGTH_SHORT).show();


                    StayInformation obj = new StayInformation();
                    obj.location = place;
                    obj.startTime = startTime;
                    obj.EndTime = dateFormat.format(new Date());
                    obj.Duration = "Stayed Duration: " + DurationStayed;
                    obj.latitude = stayedPlaceLatitude;
                    obj.longitude = stayedPlaceLongitude;
                    obj.Day = day.format(new Date());
                    obj.locationName = getAddress(new LatLng(stayedPlaceLatitude,stayedPlaceLongitude));
                    obj.distance = 0;
                    obj.save();
                }
                /*DurationStayed = "";
                long diff = currentTime - StayStartTime;
                int days = (int) (diff / (1000 * 60 * 60 * 24));
                int hours = (int) ((diff - (1000 * 60 * 60 * 24 * days)) / (1000 * 60 * 60));
                int min = (int) (diff - (1000 * 60 * 60 * 24 * days) - (1000 * 60 * 60 * hours))
                        / (1000 * 60);
                min = min %60;
                int sec = (int)((diff/(1000))%60);

                if(hours != 0){
                    DurationStayed = String.valueOf(hours) + " hours";
                }
                if(min != 0){
                    DurationStayed = DurationStayed + "  " + String.valueOf(min) + " min";
                }
                DurationStayed = DurationStayed + "  " + String.valueOf(sec) + " sec";
                //Toast.makeText(TrackingService.this,"Stayed Duration: " + DurationStayed,Toast.LENGTH_SHORT).show();


                StayInformation obj = new StayInformation();
                obj.location = place;
                obj.startTime = startTime;
                obj.EndTime = dateFormat.format(new Date());
                obj.Duration = "Stayed Duration: " + DurationStayed;
                obj.latitude = stayedPlaceLatitude;
                obj.longitude = stayedPlaceLongitude;
                obj.Day = day.format(new Date());
                obj.locationName = getAddress(new LatLng(stayedPlaceLatitude,stayedPlaceLongitude));
                obj.distance = 0;
                obj.save();*/
                /*if (preferences.getBoolean("isFirst",false)){
                    boolean isCal = true;
                    editor.putBoolean("isFirst",false);
                    editor.apply();
                    ArrayList<LatLng> latLngs = new ArrayList<LatLng>();
                    List<TraveledDistance> distances = TraveledDistance.listAll(TraveledDistance.class);
                    for(TraveledDistance obj1 : distances){
                        if(isCal){
                            latLngs.add(new LatLng(obj1.lat,obj1.lng));
                        }
                        if (stayedPlaceLatitude == obj1.lat && stayedPlaceLongitude == obj1.lng){
                            isCal = false;
                        }
                    }

                    double totalDistance = SphericalUtil.computeLength(latLngs)/1000;
                    NumberFormat nf = NumberFormat.getInstance();
                    nf.setMaximumFractionDigits(2); // set as you need
                    String myStringmax = nf.format(totalDistance / 1000);

                    obj.distance = Double.parseDouble(myStringmax);
                }
                else {
                    boolean isCal = false;

                    ArrayList<LatLng> latLngs = new ArrayList<LatLng>();
                    List<TraveledDistance> distances = TraveledDistance.listAll(TraveledDistance.class);
                    List<StayInformation> stayInformations = StayInformation.listAll(StayInformation.class);
                    StayInformation stayInformation = stayInformations.get(stayInformations.size()-1);
                    for(TraveledDistance obj1 : distances){
                        if(obj1.lat == stayInformation.latitude && obj1.lng == stayInformation.longitude){
                            isCal = true;
                        }

                        if(obj1.lat == stayedPlaceLatitude && obj1.lng == stayedPlaceLongitude){
                            isCal = false;
                        }

                        if(isCal){
                            latLngs.add(new LatLng(obj1.lat,obj1.lng));
                        }
                        //latLngs.add(new LatLng(obj1.lat,obj1.lng));
                    }

                    double totalDistance = SphericalUtil.computeLength(latLngs)/1000;
                    NumberFormat nf = NumberFormat.getInstance();
                    nf.setMaximumFractionDigits(2); // set as you need
                    String myStringmax = nf.format(totalDistance / 1000);

                    obj.distance = Double.parseDouble(myStringmax);
                }*/


            }
            checkinCheckOutTable.isMarked = false;
            checkinCheckOutTable.save();
            SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm:s a");
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+05"));
            isStay = false;
            Date date = new Date();
            StayStartTime = date.getTime();
            startTime = dateFormat.format(new Date());
            String a = "Current Location: " + location.getLatitude() + ", " + location.getLongitude();
            place = "Location: " + location.getLatitude() + ", " + location.getLongitude();
            stayedPlaceLatitude = location.getLatitude();
            stayedPlaceLongitude = location.getLongitude();

            //Toast.makeText(TrackingService.this,a,Toast.LENGTH_SHORT).show();
            //Log.e(TAG, "onLocationChanged: " + location);

            /*if(haveNetworkConnection()){
                sendStayInfo();
            }*/
            mLastLocation.set(location);
        }

        @Override
        public void onProviderDisabled(String provider)
        {
            //Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            //Log.e(TAG, "onProviderEnabled: " + provider);
            Toast.makeText(TrackingService.this,provider,Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            //Log.e(TAG, "onStatusChanged: " + provider);
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
        previousTime = new Date().getTime();

        CheckinCheckOutTable obj = CheckinCheckOutTable.findById(CheckinCheckOutTable.class, (long) 1);
        obj.isKnowForOffline = false;
        obj.save();
        //Toast.makeText(this, "Tracking Service Starts", Toast.LENGTH_SHORT).show();
        initializeLocationManager();
        /*try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Toast.makeText(TrackingService.this,"fail to request location update, ignore",Toast.LENGTH_SHORT).show();

            //Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Toast.makeText(TrackingService.this,"network provider does not exist, " + ex.getMessage(),Toast.LENGTH_SHORT).show();
            //Log.d(TAG, "network provider does not exist, " + ex.getMessage());
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
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        Thread t = Thread.currentThread();
        t.setDefaultUncaughtExceptionHandler(new MyThreadUncaughtExceptionHandler());
        sendStayInfo();


        new Thread(new Runnable(){

            @Override
            public void run() {
                while (true){
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //final boolean istrue = true;
                    //Toast.makeText(TrackingService.this,"start",Toast.LENGTH_SHORT).show();
                    //Log.e(TAG, "onCreate");
                    Date date = new Date();
                    long currentTime = date.getTime();
                    long diff = currentTime - StayStartTime;
                    int days = (int) (diff / (1000 * 60 * 60 * 24));
                    int hours = (int) ((diff - (1000 * 60 * 60 * 24 * days)) / (1000 * 60 * 60));
                    int min = (int) (diff - (1000 * 60 * 60 * 24 * days) - (1000 * 60 * 60 * hours))
                            / (1000 * 60);
                    int sec = (int)((diff/(1000)));
                    if(sec > 300) {
                        isStay = true;
                        //print("stayed");
                        //istrue[0] = false;
                    }

                    long currentTimes = new Date().getTime();

                    long diffs = currentTimes - previousTime;

                    CheckinCheckOutTable obj = CheckinCheckOutTable.findById(CheckinCheckOutTable.class, (long) 1);



                    if((!haveNetworkConnection() && diffs >= 5 * 60 * 1000) && !obj.isKnowForOffline){
                        previousTime = new Date().getTime();
                        Intent i = new Intent(TrackingService.this,AlertActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                    }
                    if(haveNetworkConnection() && diffs >= 5 * 60 * 1000){
                        sendMarkedLocations();
                        sendMarkedStayInfo();
                        sendStayInfo();
                    }
                    

                }
            }
        }).start();

        /*final boolean istrue = true;
        //Toast.makeText(TrackingService.this,"start",Toast.LENGTH_SHORT).show();
        //Log.e(TAG, "onCreate");
        Date date = new Date();
        long currentTime = date.getTime();
        long diff = currentTime - StayStartTime;
        int sec = (int)(diff/(1000));
        if(sec > 1){
            Toast.makeText(TrackingService.this,"Stayed",Toast.LENGTH_LONG).show();
            //istrue[0] = false;
        }*/
    }

    @Override
    public void onDestroy()
    {
        //Log.e(TAG, "onDestroy");
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
            startService(new Intent(TrackingService.this,CurrentPositionService.class));
            startService(new Intent(TrackingService.this,TrackingService.class));
        }
        /*if(!AddLocationData.isCheckOut){
            startService(new Intent(TrackingService.this,CurrentPositionService.class));
            startService(new Intent(TrackingService.this,TrackingService.class));
        }*/
    }
    @Override
    public boolean stopService(Intent name) {
        //Toast.makeText(MyService.this,"Service stoped",Toast.LENGTH_SHORT).show();

        CheckinCheckOutTable obj = CheckinCheckOutTable.findById(CheckinCheckOutTable.class, Long.valueOf(1));
        if(obj.isCheckIn){
            //Toast.makeText(this, "Not Stopable", Toast.LENGTH_SHORT).show();
            startService(new Intent(TrackingService.this,CurrentPositionService.class));
            startService(new Intent(TrackingService.this,TrackingService.class));
        }
        return super.stopService(name);
    }

    private void initializeLocationManager() {
        //Log.e(TAG, "initializeLocationManager");
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

    public void print(String time){
        Toast.makeText(TrackingService.this,time,Toast.LENGTH_SHORT).show();
    }


    public void sendStayInfo(){

        List<StayInformation> informations = StayInformation.listAll(StayInformation.class);

        for(final StayInformation obj : informations){
            if(!obj.isSend){
                StayInformation forSendTrue = StayInformation.findById(StayInformation.class,obj.getId());
                forSendTrue.isSend = true;
                forSendTrue.save();
                final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(TrackingService.this);
                StringRequest stringRequest = new StringRequest(Request.Method.POST, LOGIN_URL,
                        new Response.Listener<String>() {

                            @Override
                            public void onResponse(String response) {


                                try{
                                    JSONArray jsonArray = new JSONArray(response);
                                    JSONObject jsonObject = jsonArray.getJSONObject(0);



                                }catch (JSONException e){

//                                    Toast.makeText(TrackingService.this,"Json Error",Toast.LENGTH_SHORT).show();
//
//                                    Toast.makeText(TrackingService.this,e.toString(),Toast.LENGTH_SHORT).show();

                                    e.printStackTrace();
                                }




//
                            }
                        },

                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                                StayInformation forSendTrue = StayInformation.findById(StayInformation.class,obj.getId());
                                forSendTrue.isSend = false;
                                forSendTrue.save();
                                //Toast.makeText(TrackingService.this,error.toString(), Toast.LENGTH_LONG ).show();//error.toString()
                                //pDialog.hide();
                            }
                        }){

                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String,String> map = new HashMap<String,String>();

                        map.put("latitude",String.valueOf(obj.latitude));
                        map.put("longitude",String.valueOf(obj.longitude));
                        map.put("start_time",obj.startTime);
                        map.put("end_time",obj.EndTime);
                        map.put("duration",obj.Duration);
                        map.put("distance",String.valueOf(obj.distance));
                        map.put("day",obj.Day);
                        map.put("locationName",obj.locationName);
                        map.put("id",preferences.getString("userId",""));



                        return map;
                    }

                };

                int socketTimeout = 30000;
                RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                stringRequest.setRetryPolicy(policy);
                RequestQueue requestQueue = Volley.newRequestQueue(TrackingService.this);
                requestQueue.add(stringRequest);
            }
        }

    }
    public void sendMarkedStayInfo(){

        List<MarkedStayTime> informations = MarkedStayTime.listAll(MarkedStayTime.class);

        for(final MarkedStayTime obj : informations){
            if(!obj.isSend){
                MarkedStayTime forSendTrue = MarkedStayTime.findById(MarkedStayTime.class,obj.getId());
                forSendTrue.isSend = true;
                forSendTrue.save();

                final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(TrackingService.this);

                AndroidNetworking.post(Marked_Location_Stay_URL)
                        .addBodyParameter("id", preferences.getString("userId", ""))
                        .addBodyParameter("latitude",String.valueOf(obj.LocationLat))
                        .addBodyParameter("longitude",String.valueOf(obj.LocationLng))
                        .addBodyParameter("start_time",obj.startTime)
                        .addBodyParameter("end_time",obj.EndTime)
                        .addBodyParameter("duration",obj.Duration)
                        .addBodyParameter("name",obj.LocationName)
                        .addBodyParameter("image",obj.Image)
                        .addBodyParameter("MarkedStaySartTime",obj.MarkedStaySartTime)
                        .addBodyParameter("TotalStayDuration",obj.TotalStayDuration)
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
                                MarkedStayTime forSendTrue = MarkedStayTime.findById(MarkedStayTime.class,obj.getId());
                                forSendTrue.isSend = false;
                                forSendTrue.save();
                                Toast.makeText(TrackingService.this, error.toString(), Toast.LENGTH_SHORT).show();

                                // handle error
                            }
                        });



                /*StringRequest stringRequest = new StringRequest(Request.Method.POST, Marked_Location_Stay_URL,
                        new Response.Listener<String>() {

                            @Override
                            public void onResponse(String response) {


                                try{
                                    JSONArray jsonArray = new JSONArray(response);
                                    JSONObject jsonObject = jsonArray.getJSONObject(0);



                                }catch (JSONException e){

//                                    Toast.makeText(TrackingService.this,"Json Error",Toast.LENGTH_SHORT).show();
//
//                                    Toast.makeText(TrackingService.this,e.toString(),Toast.LENGTH_SHORT).show();

                                    e.printStackTrace();
                                }




//
                            }
                        },

                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                                StayInformation forSendTrue = StayInformation.findById(StayInformation.class,obj.getId());
                                forSendTrue.isSend = false;
                                forSendTrue.save();
                                //Toast.makeText(TrackingService.this,error.toString(), Toast.LENGTH_LONG ).show();//error.toString()
                                //pDialog.hide();
                            }
                        }){

                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String,String> map = new HashMap<String,String>();

                        map.put("latitude",String.valueOf(obj.LocationLat));
                        map.put("longitude",String.valueOf(obj.LocationLng));
                        map.put("start_time",obj.startTime);
                        map.put("end_time",obj.EndTime);
                        map.put("duration",obj.Duration);
                        map.put("image",obj.Image);
                        map.put("id",preferences.getString("userId",""));



                        return map;
                    }

                };

                int socketTimeout = 30000;
                RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                stringRequest.setRetryPolicy(policy);
                RequestQueue requestQueue = Volley.newRequestQueue(TrackingService.this);
                requestQueue.add(stringRequest);*/
            }
        }

    }
    public void sendMarkedLocations(){

        List<AddLocaton> informations = AddLocaton.listAll(AddLocaton.class);

        for(final AddLocaton obj : informations){
            if(!obj.isSend){

                AddLocaton forSendTrue = AddLocaton.findById(AddLocaton.class,obj.getId());
                forSendTrue.isSend = true;
                forSendTrue.save();
                final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(TrackingService.this);
                StringRequest stringRequest = new StringRequest(Request.Method.POST, Marked_Location_URL,
                        new Response.Listener<String>() {

                            @Override
                            public void onResponse(String response) {


                                try{
                                    JSONArray jsonArray = new JSONArray(response);
                                    JSONObject jsonObject = jsonArray.getJSONObject(0);



                                }catch (JSONException e){

//                                    Toast.makeText(TrackingService.this,"Json Error",Toast.LENGTH_SHORT).show();
//
//                                    Toast.makeText(TrackingService.this,e.toString(),Toast.LENGTH_SHORT).show();

                                    e.printStackTrace();
                                }




//
                            }
                        },

                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                AddLocaton forSendTrue = AddLocaton.findById(AddLocaton.class,obj.getId());
                                forSendTrue.isSend = false;
                                forSendTrue.save();

                                //Toast.makeText(TrackingService.this,error.toString(), Toast.LENGTH_LONG ).show();//error.toString()
                                //pDialog.hide();
                            }
                        }){

                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String,String> map = new HashMap<String,String>();

                        map.put("id",preferences.getString("userId",""));
                        map.put("name",obj.LocationName);
                        map.put("latitude",String.valueOf(obj.LocationLat));
                        map.put("longitude",String.valueOf(obj.LocationLng));
                        map.put("image",obj.Image);
                        return map;
                    }

                };

                int socketTimeout = 30000;
                RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                stringRequest.setRetryPolicy(policy);
                RequestQueue requestQueue = Volley.newRequestQueue(TrackingService.this);
                requestQueue.add(stringRequest);
            }
        }

    }



    String getAddress(LatLng latLng){
        Geocoder geocoder = new Geocoder(TrackingService.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
            Address obj = addresses.get(0);

            String add = obj.getAddressLine(0);
            add += " " + obj.getCountryName();
            add += " " + obj.getCountryCode();
            add += " " + obj.getAdminArea();
            add += " " + obj.getPostalCode();
            add += " " + obj.getSubAdminArea();
            add += " " + obj.getLocality();
            add += " " + obj.getSubThoroughfare();

            return add;
        }
        catch (Exception e){

        }
        return "";
    }
}
