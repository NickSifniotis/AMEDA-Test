package au.net.nicksifniotis.amedatest.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

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
            FailButDontDie("No connection detected. Please connect before attempting to begin this activity.");
    }


    /**
     * The callback function for handling messages received from the connection manager.
     *
     * @param msg The message received.
     */
    public void handleManagerMessage(Message msg)
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

            case CONNECTION_DROPPED:
                FailButDontDie("Connection dropped. Please reconnect to continue.");
                break;

            case CONNECTION_RESUMED:
                FailButDontDie("Connection restored. You may now resume this activity.");
                break;

            default:
                break;
        }
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


    /**
     * Not-so-serious error failure routine. Displays a dialog box, but doesn't terminate.
     *
     * @param message The message to display to the user.
     */
    protected void FailButDontDie(String message)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alert")
                .setMessage(message)
                .setCancelable(false)
                .setNegativeButton(getString(R.string.btn_done), null);

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
