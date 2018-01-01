package soft.salesmantracking;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        List<CheckinCheckOutTable> data = CheckinCheckOutTable.listAll(CheckinCheckOutTable.class);
        if(data.size() == 0){
            CheckinCheckOutTable obj =  new CheckinCheckOutTable();
            obj.isCheckIn = false;
            obj.isCheckOut = false;
            obj.save();
        }



        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        new Timer().schedule(new TimerTask(){
            public void run(){
                Splash.this.runOnUiThread(new Runnable() {
                    public void run(){
                        finish();
                        if(preferences.getBoolean("ïsSignIn",false) && preferences.getString("isActive","").equalsIgnoreCase("0")){
                            startActivity(new Intent(Splash.this,MainActivity.class));
                        }
                        else if(preferences.getString("isActive","").equalsIgnoreCase("1")){
                            startActivity(new Intent(Splash.this,DeactiveScreen.class));
                        }
                        else {
                            startActivity(new Intent(Splash.this,LogIn.class));
                        }

                    }
                });
            }
        },2500);
    }
}
