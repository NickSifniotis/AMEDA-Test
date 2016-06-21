package au.net.nicksifniotis.amedatest;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import au.net.nicksifniotis.amedatest.AMEDAManager.AMEDA;
import au.net.nicksifniotis.amedatest.AMEDAManager.AMEDAImplementation;
import au.net.nicksifniotis.amedatest.AMEDAManager.VirtualAMEDA;

public class HomeActivity extends AppCompatActivity {

    private static TextView status_bar;
    private AMEDA _device;
    private Handler _handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        status_bar = (TextView)findViewById(R.id.txtStatus);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.calibrate_mnu:
                Calibrate();
                return true;
            case R.id.help_mnu:
                Help();
                return true;
            case R.id.del_record_mnu:
                DeleteRecord();
                return true;
            case R.id.new_record_mnu:
                NewRecord();
                return true;
            case R.id.tutorial_mnu:
                Tutorial();
                return true;
            case R.id.famil_mnu:
                Familiarise();
                return true;
            case R.id.manage_mnu:
                ManageRecords();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * Button click event handlers.
     *
     * @param view
     */
    public void btn_home_tutorial(View view)
    {
        Tutorial();
    }

    public void btn_home_familiarise(View view)
    {
        Familiarise();
    }

    public void btn_home_new_user(View view)
    {
        NewRecord();
    }


    /*
        The action methods that do the things.
        Usually by loading up other activities and making them do the things.
     */
    private void Tutorial() {
        status_bar.setText("Tutorial");
    }

    private void Help() {
        status_bar.setText("Help Screen");
    }

    private void NewRecord() {
        Intent newRecIntent = new Intent(this, NewRecordActivity.class);
        startActivity(newRecIntent);
    }

    private void Familiarise() {
        Intent familiarisationIntent = new Intent (this, FamiliarisationActivity.class);
        startActivity (familiarisationIntent);
    }


    /**
     * Calibrate the AMEDA device. The call is a blocking call that will suspend execution
     * until the AMEDA confirms that it has succeeded (or otherwise..)
     *
     */
    private void Calibrate()
    {
        makeToast("Calibrating ..");

        AMEDA device = new VirtualAMEDA();

        if (!device.Calibrate())
            makeToast ("Calibration failed. Try again.");
        else
            makeToast ("Calibration succeeded.");

        device.Terminate();
    }

    private void DeleteRecord() {
        status_bar.setText("Delete Record!");
    }

    private void ManageRecords() {
        Intent manageRecIntent = new Intent(this, ManageRecordsActivity.class);
        startActivity(manageRecIntent);
    }


    public void makeToast (String message)
    {
        Toast t = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        t.show();
    }
}
