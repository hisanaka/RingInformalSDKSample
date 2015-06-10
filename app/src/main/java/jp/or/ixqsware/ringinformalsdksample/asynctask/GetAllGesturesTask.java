package jp.or.ixqsware.ringinformalsdksample.asynctask;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.ArrayList;

import jp.or.ixqsware.ringinformaldriver.GestureInformation;
import jp.or.ixqsware.ringinformaldriver.RingDevice;

/**
 * Created by hnakadate on 15/04/19.
 */
public class GetAllGesturesTask extends AsyncTaskLoader<ArrayList<GestureInformation>> {
    private Context context;
    private RingDevice.RingDeviceCallback mCallback = new RingDevice.RingDeviceCallback() {
        @Override
        public void onDeviceConnected() {}

        @Override
        public void onDeviceConnectionFailed(int result) {}

        @Override
        public void onDeviceDisconnected() {}

        @Override
        public void onCharacteristicRead(String name, String result) {}

        @Override
        public void onCharacteristicWrote(int result) {}

        @Override
        public void onGestureDetected(GestureInformation performed,
                                      ArrayList<GestureInformation> recognized) {}

        @Override
        public void onGestureRegistered(ArrayList<GestureInformation> gestures) {}
    };

    public GetAllGesturesTask(Context context_) {
        super(context_);
        this.context = context_;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
        super.onStartLoading();
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
        super.onStopLoading();
    }

    @Override
    protected void onReset() {
        cancelLoad();
        super.onReset();
    }

    @Override
    public ArrayList<GestureInformation> loadInBackground() {
        RingDevice myRing = new RingDevice(context, mCallback);
        ArrayList<GestureInformation> arrGestures = myRing.getAllGestures();
        return arrGestures;
    }
}
