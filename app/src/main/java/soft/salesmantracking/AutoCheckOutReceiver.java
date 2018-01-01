package soft.salesmantracking;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by Hassan on 12/11/2017.
 */

public class AutoCheckOutReceiver extends BroadcastReceiver {
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    public static final String CHECKOUT_URL = "http://www.swmapplication.com/API/check_out";
    @Override
    public void onReceive(Context context, Intent intent) {

        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = preferences.edit();

        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");
        dayFormat.setTimeZone(TimeZone.getTimeZone("GMT+05"));

        String checkOutTime = preferences.getString("OfficeTimeOut","");


        CheckinCheckOutTable obj = CheckinCheckOutTable.findById(CheckinCheckOutTable.class, Long.valueOf(1));
        if(obj.isCheckIn){
            obj.isCheckOut = true;
            obj.isCheckIn = false;
            obj.checkOutLat = obj.lat;
            obj.CheckOutLng = obj.lng;
            obj.time = checkOutTime;
            obj.save();

            editor.putBoolean("IsCheckIn",false);
            editor.putBoolean("IsCheckOut",true);
            editor.putString("CheckOutTime",preferences.getString("OfficeTimeOut",""));
            editor.putString("lastLatitude",obj.lat);
            editor.putString("lastLongitude",obj.lng);
            editor.putString("CheckOutDay", dayFormat.format(new Date()));
            editor.apply();
            if(haveNetworkConnection(context)){
                obj.isSend = true;
                obj.save();
                try {



                    //sendTime("0:00 0");
                    UploadAutoCheckOut(checkOutTime,Double.parseDouble(obj.lat),Double.parseDouble(obj.lng),context);
                    List<TraveledDistance> distances = TraveledDistance.listAll(TraveledDistance.class);
                    //TraveledDistance.deleteAll(TraveledDistance.class);

                    AddLocationData.isCheckOut = true;

                    //broadcaster.sendBroadcast(intent);


                }
                catch (Exception e){
                        /**/
                }
            }
            else {
                obj.isSend = false;
                obj.save();

            }
            context.stopService(new Intent(context,CurrentPositionService.class));
            context.stopService(new Intent(context,TrackingService.class));

            if(haveNetworkConnection(context)){
                Intent i = new Intent(context,MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                PendingIntent pendingIntent = PendingIntent.getActivity(context,0,i,PendingIntent.FLAG_UPDATE_CURRENT);

                Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                Vibrator vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
                long pattern[] = { 0, 100, 200, 300, 400 };
                vibrator.vibrate(pattern,0);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                        .setAutoCancel(true)
                        .setContentTitle("SWMA")
                        .setContentText("Auto Checked Out")
                        .setSmallIcon(R.mipmap.swma)
                        .setSound(soundUri)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText("Auto Checked Out"))
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.mipmap.swma))
                        .setContentIntent(pendingIntent);

                NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

                manager.notify(0,builder.build());
                vibrator.cancel();
            }
        }


    }



    public void UploadAutoCheckOut(final String Time, final double latitude, final double longitude, Context context){
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
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
}
