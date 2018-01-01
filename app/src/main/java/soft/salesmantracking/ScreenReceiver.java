package soft.salesmantracking;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInstaller;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by Shah on 12/22/2016.
 */

public class ScreenReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+05"));
        Date currentDate = null;
        try {
            currentDate = dateFormat.parse(dateFormat.format(new Date()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        /*if(preferences.getBoolean("IsCheckIn",false)){

            if(isMyServiceRunningCTracking(context)){
                context.startService(new Intent(context,TrackingService.class));
            }
            if(isMyServiceRunningCurrent(context)){
                context.startService(new Intent(context,CurrentPositionService.class));
            }


        }*/
        if(preferences.getString("isTour","").equalsIgnoreCase("0")){
            List<Tour> tours = Tour.listAll(Tour.class);
            for(Tour obj : tours){
                if(obj.startDate != null){
                    Date StartDate  = null;
                    try {
                        StartDate = dateFormat.parse(obj.startDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if(StartDate.equals(currentDate)){
                        Toast.makeText(context, "Tour Starts", Toast.LENGTH_LONG).show();
                        editor.putString("isTour","1");
                        editor.putString("TourId",obj.identity);
                        editor.putString("tourStart",obj.startDate);
                        editor.putString("tourEnd",obj.endDate);
                        editor.apply();

                        //context.startService(new Intent(context,TrackingService.class));
                        //context.startService(new Intent(context,CurrentPositionService.class));
                        Intent i = new Intent(context,MainActivity.class);
                        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,i,PendingIntent.FLAG_UPDATE_CURRENT);

                        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                        long pattern[] = { 0, 100, 200, 300, 400 };
                        vibrator.vibrate(pattern,0);

                        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                                .setAutoCancel(true)
                                .setContentTitle("SWMA")
                                .setContentText("Your Tour Started Mark Attendance")
                                .setSmallIcon(R.mipmap.swma)
                                .setSound(soundUri)
                                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.mipmap.swma))
                                .setContentIntent(pendingIntent);

                        NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

                        manager.notify(0,builder.build());
                        vibrator.cancel();
                    }
                }
                //Toast.makeText(context, obj.startDate, Toast.LENGTH_SHORT).show();

            }
        }
        else {
            //Toast.makeText(context, "Not in start and not in end", Toast.LENGTH_SHORT).show();
            Date endDate = null;
            try {
                endDate = dateFormat.parse(preferences.getString("tourEnd",""));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if(currentDate.equals(endDate)){
                //editor.putString("isTour","0");
                editor.putString("tourEnd","0");
                editor.apply();

                Toast.makeText(context, "Tour Ends Mark Check Out", Toast.LENGTH_LONG).show();
                //context.stopService(new Intent(context,TrackingService.class));
                //context.stopService(new Intent(context,CurrentPositionService.class));
                Intent i = new Intent(context,MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(context,0,i,PendingIntent.FLAG_UPDATE_CURRENT);

                Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                long pattern[] = { 0, 100, 200, 300, 400 };
                vibrator.vibrate(pattern,0);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                        .setAutoCancel(true)
                        .setContentTitle("SWMA")
                        .setContentText("Your Tour Ended Mark Check Out")
                        .setSmallIcon(R.mipmap.swma)
                        .setSound(soundUri)
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.mipmap.swma))
                        .setContentIntent(pendingIntent);

                NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

                manager.notify(0,builder.build());
                vibrator.cancel();
            }
        }

    }


    private boolean isMyServiceRunningCurrent(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (CurrentPositionService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }

        return false;
    }private boolean isMyServiceRunningCTracking(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (TrackingService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }

        return false;
    }
}
