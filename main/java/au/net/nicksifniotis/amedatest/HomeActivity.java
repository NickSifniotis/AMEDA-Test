package au.net.nicksifniotis.amedatest;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import au.net.nicksifniotis.amedatest.activities.CalibrationActivity;
import au.net.nicksifniotis.amedatest.activities.FamiliarisationActivity;
import au.net.nicksifniotis.amedatest.activities.ManageRecordsActivity;
import au.net.nicksifniotis.amedatest.activities.Tutorial;


/**
 * The main activity of the AMEDA app.
 *
 * @TODO Outstanding issues:
 * - layout needs tweaking - namely, the three big buttons are a bit ugly at the moment
 * - open record does nothing because the activity that it calls doesnt exist yet.
 *
 */
public class HomeActivity extends AppCompatActivity
{
    /**
     * Set up the toolbar and navigation drawer components.
     *
     * @param savedInstanceState Not really used.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        Toolbar bar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(bar);
        if (bar != null)
            bar.setNavigationIcon(R.drawable.toolbar_nav);

        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle("");

        DrawerLayout mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, bar, R.string.h_d_nav_open, R.string.h_d_nav_close)
        {
            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view)
            {
                super.onDrawerClosed(view);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView)
            {
                super.onDrawerOpened(drawerView);
            }
        };

        if (mDrawerLayout != null)
            mDrawerLayout.addDrawerListener(mDrawerToggle);
    }


    /**
     * Click event handlers.
     *
     * @param view Not used.
     */
    public void h_btn_tutorial(View view)
    {
        _launch_child_activity(Tutorial.class);
    }

    public void h_btn_familiarisation(View view)
    {
        _launch_child_activity(FamiliarisationActivity.class);
    }

    public void h_btn_begintest(View view)
    {
        _launch_test();
    }

    public void h_d_new(View view)
    {
        _launch_test();
    }

    public void h_d_open(View view)
    {
        // this is a todoo
    }

    public void h_d_manage(View view)
    {
        _launch_manage_records();
    }

    public void h_d_calibrate(View view)
    {
        _launch_child_activity(CalibrationActivity.class);
    }

    public void h_d_help(View view)
    {
        _launch_help();
    }


    /**
     * Show the app help, when that's implemented.
     * In the meantime, show a message about the help not being implemented yet.
     */
    private void _launch_help()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.not_implemented_title))
                .setMessage(getString(R.string.not_implemented_desc))
                .setPositiveButton(getString(R.string.btn_done), null);

        builder.create().show();
    }


    /**
     * Launches the 'select a person' activity with a view towards beginning the test instead
     * of editing their details.
     */
    private void _launch_test()
    {
        Intent testIntent = new Intent(this, ManageRecordsActivity.class);
        testIntent.putExtra("activity", ManageRecordsEnum.START_TEST.ordinal());
        startActivity(testIntent);
    }


    /**
     * Launch the 'look at a list of every user' activity.
     */
    private void _launch_manage_records()
    {
        Intent manageRecIntent = new Intent(this, ManageRecordsActivity.class);
        manageRecIntent.putExtra("activity", ManageRecordsEnum.EDIT_RECORD.ordinal());
        startActivity(manageRecIntent);
    }


    /**
     * Utility function to launch 'some' activity.
     *
     * @param activity The activity class to launch.
     */
    private void _launch_child_activity (Class activity)
    {
        Intent intent = new Intent(this, activity);
        startActivity(intent);
    }
}
