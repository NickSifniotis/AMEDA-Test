package au.net.nicksifniotis.amedatest.Activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import au.net.nicksifniotis.amedatest.AMEDAManager.AMEDA;
import au.net.nicksifniotis.amedatest.AMEDAManager.AMEDAException;
import au.net.nicksifniotis.amedatest.AMEDAManager.AMEDAImplementation;
import au.net.nicksifniotis.amedatest.AMEDAManager.AMEDAInstruction;
import au.net.nicksifniotis.amedatest.AMEDAManager.AMEDAInstructionEnum;
import au.net.nicksifniotis.amedatest.AMEDAManager.AMEDAInstructionFactory;
import au.net.nicksifniotis.amedatest.AMEDAManager.AMEDAInstructionQueue;
import au.net.nicksifniotis.amedatest.AMEDAManager.AMEDAResponse;
import au.net.nicksifniotis.amedatest.AMEDAManager.VirtualAMEDA;
import au.net.nicksifniotis.amedatest.Globals;
import au.net.nicksifniotis.amedatest.R;


/**
 * Familiarisation task activity.
 */
public class FamiliarisationActivity extends AMEDAActivity
{
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


                _fields[num].setText(String.format(Locale.ENGLISH, "%d", curr_value));
            }
            else
                makeToast ("Sorry, you've already used up your five moves to this position.");
        }
        else
            DebugToast ("Strange error in that execute has been invoked with num=" + num);
    }


    /**
     * Interpret and respond to the AMEDA's response to the last instruction.
     *
     * @param instruction The instruction that was sent to the AMEDA.
     * @param response The AMEDA's response to that instruction.
     */
    @Override
    protected void ProcessAMEDAResponse (AMEDAInstruction instruction, AMEDAResponse response)
    {
        if (instruction.GetInstruction().IsValidResponse(response))
        {
            switch (response)
            {
                case READY:
                    ExecuteNextInstruction();
                    break;
                case CANNOT_MOVE:
                    CannotMoveDialog();
                    break;
                case CALIBRATION_FAIL:
                case UNKNOWN_COMMAND:
                case NO_RESPONSE_ANGLE:
                    makeToast("Received unknown response code for current command.");
                    break;
            }
        }
        else
        {
            DebugToast ("Received response " + response.toString() + " to command " + instruction.Build());
            finish();
        }
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
