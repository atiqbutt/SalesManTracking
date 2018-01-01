package soft.salesmantracking;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
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

import java.util.HashMap;
import java.util.Map;

public class DeactiveScreen extends AppCompatActivity {

    public static final String Update_Info_URL = "http://www.swmapplication.com/API/getUpdateInfo";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deactive_screen);

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(DeactiveScreen.this);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(preferences.getString("username",""));
    }

    public void reloadClick(View view) {
        UpdateInfo();
    }


    public void UpdateInfo(){
        final ProgressDialog dialog = new ProgressDialog(DeactiveScreen.this);
        dialog.setMessage("Updating Profile...");
        dialog.setCancelable(false);
        dialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Update_Info_URL,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {


                        try{
                            JSONArray jsonArray = new JSONArray(response);
                            JSONObject jsonObject = jsonArray.getJSONObject(0);

                            String msg, coordinates, timeIn, timeOut, rate, currency, rateCurrency, username, isActive, isOutDoor;
                            msg = jsonObject.getString("msg");
                            //Toast.makeText(LogIn.this,"true",Toast.LENGTH_SHORT).show();

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
                                final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(DeactiveScreen.this);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("OfficeCoordinates",coordinates);
                                editor.putString("OfficeTimeIn",timeIn);
                                editor.putString("OfficeTimeOut",timeOut);
                                editor.putString("rate",rate);
                                editor.putString("isOutdoor",isOutDoor);
                                editor.putString("currency",currency);
                                editor.putString("username",username);
                                editor.apply();
                                dialog.dismiss();
                                if(isActive.equalsIgnoreCase("1")){
                                    editor.putString("isActive","1");
                                    editor.apply();
                                    Toast.makeText(DeactiveScreen.this,"Profile is Still Deactive",Toast.LENGTH_LONG).show();
                                }
                                else {
                                    editor.putString("isActive","0");
                                    editor.apply();
                                    Toast.makeText(DeactiveScreen.this,"Profile Activated",Toast.LENGTH_LONG).show();
                                    finish();
                                    startActivity(new Intent(DeactiveScreen.this,MainActivity.class));
                                }
                            }
                            else {
                                TraveledDistance.deleteAll(TraveledDistance.class);
                                AddLocaton.deleteAll(AddLocaton.class);
                                StayInformation.deleteAll(StayInformation.class);
                                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(DeactiveScreen.this);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("isActive","0");
                                editor.putBoolean("ïsSignIn",false);
                                editor.apply();

                                Toast.makeText(DeactiveScreen.this,"Your Profile is Deleted",Toast.LENGTH_LONG).show();
                                finish();
                                startActivity(new Intent(DeactiveScreen.this,LogIn.class));

                            }




                            overridePendingTransition(0,0);

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
                        Toast.makeText(DeactiveScreen.this,"Connection Failed", Toast.LENGTH_LONG ).show();

                    }
                }){

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> map = new HashMap<String,String>();
                final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(DeactiveScreen.this);
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
}
