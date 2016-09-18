package au.net.nicksifniotis.amedatest;

import android.os.Message;
import android.os.Messenger;


/**
 * Created by Nick Sifniotis on 18/09/16.
 *
 * Proof of concept for service thread sharing across activities.
 */
public class DebugToastService implements Runnable
{
    private Messenger _messenger;
    private volatile boolean _alive;

    public DebugToastService(Messenger m)
    {
        _messenger = m;
        _alive = false;
    }


    @Override
    public void run()
    {
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


    public void Shutdown()
    {
        _alive = false;
    }


    /**
     * Sends a toasty message to the screen. The call to Toast.makeText is run through the UI thread
     * since there is no guarantee that the caller of this method is working in that thread.
     *
     * [Toast needs to run through the UI thread because just because.]
     *
     * Messages are only displayed if debug mode is on - if not, they are suppressed.
     *
     * This method is designed to be used for debugging only - do not send messages through this
     * method that you want the user to see.
     *
     * @param msg The message to display.
     */
    public void Send (final String msg)
    {
        if (!Globals.DEBUG_MODE)
            return;

        Message m = new Message();
        m.what = 1;
        m.obj = msg;

        try
        {
            _messenger.send(m);
        }
        catch (Exception e)
        {
            //lhfdjgh
        }
    }
}
