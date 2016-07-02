package au.net.nicksifniotis.amedatest;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import au.net.nicksifniotis.amedatest.AMEDAManager.AMEDA;
import au.net.nicksifniotis.amedatest.AMEDAManager.AMEDAImplementation;
import au.net.nicksifniotis.amedatest.AMEDAManager.VirtualAMEDA;
import au.net.nicksifniotis.amedatest.activities.FamiliarisationActivity;
import au.net.nicksifniotis.amedatest.activities.ManageRecordsActivity;
import au.net.nicksifniotis.amedatest.activities.NewRecordActivity;
import au.net.nicksifniotis.amedatest.activities.Tutorial;


/**
 * The main activity of the AMEDA app.
 *
 */
public class HomeActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle item selection
        switch (item.getItemId())
        {
            case R.id.calibrate_mnu:
                _calibrate();
                return true;
            case R.id.help_mnu:
                _launch_help();
                return true;
            case R.id.new_record_mnu:
                _launch_child_activity(NewRecordActivity.class);
                return true;
            case R.id.tutorial_mnu:
                _launch_child_activity(Tutorial.class);
                return true;
            case R.id.famil_mnu:
                _launch_child_activity(FamiliarisationActivity.class);
                return true;
            case R.id.manage_mnu:
                _launch_manage_records();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * Button click event handlers.
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


    /**
     * Show the app help, when that's implemented.
     * In the meantime, show a message about the help not being implemented yet.
     */
    private void _launch_help()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.not_implemented_title))
                .setMessage(getString(R.string.not_implemented_desc))
                .setPositiveButton(getString(R.string.btn_done), new DialogInterface.OnClickListener()
                {
                    /**
                     * Do nothing on click. Kind of a waste of 10 lines of code.
                     *
                     * @param dialog Not used
                     * @param which Not used
                     */
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                    }
                });

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
     * Calibrate the AMEDA device. The call is a blocking call that will suspend execution
     * until the AMEDA confirms that it has succeeded (or otherwise..)
     *
     */
    private void _calibrate()
    {
        makeToast("Calibrating ..");

        try
        {
            // @TODO implement a response code handler
            AMEDA device = (Globals.AMEDA_FREE) ? new VirtualAMEDA(this, null) : new AMEDAImplementation(this, null);

            if (!device.Calibrate())
                makeToast("Calibration failed. Try again.");
            else
                makeToast("Calibration succeeded.");

            device.Disconnect();
        }
        catch (Exception e)
        {
            makeToast ("Unable to connect to AMEDA device.");
        }
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


    public void makeToast (String message)
    {
        Toast t = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        t.show();
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
