package au.net.nicksifniotis.amedatest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class HomeActivity extends AppCompatActivity {

    private static TextView status_bar;

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
        status_bar.setText("Familiarise");
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
    }
}
