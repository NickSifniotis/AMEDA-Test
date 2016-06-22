package au.net.nicksifniotis.amedatest;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.net.URI;

import au.net.nicksifniotis.amedatest.AMEDAManager.AMEDA;
import au.net.nicksifniotis.amedatest.AMEDAManager.AMEDAImplementation;
import au.net.nicksifniotis.amedatest.AMEDAManager.VirtualAMEDA;


/**
 * The main activity of the AMEDA app.
 *
 */
public class HomeActivity extends AppCompatActivity
{
    private static TextView _status_bar;
    private static VideoView _tutorial_viewer;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        _connect_gui();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }


    /**
     * Connect the GUI components to the local variables ...
     * I'm seeing this method appear over and over!
     *
     */
    private void _connect_gui()
    {
        _status_bar = (TextView)findViewById(R.id.txtStatus);
        _tutorial_viewer = (VideoView)findViewById(R.id.tutorial_viewer);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle item selection
        switch (item.getItemId())
        {
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
    private void Tutorial()
    {
       // _tutorial_viewer.setVideoURI(android.net.Uri.parse(android/+R.raw.tutorial));

        MediaController mediaControls = new MediaController(this);

        // create a progress bar while the video file is loading
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Test dialog plxz wait");
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        try
        {
            _tutorial_viewer.setMediaController(mediaControls);
            _tutorial_viewer.setVideoURI(Uri.parse("https://www.youtube.com/watch?v=tT9gT5bqi6Y"));
        }
        catch (Exception e)
        {
            makeToast("Error: " + e.getMessage());
        }

        _tutorial_viewer.requestFocus();
        _tutorial_viewer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            public void onPrepared(MediaPlayer mediaPlayer)
            {
                progressDialog.dismiss();
                _tutorial_viewer.seekTo(0);
                _tutorial_viewer.start();
            }
        });
    }

    private void Help() {
        _status_bar.setText("Help Screen");
    }

    private void NewRecord()
    {
        Intent newRecIntent = new Intent(this, NewRecordActivity.class);
        startActivity(newRecIntent);
    }

    private void Familiarise()
    {
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

        try
        {
            AMEDA device = (Globals.AMEDA_FREE) ? new VirtualAMEDA(this) : new AMEDAImplementation(this);

            if (!device.Calibrate())
                makeToast("Calibration failed. Try again.");
            else
                makeToast("Calibration succeeded.");

            device.Terminate();
        }
        catch (Exception e)
        {
            makeToast ("Unable to connect to AMEDA device.");
        }
    }


    private void DeleteRecord()
    {
        _status_bar.setText("Delete Record!");
    }


    /**
     * Launch the 'look at a list of every user' activity.
     */
    private void ManageRecords()
    {
        Intent manageRecIntent = new Intent(this, ManageRecordsActivity.class);
        startActivity(manageRecIntent);
    }


    public void makeToast (String message)
    {
        Toast t = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        t.show();
    }
}
