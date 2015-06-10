package jp.or.ixqsware.ringinformalsdksample.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import jp.or.ixqsware.ringinformaldriver.GestureInformation;
import jp.or.ixqsware.ringinformaldriver.RingDevice;
import jp.or.ixqsware.ringinformaldriver.RingDeviceInformation;
import jp.or.ixqsware.ringinformalsdksample.MainActivity;
import jp.or.ixqsware.ringinformalsdksample.R;
import jp.or.ixqsware.ringinformalsdksample.dialog.ConnectDialog;

import static jp.or.ixqsware.ringinformalsdksample.Constants.*;

/**
 * Sample of getting device information and sending notification.
 *
 * Created by hnakadate on 15/04/17.
 */
public class OperationFragment extends Fragment implements View.OnClickListener {
    private Button manufactureButton;
    private Button vibrationButton;
    private Button connectButton;
    private TextView resultView;
    private RingDevice ringDevice;
    private ConnectDialog connectDialog;
    private Handler mHandler = new Handler();

    private RingDevice.RingDeviceCallback ringCallback = new RingDevice.RingDeviceCallback() {
        /**
         * It invoked when the connection is successful.
         */
        @Override
        public void onDeviceConnected() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (!isAdded()) { return; }
                    connectButton.setText(getString(R.string.disconnect_label));
                    manufactureButton.setEnabled(true);
                    vibrationButton.setEnabled(true);
                    resultView.setText("Success to connect");
                    if (connectDialog != null && connectDialog.getShowsDialog()) {
                        connectDialog.onDismiss(connectDialog.getDialog());
                    }
                }
            });
        }

        /**
         * It invoked when the connection is failed.
         * @param result Reason of connection failed. (See below)
         */
        @Override
        public void onDeviceConnectionFailed(final int result) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (!isAdded()) { return; }
                    String reason = "Unknown";
                    switch (result) {
                        case 0:
                            reason = "Bluetooth not available.";
                            break;
                        case 1:
                            reason = "Not found Ring.";
                            break;
                        case 2:
                            reason = "Failed to get services.";
                            break;
                        case 3:
                            reason = "Failed to get characteristic.";
                            break;
                        case 4:
                            reason = "Connection is congested.";
                            break;
                    }
                    String msg = getString(R.string.connection_failed, reason);
                    resultView.setText(msg);
                    if (connectDialog != null && connectDialog.getShowsDialog()) {
                        connectDialog.onDismiss(connectDialog.getDialog());
                    }
                }
            });
        }

        /**
         * It invoked when disconnect from Ring.
         */
        @Override
        public void onDeviceDisconnected() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (!isAdded()) { return; }
                    resultView.setText("Disconnected.");
                    connectButton.setText(getString(R.string.connect_label));
                    manufactureButton.setEnabled(false);
                    vibrationButton.setEnabled(false);
                    if (connectDialog != null && connectDialog.getShowsDialog()) {
                        connectDialog.onDismiss(connectDialog.getDialog());
                    }
                }
            });
        }

        /**
         * It invoked when characteristic was read. (Result of 'getDeviceInformation')
         * @param name Characteristic name(RingDeviceInformation)
         * @param result Characteristic's value
         */
        @Override
        public void onCharacteristicRead(final String name, final String result) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (!isAdded()) { return; }
                    resultView.setText(name + ":" + result);
                }
            });
        }

        /**
         * It invoked when characteristic was wrote. (Result of 'sendNotification').
         * @param result Result code.
         */
        @Override
        public void onCharacteristicWrote(final int result) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (!isAdded()) { return; }
                    String msg = "";
                    switch (result) {
                        case 0:
                            msg = "Success to write characteristic.";
                            break;
                        case 1:
                            msg = "Failed to write characteristic.";
                            break;
                    }
                    resultView.setText(msg);
                }
            });
        }

        @Override
        public void onGestureDetected(GestureInformation performed,
                                      ArrayList<GestureInformation> recognized) {
        /* See DetectGestureFragment for sample */
        }

        @Override
        public void onGestureRegistered(ArrayList<GestureInformation> gestures) {
        /* See RegisterGestureDialog for sample */
        }
    };

    public static OperationFragment newInstance(int sectionNumber) {
        OperationFragment fragment = new OperationFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER, OPERATION_SECTION_ID));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_operation, container, false);

        connectButton = (Button) rootView.findViewById(R.id.connect_button);
        manufactureButton = (Button) rootView.findViewById(R.id.manufacture_name_button);
        vibrationButton = (Button) rootView.findViewById(R.id.vibration_button);

        connectButton.setOnClickListener(this);
        manufactureButton.setOnClickListener(this);
        vibrationButton.setOnClickListener(this);

        manufactureButton.setEnabled(false);
        vibrationButton.setEnabled(false);

        resultView = (TextView) rootView.findViewById(R.id.result_view);

        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        if (ringDevice != null) { ringDevice.disconnect(); }
        super.onStop();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.connect_button:
                if (getString(R.string.connect_label).equals(connectButton.getText())) {
                    ringDevice = new RingDevice(getActivity(), ringCallback);
                    connectDialog = ConnectDialog.newInstance(CONNECT_DIALOG_SECTION_ID);
                    connectDialog.setShowsDialog(true);
                    FragmentManager manager = getFragmentManager();
                    connectDialog.show(manager, "connect");
                    ringDevice.connect();
                } else {
                    ringDevice.disconnect();
                    ringDevice = null;
                }
                break;

            case R.id.manufacture_name_button:
                ringDevice.getDeviceInformation(RingDeviceInformation.MANUFACTURE_NAME);
                break;

            case R.id.vibration_button:
                /* Notification Type (0:LED, 1:Vibration, 2:LED+Vibration) */
                int type = 2;
                /* Notification count */
                int count = 2;
                ringDevice.sendNotification(type, count);
                break;
        }

    }
}
