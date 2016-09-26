package au.net.nicksifniotis.amedatest.Messages;

import android.os.Message;


/**
 * Created by Nick Sifniotis on 26/09/16.
 *
 * Message class for messages generated by the virtual AMEDA device.
 */
public enum VirtualAMEDAMessage
{
    INSTRUCTION,            /* Sending an instruction packet to the connection */
    MESSENGER_READY;        /* Signals to the connection that my messenger is ready to receive */


    /**
     * Returns the message code that corresponds to the message that has been received.
     *
     * @param msg The message recevied from the messenger.
     * @return The message code.
     */
    public static VirtualAMEDAMessage Message (Message msg)
    {
        return VirtualAMEDAMessage.values()[msg.what];
    }


    /**
     * Get the stringy representation of this message.
     *
     * @param msg The message to stringify.
     * @return The string.
     */
    public static String toString(Message msg)
    {
        return Message(msg).toString();
    }
}
