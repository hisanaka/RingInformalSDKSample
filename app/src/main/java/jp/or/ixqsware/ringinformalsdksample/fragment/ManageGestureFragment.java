package jp.or.ixqsware.ringinformalsdksample.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import jp.or.ixqsware.ringinformaldriver.GestureInformation;
import jp.or.ixqsware.ringinformaldriver.RingDevice;
import jp.or.ixqsware.ringinformalsdksample.MainActivity;
import jp.or.ixqsware.ringinformalsdksample.R;
import jp.or.ixqsware.ringinformalsdksample.asynctask.GetAllGesturesTask;
import jp.or.ixqsware.ringinformalsdksample.db.SampleDatabaseHelper;

import static jp.or.ixqsware.ringinformalsdksample.Constants.*;

/**
 * Sample of gesture management. - Get all gestures information(id, image).
 *
 * Created by hnakadate on 15/04/19.
 */
public class ManageGestureFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<ArrayList<GestureInformation>> {
    private GridView gridView;
    private int displayWidth;
    private Fragment thisFragment;
    private RingDevice ringDevice;
    private ProgressDialog dialog = null;

    private RingDevice.RingDeviceCallback mRingDeviceCallback = new RingDevice.RingDeviceCallback() {
        @Override
        public void onDeviceConnected() {}

        @Override
        public void onDeviceConnectionFailed(final int result) {}

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

    public static ManageGestureFragment newInstance(int sectionNumber) {
        ManageGestureFragment fragment = new ManageGestureFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER, MANAGER_SECTION_ID));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_manage, container, false);
        setHasOptionsMenu(false);

        gridView = (GridView) rootView.findViewById(R.id.gridView);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        thisFragment = this;

        WindowManager windowManager;
        windowManager = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        final Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        displayWidth = size.x;
        int colCnt = 3;
        if (displayWidth < 800) {
            colCnt = 2;
        } else if (displayWidth > 1280) {
            colCnt = 4;
        }
        gridView.setNumColumns(colCnt);

        ringDevice = new RingDevice(getActivity(), mRingDeviceCallback);

        LoaderManager loaderManager = getLoaderManager();
        loaderManager.restartLoader(GET_GESTURES_TASK_ID, null, ManageGestureFragment.this);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        ringDevice.disconnect();
        super.onStop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) return;

        Bundle bundle = data.getExtras();
        long gestureId = bundle.getLong(getString(R.string.gesture_id_key));
        ringDevice.removeGesture(gestureId);

        // reload
        dialog = new ProgressDialog(getActivity());
        dialog.setMessage(getString(R.string.progress_label));
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.show();

        LoaderManager loaderManager = getLoaderManager();
        loaderManager.restartLoader(GET_GESTURES_TASK_ID, null, this);
    }

    @Override
    public Loader<ArrayList<GestureInformation>> onCreateLoader(int id, Bundle args) {
        dialog = new ProgressDialog(getActivity());
        dialog.setMessage(getString(R.string.progress_label));
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.show();

        return new GetAllGesturesTask(getActivity());
    }

    @Override
    public void onLoadFinished(
            Loader<ArrayList<GestureInformation>> loader, ArrayList<GestureInformation> data) {
        GestureAdapter gestureAdapter = new GestureAdapter(getActivity(), 0, data);
        gridView.setAdapter(gestureAdapter);
        gestureAdapter.setNotifyOnChange(true);
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<GestureInformation>> loader) {}

    static class ViewHolder {
        ImageView gestureView;
        ImageView closeButton;
        TextView titleView;
    }

    private class GestureAdapter extends ArrayAdapter<GestureInformation> {
        private LayoutInflater layoutInflater;
        private SampleDatabaseHelper helper;
        private SQLiteDatabase db = null;

        public GestureAdapter(Context context, int textViewResourceId, List<GestureInformation> objects) {
            super(context, textViewResourceId, objects);
            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            helper = SampleDatabaseHelper.getInstance(getActivity());
            if (db == null || !db.isOpen()) db = helper.getReadableDatabase();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            GestureInformation item = getItem(position);
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.gesture_item, null);
                viewHolder = new ViewHolder();
                viewHolder.gestureView = (ImageView) convertView.findViewById(R.id.gesture_view);
                viewHolder.closeButton = (ImageView) convertView.findViewById(R.id.close_button);
                viewHolder.titleView = (TextView) convertView.findViewById(R.id.title_view);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            int colCnt = 3;
            if (displayWidth < 800) {
                colCnt = 2;
            } else if (displayWidth > 1280) {
                colCnt = 4;
            }
            int minHeight = displayWidth / colCnt;
            convertView.setMinimumHeight(minHeight);

            String title = helper.getGestureTitle(db, item.getId());
            viewHolder.titleView.setText(title);
            viewHolder.gestureView.setImageBitmap(item.getBitmap());
            viewHolder.closeButton.setTag(R.string.gesture_id_key, item.getId());
            viewHolder.closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Long id = (Long) v.getTag(R.string.gesture_id_key);
                    Bundle bundle = new Bundle();
                    bundle.putLong(getString(R.string.gesture_id_key), id);
                    DialogFragment removeConfFragment = new ConfirmRemoveDialog();
                    removeConfFragment.setTargetFragment(thisFragment, 0);
                    removeConfFragment.setArguments(bundle);
                    removeConfFragment.show(getFragmentManager(), "Remove");
                }
            });

            return convertView;
        }
    }

    public static class ConfirmRemoveDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final long gesturId = this.getArguments().getLong(getString(R.string.gesture_id_key));
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(getString(R.string.confirm_remove))
                    .setPositiveButton(
                            getString(R.string.yes_label),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Bundle bundle = new Bundle();
                                    bundle.putLong(getString(R.string.gesture_id_key), gesturId);
                                    Intent returnIntent = new Intent();
                                    returnIntent.putExtras(bundle);
                                    getTargetFragment().onActivityResult(
                                            1, Activity.RESULT_OK, returnIntent);
                                }
                            }
                    )
                    .setNegativeButton(
                            getString(R.string.no_label),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent returnIntent = new Intent();
                                    getTargetFragment().onActivityResult(
                                            1, Activity.RESULT_CANCELED, returnIntent);
                                }
                            }
                    );
            return builder.create();
        }
    }
}
