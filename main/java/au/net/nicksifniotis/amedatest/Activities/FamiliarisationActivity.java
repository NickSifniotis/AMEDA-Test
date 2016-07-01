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
public class FamiliarisationActivity extends AppCompatActivity
{
    private AMEDA _device;
    private TextView[] _fields;
    private Handler _response_handler;
    private AMEDAInstructionQueue _instruction_buffer;


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

        _instruction_buffer = new AMEDAInstructionQueue();
        _response_handler = new Handler(new Handler.Callback()
        {
            /**
             * Handle responses received from the AMEDA device.
             *
             * At the moment, there is only one sort of message that this method handles, the
             * 'response received' message with a @TODO magic number of 1
             *
             * @param msg The message received from the AMEDA reader.
             * @return True if succesful, true otherwise @TODO come on what
             */
            @Override
            public boolean handleMessage(Message msg)
            {
                makeToast ("Message received.");

                int what = msg.what;
                if (what == 1)
                {
                    AMEDAResponse response = (AMEDAResponse) msg.obj;
                    _response_received(response);
                }

                return true;
            }
        });

        _device = (Globals.AMEDA_FREE) ? new VirtualAMEDA(this) : new AMEDAImplementation(this, _response_handler);
    }


    @Override
    protected void onStop()
    {
        super.onStop();

        _device.Disconnect();
    }

    @Override
    protected void onStart()
    {
        super.onStart();

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

                _instruction_buffer.Enqueue(AMEDAInstructionFactory.Create()
                        .Instruction(AMEDAInstructionEnum.MOVE_TO_POSITION)
                        .N(1));

                _instruction_buffer.Enqueue(AMEDAInstructionFactory.Create()
                        .Instruction(AMEDAInstructionEnum.MOVE_TO_POSITION)
                        .N(5));

                _instruction_buffer.Enqueue(AMEDAInstructionFactory.Create()
                        .Instruction(AMEDAInstructionEnum.MOVE_TO_POSITION)
                        .N(num));

                _instruction_buffer.Enqueue(AMEDAInstructionFactory.Create()
                        .Instruction(AMEDAInstructionEnum.BUZZER_SHORT)
                        .N(1));

                _next_instruction();
                //_fields[num].setText(String.format(Locale.ENGLISH, "%d", curr_value));
            }
            else
                makeToast ("Sorry, you've already used up your five moves to this position.");
        }
        else
            makeToast ("Strange error in that execute has been invoked with num=" + num);
    }


    /**
     * The AMEDA has responded to the instruction just sent to it.
     * Advance to the appropriate state based on the device's response.
     *
     * @param response The response code received.
     */
    private void _response_received(AMEDAResponse response)
    {
        switch (response)
        {
            case READY:
                _next_instruction();
                break;
            case CANNOT_MOVE:
                _cannot_move_handler();
                break;
            case CALIBRATION_FAIL:
            case UNKNOWN_COMMAND:
            case NO_RESPONSE_ANGLE:
                makeToast("Received unknown response code for current command.");
                break;
        }
    }


    /**
     * Sends the next instruction to the AMEDA device.
     *
     * If the instruction queue is empty, then do nothing.
     */
    private void _next_instruction()
    {
        _instruction_buffer.Advance();
        _repeat_instruction();
    }


    /**
     * Retransmits the last instruction to the AMEDA device.
     */
    private void _repeat_instruction()
    {
        makeToast("Executing " + _instruction_buffer.Current().Build());
        ((AMEDAImplementation) _device).SendInstruction(_instruction_buffer.Current());
    }


    /**
     * Method called when the AMEDA reports that it is unable to move to the next position.
     * Display a dialog box complaining about it to the user, and then re-try sending the
     * instruction again.
     */
    private void _cannot_move_handler()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error")
                .setMessage("Unable to move the AMEDA device. Please make sure the plate is horizontal.")
                .setCancelable(false)
                .setPositiveButton("Try Again", new DialogInterface.OnClickListener()
                {
                    /**
                     * Try again - resend the last instruction to the AMEDA.
                     *
                     * @param dialog Unused
                     * @param which Unused
                     */
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        _repeat_instruction();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    /**
                     * Cancel - so clear the instruction buffer.
                     *
                     * @param dialog Unused
                     * @param which Unused
                     */
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        _instruction_buffer.Clear();
                    }
                });

        builder.create().show();
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
