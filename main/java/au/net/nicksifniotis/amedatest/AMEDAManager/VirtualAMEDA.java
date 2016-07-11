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
     *
     * There's nothing to connect to, but we still need to signal a successful connection to the
     * UI thread.
     */
    @Override
    public boolean Connect()
    {
        Message msg = _response_handler.obtainMessage(AMEDA.CONNECTED);
        _response_handler.sendMessageDelayed(msg, 2000);

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
