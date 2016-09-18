package au.net.nicksifniotis.amedatest.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import au.net.nicksifniotis.amedatest.AMEDA.AMEDA;
import au.net.nicksifniotis.amedatest.Connection.AMEDAConnection;
import au.net.nicksifniotis.amedatest.AMEDA.AMEDAInstruction;
import au.net.nicksifniotis.amedatest.AMEDA.AMEDAInstructionEnum;
import au.net.nicksifniotis.amedatest.AMEDA.AMEDAInstructionQueue;
import au.net.nicksifniotis.amedatest.AMEDA.AMEDAResponse;
import au.net.nicksifniotis.amedatest.Connection.Connection;
import au.net.nicksifniotis.amedatest.Connection.ConnectionMessage;
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
    private Connection _device;
    private boolean _connecting;
    private ProgressDialog _connect_progress;
    private AMEDAInstructionQueue _instruction_buffer;
    private Messenger _data_received;
    private Messenger _data_sent;


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
        _data_received = new Messenger (new Handler(new Handler.Callback()
        {
            @Override
            public boolean handleMessage(Message msg)
            {
                Globals.DebugToast.Send("AMEDAActivity handling message " + msg.what + " from connection");

                int msg_type = msg.what;

                if (msg_type == ConnectionMessage.CONNECTED.ordinal())
                    _handle_connected();
                else if (msg_type == ConnectionMessage.MESSENGER_READY.ordinal())
                {
                    _data_sent = _device.get_connection();

                    Message m = new Message();
                    m.what = ConnectionMessage.CONNECT.ordinal();
                    send_connection(m);
                }
                else if (msg_type == ConnectionMessage.RCVD.ordinal())
                {
                    AMEDAResponse response = (AMEDAResponse) msg.obj;
                    Globals.DebugToast.Send("Received " + response.toString() + " from connection");

                    ProcessAMEDAResponse(_instruction_buffer.Current(), response);
                }

                return true;
            }
        }));

        _connecting = false;
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

        Disconnect();
    }


    /**
     * Empty out the message handler on destruction.
     */
    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        _data_received = null;
    }


    /**
     * Attempts to establish a connection to the AMEDA device.
     *
     * Displays a funky dialog to the user to keep them occupied.
     */
    private void _reconnect()
    {
        if (_device != null)
            Disconnect();

        // Display the 'connecting' dialog box.
        if (_connect_progress != null)
            _connect_progress.dismiss();

        _connecting = true;

        _connect_progress = new ProgressDialog(this);
        _connect_progress.setTitle(getString(R.string.connecting_title));
        _connect_progress.setMessage(getString(R.string.connecting_desc));
        _connect_progress.setCancelable(false);
        _connect_progress.show();

        // try to connect to the thing.
        _device = (Globals.AMEDA_FREE) ?
                new VirtualConnection(_data_received) :
                new AMEDAConnection(this, _data_received);

        new Thread(_device).start();
    }


    /**
     * Send a message to the connection asking it to disconnect. todo convert this to midtier msg
     */
    protected void Disconnect ()
    {
        Message msg = new Message();
        msg.what = ConnectionMessage.SHUTDOWN.ordinal();

        send_connection(msg);
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
        Globals.DebugToast.Send("Received connection confirmation.");
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
        Globals.DebugToast.Send("Executing " + _instruction_buffer.Current().Build());

        Message m = new Message();
        m.what = ConnectionMessage.XMIT.ordinal();
        m.obj = _instruction_buffer.Current();

        send_connection(m);
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


    private void send_connection (Message m)
    {
        try
        {
            _data_sent.send(m);
        }
        catch (RemoteException e)
        {
            ///lkdfhgdkjh
        }
    }
}
