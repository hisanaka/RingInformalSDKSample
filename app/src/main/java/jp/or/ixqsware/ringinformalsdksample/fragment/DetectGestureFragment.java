package jp.or.ixqsware.ringinformalsdksample.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import jp.or.ixqsware.ringinformaldriver.GestureInformation;
import jp.or.ixqsware.ringinformaldriver.RingDevice;
import jp.or.ixqsware.ringinformaldriver.RingDevice.RingDeviceCallback;
import jp.or.ixqsware.ringinformalsdksample.MainActivity;
import jp.or.ixqsware.ringinformalsdksample.R;
import jp.or.ixqsware.ringinformalsdksample.db.SampleDatabaseHelper;
import jp.or.ixqsware.ringinformalsdksample.dialog.ConnectDialog;

import static jp.or.ixqsware.ringinformalsdksample.Constants.*;

/**
 * Sample of gesture detection.
 *
 * Created by hnakadate on 15/04/20.
 */
public class DetectGestureFragment extends Fragment implements View.OnClickListener {
    private ConnectDialog connectDialog;
    private Button connectButton;
    private RingDevice ringDevice;
    private GestureListAdapter adapter;
    private Handler mHandler = new Handler();
    private ListView listView;
    private ImageView detectImage;

    private RingDeviceCallback ringCallback = new RingDeviceCallback() {
        @Override
        public void onDeviceConnected() {
            /*
                If you want to set threshold difference between defined gestures and detect gesture.
                Default: 10.0f
             */
            ringDevice.setThreshold(13.0f);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (!isAdded()) { return; }
                    Toast.makeText(getActivity(), "Success to connect.", Toast.LENGTH_SHORT).show();
                    connectButton.setText(getString(R.string.disconnect_label));
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
                    Toast.makeText(getActivity(), reason, Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getActivity(), "Disconnected.", Toast.LENGTH_SHORT).show();
                    connectButton.setText(getString(R.string.connect_label));
                    if (connectDialog != null && connectDialog.getShowsDialog()) {
                        connectDialog.onDismiss(connectDialog.getDialog());
                    }
                }
            });
        }

        @Override
        public void onCharacteristicRead(String name, String result) {
        }

        @Override
        public void onCharacteristicWrote(int result) {
        }

        /*
            It invoked when the gesture detected.
            @param performed Information of the gesture you did.
            @param recognized List of Information of gestures that match the gesture you did.
         */
        @Override
        public void onGestureDetected(final GestureInformation performed,
                                      final ArrayList<GestureInformation> recognized) {
            if (recognized.size() == 0) {
                /* No matches */
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!isAdded()) { return; }
                        ringDevice.sendNotification(2, 2);
                        detectImage.setImageBitmap(performed.getBitmap());
                        listView.setVisibility(View.GONE);
                    }
                });
            } else {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!isAdded()) { return; }
                        ringDevice.sendNotification(2, 1);
                        ArrayList<GestureInformation> gestures = new ArrayList<>();
                        gestures.add(performed);
                        gestures.addAll(recognized);
                        detectImage.setImageBitmap(gestures.get(0).getBitmap());
                        listView.setVisibility(View.VISIBLE);
                        adapter = new GestureListAdapter(
                                getActivity(),
                                0,
                                gestures.subList(1, gestures.size()));
                        listView.setAdapter(adapter);
                        adapter.setNotifyOnChange(true);
                    }
                });
            }
        }

        @Override
        public void onGestureRegistered(ArrayList<GestureInformation> gestures) {
        }
    };

    public static DetectGestureFragment newInstance(int sectionNumber) {
        DetectGestureFragment fragment = new DetectGestureFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
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
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);

        connectButton = (Button) rootView.findViewById(R.id.connect_button);
        connectButton.setOnClickListener(this);

        listView = (ListView) rootView.findViewById(R.id.matching_list);

        detectImage = (ImageView) rootView.findViewById(R.id.detect_image);

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

        }
    }

    static class ViewHolder {
        ImageView gestureView;
        TextView idView;
        TextView titleView;
        TextView distanceView;
    }

    public class GestureListAdapter extends ArrayAdapter<GestureInformation> {
        private LayoutInflater layoutInflater;
        private SampleDatabaseHelper helper;
        private SQLiteDatabase db = null;

        public GestureListAdapter(Context context, int textViewResourceId,
                                  List<GestureInformation> objects) {
            super(context, textViewResourceId, objects);
            helper = SampleDatabaseHelper.getInstance(getActivity());
            if (db == null || !db.isOpen()) db = helper.getReadableDatabase();
            layoutInflater
                    = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final GestureInformation item = getItem(position);
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.list_item, null);
                viewHolder = new ViewHolder();
                viewHolder.gestureView = (ImageView) convertView.findViewById(R.id.image_view);
                viewHolder.idView = (TextView) convertView.findViewById(R.id.id_view);
                viewHolder.titleView = (TextView) convertView.findViewById(R.id.title_view);
                viewHolder.distanceView = (TextView) convertView.findViewById(R.id.distance_view);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            String title = helper.getGestureTitle(db, item.getId());
            viewHolder.titleView.setText("Title: " + title);
            viewHolder.gestureView.setImageBitmap(item.getBitmap());
            viewHolder.idView.setText("ID: " + item.getId());
            viewHolder.distanceView.setText("Distance: " + item.getDifferenceRate());

            return convertView;
        }
    }
}
