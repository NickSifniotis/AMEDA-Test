package au.net.nicksifniotis.amedatest;

import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import au.net.nicksifniotis.amedatest.activities.CalibrationActivity;
import au.net.nicksifniotis.amedatest.activities.FamiliarisationActivity;
import au.net.nicksifniotis.amedatest.activities.ManageRecordsActivity;
import au.net.nicksifniotis.amedatest.activities.Tutorial;


/**
 * The main activity of the AMEDA app.
 *
 */
public class HomeActivity extends AppCompatActivity
{
    private TextView _ameda_toggle;
    private TextView _debug_toggle;
    private TextView _short_test_toggle;
    private TextView _address_toggle;



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

        _connect_gui();

        _update_drawer_toggles();
    }


    /**
     * Connects the GUI elements to the variables that hold them.
     */
    private void _connect_gui()
    {
        // Set up the toolbar and the navigation pane.
        Toolbar bar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(bar);
        if (bar != null)
            bar.setNavigationIcon(R.drawable.toolbar_nav);

        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle("");

        DrawerLayout mDrawerLayout = (DrawerLayout)findViewById(R.id.h_drawer);

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


        // Link to the textbox accessor variables.
        _ameda_toggle = (TextView)findViewById(R.id.h_d_t_ameda);
        _debug_toggle = (TextView)findViewById(R.id.h_d_t_debug);
        _short_test_toggle = (TextView)findViewById(R.id.h_d_t_shorttest);
        _address_toggle = (TextView)findViewById(R.id.h_d_t_address);
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
        _launch_edit_records();
    }

    public void h_d_calibrate(View view)
    {
        _launch_child_activity(CalibrationActivity.class);
    }

    public void h_d_help(View view)
    {
        _launch_help();
    }

    public void h_d_exit(View view)
    {
        finish();
    }

    public void h_d_t_ameda(View view)
    {
        Globals.AMEDA_FREE = !Globals.AMEDA_FREE;
        _update_drawer_toggles();
    }

    public void h_d_t_debug(View view)
    {
        Globals.DEBUG_MODE = !Globals.DEBUG_MODE;
        _update_drawer_toggles();
    }

    public void h_d_t_shorttest(View view)
    {
        Globals.SHORT_TESTS = !Globals.SHORT_TESTS;
        _update_drawer_toggles();
    }

    public void h_d_t_address(View view)
    {
        Globals.USING_ADDRESSES = !Globals.USING_ADDRESSES;
        _update_drawer_toggles();
    }


    /**
     * Update the text on the navigation drawer's 'developer options' subsegment.
     * To reflect the current state of the global variables that they are linked to.
     */
    private void _update_drawer_toggles()
    {
        _address_toggle.setText(getString(R.string.h_d_t_address, String.valueOf(Globals.USING_ADDRESSES)));
        _ameda_toggle.setText(getString(R.string.h_d_t_ameda, String.valueOf(Globals.AMEDA_FREE)));
        _debug_toggle.setText(getString(R.string.h_d_t_debug, String.valueOf(Globals.DEBUG_MODE)));
        _short_test_toggle.setText(getString(R.string.h_d_t_shorttest, String.valueOf(Globals.SHORT_TESTS)));
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
    private void _launch_edit_records()
    {
        Intent manageRecIntent = new Intent(this, ManageRecordsActivity.class);
        manageRecIntent.putExtra("activity", ManageRecordsEnum.VIEW_RECORD.ordinal());
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
