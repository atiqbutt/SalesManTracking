package soft.salesmantracking;

import com.orm.SugarRecord;

/**
 * Created by Malik on 25/07/2017.
 */

public class AttendanceData extends SugarRecord<AttendanceData> {
    public AttendanceData(){
        identity = "a";

    }
    boolean isCheckOut,isCheckIn;
    String identity;

}
