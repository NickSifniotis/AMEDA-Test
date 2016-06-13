package au.net.nicksifniotis.amedatest;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
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

    public void btn_Tute(View view)
    {
        Tutorial();
    }

    public void btn_Famil(View view)
    {
        Familiarise();
    }

    public void btn_NewRec(View view)
    {
        NewRecord();
    }

    public void btn_RunTest(View view) { RunTest(); }

    public void btn_Connect (View view) { Connect(); }

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

    private void Calibrate() {
        status_bar.setText("Calibrate");
    }

    private void DeleteRecord() {
        status_bar.setText("Delete Record!");
    }

    private void ManageRecords() {
        Intent manageRecIntent = new Intent(this, ManageRecordsActivity.class);
        startActivity(manageRecIntent);
    }

    private void RunTest() {
        Intent runTestIntent = new Intent (this, Test.class);
        startActivity(runTestIntent);

        // run an actual connection test
//        Handler h = new Handler(Looper.getMainLooper());
//        AMEDA device = null;
//        try {
////            device = new AMEDAImplementation(this, h, false);
////            makeToast("AMEDA connected!");
//
//       //     _device.Calibrate();
//       //     makeToast("Calibration command sent!");
//       //     Thread.sleep(2000);
//
//       //     _device.BeepTest(3);
//
//            _device.GoToPosition(1);
//            makeToast ("Moved to position 1");
//          //  Thread.sleep (2000);
//
//            _device.BeepTest(3);
//
//            _device.GoToPosition(3);
//            makeToast ("Moved to position 3");
//          //  Thread.sleep (2000);
//
//            _device.BeepTest(3);
//
//            _device.GoToPosition(5);
//            makeToast ("Moved to position 5");
//          //  Thread.sleep (2000);
//
//            _device.Terminate();
//        }
//        catch (Exception e)
//        {
//            makeToast("Error connecting to AMEDA");
//            return;
//        }


    }

    public void Connect ()
    {
        _handler = new Handler(Looper.getMainLooper());
        try
        {
            _device = new AMEDAImplementation(this, _handler, false);
        }
        catch (Exception e)
        {
            makeToast (e.getMessage());
        }

        makeToast ("AMEDA seems to be connected");
    }


    public void makeToast (String message)
    {
        Toast t = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        t.show();
    }
}
