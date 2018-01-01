package soft.salesmantracking;

import com.orm.SugarRecord;

/**
 * Created by Malik on 14/07/2017.
 */

public class StayInformation extends SugarRecord<StayInformation> {
    public StayInformation(){
        isSend = false;
    }
    int identity;
    String location, startTime, EndTime, Duration,Day,locationName;
    double longitude, latitude, distance;
    boolean isSend;
}
