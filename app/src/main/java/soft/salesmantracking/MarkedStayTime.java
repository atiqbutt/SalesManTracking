package soft.salesmantracking;

import com.orm.SugarRecord;

/**
 * Created by Hassan on 11/9/2017.
 */

public class MarkedStayTime extends SugarRecord {
    public MarkedStayTime(){
        isSend = false;
    }
    boolean isSend;

    String LocationName, Image, startTime, EndTime, Duration,Day, TotalStayDuration, MarkedStaySartTime;
    double LocationLat, LocationLng;
}
