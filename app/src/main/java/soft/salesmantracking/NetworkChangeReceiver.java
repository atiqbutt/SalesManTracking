package soft.salesmantracking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
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
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by Hassan on 10/17/2017.
 */

public class NetworkChangeReceiver extends BroadcastReceiver {
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

    Context context;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    CheckinCheckOutTable obj;
    @Override
    public void onReceive(Context context, Intent intent) {

        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = preferences.edit();

        this.context = context;
        obj = CheckinCheckOutTable.findById(CheckinCheckOutTable.class, (long) 1);

        SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a");
        SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat1.setTimeZone(TimeZone.getTimeZone("GMT+05"));
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+05"));

        if(haveNetworkConnection(context)){
            //Toast.makeText(context, "Net Connected", Toast.LENGTH_LONG).show();

            if(obj.isCheckIn){
                if(preferences.getString("isTour","").equalsIgnoreCase("1")){
                    TourUpdateDistance();
                }
                else {
                    updateDistance();

                }
                sendTraveledCoordinates();
                sendCurrentLocation(obj.lat,obj.lng);
            }
            if(obj.isCheckIn && !obj.isSend){
                if(preferences.getString("isTour","").equalsIgnoreCase("1")){
                                                    /*if(!preferences.getString("CheckInDay","").equalsIgnoreCase(dateFormat1.format(new Date()))){
                                                        UploadTourCheckIn(checkInTime);
                                                        TourDistanceOnCheckIn();
                                                    }*/
                    UploadTourCheckIn(preferences.getString("CheckInTime",""));
                    TourDistanceOnCheckIn();

                }
                else {
                    if(!preferences.getString("CheckInDay","").equalsIgnoreCase(dateFormat1.format(new Date()))){
                        UploadCheckIn(preferences.getString("CheckInTime",""));
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
                sendCurrentLocation(preferences.getString("checkInLat",""),String.valueOf(preferences.getString("checkInLng","")));

                //UpdateInfo();


                editor.putBoolean("isSend",true);
                editor.apply();

                /*startService(new Intent(context, CurrentPositionService.class));
                startService(new Intent(MainActivity.this, TrackingService.class));*/
            }
            else if(obj.isCheckOut && !obj.isSend){
                //Toast.makeText(context, "Tour", Toast.LENGTH_SHORT).show();
                sendTime();
                String TourId = "";
                //TraveledDistance.deleteAll(TraveledDistance.class);
                if(preferences.getString("isTour","").equalsIgnoreCase("1")){
                    TourUpdateDistance();

                    UploadTourCheckOut(preferences.getString("CheckOutTime",""));
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
                    //Toast.makeText(context, "Office", Toast.LENGTH_SHORT).show();
                    updateDistance();
                    UploadCheckOut(preferences.getString("CheckOutTime",""));
                    sendTraveledCoordinates();
                }
                editor.putString("isTour", "0");

                editor.putString("tourEnd", "0");
                editor.putString("TourId", "0");
                editor.apply();

                editor.putBoolean("isSend",true);
                editor.apply();
            }
        }

    }

    private boolean haveNetworkConnection(Context context) {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
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




    public void UploadCheckIn(final String Time) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, LOGIN_URL,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        /*editor.putString("isCheckInSend","1");
                        editor.apply();*/

                        CheckinCheckOutTable obj = CheckinCheckOutTable.findById(CheckinCheckOutTable.class, (long) 1);

                        obj.isSend = true;
                        obj.save();
//
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        editor.putString("isCheckInSend","0");
                        editor.apply();
                        CheckinCheckOutTable obj = CheckinCheckOutTable.findById(CheckinCheckOutTable.class, (long) 1);

                        obj.isSend = false;
                        obj.save();
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
                map.put("checkInLat",preferences.getString("checkInLat",""));
                map.put("checkInLng",preferences.getString("checkInLng",""));
                map.put("id", preferences.getString("userId", ""));
                map.put("battery",preferences.getString("battery",""));


                return map;
            }

        };

        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }


    public void UploadCheckOut(final String Time) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, CHECKOUT_URL,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        editor.putString("isCheckOutSend","1");
                        editor.apply();

                        CheckinCheckOutTable obj = CheckinCheckOutTable.findById(CheckinCheckOutTable.class, (long) 1);

                        obj.isSend = true;
                        obj.save();

//
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        editor.putString("isCheckOutSend","0");
                        editor.apply();
                        CheckinCheckOutTable obj = CheckinCheckOutTable.findById(CheckinCheckOutTable.class, (long) 1);

                        obj.isSend = false;
                        obj.save();
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
                map.put("latitude", obj.checkOutLat);
                map.put("longitude", obj.CheckOutLng);
                map.put("id", preferences.getString("userId", ""));

                map.put("day",dateFormat.format(new Date()));


                return map;
            }

        };

        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }


    public void UploadTourCheckIn(final String Time) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Tour_Check_In_URL,
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
                map.put("checkInLat",preferences.getString("checkInLat",""));
                map.put("checkInLng",preferences.getString("checkInLng",""));
                map.put("tour_id", preferences.getString("TourId", ""));
                map.put("id", preferences.getString("userId", ""));


                return map;
            }

        };

        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }


    public void UploadTourCheckOut(final String Time) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Tour_Check_Out_URL,
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
                map.put("latitude", preferences.getString("checkoutLat",""));
                map.put("longitude", preferences.getString("checkoutLng",""));
                map.put("id", preferences.getString("userId", ""));

                map.put("day",dateFormat.format(new Date()));


                return map;
            }

        };

        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }

    public void DistanceOnCheckIn() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, DISTANCE_URL,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        CheckinCheckOutTable obj = CheckinCheckOutTable.findById(CheckinCheckOutTable.class, (long) 1);

                        obj.isSend = true;
                        obj.save();
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        CheckinCheckOutTable obj = CheckinCheckOutTable.findById(CheckinCheckOutTable.class, (long) 1);

                        obj.isSend = false;
                        obj.save();
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
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }

    public void TourDistanceOnCheckIn() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Distance_At_Tour_CheckIn_URL,
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
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }


    void sendTime() {

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

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
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }


    public void sendCurrentLocation(final String lat, final String lng){
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Update_Location_Url,
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
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }


    public void ClearCheckOut() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, CLEAR_CHECKOUT_URL,
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
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }

    public void edit_tour(final String tourId){

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, EDIT_TOUR_URL,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        /*editor.putString("isCheckOutSend","1");
                        editor.apply();*/
//
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                       /* editor.putString("isCheckOutSend","0");
                        editor.apply();*/
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
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);

    }


    void updateDistance(){
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

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
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

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

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
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
}
