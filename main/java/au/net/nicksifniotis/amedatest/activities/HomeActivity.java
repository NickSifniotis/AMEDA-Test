package au.net.nicksifniotis.amedatest.activities;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import au.net.nicksifniotis.amedatest.Globals;
import au.net.nicksifniotis.amedatest.ManageRecordsEnum;
import au.net.nicksifniotis.amedatest.R;


/**
 * The main activity of the AMEDA app.
 *
 * Displays the navigation drawer pane and the three big buttons that launch activities.
 *
 */
public class HomeActivity extends NoConnectionActivity
{
    private TextView _debug_toggle;
    private TextView _short_test_toggle;
    private TextView _address_toggle;
    private DrawerLayout _drawer;


    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        _connect_gui();

        _update_drawer_toggles();

        checkBTState();

        Globals.InitialiseServices(this);
    }


    /**
     * Fire up the Bluetooth adapter if not already active.
     */
    private void checkBTState()
    {
        BluetoothAdapter _bt_adaptor = BluetoothAdapter.getDefaultAdapter();

        if (_bt_adaptor == null)
            Globals.Error(this, getString(R.string.error_ameda_no_bluetooth));
        else if (!_bt_adaptor.isEnabled())
        {
            Intent enableBtIntent = new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE");
            startActivityForResult(enableBtIntent, 1);
        }
    }


    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Globals.TerminateServices();
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

        _drawer = (DrawerLayout)findViewById(R.id.h_drawer);

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(
                this, _drawer, bar, R.string.h_d_nav_open, R.string.h_d_nav_close);

        if (_drawer != null)
            _drawer.addDrawerListener(mDrawerToggle);

        // Link to the textbox accessor variables.
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

    public void h_new_user(View view)
    {
        _launch_child_activity(EditUserActivity.class);
    }

    public void h_open_users(View view)
    {
        _launch_child_activity(ManageRecordsActivity.class);
    }

    private  File copyFileToExternal(String fileName)
    {
        File path = getDatabasePath(fileName);

        File file = null;
        String newPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
        try {
            FileInputStream fin = new FileInputStream (path.getAbsolutePath());
            FileOutputStream fos = new FileOutputStream(newPath + fileName);
            byte[] buffer = new byte[1024];
            int len1 = 0;
            while ((len1 = fin.read(buffer)) != -1) {
                fos.write(buffer, 0, len1);
            }
            fin.close();
            fos.close();
            file = new File(newPath + fileName);
            if (file.exists())
                return file;
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
        return null;
    }

    private void sendEmail(String email) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String currentDateandTime = sdf.format(new Date());

        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "AMEDA.db");
        Uri path = Uri.fromFile(file);
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("application/octet-stream");
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "AMEDA Database Backup");
        String to[] = { email };
        intent.putExtra(Intent.EXTRA_EMAIL, to);
        intent.putExtra(Intent.EXTRA_TEXT, "Copy of database taken on " + currentDateandTime);
        intent.putExtra(Intent.EXTRA_STREAM, path);
        startActivityForResult(Intent.createChooser(intent, "Send mail..."),
                1222);
    }

    public void h_d_email(View view)
    {
        copyFileToExternal("AMEDA.db");
        sendEmail("u5809912@anu.edu.au");
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
     * Utility function to launch 'some' activity.
     *
     * @param activity The activity class to launch.
     */
    private void _launch_child_activity (Class activity)
    {
        _launch_child_activity(activity, null);
    }


    /**
     * Overloaded utility function to launch some activity with a specified
     * extra data packet for the intent.
     *
     * @param activity The activity class to launch.
     * @param extra_intent The data to pass through to the intent.
     */
    private void _launch_child_activity (Class activity, ManageRecordsEnum extra_intent)
    {
        _drawer.closeDrawer(GravityCompat.START);

        Intent intent = new Intent(this, activity);
        if (extra_intent != null)
                intent.putExtra("activity", extra_intent.ordinal());
        startActivity(intent);
    }
}
