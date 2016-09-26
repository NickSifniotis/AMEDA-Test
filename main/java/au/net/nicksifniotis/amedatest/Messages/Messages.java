package au.net.nicksifniotis.amedatest.Messages;

import android.os.Message;

import au.net.nicksifniotis.amedatest.AMEDA.AMEDAInstruction;
import au.net.nicksifniotis.amedatest.AMEDA.AMEDAResponse;


/**
 * Created by Nick Sifniotis on 26/09/16.
 *
 * Base class for all asynchronous messages passed between the processes in this app.
 */
public class Messages
{
    /**
     * Creates a new message object and returns it.
     *
     * @param what Value to set the what parameter to.
     * @return The new message object.
     */
    public static Message Create(Enum what)
    {
        Message res = new Message();
        res.what = what.ordinal();
        return res;
    }


    /**
     * Creates a new message object and attaches an AMEDAInstruction to it.
     *
     * @param what Value to set the what parameter to.
     * @param instruction The instruction to attach to this message's payload.
     * @return The new message instance.
     */
    public static Message Create(Enum what, AMEDAInstruction instruction)
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
    public static Message Create(Enum what, AMEDAResponse response)
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
    public static Message Create(Enum what, String response)
    {
        Message res = Create(what);
        res.obj = response;
        return res;
    }


    /**
     * Returns the payload attached to this message, as a string.
     *
     * @param msg The message to interpret.
     * @return A stringy payload.
     */
    public static String GetString(Message msg)
    {
        return (String) msg.obj;
    }


    /**
     * Returns the payload attached to this message, as an AMEDA Response object.
     *
     * @param msg The message to interpret.
     * @return The responsy payload.
     */
    public static AMEDAResponse GetResponse(Message msg)
    {
        return (AMEDAResponse) msg.obj;
    }


    /**
     * Returns the payload attached to this message, as an AMEDA Instruction object.
     *
     * @param msg The message to interpret.
     * @return The instruction.
     */
    public static AMEDAInstruction GetInstruction(Message msg)
    {
        return (AMEDAInstruction) msg.obj;
    }
}
