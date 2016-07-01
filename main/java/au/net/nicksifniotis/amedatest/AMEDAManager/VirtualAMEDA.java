package au.net.nicksifniotis.amedatest.AMEDAManager;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;

import au.net.nicksifniotis.amedatest.R;

/**
 * Implementation of a virtual AMEDA device, for testing purposes.
 *
 * This fully implements the AMEDA interface to enable testing and integration
 * with the remainder of the system.
 */
public class VirtualAMEDA implements AMEDA
{
    private Context _context;
    private Handler _response_handler;


    /**
     * Construct this virtual AMEDA device.
     *
     * @param context The activity that owns this device.
     * @param handler The handler to send response messages through to.
     */
    public VirtualAMEDA(Context context, Handler handler)
    {
        _context = context;
        _response_handler = handler;
    }


    /**
     * Moves the AMEDA plate to the given position.
     *
     * As the virtual AMEDA doesn't have a plate to move, every call to this function returns
     * a true.
     *
     * @param position The position to move the device to. Valid range is from 1 to 5.
     * @return True if the device moved successfully, false otherwise.
     */
    @Override
    public boolean GoToPosition(int position)
    {

        return true;
    }


    /**
     * Calibration should occur before the AMEDA device is used.
     * The virtual AMEDA doesn't need to calibrate, so always indicate success
     * by returning a true.
     * @return True if the calibration was successful, false if the AMEDA is returning an
     * error that it cannot recover from.
     */
    @Override
    public boolean Calibrate()
    {
        return true;
    }


    /**
     * Implementation of the terminate connection method.
     * The Virtual AMEDA has no connection to terminate.
     * So do nothing.
     */
    @Override
    public void Disconnect()
    {
    }


    /**
     * Implementation of the open connection method.
     * The virtual AMEDA has nothing to connect to, so do nothing.
     */
    @Override
    public void Connect()
    {
    }


    /**
     * Implementation of 'beep'.
     *
     * It should actually play a 'beep' sound I reckon.
     *
     * @param num_beeps The number of beeps to emit.
     * @return True if successful, false otherwise.
     */
    @Override
    public boolean Beep(int num_beeps)
    {
        return true;
    }


    /**
     * Sends the instruction to the make-believe AMEDA.
     *
     * So - beep, if we were asked to beep.
     * Send a delay of about 100ms and a READY message back if the
     * instruction was anything else.
     *
     * @param instruction The instruction to transmit.
     */
    @Override
    public void SendInstruction(AMEDAInstruction instruction)
    {
        if (instruction.GetInstruction() == AMEDAInstructionEnum.BUZZER_LONG
                || instruction.GetInstruction() == AMEDAInstructionEnum.BUZZER_SHORT)
            _beep();
        else
        {
            Message msg = _response_handler.obtainMessage(AMEDA.RESPONSE, AMEDAResponse.READY);
            _response_handler.sendMessageDelayed(msg, 100);
        }
    }


    /**
     * Beep.
     */
    private void _beep()
    {
        MediaPlayer mp = MediaPlayer.create(_context, R.raw.virtual_ameda);
        mp.start();
    }
}
