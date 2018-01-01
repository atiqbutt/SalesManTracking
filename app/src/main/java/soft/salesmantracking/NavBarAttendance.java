package soft.salesmantracking;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;


public class NavBarAttendance extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LocationListener {


    Button CheckInBtn, CheckOutBtn;
    double longitude, latitude, officeLongitutde, officeLatitude;
    LocationManager locationManager;
    boolean isCheckIn = false;
    TextView CheckInTime, CheckOutTime, DistanceTraveled, distance, allowance;
    String OfficeTimeIn, OfficeTimeOut;
    public static final String LOGIN_URL = "http://www.swmapplication.com/API/check_in";
    public static final String CHECKOUT_URL = "http://www.swmapplication.com/API/check_out";
    public static final String DISTANCE_URL = "http://www.swmapplication.com/API/distanceAtCheckIn";
    int hour, min;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_bar_attendance);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

       /* FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Thread myThread = null;

        Runnable myRunnableThread = new CountDownRunner();
        myThread= new Thread(myRunnableThread);
        myThread.start();



        CheckInBtn = (Button) findViewById(R.id.AttendanceBtn);
        CheckOutBtn = (Button) findViewById(R.id.CheckOut);
        CheckInTime = (TextView) findViewById(R.id.CheckInTime);
        CheckOutTime = (TextView) findViewById(R.id.CheckOutTime);

        distance = (TextView) findViewById(R.id.DistanceText);
        allowance = (TextView) findViewById(R.id.AllowanceText);


        //CheckInTime.setVisibility(View.INVISIBLE);
        //CheckOutTime.setVisibility(View.INVISIBLE);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = preferences.edit();

        String coordinates = preferences.getString("OfficeCoordinates", "");
        OfficeTimeIn = preferences.getString("OfficeTimeIn", "");
        OfficeTimeOut = preferences.getString("OfficeTimeOut", "");


        String[] separated = coordinates.split(",");
        String Slat = separated[0];
        String Slng = separated[1];

        latitude = Double.parseDouble(Slat);
        longitude = Double.parseDouble(Slng);
        //separated[0]; // this will contain "Fruit"
        //separated[1]; // this will contain " they taste good"

        //DistanceTraveled.setText(preferences.getString("TravelledDistance",""));


        //CheckInTime.setText("CheckIn Time: " + preferences.getString("CheckInTime",""));
        SimpleDateFormat dateFormat1 = new SimpleDateFormat("h:mm a");
        boolean IsEnableTime = false;
        String time = "5:00 AM";
        try {
            Date EnableTime = dateFormat1.parse(time);
            if (dateFormat1.parse(dateFormat1.format(new Date())).after(EnableTime)) {
                IsEnableTime = true;
            }
        } catch (Exception e) {

        }
        //int time = Integer.parseInt(dateFormat1.format(new Date()));
        if (preferences.getBoolean("IsCheckOut", false) && !IsEnableTime) {
            CheckOutBtn.setClickable(false);
            CheckOutBtn.setEnabled(false);

            CheckInBtn.setClickable(false);
            CheckInBtn.setEnabled(false);

            CheckInTime.setText("Check In Time: " + preferences.getString("CheckInTime", ""));
            CheckOutTime.setText("Check Out Time: " + preferences.getString("CheckOutTime", ""));
            //Toast.makeText(this,"first",Toast.LENGTH_SHORT).show();

        } else if (preferences.getBoolean("IsCheckIn", false)) {
            CheckInTime.setText("Check In Time: " + preferences.getString("CheckInTime", ""));

            // Toast.makeText(this,"2   " + preferences.getString("CheckInTime","") ,Toast.LENGTH_SHORT).show();

            CheckInBtn.setClickable(false);
            CheckInBtn.setEnabled(false);

        } else {
            editor.putString("CheckInTime", "");
            editor.apply();
            editor.putString("CheclOutTime", "");
            editor.apply();
            editor.putBoolean("IsCheckOut", false);
            //Toast.makeText(this,"3",Toast.LENGTH_SHORT).show();

        }


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 400, 4, this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }


        CheckInBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        isCheckIn = true;
                        editor.putBoolean("IsCheckIn", true);
                        editor.apply();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String checkInTime = dateFormat.format(new Date());
                        CheckInTime.setVisibility(View.VISIBLE);
                        CheckInTime.setText("Check In Time: " + checkInTime);
                        editor.putString("CheckInTime", checkInTime);
                        editor.apply();
                        CheckInBtn.setClickable(false);
                        CheckInBtn.setEnabled(false);
                        CheckOutBtn.setClickable(true);
                        CheckOutBtn.setEnabled(true);
                        UploadCheckIn(checkInTime);

                        startService(new Intent(NavBarAttendance.this, CurrentPositionService.class));
                        startService(new Intent(NavBarAttendance.this, TrackingService.class));
                    }
                }
        );
        CheckOutBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        isCheckIn = false;
                        editor.putBoolean("IsCheckIn", false);
                        editor.apply();
                        editor.putBoolean("IsCheckOut", true);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String checkOutTime = dateFormat.format(new Date());
                        CheckOutTime.setVisibility(View.VISIBLE);
                        CheckOutTime.setText("Check Out Time: " + checkOutTime);
                        editor.putString("CheckOutTime", checkOutTime);
                        editor.apply();
                        CheckOutBtn.setClickable(false);
                        CheckOutBtn.setEnabled(false);
                        UploadCheckOut(checkOutTime);
                        updateDistance();
                        stopService(new Intent(NavBarAttendance.this, CurrentPositionService.class));
                        stopService(new Intent(NavBarAttendance.this, TrackingService.class));
                    }
                }
        );


        distanceAndAllowance();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.mapnav) {
            finish();
            startActivity(new Intent(this, Attendance.class));
            overridePendingTransition(0, 0);
            // Handle the camera action
        } else if (id == R.id.navBarDetails) {

        } else if (id == R.id.navBarMarkedList) {
            finish();
            startActivity(new Intent(this, ListOfMarkedPlaces.class));
            overridePendingTransition(0, 0);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onLocationChanged(Location location) {
        officeLatitude = latitude;
        officeLongitutde = longitude;
        Date OfficeTime = null, timeOut = null;
        Date CurrentTime = null;


        float[] results = new float[1];
        Location.distanceBetween(latitude, longitude, location.getLatitude(), location.getLongitude(), results);
        SimpleDateFormat dateFormat1 = new SimpleDateFormat("h:mm a");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        dateFormat1.setTimeZone(TimeZone.getTimeZone("GMT+05"));

        boolean isOfficeTime = false;
        try {
            OfficeTime = dateFormat1.parse(OfficeTimeIn);
            timeOut = dateFormat1.parse(OfficeTimeOut);

            CurrentTime = dateFormat1.parse(dateFormat1.format(new Date()));
            if (CurrentTime.after(OfficeTime) && CurrentTime.before(timeOut)) {
                //Toast.makeText(NavBarAttendance.this, "true", Toast.LENGTH_SHORT).show();
                isOfficeTime = true;
            }

        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(NavBarAttendance.this, e.toString(), Toast.LENGTH_SHORT).show();
        }


//        if(CurrentTime.after(OfficeTime)){
//            isOfficeTime = true;
//        }


        //location.getLongitude() == officeLongitutde && location.getLatitude() == officeLatitude

        if (results[0] <= 150) {
            if (!preferences.getBoolean("IsCheckIn", false) && !preferences.getBoolean("IsCheckOut", false) && isOfficeTime) {
                CheckOutBtn.setClickable(false);
                CheckOutBtn.setEnabled(false);

                CheckInBtn.setClickable(true);
                CheckInBtn.setEnabled(true);


            } else if (preferences.getBoolean("IsCheckOut", false)) {

            }
            else if(!isOfficeTime){
                CheckInBtn.setClickable(false);
                CheckInBtn.setEnabled(false);

                CheckOutBtn.setClickable(false);
                CheckOutBtn.setEnabled(false);

            }else {
                CheckInBtn.setClickable(false);
                CheckInBtn.setEnabled(false);

                CheckOutBtn.setClickable(true);
                CheckOutBtn.setEnabled(true);

                CheckInTime.setText("Check In Time: " + preferences.getString("CheckInTime", ""));
            }
        } else {
            CheckOutBtn.setClickable(false);
            CheckOutBtn.setEnabled(false);

            CheckInBtn.setClickable(false);
            CheckInBtn.setEnabled(false);
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }


    public void distanceAndAllowance() {
        List<TraveledDistance> distances = TraveledDistance.listAll(TraveledDistance.class);
        double totalDistance = 0, totalAllowance = 0;
        Location locationA = null, locationB = null;
        if (distances.size() != 0) {
            for (int i = 0; i < distances.size() - 1; i++) {
                locationA.setLatitude(distances.get(i).lat);
                locationA.setLongitude(distances.get(i).lng);

                locationB.setLatitude(distances.get(i + 1).lat);
                locationB.setLongitude(distances.get(i + 1).lng);

                totalDistance += locationA.distanceTo(locationB);
            }
        }


        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2); // set as you need
        String myStringmax = nf.format(totalDistance / 1000);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        double AllowanceRate = Double.parseDouble(preferences.getString("rate", ""));

        distance.setText(myStringmax);
        allowance.setText(String.valueOf((totalDistance / 1000) * AllowanceRate) + preferences.getString("currency", ""));
    }

    public void UploadCheckIn(final String Time) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, LOGIN_URL,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            JSONObject jsonObject = jsonArray.getJSONObject(0);


                        } catch (JSONException e) {

                            Toast.makeText(NavBarAttendance.this, "Json Error", Toast.LENGTH_SHORT).show();

                            Toast.makeText(NavBarAttendance.this, e.toString(), Toast.LENGTH_SHORT).show();

                            e.printStackTrace();
                        }


//
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Toast.makeText(NavBarAttendance.this, error.toString(), Toast.LENGTH_LONG).show();//error.toString()
                        //pDialog.hide();
                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();

                map.put("checkIn", Time);
                map.put("id", preferences.getString("id", ""));


                return map;
            }

        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }


    public void UploadCheckOut(final String Time) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, CHECKOUT_URL,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            JSONObject jsonObject = jsonArray.getJSONObject(0);


                        } catch (JSONException e) {

                            Toast.makeText(NavBarAttendance.this, "Json Error", Toast.LENGTH_SHORT).show();

                            Toast.makeText(NavBarAttendance.this, e.toString(), Toast.LENGTH_SHORT).show();

                            e.printStackTrace();
                        }


//
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Toast.makeText(NavBarAttendance.this, error.toString(), Toast.LENGTH_LONG).show();//error.toString()
                        //pDialog.hide();
                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();

                map.put("checkOut", Time);
                map.put("auto", "No");
                map.put("latitude", String.valueOf(latitude));
                map.put("longitude", String.valueOf(longitude));
                map.put("id", preferences.getString("id", ""));


                return map;
            }

        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    void updateDistance() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, DISTANCE_URL,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            JSONObject jsonObject = jsonArray.getJSONObject(0);


                        } catch (JSONException e) {

                            Toast.makeText(NavBarAttendance.this, "Json Error", Toast.LENGTH_SHORT).show();

                            Toast.makeText(NavBarAttendance.this, e.toString(), Toast.LENGTH_SHORT).show();

                            e.printStackTrace();
                        }


//
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Toast.makeText(NavBarAttendance.this, error.toString(), Toast.LENGTH_LONG).show();//error.toString()
                        //pDialog.hide();
                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();

                map.put("id", preferences.getString("id", ""));


                return map;
            }

        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }


    public void doWork() {
        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    TextView txtCurrentTime = (TextView) findViewById(R.id.myText);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm:ss a");
                    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+05"));
                    Date dt = new Date();
                    int hours = dt.getHours();
                    int minutes = dt.getMinutes();
                    int seconds = dt.getSeconds();

                    String curTime = hours + ":" + minutes + ":" + seconds;
                    txtCurrentTime.setText(dateFormat.format(new Date()));
                } catch (Exception e) {
                }
            }
        });
    }


    class CountDownRunner implements Runnable {
        // @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    doWork();
                    Thread.sleep(1000); // Pause of 1 Second
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                }
            }
        }
    }
}
