package au.net.nicksifniotis.amedatest.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import au.net.nicksifniotis.amedatest.R;

/**
 * View User activity.
 *
 * View a user's details including their test history. Retake or delete unfinished tests.
 * Watch the user's progress improve (or otherwise) with repeated practice on the AMEDA
 * device.
 *
 * todo - pretty much build this one up from scratch. She won't be polished.
 */
public class ViewUser extends AppCompatActivity
{
    private int _user_id;


    /**
     * Create the activity, set up the GUI and look up the person
     * who's record we're seeing.
     *
     * @param savedInstanceState Not used.
     */
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_user_activity);

        // get the user_id of the person we are looking at.
        Intent intent = getIntent();
        _user_id = intent.getIntExtra("id", -1);
        if (_user_id == -1)
        {
            finish();
            return;
        }

        _connect_gui();
    }


    /**
     * Set up the GUI elements.
     */
    private void _connect_gui()
    {
        Toolbar bar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(bar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle("");

        if (bar != null)
        {
            bar.setNavigationIcon(R.drawable.toolbar_back);
            bar.setNavigationOnClickListener(new View.OnClickListener()
            {
                /**
                 * Clicking the back arrow is equivalent to saying 'stop the test, I wanna get off'
                 * So record it as an interrupted test.
                 *
                 * @param v Not used. Poor v :(
                 */
                @Override
                public void onClick(View v)
                {
                    finish();   // todo make sure this is right
                }
            });
        }
    }
}
