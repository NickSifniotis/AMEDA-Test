package au.net.nicksifniotis.amedatest.Messages;

import android.os.Message;

import au.net.nicksifniotis.amedatest.AMEDA.AMEDAInstruction;
import au.net.nicksifniotis.amedatest.AMEDA.AMEDAResponse;

/**
 * Created by Nick Sifniotis on 26/09/16.
 *
 * Base class for all asynchronous messages passed between the processes in this app.
 */
public class BaseMessage
{
    /**
     * Creates a new message object and returns it.
     *
     * @param what Value to set the what parameter to.
     * @return The new message object.
     */
    public static Message Create(int what)
    {
        Message res = new Message();
        res.what = what;
        return res;
    }


    /**
     * Creates a new message object and attaches an AMEDAInstruction to it.
     *
     * @param what Value to set the what parameter to.
     * @param instruction The instruction to attach to this message's payload.
     * @return The new message instance.
     */
    public static Message Create(int what, AMEDAInstruction instruction)
    {
        Message res = Create(what);
        res.obj = instruction;
        return res;
    }


    /**
     * Creates a new message object and attaches an AMEDAResponse to it.
     *
     * @param what Value to set what parameter to.
     * @param response The response to attach to this message's payload.
     * @return The new message instance.
     */
    public static Message Create(int what, AMEDAResponse response)
    {
        Message res = Create(what);
        res.obj = response;
        return res;
    }


    /**
     * Creates a new message object and attaches an AMEDAResponse to it.
     *
     * @param what Value to set what parameter to.
     * @param response The response to attach to this message's payload.
     * @return The new message instance.
     */
    public static Message Create(int what, String response)
    {
        Message res = Create(what);
        res.obj = response;
        return res;
    }
}
