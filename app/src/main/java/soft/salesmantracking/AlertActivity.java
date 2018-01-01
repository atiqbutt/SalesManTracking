package soft.salesmantracking;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class AlertActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("No Internet");
        alertDialog.setMessage("You Are CheckedIn in SWMA");

        alertDialog.setPositiveButton("I Know",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        CheckinCheckOutTable obj = CheckinCheckOutTable.findById(CheckinCheckOutTable.class, (long) 1);
                        obj.isKnowForOffline = true;
                        obj.save();

                        finish();
                    }
                }
        );
        alertDialog.setNegativeButton("Remind Me Later",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();

                        finish();
                    }
                }
        );

        alertDialog.show();


    }
}
