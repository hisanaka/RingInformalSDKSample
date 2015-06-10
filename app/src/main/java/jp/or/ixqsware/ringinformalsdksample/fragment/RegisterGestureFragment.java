package jp.or.ixqsware.ringinformalsdksample.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import jp.or.ixqsware.ringinformaldriver.GestureInformation;
import jp.or.ixqsware.ringinformaldriver.RingDevice;
import jp.or.ixqsware.ringinformalsdksample.MainActivity;
import jp.or.ixqsware.ringinformalsdksample.R;
import jp.or.ixqsware.ringinformalsdksample.db.SampleDatabaseHelper;
import jp.or.ixqsware.ringinformalsdksample.dialog.ConnectDialog;

import static jp.or.ixqsware.ringinformalsdksample.Constants.*;

/**
 * Sample of gesture registration.
 *
 * Created by hnakadate on 15/04/18.
 */
public class RegisterGestureFragment extends Fragment implements View.OnClickListener {
    private ImageView imageView;
    private TextView resultView;
    private EditText titleEdit;
    private Button connectButton;
    private Button captureButton;
    private Button registerButton;
    private Button cancelButton;
    private RingDevice ringDevice;
    private ConnectDialog connectDialog;
    private Long gestureId = -1L;
    private Handler mHandler = new Handler();
    private static SQLiteDatabase db = null;

    private RingDevice.RingDeviceCallback ringDeviceCallback = new RingDevice.RingDeviceCallback() {
        @Override
        public void onDeviceConnected() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (!isAdded()) { return; }
                    connectButton.setText(getString(R.string.disconnect_label));
                    captureButton.setEnabled(true);
                    resultView.setText("Success to connect");
                    if (connectDialog != null && connectDialog.getShowsDialog()) {
                        connectDialog.onDismiss(connectDialog.getDialog());
                    }
                }
            });
        }

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
                            reason = "Ring not found.";
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
                    resultView.setText(reason);
                    if (connectDialog != null && connectDialog.getShowsDialog()) {
                        connectDialog.onDismiss(connectDialog.getDialog());
                    }
                }
            });
        }

        @Override
        public void onDeviceDisconnected() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (!isAdded()) { return; }
                    connectButton.setText(getString(R.string.connect_label));
                    captureButton.setText(getString(R.string.capture_label));
                    captureButton.setEnabled(false);
                    registerButton.setEnabled(false);
                    cancelButton.setEnabled(false);
                    resultView.setText("Disconnected.");
                    if (connectDialog != null && connectDialog.getShowsDialog()) {
                        connectDialog.onDismiss(connectDialog.getDialog());
                    }
                }
            });
        }

        @Override
        public void onCharacteristicRead(String name, String result) {
            /* See OperationFragment for sample. */
        }

        @Override
        public void onCharacteristicWrote(int result) {
            /* See OperationFragment for sample. */
        }

        @Override
        public void onGestureDetected(GestureInformation performed,
                                      ArrayList<GestureInformation> recognized) {
            /* See DetectGestureFragment for sample. */
        }

        /*
           This callback invoked when detected gesture is registered in the database that is exists
           in the RingInformalDriver.
         */
        @Override
        public void onGestureRegistered(final ArrayList<GestureInformation> gestures) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (!isAdded()) { return; }
                    /* Once you get the gesture to be registered, and then exit the registration. */
                    ringDevice.cancelRegisterGesture();

                    ringDevice.sendNotification(2, 1);
                    GestureInformation gesture = gestures.get(0);
                    gestureId = gesture.getId();
                    imageView.setImageBitmap(gesture.getBitmap());
                    imageView.setTag(R.string.gesture_id_key, gestureId);

                    registerButton.setEnabled(true);
                    cancelButton.setEnabled(true);
                    captureButton.setText(getString(R.string.capture_label));
                }
            });
        }
    };

    public static RegisterGestureFragment newInstance(int sectionNumber) {
        RegisterGestureFragment fragment = new RegisterGestureFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER, REGISTER_SECTION_ID));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_register, container, false);

        titleEdit = (EditText) rootView.findViewById(R.id.title_edit);

        connectButton = (Button) rootView.findViewById(R.id.connect_button);
        captureButton = (Button) rootView.findViewById(R.id.capture_button);
        registerButton = (Button) rootView.findViewById(R.id.register_button);
        cancelButton = (Button) rootView.findViewById(R.id.cancel_button);

        connectButton.setOnClickListener(this);
        captureButton.setOnClickListener(this);
        registerButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);

        captureButton.setEnabled(false);
        registerButton.setEnabled(false);
        cancelButton.setEnabled(false);

        imageView = (ImageView) rootView.findViewById(R.id.gesture_view);

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
                    ringDevice = new RingDevice(getActivity(), ringDeviceCallback);
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

            case R.id.capture_button:
                if (getString(R.string.capture_label).equals(captureButton.getText())) {
                    captureButton.setText(getString(R.string.cancel_label));
                    resultView.setText("Start capture.");
                    imageView.setImageBitmap(null);
                    titleEdit.setText("");
                    ringDevice.registerGesture();
                } else {
                    ringDevice.cancelRegisterGesture();
                    captureButton.setText(getString(R.string.capture_label));
                    resultView.setText("Stop capture.");
                }
                registerButton.setEnabled(false);
                cancelButton.setEnabled(false);
                break;

            case R.id.register_button:
                /*
                   Do something.
                   Notice: Gesture data is already registered in the database that is exist in the
                           RingInformalDriver. So, you might register gesture ID with the action
                           that you want to in your app's database, preferences, or else.
                 */
                SampleDatabaseHelper helper = SampleDatabaseHelper.getInstance(getActivity());
                if (db == null || !db.isOpen()) db = helper.getWritableDatabase();
                String title = titleEdit.getText().toString();
                if (title.length() == 0) {
                    Toast.makeText(getActivity(), "Input title", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (helper.registerGestureTitle(db, gestureId, title)) {
                    Toast.makeText(getActivity(), "Registered", Toast.LENGTH_SHORT).show();
                    imageView.setImageBitmap(null);
                    titleEdit.getEditableText().clear();
                } else {
                    Toast.makeText(getActivity(), "Failed to register", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.cancel_button:
                /*
                   If you don't want to register the gesture, you MUST remove gesture from
                   RingInformalDriver's internal database.
                 */
                if (ringDevice.removeGesture(gestureId)) {
                    imageView.setImageBitmap(null);
                    titleEdit.getEditableText().clear();
                    resultView.setText("Success to remove gesture data.");
                } else {
                    resultView.setText("Failed to remove gesture data.");
                }
                break;
        }
    }
}
