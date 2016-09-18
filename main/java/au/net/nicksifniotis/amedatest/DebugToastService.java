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


    public void Send (final String msg)
    {
        Message m = new Message();
        m.what = 1;
        m.obj = msg;

        try {
            _messenger.send(m);
        }
        catch (Exception e)
        {
            //lhfdjgh
        }
    }
}
