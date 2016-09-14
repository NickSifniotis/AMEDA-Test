package au.net.nicksifniotis.amedatest.ConnectionManager;

import android.os.Message;
import android.os.Messenger;

import au.net.nicksifniotis.amedatest.Connection.Connection;
import au.net.nicksifniotis.amedatest.Connection.ConnectionMessage;


/**
 * Created by Nick Sifniotis on 14/09/16.
 *
 * The mid-tier layer that interfaces with implementations of Connection on the one hand,
 * and the application-level API on the other.
 */
public class ConnectionManager implements Runnable
{
    // connection data
    private boolean _connected;
    private Connection _connection;
    private Messenger _data_to_connection;

    // message passing mapping

    // me, myself and I
    private volatile boolean _alive;


    @Override
    public void run() {
        _alive = true;

        while (_alive)
        {
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                _alive = false;
            }
        }
    }


    private void connect (Connection c)
    {
        _connection = c;
        _data_to_connection = c.get_connection();

        new Thread(c).start();
    }


    private void handleConnectionMessage (Message msg)
    {
        ConnectionMessage message_type = ConnectionMessage.index(msg.what);
        switch (message_type)
        {
            case CONNECTED:
                _connected = true;
                break;
            case DISCONNECTED:
                _connected = false;
                // todo send a message to the device instructing it to terminate itself.
                break;
            case RCVD:
                // Ah. Data received from the device. What should I do with it?
                break;
            case XMIT:
            case SHUTDOWN:
                // heh.
                // I generate these messages, I don't receive them ...
                break;
        }
    }
}
