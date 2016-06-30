package au.net.nicksifniotis.amedatest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import au.net.nicksifniotis.amedatest.AMEDAManager.AMEDA;
import au.net.nicksifniotis.amedatest.AMEDAManager.AMEDAImplementation;
import au.net.nicksifniotis.amedatest.AMEDAManager.NewAmeda;
import au.net.nicksifniotis.amedatest.AMEDAManager.VirtualAMEDA;


/**
 * Familiarisation task activity.
 */
public class FamiliarisationActivity extends AppCompatActivity
{
    private AMEDA _device;
    private TextView[] _fields;


    /**
     * Launches the activity.
     *
     * Launch sounds so much better than create.
     *
     * @param savedInstanceState Restoration bundle from previous instance.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.familiarisation);
        _connect_gui();

        try
        {
            _device = (Globals.AMEDA_FREE) ? new VirtualAMEDA(this) : new NewAmeda(this);
            _device.Connect();
        }
        catch (Exception e)
        {
            makeToast("Unable to connect to AMEDA in constructor");
        }
    }


    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        _device.Terminate();
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        _device.Terminate();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        _device.Connect();
    }


    /**
     * Connects the GUI elements to the local variables that represent them.
     */
    private void _connect_gui ()
    {
        _fields = new TextView[6];
        _fields[0] = null;
        _fields[1] = (TextView)(findViewById(R.id.famil_text_1));
        _fields[2] = (TextView)(findViewById(R.id.famil_text_2));
        _fields[3] = (TextView)(findViewById(R.id.famil_text_3));
        _fields[4] = (TextView)(findViewById(R.id.famil_text_4));
        _fields[5] = (TextView)(findViewById(R.id.famil_text_5));
    }


    /**
     * Button onClick event handlers.
     *
     * @param view Not used.
     */
    public void btn_famil_1(View view)
    {
        execute (1);
    }

    public void btn_famil_2(View view)
    {
        execute (2);
    }

    public void btn_famil_3(View view)
    {
        execute (3);
    }

    public void btn_famil_4(View view)
    {
        execute (4);
    }

    public void btn_famil_5(View view)
    {
        execute (5);
    }

    public void btn_familiarise_done(View view)
    {
        done();
    }


    /**
     * Make toast shortcut method. This is used everywhere it should live in the globals or something.
     *
     * @param message The message to display.
     */
    public void makeToast (String message)
    {
        Toast t = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        t.show();
    }


    /**
     * Move the device to the requested position, for familiarisation purposes.
     *
     * @param num The position to move the AMEDA to.
     */
    private void execute(int num)
    {
        if (_fields[num] != null)
        {
            int curr_value = Integer.parseInt(_fields[num].getText().toString());
            if (curr_value < 5)
            {
                curr_value++;

                _device.GoToPosition(1);
                _device.GoToPosition(5);
                _device.GoToPosition(num);
                _device.Beep(1);

                _fields[num].setText (String.format("%d", curr_value));
            }
            else
                makeToast ("Sorry, you've already used up your five moves to this position.");
        }
        else
            makeToast ("Strange error in that execute has been invoked with num=" + num);
    }


    /**
     * That's it, we are done.
     *
     * If a score or state needs to be saved, this is the place in which to do it.
     */
    private void done()
    {
        finish();
    }
}
