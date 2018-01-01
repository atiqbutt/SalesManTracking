package soft.salesmantracking;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
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
import java.util.List;
import java.util.Map;

public class LogIn extends AppCompatActivity {

    public static final String LOGIN_URL = "http://www.swmapplication.com/API/login";
    //public static final String LOGIN_URL = "http://alrehmansteel.com/webportal/volleyLogin.php";
   // public static final String LOGIN_URL = "https://malikparactice.000webhostapp.com/volleyLogin.php";
    public static final String KEY_USERNAME="username";
    public static final String KEY_PASSWORD="password";
    public static final String KEY_PHONE="phone";
    private EditText editTextUsername;
    private EditText editTextPassword;
    private EditText editTextPhone;
    private String username;
    private String password;
    private String phone;
    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        //editTextUsername = (EditText) findViewById(R.id.editTextUsername);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        editTextPhone = (EditText) findViewById(R.id.editTextPhone);


        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        NetworkChangeReceiver receiver = new NetworkChangeReceiver();
        this.registerReceiver(receiver,filter);



    }

    private void userLogin() {
        //username = editTextUsername.getText().toString().trim();
        password = editTextPassword.getText().toString().trim();
        phone = editTextPhone.getText().toString().trim();


        boolean isEmpty=false;
//        if(TextUtils.isEmpty(editTextUsername.getText().toString())) {
//            editTextUsername.setError("Enter User Name first");
//            isEmpty = true;
//        }
            if(TextUtils.isEmpty(editTextPassword.getText().toString())) {
                editTextPassword.setError("Enter Password");
                isEmpty = true;
            }
        if(TextUtils.isEmpty(editTextPhone.getText().toString())) {
            editTextPhone.setError("Enter Phone Number");
            isEmpty = true;
        }
            if(!isEmpty){

                final ProgressDialog pDialog = new ProgressDialog(this);
                pDialog.setMessage("Signing in...");
                pDialog.setCancelable(false);
//                            pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//                            pDialog.setIndeterminate(true);
                pDialog.show();
                StringRequest stringRequest = new StringRequest(Request.Method.POST, LOGIN_URL,
                        new Response.Listener<String>() {

                            @Override
                            public void onResponse(String response) {


                                try{
                                    JSONArray jsonArray = new JSONArray(response);
                                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                                    String message = jsonObject.getString("message");

                                    if(message.equalsIgnoreCase("false")){
                                        Toast.makeText(LogIn.this,"Incorect Password or Phone Number.",Toast.LENGTH_SHORT).show();
                                    }
                                    else if(message.equalsIgnoreCase("already")){
                                        Toast.makeText(LogIn.this,"This Account Has Already Logged In",Toast.LENGTH_SHORT).show();
                                    }
                                    else if(message.equalsIgnoreCase("true")) {
                                        String id, coordinates, timeIn, timeOut, rate, currency, rateCurrency, username, isOutDoor;

                                        //Toast.makeText(LogIn.this,"true",Toast.LENGTH_SHORT).show();

                                        id = jsonObject.getString("id");
                                        coordinates = jsonObject.getString("coordinates");
                                        timeIn = jsonObject.getString("time_in");
                                        timeOut = jsonObject.getString("time_out");
                                        username = jsonObject.getString("username");
                                        isOutDoor = jsonObject.getString("isOutdoor");
                                        rateCurrency = jsonObject.getString("rate");

                                        try {
                                            String[] seperated = rateCurrency.split(" ");
                                            rate = seperated[0];
                                            currency = seperated[1];
                                            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(LogIn.this);
                                            SharedPreferences.Editor editor = preferences.edit();
                                            editor.putBoolean("ïsSignIn",true);
                                            editor.apply();
                                            editor.putString("userId",id);
                                            editor.putString("OfficeCoordinates",coordinates);
                                            editor.putString("OfficeTimeIn",timeIn);
                                            editor.putString("OfficeTimeOut",timeOut);
                                            editor.putString("rate",rate);
                                            editor.putString("currency",currency);
                                            editor.putString("username",username);
                                            editor.putString("isOutdoor",isOutDoor);
                                            editor.putString("isActive","0");
                                            editor.putString("isTour","0");
                                            editor.apply();
                                            finish();
                                            Intent maps = new Intent(LogIn.this,MainActivity.class);
                                            startActivity(maps);
                                        }
                                        catch (Exception e){
                                          Toast.makeText(LogIn.this,"Data from Admin side cannot be passed Correctly",Toast.LENGTH_LONG).show();
                                        }



                                    }
                                    pDialog.dismiss();
                                }catch (JSONException e){
                                    pDialog.dismiss();
                                   // Toast.makeText(LogIn.this,"Json Error",Toast.LENGTH_SHORT).show();

                                    //Toast.makeText(LogIn.this,e.toString(),Toast.LENGTH_SHORT).show();

                                    e.printStackTrace();
                                }




//                                //Toast.makeText(LoginVolley.this ,response, Toast.LENGTH_SHORT).show();
//                                if(response.equalsIgnoreCase("true")){
//
//                                    final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(LogIn.this);
//                                    SharedPreferences.Editor editor = preferences.edit();
//                                    editor.putBoolean("ïsSignIn",true);
//                                    editor.apply();
//                                    finish();
//                                    Intent maps = new Intent(LogIn.this,Attendance.class);
//                                    startActivity(maps);
//                                    pDialog.dismiss();
//                                    // Toast.makeText(LoginVolley.this ,response, Toast.LENGTH_SHORT).show();
//                                    //openProfile();
//
//                                    //pDialog.show();
//                                }else{
//                                    Toast.makeText(LogIn.this,"Incorect Username, Password or Phone Number.",Toast.LENGTH_LONG).show();//response
//                                    pDialog.dismiss();
//                                }
                            }
                        },

                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(LogIn.this,"Connection Failed", Toast.LENGTH_LONG ).show();
                                pDialog.hide();
                            }
                        }){

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> map = new HashMap<String,String>();

                //map.put(KEY_USERNAME,username);
                map.put(KEY_PASSWORD,password);
                map.put(KEY_PHONE,phone);


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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
       // finish();
    }

    public void Maps(View view) {
        userLogin();
    }

    public String MD5(String md5) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
        }
        return null;
    }


}
