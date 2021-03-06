package au.net.nicksifniotis.amedatest.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

import au.net.nicksifniotis.amedatest.AMEDA.AMEDAInstruction;
import au.net.nicksifniotis.amedatest.AMEDA.AMEDAInstructionEnum;
import au.net.nicksifniotis.amedatest.AMEDA.AMEDAInstructionQueue;
import au.net.nicksifniotis.amedatest.AMEDA.AMEDAResponse;
import au.net.nicksifniotis.amedatest.Globals;
import au.net.nicksifniotis.amedatest.Messages.ActivityMessage;
import au.net.nicksifniotis.amedatest.Messages.ManagerMessage;
import au.net.nicksifniotis.amedatest.Messages.Messages;
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
    private AMEDAInstructionQueue _instruction_buffer;
    protected boolean uses_connection = true;
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
    }


    /**
     * Boilerplace stuff for connecting this activity to the global ConnectionManager object.
     */
    @Override
    protected void onStart()
    {
        super.onStart();

        _data_sent = Globals.ConnectionManager.UpdateActivity(this);

        if (!Globals.ConnectionManager.Connected && uses_connection)
        {
            DisconnectionHandler();
            FailButDontDie("No connection detected. Please connect before attempting to begin this activity.");
        }
    }


    /**
     * The callback function for handling messages received from the connection manager.
     *
     * @param msg The message received.
     */
    public void HandleManagerMessage(Message msg)
    {
        Globals.DebugToast.Send("AMEDAActivity handling message "
                + ManagerMessage.toString(msg) + " from manager");

        switch (ManagerMessage.Message(msg))
        {
            case RCVD:
                // data packet has been received from the AMEDA!
                AMEDAResponse response = Messages.GetResponse(msg);
                Globals.DebugToast.Send("Received " + response.toString() + " from connection");

                ProcessAMEDAResponse(_instruction_buffer.Current(), response);
                break;

            case TIMEOUT:
                TimeoutHandler();
                break;

            case CONNECTION_DROPPED:
                DisconnectionHandler();
                FailButDontDie("Connection dropped. Please reconnect to continue.");
                break;

            case CONNECTION_RESUMED:
                ReconnectionHandler();
                FailButDontDie("Connection restored. You may now resume this activity.");
                break;

            default:
                break;
        }
    }


    /**
     * Contains the mechanisms needed to force the connection to reset.
     */
    public void Timeout_Reset()
    {
        Globals.DebugToast.Send("Timeout Reset called");
        send_connection(Messages.Create(ActivityMessage.TIMEOUT_ABORT));
    }


    /**
     * Handles an AMEDA timeout event. The default action is to do nothing; activities may
     * override this default behaviour as they see fit.
     */
    public void TimeoutHandler()
    {
        FailButDontDie("AMEDA timed out.");
    }


    /**
     * Handle a connection loss by disabling the part of the view that requires the connection
     * to be live for it to make any sense.
     */
    public void DisconnectionHandler()
    {
        setViewAndChildrenEnabled(findViewById(R.id.layout_base), false);
    }


    /**
     * Handles a reconnection after a dropout.
     *
     * This method is guaranteed to not be called before a call to DisconnectionHandler() is made.
     */
    public void ReconnectionHandler()
    {
        setViewAndChildrenEnabled(findViewById(R.id.layout_base), true);
    }


    /**
     * Enqueues an instruction directing the AMEDA to move to a position.
     *
     * @param position The position to set the AMEDA to. Must be an integer between 1 and 5 inclusive.
     */
    void GoToPosition (int position)
    {
        if (!Globals.ConnectionManager.Connected)
            return;

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
        if (!Globals.ConnectionManager.Connected)
            return;

        _instruction_buffer.Enqueue(
                AMEDAInstruction.Create()
                .Instruction(AMEDAInstructionEnum.CALIBRATE)
        );
    }


    /**
     * Sends a request to the AMEDA for the current angle of the wobble board.
     */
    protected void RequestAngle ()
    {
        _instruction_buffer.Enqueue(
                AMEDAInstruction.Create()
                .Instruction(AMEDAInstructionEnum.REQUEST_ANGLE)
        );
    }


    /**
     * Instructs the AMEDA to beep num_times times, capped at nine beeps.
     *
     * @param num_times The number of beeps to beep.
     */
    void Beep (int num_times)
    {
        if (!Globals.ConnectionManager.Connected)
            return;

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
        if (!Globals.ConnectionManager.Connected)
            return;

        _instruction_buffer.Advance();
        RepeatInstruction();
    }


    /**
     * Retransmits the last instruction to the AMEDA device.
     */
    void RepeatInstruction()
    {
        Globals.DebugToast.Send("Repeat instruction called");

        if (!Globals.ConnectionManager.Connected)
            return;

        Globals.DebugToast.Send("Executing " + _instruction_buffer.Current().Build());

        send_connection(Messages.Create(ActivityMessage.SEND, _instruction_buffer.Current()));
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
    protected void CannotMoveDialog()
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
    protected void FailAndDieDialog(String message)
    {
        Globals.Error (this, message);
    }


    /**
     * Not-so-serious error failure routine. Displays a dialog box, but doesn't terminate.
     *
     * @param message The message to display to the user.
     */
    protected void FailButDontDie(String message)
    {
        Globals.Alert (this, message);
    }


    /**
     * Safe tramsmission of a message to the connection manager.
     *
     * @param m The message to send.
     */
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


    /**
     * Recursively set the enabled parameter on the given View object and any children that
     * the view might have.
     *
     * @param view The object to en/disable.
     * @param enabled True to enable, false to disable.
     */
    private static void setViewAndChildrenEnabled(View view, boolean enabled)
    {
        view.setEnabled(enabled);
        if (view instanceof ViewGroup)
        {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++)
            {
                View child = viewGroup.getChildAt(i);
                setViewAndChildrenEnabled(child, enabled);
            }
        }
    }
}
