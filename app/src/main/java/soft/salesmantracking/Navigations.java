package soft.salesmantracking;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class Navigations extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigations);
    }





    public void AddLocation(View view) {

        Intent addLocation = new Intent(this,MainActivity.class);
        //addLocation.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(addLocation);

    }

    public void SearchLocation(View view) {

        Intent searchLocation = new Intent(this,MainActivity.class);
        searchLocation.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(searchLocation);
    }

    public void CurrentLocation(View view) {

        Intent currentLocation = new Intent(this,MainActivity.class);
        currentLocation.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(currentLocation);


    }
}
