package soft.salesmantracking;

import android.util.Log;

import static android.content.ContentValues.TAG;

/**
 * Created by Hassan on 9/29/2017.
 */

public class MyThreadUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {

        Log.e(TAG, "Received exception '" + ex.getMessage() + "' from thread " + thread.getName(), ex);
    }
}
