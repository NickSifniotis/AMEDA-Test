package au.net.nicksifniotis.amedatest.Activities;

import android.media.MediaPlayer;
import android.view.View;
import android.widget.MediaController;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.VideoView;

import au.net.nicksifniotis.amedatest.R;

/**
 * Launches the 'short tutorial video' activity!
 */
public class Tutorial extends AppCompatActivity
{
    private static VideoView _tutorial_viewer;
    private boolean _tutorial_on;
    private int _tutorial_position;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tutorial);

        _connect_gui();
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
    //    makeToast("in start tute");
        MediaController mediaControls = new MediaController(this);

        try
        {
            _tutorial_viewer.setMediaController(mediaControls);
            _tutorial_viewer.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/"
                                    + R.raw.tutorial));
        }
        catch (Exception e)
        {
   //         makeToast("Error: " + e.getMessage());
        }

        _tutorial_viewer.requestFocus();
        _tutorial_viewer.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
        {
            public void onPrepared(MediaPlayer mediaPlayer)
            {
                _tutorial_viewer.seekTo(_tutorial_position);
                _tutorial_viewer.start();
                _tutorial_on = true;
            }
        });

        _tutorial_viewer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            @Override
            public void onCompletion(MediaPlayer mp)
            {
                //_end_tutorial();
                _tutorial_on = false;
       //         makeToast("tutorial off");
            }
        });
    }
}
