package soft.salesmantracking;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by Hassan on 10/16/2017.
 */

public class AttendanceService extends Service {
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

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


    boolean isuploadCheckIn, isDistanceOnCheckIn, isClear, isSendCurrentLocation;

    SimpleDateFormat dateFormat1;
    String s="";


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Toast.makeText(this, "Attendance Started", Toast.LENGTH_SHORT).show();
        preferences = PreferenceManager.getDefaultSharedPreferences(AttendanceService.this);
        editor = preferences.edit();

        dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat1.setTimeZone(TimeZone.getTimeZone("GMT+05"));
        if(preferences.getString("isTour","").equalsIgnoreCase("1")){
                                                    /*if(!preferences.getString("CheckInDay","").equalsIgnoreCase(dateFormat1.format(new Date()))){
                                                        UploadTourCheckIn(checkInTime);
                                                        TourDistanceOnCheckIn();
                                                    }*/
            UploadTourCheckIn(preferences.getString("CheckInTime",""));
        }
        else {
            if(!preferences.getString("CheckInDay","").equalsIgnoreCase(dateFormat1.format(new Date()))){
                UploadCheckIn(preferences.getString("CheckInTime",""));

            }
            else{
                ClearCheckOut();
            }

        }
        /*new Thread(new Runnable(){
            @Override
            public void run() {
                while(true){
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }).start();*/
    }


    @Override
    public boolean stopService(Intent name) {
        Toast.makeText(AttendanceService.this,"Service stoped",Toast.LENGTH_SHORT).show();

        return super.stopService(name);
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



    public void UploadCheckIn(final String Time) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, LOGIN_URL,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        editor.putString("isCheckInSend","1");
                        editor.apply();
                        isuploadCheckIn = true;
                        DistanceOnCheckIn();
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        UploadCheckIn(preferences.getString("CheckInTime",""));
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
                map.put("checkInLat",String.valueOf(preferences.getString("checkInLat","")));
                map.put("checkInLng",String.valueOf(preferences.getString("checkInLng","")));
                map.put("id", preferences.getString("userId", ""));


                return map;
            }

        };

        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }


    public void UploadCheckOut(final String Time) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, CHECKOUT_URL,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        editor.putString("isCheckOutSend","1");
                        editor.apply();

                        isuploadCheckIn = true;


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
                map.put("latitude", String.valueOf(preferences.getString("checkoutLat","")));
                map.put("longitude", String.valueOf(preferences.getString("checkoutLng","")));
                map.put("id", preferences.getString("userId", ""));

                map.put("day",dateFormat.format(new Date()));


                return map;
            }

        };

        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }


    public void UploadTourCheckIn(final String Time) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Tour_Check_In_URL,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        editor.putString("isCheckInSend","1");
                        editor.apply();
                        TourDistanceOnCheckIn();
                        /*try {
                            JSONArray jsonArray = new JSONArray(response);
                            JSONObject jsonObject = jsonArray.getJSONObject(0);


                        } catch (JSONException e) {


                            e.printStackTrace();
                        }*/
                        isuploadCheckIn = true;

//
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        UploadTourCheckIn(preferences.getString("CheckInTime",""));
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
                map.put("checkInLat",String.valueOf(preferences.getString("checkInLat","")));
                map.put("checkInLng",String.valueOf(preferences.getString("checkInLng","")));
                /*map.put("checkInLat",String.valueOf(testLat));
                map.put("checkInLng",String.valueOf(testLng));*/
                map.put("tour_id", preferences.getString("TourId", ""));
                map.put("id", preferences.getString("userId", ""));


                return map;
            }

        };

        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }


    public void UploadTourCheckOut(final String Time) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Tour_Check_Out_URL,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        editor.putString("isCheckOutSend","1");
                        editor.apply();
                        /*try {
                            JSONArray jsonArray = new JSONArray(response);
                            JSONObject jsonObject = jsonArray.getJSONObject(0);


                        } catch (JSONException e) {


                            e.printStackTrace();
                        }*/

                        isuploadCheckIn = true;


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
               /* map.put("latitude", String.valueOf(testLat));
                map.put("longitude", String.valueOf(testLng));*/
                map.put("latitude", String.valueOf(preferences.getString("checkoutLat","")));
                map.put("longitude", String.valueOf(preferences.getString("checkoutLng","")));
                map.put("id", preferences.getString("userId", ""));

                map.put("day",dateFormat.format(new Date()));


                return map;
            }

        };

        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    public void DistanceOnCheckIn() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, DISTANCE_URL,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        isDistanceOnCheckIn = true;
                        sendCurrentLocation(preferences.getString("checkInLat",""),preferences.getString("checkInLng",""));
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        DistanceOnCheckIn();
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
        requestQueue.add(stringRequest);
    }

    public void TourDistanceOnCheckIn() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Distance_At_Tour_CheckIn_URL,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        isDistanceOnCheckIn = true;
                        sendCurrentLocation(preferences.getString("checkInLat",""),preferences.getString("checkInLng",""));
                        /*try {
                            JSONArray jsonArray = new JSONArray(response);
                            JSONObject jsonObject = jsonArray.getJSONObject(0);


                        } catch (JSONException e) {


                            e.printStackTrace();
                        }*/


//
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        TourDistanceOnCheckIn();
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
        requestQueue.add(stringRequest);
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

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Update_Location_Url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        isSendCurrentLocation = true;
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
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }


    public void ClearCheckOut() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, CLEAR_CHECKOUT_URL,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        isClear = true;
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
        requestQueue.add(stringRequest);
    }

    public void edit_tour(final String tourId){

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
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
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }
}
