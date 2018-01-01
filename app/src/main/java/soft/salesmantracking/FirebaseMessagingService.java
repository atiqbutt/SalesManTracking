package soft.salesmantracking;

/**
 * Created by shah on 8/23/2017.
 */

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.google.firebase.messaging.RemoteMessage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by shah on 8/10/2017.
 */

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService  {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {


        String id = remoteMessage.getData().get("id");
        String isAdd = remoteMessage.getData().get("add");
        String startDate = remoteMessage.getData().get("start");
        String endDate = remoteMessage.getData().get("end");

        if(isAdd.equalsIgnoreCase("add")){
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = preferences.edit();
            showNotification(remoteMessage.getData().get("message"));
            Tour tour = new Tour();
            tour.startDate = startDate;
            tour.endDate = endDate;
            tour.identity = id;
            tour.save();

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+05"));
            Date currentDate = null;
            try {
                currentDate = dateFormat.parse(dateFormat.format(new Date()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Date StartDate  = null;
            try {
                StartDate = dateFormat.parse(startDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if(StartDate.equals(currentDate)){
                //Toast.makeText(getApplicationContext(), "Tour Starts", Toast.LENGTH_LONG).show();
                editor.putString("isTour","1");
                editor.putString("TourId",tour.identity);
                editor.putString("tourEnd",tour.endDate);
                editor.apply();

                //context.startService(new Intent(context,TrackingService.class));
                //context.startService(new Intent(context,CurrentPositionService.class));
                Intent i = new Intent(getApplicationContext(),MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),2,i,PendingIntent.FLAG_UPDATE_CURRENT);

                Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                Vibrator vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                long pattern[] = { 0, 100, 200, 300, 400 };
                vibrator.vibrate(pattern,0);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                        .setAutoCancel(true)
                        .setContentTitle("SWMA")
                        .setContentText("Your Tour Started Mark Attendance")
                        .setSmallIcon(R.mipmap.swma)
                        .setSound(soundUri)
                        .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),R.mipmap.swma))
                        .setContentIntent(pendingIntent);

                NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);

                manager.notify(0,builder.build());
                vibrator.cancel();
            }
        }
        else if (isAdd.equalsIgnoreCase("del")){
            List<Tour> tours = Tour.listAll(Tour.class);
            Tour delObj = null;
            for (Tour obj : tours){
                if(obj.identity.equalsIgnoreCase(id)){
                    delObj = obj;
                }
            }
            delObj.delete();
            //tours.remove(delObj);
            //showNotification("Your Tour Deleted");
        }else if(isAdd.equalsIgnoreCase("edit")){
            List<Tour> tours = Tour.listAll(Tour.class);
            for (Tour obj : tours){
                if(obj.identity.equalsIgnoreCase(id)){
                    obj.startDate = startDate;
                    obj.endDate = endDate;
                    obj.save();
                }
            }
        }
    }

    private void showNotification(String message) {
        Intent i = new Intent(this,MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,i,PendingIntent.FLAG_UPDATE_CURRENT);

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        long pattern[] = { 0, 100, 200, 300, 400 };
        vibrator.vibrate(pattern,0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setContentTitle("SWMA")
                .setContentText(message)
                .setSmallIcon(R.mipmap.swma)
                .setSound(soundUri)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(),R.mipmap.swma))
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        manager.notify(0,builder.build());
        vibrator.cancel();

    }

}
