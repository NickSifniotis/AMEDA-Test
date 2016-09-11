package au.net.nicksifniotis.amedatest.AMEDAManager.VirtualAMEDA;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.widget.Toast;

import au.net.nicksifniotis.amedatest.AMEDAManager.AMEDA;
import au.net.nicksifniotis.amedatest.AMEDAManager.AMEDAInstruction;
import au.net.nicksifniotis.amedatest.AMEDAManager.AMEDAResponse;


/**
 * Implementation of a virtual AMEDA device, for testing purposes.
 *
 * This fully implements the AMEDA interface to enable testing and integration
 * with the remainder of the system.
 */
public class Connection implements AMEDA
{
    // Members that deal with the connection to the device.
    private VirtualDevice _device;
    private Messenger _data_received;
    private Messenger _data_sent;

    // Members that deal with the connection to the UI / 'main' thread.
    final private Activity _context;
    final private Handler _response_handler;


    /**
     * Construct this virtual AMEDA device.
     *
     * @param context The activity that owns this device.
     * @param handler The handler to send response messages through to.
     */
    public Connection(Activity context, Handler handler)
    {
        _context = context;
        _response_handler = handler;
    }


    /**
     * Implementation of the terminate connection method.
     *
     * Shut down the thread, and null out the communications channels.
     */
    @Override
    public void Disconnect()
    {
        if (_device == null)
            return;

        Message shutdown_msg = new Message();
        shutdown_msg.what = 2;

        try
        {
            _data_sent.send(shutdown_msg);
        }
        catch (RemoteException e)
        {
            // Empty catch block, because java.
        }

        _data_sent = null;
        _data_received = null;
        _device = null;
    }


    /**
     * Connect to the virtual device by create an instance of the task, and swapping messagers
     * and handlers.
     *
     * Once this is done, signal a successful connection to the UI thread.
     */
    @Override
    public boolean Connect()
    {
        _data_received = new Messenger(new Handler(new AMEDA_Handler()));
        _device = new VirtualDevice(_context, _data_received);
        _data_sent = _device.GetMessenger();

        new Thread(_device).start();

        Message msg = _response_handler.obtainMessage(AMEDA.CONNECTED);
        _response_handler.sendMessageDelayed(msg, 2000);

        return true;
    }


    /**
     * Sends the instruction to the make-believe AMEDA.
     *
     * Convert the instruction to a packet, and send it through the communications
     * channel that has been opened up.
     *
     * Interestingly, there's no room to return false if the connection doesn't exist.
     * todo something about that, probably.
     *
     * @param instruction The instruction to transmit.
     */
    @Override
    public void SendInstruction(AMEDAInstruction instruction)
    {
        if (_device == null)
            return;

        Message msg = Message.obtain();
        msg.what = 1;
        msg.obj = instruction.Build();

        try
        {
            _data_sent.send(msg);
        }
        catch (RemoteException e)
        {
            // haha, nothing
        }
    }


    /**
     * Callback class for processing receiving messages from the virtual AMEDA.
     */
    class AMEDA_Handler implements Handler.Callback
    {
        /**
         * Handle messages received by the AMEDA.
         * Essentially these are part-processed and piped through to the UI thread
         * via the other handler (that will be refactored into a Messenger object in time ..)
         *
         * @param msg The message received from the virtual AMEDA.
         * @return True! Or false, if the message is wrong.
         */
        @Override
        public boolean handleMessage(Message msg)
        {
            Toast.makeText(_context,
                    "Connection received response from virtual device: " + (String)msg.obj,
                    Toast.LENGTH_SHORT).show();

            AMEDAResponse response = new AMEDAResponse((String) msg.obj);
            msg = _response_handler.obtainMessage(AMEDA.RESPONSE, response);
            _response_handler.sendMessage(msg);

            return true;
        }
    }
}
