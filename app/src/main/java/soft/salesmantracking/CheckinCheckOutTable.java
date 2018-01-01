package soft.salesmantracking;

import com.orm.SugarRecord;

/**
 * Created by Hassan on 10/17/2017.
 */

public class CheckinCheckOutTable extends SugarRecord {
    public CheckinCheckOutTable(){

    }
    boolean isCheckIn, isCheckOut, isSend, isMarked, isKnowForOffline ;
    String checkOutLat, CheckOutLng, time, lat, lng;
    long markTime, markedPlcaeId;
}
