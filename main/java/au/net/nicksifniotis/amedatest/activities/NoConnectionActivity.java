package au.net.nicksifniotis.amedatest.activities;

import android.os.Message;

import au.net.nicksifniotis.amedatest.AMEDA.AMEDAInstruction;
import au.net.nicksifniotis.amedatest.AMEDA.AMEDAResponse;


/**
 * Created by Nick Sifniotis on 19/09/16.
 *
 * An extension of thr AMEDAActivity abstract class, for activities which do not
 * use the connection to the device at all.
 */
public abstract class NoConnectionActivity extends AMEDAActivity
{
    /**
     * This is an ugly hack but it'll do.
     */
    @Override
    public void onStart()
    {
        uses_connection = false;
        super.onStart();
    }


    /**
     * Null response. Ignore this.
     *
     * @param instruction The instruction that was sent to the AMEDA.
     * @param response The AMEDA's response to that instruction.
     */
    @Override
    protected void ProcessAMEDAResponse(AMEDAInstruction instruction, AMEDAResponse response)
    {
        // Do nothing!
    }


    /**
     * Null response for any messages received by the connection manager.
     *
     * @param msg The message received.
     */
    @Override
    public void HandleManagerMessage(Message msg)
    {
        // Again, do nothing.
    }
}
