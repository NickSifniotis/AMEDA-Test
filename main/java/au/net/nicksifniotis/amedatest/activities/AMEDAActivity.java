package au.net.nicksifniotis.amedatest.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import au.net.nicksifniotis.amedatest.AMEDAManager.AMEDA;
import au.net.nicksifniotis.amedatest.AMEDAManager.AMEDAImplementation;
import au.net.nicksifniotis.amedatest.AMEDAManager.AMEDAInstruction;
import au.net.nicksifniotis.amedatest.AMEDAManager.AMEDAInstructionEnum;
import au.net.nicksifniotis.amedatest.AMEDAManager.AMEDAInstructionQueue;
import au.net.nicksifniotis.amedatest.AMEDAManager.AMEDAResponse;
import au.net.nicksifniotis.amedatest.Connection.VirtualConnection;
import au.net.nicksifniotis.amedatest.Globals;
import au.net.nicksifniotis.amedatest.R;

/**
 * Extension of the default activity class, which hides boilerplate code
 * for accessing the AMEDA device.
 *
 * Connectivity is dealt with in this layer.
 *
 */
public abstract class AMEDAActivity extends AppCompatActivity
{
    private AMEDA _device;
    private boolean _connecting;
    private ProgressDialog _connect_progress;
    private AMEDAInstructionQueue _instruction_buffer;
    private Handler _response_handler;


    /**
     * Create the message handlers and an instance of the correct AMEDA device to connect to.
     *
     * @param savedInstanceState Not used.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        _instruction_buffer = new AMEDAInstructionQueue();
        _response_handler = new Handler(new Handler.Callback()
        {
            @Override
            public boolean handleMessage(Message msg)
            {
                int msg_type = msg.what;

                if (msg_type == AMEDA.RESPONSE)
                {
                    AMEDAResponse response = (AMEDAResponse) msg.obj;
                    DebugToast("Handling message " + response.toString());

                    ProcessAMEDAResponse(_instruction_buffer.Current(), response);
                }
                else if (msg_type == AMEDA.CONNECTED)
                    _handle_connected();

                return true;
            }
        });

        _connecting = false;
        _device = (Globals.AMEDA_FREE)
                ? new VirtualConnection(this, _response_handler)
                : new AMEDAImplementation(this, _response_handler);
    }


    /**
     * Connectivity is automatically taken care of when the activity starts.
     */
    @Override
    protected void onStart()
    {
        super.onStart();

        _reconnect();
    }


    /**
     * Disconnections happen automatically when the activity is asked to stop.
     */
    @Override
    protected void onStop()
    {
        super.onStop();

        _device.Disconnect();
    }


    /**
     * Empty out the message handler on destruction.
     */
    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        _response_handler.removeCallbacksAndMessages(null);
    }


    /**
     * Attempts to establish a connection to the AMEDA device.
     *
     * Displays a funky dialog to the user to keep them occupied.
     */
    private void _reconnect()
    {
        if (_connect_progress != null)
            _connect_progress.dismiss();

        _connecting = true;

        _connect_progress = new ProgressDialog(this);
        _connect_progress.setTitle(getString(R.string.connecting_title));
        _connect_progress.setMessage(getString(R.string.connecting_desc));
        _connect_progress.setCancelable(false);
        _connect_progress.setOnShowListener(new DialogInterface.OnShowListener()
        {
            /**
             * Cleverly, do not attempt to connect to the device until the dialog box has been
             * presented to the user. This is to prevent the UI thread from hanging on the call
             * to Connect. todo its not that clever, its not working
             *
             * @param dialog Not used.
             */
            @Override
            public void onShow(DialogInterface dialog)
            {
                if (!_device.Connect())
                    _connection_failure();
            }
        });
        _connect_progress.show();
    }


    /**
     * The device being connected to reported a failure to connect.
     *
     * Handle this situation by shutting down the activity.
     */
    private void _connection_failure()
    {
        _connect_progress.dismiss();
        FailAndDieDialog(getString(R.string.error_ameda_cannot_connect));
    }


    /**
     * Handles a 'we are connected' message from the AMEDA device.
     */
    private void _handle_connected()
    {
        DebugToast("Received connection confirmation.");
        if (_connecting)
        {
            if (_connect_progress != null)
            {
                _connect_progress.dismiss();
                _connect_progress = null;
            }

            _connecting = false;
        }
    }


    /**
     * Allows the child activity to (re)open a connection to the AMEDA device.
     * This call would usually follow on from a user-induced pause or stop in the activity.
     *
     * Don't call this method unless you've already called Disconnect(). Activity start
     * events connect themselves.
     */
    void Connect()
    {
        _reconnect();
    }


    /**
     * Disconnects from the AMEDA. Gives the child activity the ability to interrupt the
     * connection to the AMEDA device.
     */
    void Disconnect()
    {
        _device.Disconnect();
    }


    /**
     * Enqueues an instruction directing the AMEDA to move to a position.
     *
     * @param position The position to set the AMEDA to. Must be an integer between 1 and 5 inclusive.
     */
    void GoToPosition (int position)
    {
        if (position >= 1 && position <= 5)
        {
            _instruction_buffer.Enqueue(
                    AMEDAInstruction.Create()
                    .Instruction(AMEDAInstructionEnum.MOVE_TO_POSITION)
                    .N(position)
            );
        }
    }


    /**
     * Enqueues an instruction to begin the AMEDA's self calibration process.
     */
    void Calibrate ()
    {
        _instruction_buffer.Enqueue(
                AMEDAInstruction.Create()
                .Instruction(AMEDAInstructionEnum.CALIBRATE)
        );
    }


    /**
     * Sends a ping message to the AMEDA. Keep alive!
     */
    protected void Ping ()
    {
        _instruction_buffer.Enqueue(
                AMEDAInstruction.Create()
                .Instruction(AMEDAInstructionEnum.HELLO)
        );
    }


    /**
     * Sends a request to the AMEDA for the current angle of the wobble board.
     * @TODO implementation disabled as no way to correctly process this.
     */
    protected void RequestAngle ()
    {
//        _instruction_buffer.Enqueue(
//                AMEDAInstructionFactory.Create()
//                .Instruction(AMEDAInstructionEnum.REQUEST_ANGLE)
//        );
    }


    /**
     * Instructs the AMEDA to beep num_times times, capped at nine beeps.
     *
     * @param num_times The number of beeps to beep.
     */
    void Beep (int num_times)
    {
        if (num_times > 9)
            num_times = 9;

        _instruction_buffer.Enqueue(
                AMEDAInstruction.Create()
                .Instruction(AMEDAInstructionEnum.BUZZER_SHORT)
                .N(num_times)
        );
    }


    /**
     * Sends the next instruction to the AMEDA device.
     *
     * If the instruction queue is empty, then do nothing.
     */
    void ExecuteNextInstruction()
    {
        _instruction_buffer.Advance();
        RepeatInstruction();
    }


    /**
     * Retransmits the last instruction to the AMEDA device.
     */
    void RepeatInstruction()
    {
        DebugToast("Executing " + _instruction_buffer.Current().Build());
        _device.SendInstruction(_instruction_buffer.Current());
    }


    /**
     * Clear the instruction buffer. Could be used when failing or no response from machine.
     */
    void ClearInstructions()
    {
        _instruction_buffer.Clear();
    }


    /**
     * @return True if there are more instructions remaining in the instruction queue.
     */
    boolean HasMoreInstructions() { return _instruction_buffer.HasNext(); }


    /**
     * Process the AMEDA's response to the instruction that it just received.
     *
     * This abstract method needs to be implemented by every Activity that sends commands
     * to the AMEDA device.
     *
     * @param instruction The instruction that was sent to the AMEDA.
     * @param response The AMEDA's response to that instruction.
     */
    protected abstract void ProcessAMEDAResponse (AMEDAInstruction instruction, AMEDAResponse response);


    /**
     * Sends a toasty message to the screen. The call to Toast.makeText is run through the UI thread
     * since there is no guarantee that the caller of this method is working in that thread.
     *
     * [Toast needs to run through the UI thread because just because.]
     *
     * Messages are only displayed if debug mode is on - if not, they are suppressed.
     *
     * This method is designed to be used for debugging only - do not send messages through this
     * method that you want the user to see.
     *
     * @param message The message to display.
     */
    public void DebugToast (String message)
    {
        if (Globals.DEBUG_MODE)
        {
            final String msg = message;
            final Context parent = this;

            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    Toast t = Toast.makeText(parent, msg, Toast.LENGTH_SHORT);
                    t.show();
                }
            });
        }
    }


    /**
     * Produces a dialog box warning the user against fooling around with the wobble board
     * while the device is trying to move itself.
     *
     * Clears the instruction queue if the user selects cancel,
     * re-executes the last instruction if they select try again.
     */
    void CannotMoveDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.error_title))
                .setMessage(getString(R.string.error_ameda_not_horizontal))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.btn_done), new DialogInterface.OnClickListener()
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
                        RepeatInstruction();
                    }
                })
                .setNegativeButton(getString(R.string.btn_cancel), new DialogInterface.OnClickListener()
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
                        ClearInstructions();
                    }
                });

        builder.create().show();
    }


    /**
     * Serious error failure routine. Displays a dialog box and then terminates the activity.
     *
     * Called only when something has gone spectacularly wrong, like losing the connection
     * or receiving garbage or unexpected responses from the AMEDA.
     *
     * @param message The message to display to the user before dying.
     */
    void FailAndDieDialog(String message)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.error_title))
                .setMessage(message)
                .setCancelable(false)
                .setNegativeButton(getString(R.string.btn_done), new DialogInterface.OnClickListener()
                {
                    /**
                     * This is the end of all things.
                     *
                     * @param dialog Unused
                     * @param which Unused
                     */
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        finish();
                    }
                });

        builder.create().show();
    }
}
