package au.net.nicksifniotis.amedatest.ConnectionManager;

import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import au.net.nicksifniotis.amedatest.AMEDA.AMEDAInstruction;
import au.net.nicksifniotis.amedatest.AMEDA.AMEDAInstructionEnum;
import au.net.nicksifniotis.amedatest.Globals;

/**
 * Created by Nick Sifniotis on 19/09/16.
 *
 * A monitoring process which runs for as long as a connection to a device is alive.
 * Every two seconds or so, ping the device and hope to receive a message in return.
 */
public class Heartbeat implements Runnable
{
    private Messenger connection_data_out;
    private volatile boolean alive;


    public Heartbeat (Messenger out)
    {
        connection_data_out = out;
        alive = false;
    }


    /**
     * DIE DIE DIE
     */
    public void die()
    {
        alive = false;
    }


    /**
     * Send a HELLO message every two seconds.
     */
    @Override
    public void run()
    {
        alive = true;

        while (alive)
        {
            try
            {
                Thread.sleep (5000);

                AMEDAInstruction instruction = AMEDAInstruction
                        .Create()
                        .Instruction(AMEDAInstructionEnum.HELLO);

                Message m = new Message();
                m.what = ManagerMessages.SEND.ordinal();
                m.obj = instruction;

                try
                {
                    connection_data_out.send (m);
                }
                catch (RemoteException e)
                {
                    Globals.DebugToast.Send("Error sending heartbeat message!");
                }
            }
            catch (InterruptedException e)
            {
                alive = false;
            }
        }
    }
}
