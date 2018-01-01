package soft.salesmantracking;

import com.orm.SugarRecord;

/**
 * Created by Malik on 18/07/2017.
 */

public class TraveledDistance extends SugarRecord<TraveledDistance> {

    public TraveledDistance(){
        isSend = false;
    }

    double lat,lng;
    boolean isSend;
}
