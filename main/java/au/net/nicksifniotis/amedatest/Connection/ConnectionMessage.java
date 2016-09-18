package au.net.nicksifniotis.amedatest.Connection;

/**
 * Created by Nick Sifniotis on 13/09/16.
 *
 * Asynchronous messages that can be passed between connection manager objects and
 * active connections.
 */
public enum ConnectionMessage
{
    RCVD, XMIT, SHUTDOWN, CONNECTED, DISCONNECTED, MESSENGER_READY, CONNECT, DISCONNECT;


    /**
     * Convert an integer into a message type. It's annoying that enums
     * can't be directly cast into ints.
     *
     * @param c The integer to convert.
     * @return The ConnectionMessage that the integer corresponds to.
     */
    public static ConnectionMessage index(int c)
    {
        return ConnectionMessage.values()[c];
    }
}
