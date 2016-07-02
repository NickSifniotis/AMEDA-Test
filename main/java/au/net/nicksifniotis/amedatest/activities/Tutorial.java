package au.net.nicksifniotis.amedatest.activities;

import android.media.MediaPlayer;
import android.view.View;
import android.widget.MediaController;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.VideoView;

import au.net.nicksifniotis.amedatest.R;

/**
 * Launches the 'short tutorial_activity video' activity!
 */
public class Tutorial extends AppCompatActivity
{
    private static VideoView _tutorial_viewer;
    private int _tutorial_position;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tutorial_activity);

        _connect_gui();
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
        _tutorial_viewer.pause();
        savedInstanceState.putInt("Position", _tutorial_viewer.getCurrentPosition());
    }


    /**
     * When the activity is ready to roll, start the tutorial_activity video.
     */
    @Override
    public void onStart()
    {
        super.onStart();

        _start_tutorial();
    }


    /**
     * Kill the tutorial_activity when the user exits.
     */
    @Override
    public void onStop()
    {
        super.onStop();

        _end_tutorial();
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
    }


    /**
     * Connect the GUI elements to the variables that point to them.
     */
    private void _connect_gui()
    {
        _tutorial_viewer = (VideoView)findViewById(R.id.t_videoview);
    }


    /**
     * Event handler for the 'done' button.
     *
     * @param view Not used.
     */
    public void t_btn_done (View view)
    {
        finish();
    }


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
        }

        _tutorial_viewer.requestFocus();
        _tutorial_viewer.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
        {
            public void onPrepared(MediaPlayer mediaPlayer)
            {
                _tutorial_viewer.seekTo(_tutorial_position);
                _tutorial_viewer.start();
            }
        });

        _tutorial_viewer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            @Override
            public void onCompletion(MediaPlayer mp)
            {
                _end_tutorial();
            }
        });
    }


    /**
     * Terminates the tutorial_activity viewing.
     */
    private void _end_tutorial()
    {
        _tutorial_viewer.stopPlayback();
        _tutorial_viewer.setMediaController(null);
    }
}
