package au.net.nicksifniotis.amedatest.ConnectionManager;

import au.net.nicksifniotis.amedatest.Connection.Connection;


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
}
