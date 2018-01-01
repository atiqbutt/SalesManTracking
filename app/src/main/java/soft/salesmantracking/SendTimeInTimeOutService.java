package soft.salesmantracking;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by Malik on 31/07/2017.
 */

public class SendTimeInTimeOutService extends Service {

    public static final String CHECKOUT_URL = "http://www.swmapplication.com/API/check_out";
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        new Thread(new Runnable(){
            @Override
            public void run() {
                while(true){
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SendTimeInTimeOutService.this);
                    final SharedPreferences.Editor editor = preferences.edit();
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, CHECKOUT_URL,
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

                            map.put("checkOut",preferences.getString("CheckOutTime",""));
                            map.put("auto","Yes");
                            map.put("latitude",preferences.getString("lastLatitude",""));
                            map.put("longitude",preferences.getString("lastLongitude",""));
                            map.put("id",preferences.getString("userId",""));
                            //map.put("day",dateFormat.format(new Date()));




                            return map;
                        }

                    };

                    int socketTimeout = 30000;
                    RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                    stringRequest.setRetryPolicy(policy);
                    RequestQueue requestQueue = Volley.newRequestQueue(SendTimeInTimeOutService.this);
                    requestQueue.add(stringRequest);

                }
            }
        }).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        return START_STICKY;
    }
}
