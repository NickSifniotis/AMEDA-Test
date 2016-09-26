package au.net.nicksifniotis.amedatest.Connection.VirtualAMEDA;

/**
 * Created by Nick Sifniotis on 13/09/16.
 *
 * Messages passed between the virtual device and the virtual connection.
 */
public enum VAMEDAMsgBak
{
    INSTRUCTION, SHUTDOWN, MESSENGER_READY;


    /**
     * Convert an int to a message type.
     *
     * @param c The integer to convert.
     * @return The corresponding message.
     */
    public static VAMEDAMsgBak index (int c)
    {
        return VAMEDAMsgBak.values()[c];
    }
}
