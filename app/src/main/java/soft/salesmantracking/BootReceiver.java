package soft.salesmantracking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

/**
 * Created by shah on 8/23/2017.
 */

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //dToast.makeText(context,"Booted",Toast.LENGTH_LONG).show();
        //Toast.makeText(context, "In Boot", Toast.LENGTH_SHORT).show();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if(preferences.getBoolean("IsCheckIn", false) || preferences.getString("isTour","").equalsIgnoreCase("1")){
            context.startService(new Intent(context,CurrentPositionService.class));
            context.startService(new Intent(context,TrackingService.class));
        }

    }
}
