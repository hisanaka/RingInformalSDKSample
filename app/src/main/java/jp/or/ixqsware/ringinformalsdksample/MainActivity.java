package jp.or.ixqsware.ringinformalsdksample;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

import jp.or.ixqsware.ringinformalsdksample.fragment.DetectGestureFragment;
import jp.or.ixqsware.ringinformalsdksample.fragment.ManageGestureFragment;
import jp.or.ixqsware.ringinformalsdksample.fragment.NavigationDrawerFragment;
import jp.or.ixqsware.ringinformalsdksample.fragment.OperationFragment;
import jp.or.ixqsware.ringinformalsdksample.fragment.RegisterGestureFragment;

import static jp.or.ixqsware.ringinformalsdksample.Constants.*;

public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle;
    private ArrayList<Fragment> arrFragments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        arrFragments.add(OperationFragment.newInstance(OPERATION_SECTION_ID));
        arrFragments.add(RegisterGestureFragment.newInstance(REGISTER_SECTION_ID));
        arrFragments.add(DetectGestureFragment.newInstance(DETECT_SECTION_ID));
        arrFragments.add(ManageGestureFragment.newInstance(MANAGER_SECTION_ID));

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.popBackStack();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack(
                    fragmentManager.getBackStackEntryAt(0).getName(),
                    FragmentManager.POP_BACK_STACK_INCLUSIVE
            );
        }
        fragmentManager.executePendingTransactions();
        fragmentManager.beginTransaction()
                .replace(R.id.container, arrFragments.get(position))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case OPERATION_SECTION_ID:
                mTitle = getString(R.string.title_section1);
                break;
            case REGISTER_SECTION_ID:
                mTitle = getString(R.string.title_section2);
                break;
            case DETECT_SECTION_ID:
                mTitle = getString(R.string.title_section3);
                break;
            case MANAGER_SECTION_ID:
                mTitle = getString(R.string.title_section4);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
