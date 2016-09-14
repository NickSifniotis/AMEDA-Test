package au.net.nicksifniotis.amedatest.Connection;

import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import au.net.nicksifniotis.amedatest.AMEDAManager.AMEDA;
import au.net.nicksifniotis.amedatest.AMEDAManager.AMEDAInstruction;
import au.net.nicksifniotis.amedatest.AMEDAManager.AMEDAResponse;
import au.net.nicksifniotis.amedatest.Connection.VirtualAMEDA.VirtualAMEDAMessage;
import au.net.nicksifniotis.amedatest.Connection.VirtualAMEDA.VirtualDevice;


/**
 * Implementation of a virtual AMEDA device, for testing purposes.
 *
 * This fully implements the AMEDA interface to enable testing and integration
 * with the remainder of the system.
 */
public class VirtualConnection extends Connection
{
    // Members that deal with the connection to the device.
    private VirtualDevice _device;
    private Messenger _device_data_received;
    private Messenger _device_data_sent;

    // Members that deal with the connection to the UI / 'main' thread are inherited from
    // the base class.


    /**
     * Create a connection to the virtual AMEDA device..
     *
     * @param outbound_messages The messenger that allows this connection to send messages
     *                          to the connection manager.
     */
    public VirtualConnection(Messenger outbound_messages)
    {
        super(outbound_messages);
    }


    /**
     * Implementation of the terminate connection method.
     *
     * Shut down the thread, and null out the communications channels.
     */
    private void destroy_device()
    {
        if (_device == null)
            return;

        Message shutdown_msg = new Message();
        shutdown_msg.what = VirtualAMEDAMessage.SHUTDOWN.ordinal();

        try
        {
            _device_data_sent.send(shutdown_msg);
        }
        catch (RemoteException e)
        {
            // Empty catch block, because java.
        }

        _device_data_sent = null;
        _device_data_received = null;
        _device = null;
    }


    /**
     * connect to the virtual device by create an instance of the task, and swapping messagers
     * and handlers.
     *
     * Once this is done, signal a successful connection to the UI thread.
     */
    private boolean create_device()
    {
        _device_data_received = new Messenger(new Handler(new AMEDA_Handler()));
        _device = new VirtualDevice(_device_data_received);
        _device_data_sent = _device.GetMessenger();

        new Thread(_device).start();

        Message msg = new Message();
        msg.what = AMEDA.CONNECTED;

        try
        {
            _connection_out.send(msg);
        }
        catch (RemoteException e)
        {
            // do nothing.
        }
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
    private void send_instruction(AMEDAInstruction instruction)
    {
        if (_device == null)
            return;

        Message msg = Message.obtain();
        msg.what = VirtualAMEDAMessage.INSTRUCTION.ordinal();
        msg.obj = instruction.Build();

        try
        {
            _device_data_sent.send(msg);
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
            if (msg.what != VirtualAMEDAMessage.INSTRUCTION.ordinal())
            {
                // what is going on? There's literally no code in the virtual device
                // that is capable of generating messages other than INSTRUCTION

                return false;
            }

            AMEDAResponse response = new AMEDAResponse((String) msg.obj);

            msg = new Message();
            msg.what = ConnectionMessage.RCVD.ordinal();
            msg.obj = response;

            try
            {
                _connection_out.send(msg);
            }
            catch (RemoteException e)
            {
                // do nothing
            }

            return true;
        }
    }


    /**
     * The main activity of this thread - create a virtual device, connect to it, and keep looping
     * until the message to shut down is received.
     */
    @Override
    public void run()
    {
        create_device();
        _alive = true;

        while (_alive)
        {
            try
            {
                Thread.sleep(500);
            }
            catch (InterruptedException e)
            {
                _alive = false;
            }
        }

        destroy_device();
    }


    /**
     * Handles messages received from the connection manager.
     *
     * @param msg The message received.
     * @return True on success, false otherwise.
     */
    @Override
    public boolean handle_manager_message(Message msg)
    {
        switch (ConnectionMessage.index(msg.what))
        {
            case XMIT:
                // This instruction needs to be transmitted to the virtua AMEDA asap.
                AMEDAInstruction instruction = (AMEDAInstruction) msg.obj;
                send_instruction(instruction);
                break;
            case RCVD:
                break;
            case SHUTDOWN:
                _alive = false;
                break;
            default:
                return false;
        }
        return true;
    }
}
