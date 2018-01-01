package soft.salesmantracking;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.android.gms.location.LocationListener;


public class PositionService extends Service implements LocationListener{
    LocationManager locationManager;

    @Override
    public void onCreate() {
        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 40, (android.location.LocationListener) PositionService.this);

        }
        catch (java.lang.SecurityException ex){

        }catch (IllegalArgumentException ex){

        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        locationManager = (LocationManager)    getSystemService(Context.LOCATION_SERVICE);
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        Coordinates coordinates = new Coordinates();
        coordinates.lati = location.getLatitude();
        coordinates.longi = location.getLongitude();
        coordinates.save();
    }
}
