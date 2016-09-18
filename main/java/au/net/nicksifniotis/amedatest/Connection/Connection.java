package au.net.nicksifniotis.amedatest.Connection;

import android.os.Handler;
import android.os.Message;
import android.os.Messenger;

import au.net.nicksifniotis.amedatest.Globals;

/**
 * Created by Nick Sifniotis on 13/09/16.
 *
 * Abstract implementation of a connection.
 */
public abstract class Connection implements Runnable
{
    protected Messenger _connection_in;
    protected Messenger _connection_out;
    protected volatile boolean _alive;


    public Connection (Messenger connection_out)
    {
        _connection_out = connection_out;
    }


    public Messenger get_connection ()
    {
        Globals.DebugToast.Send("Connection surrendering connection_in");
        return _connection_in;
    }


    public abstract void run();

    public abstract boolean handle_manager_message(Message msg);
}
