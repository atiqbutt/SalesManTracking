package soft.salesmantracking;

import com.orm.SugarRecord;

/**
 * Created by Malik on 12/07/2017.
 */

public class AddLocaton extends SugarRecord<AddLocaton> {

    public AddLocaton(){
        isSend = false;
    }
    boolean isSend;

    String LocationName, Image;
    double LocationLat, LocationLng;

}
