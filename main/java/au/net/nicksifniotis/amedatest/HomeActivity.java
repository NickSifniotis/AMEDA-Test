package au.net.nicksifniotis.amedatest;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

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
    private static RelativeLayout _control_panel;
    private static LinearLayout _video_panel;

    private int _tutorial_position = 0;
    private boolean _tutorial_on = false;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        _connect_gui();
        _control_panel.setVisibility(View.VISIBLE);
        _video_panel.setVisibility(View.GONE);
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
        _control_panel = (RelativeLayout)findViewById(R.id.home_control_panel);
        _video_panel = (LinearLayout)findViewById(R.id.tutorial_video_layout);
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
            case R.id.new_record_mnu:
                _launch_newRecord();
                return true;
            case R.id.tutorial_mnu:
                _start_tutorial();
                return true;
            case R.id.famil_mnu:
                _launch_familiarisation();
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
        _start_tutorial();
    }

    public void btn_home_familiarise(View view)
    {
        _launch_familiarisation();
    }

    public void btn_home_new_user(View view)
    {
        _launch_newRecord();
    }

    public void btn_home_tute_finished(View view)
    {
        _end_tutorial();
    }


    /*
        The action methods that do the things.
        Usually by loading up other activities and making them do the things.
     */
    private void _start_tutorial()
    {
        MediaController mediaControls = new MediaController(this);

        try
        {
            _tutorial_viewer.setMediaController(mediaControls);
            _tutorial_viewer.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/"
                                    + R.raw.tutorial));
        }
        catch (Exception e)
        {
            makeToast("Error: " + e.getMessage());
        }

        _control_panel.setVisibility(View.GONE);
        _video_panel.setVisibility(View.VISIBLE);

        _tutorial_viewer.requestFocus();
        _tutorial_viewer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            public void onPrepared(MediaPlayer mediaPlayer)
            {
                _tutorial_viewer.seekTo(_tutorial_position);
                _tutorial_viewer.start();
                _tutorial_on = true;
            }
        });
    }


    /**
     * Terminates the tutorial viewing.
     */
    private void _end_tutorial()
    {
        _tutorial_on = false;
        _tutorial_viewer.stopPlayback();
        _tutorial_viewer.setMediaController(null);

        _video_panel.setVisibility(View.GONE);
        _control_panel.setVisibility(View.VISIBLE);
    }


    /**
     * Screen rotation - pause the video and remember where we were.
     *
     * @param savedInstanceState The very tool that shall remember where we were.
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        super.onSaveInstanceState(savedInstanceState);

        //we use onSaveInstanceState in order to store the video playback position for orientation change
        savedInstanceState.putInt("Position", _tutorial_viewer.getCurrentPosition());
        savedInstanceState.putInt("tute_on", _tutorial_on ? 1 : 0);
        _tutorial_viewer.pause();
    }


    /**
     * Screen rotation - resume video playback.
     *
     * @param savedInstanceState Where we were up to.
     */
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        //we use onRestoreInstanceState in order to play the video playback from the stored position
        _tutorial_position = savedInstanceState.getInt("Position");
        _tutorial_on = (savedInstanceState.getInt("tute_on") == 1);

        if (_tutorial_on)
            _start_tutorial();
    }


    /**
     * Show the app help, when that's implemented.
     */
    private void Help() {
        _status_bar.setText("Help screen - not implemented yet.");
    }


    /**
     * Jump straight to the new record activity.
     */
    private void _launch_newRecord()
    {
        Intent newRecIntent = new Intent(this, NewRecordActivity.class);
        startActivity(newRecIntent);
    }


    /**
     * Launch the familiarisation activity.
     */
    private void _launch_familiarisation()
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
