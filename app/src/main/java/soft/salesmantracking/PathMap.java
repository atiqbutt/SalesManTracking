package soft.salesmantracking;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class PathMap extends AppCompatActivity implements OnMapReadyCallback {


    GoogleMap mGoogleMap;
    SupportMapFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path_map);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Traveled Path");
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);


        mFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.path_map);
        mFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        List<TraveledDistance> distances = TraveledDistance.listAll(TraveledDistance.class);
        ArrayList<LatLng> latLngs = new ArrayList<LatLng>();


        if(distances.size() == 0){
            Toast.makeText(this,"You did't travel yet", Toast.LENGTH_LONG).show();
        }
        else{
            for(TraveledDistance obj : distances){
                latLngs.add(new LatLng(obj.lat,obj.lng));
            }

            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLngs.get(0)));
            mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            Polyline line = mGoogleMap.addPolyline(new PolylineOptions()
                    .addAll(latLngs)
                    .width(5)
                    .color(Color.RED));

            MarkerOptions startMarker = new MarkerOptions();
            startMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
            startMarker.position(latLngs.get(0));
            mGoogleMap.addMarker(startMarker);

            MarkerOptions endMarker = new MarkerOptions();
            endMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            endMarker.position(latLngs.get(latLngs.size() - 1));
            mGoogleMap.addMarker(endMarker);
        }

    }
}
