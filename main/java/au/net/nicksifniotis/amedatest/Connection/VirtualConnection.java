package au.net.nicksifniotis.amedatest.Connection;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import au.net.nicksifniotis.amedatest.AMEDA.AMEDAInstruction;
import au.net.nicksifniotis.amedatest.AMEDA.AMEDAResponse;
import au.net.nicksifniotis.amedatest.Connection.VirtualAMEDA.VAMEDAMsgBak;
import au.net.nicksifniotis.amedatest.Connection.VirtualAMEDA.VirtualDevice;
import au.net.nicksifniotis.amedatest.Globals;
import au.net.nicksifniotis.amedatest.Messages.VirtualAMEDAMessage;


/**
 * Implementation of a virtual AMEDA device, for testing purposes.
 *
 * This fully implements the AMEDA interface to enable testing and integration
 * with the remainder of the system.
 */
public class VirtualConnection extends Connection
{
    // Members that deal with the connection to the device.
    private boolean _connected;
    private Context _context;
    private VirtualDevice _device;
    private Messenger _device_data_received;
    private Messenger _device_data_sent;

    // Members that deal with the connection to the UI / 'main' thread are inherited from
    // the base class.


    /**
     * Create a connection to the virtual AMEDA device..
     *
     * @param c The context to send UI requests to.
     */
    public VirtualConnection(Context c)
    {
        _context = c;
    }


    @Override
    public void Shutdown()
    {
        if (Looper.myLooper() != null)
            Looper.myLooper().quitSafely();
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
        shutdown_msg.what = VAMEDAMsgBak.SHUTDOWN.ordinal();

        send_device(shutdown_msg);

        _device_data_sent = null;
        _device_data_received = null;
        _device = null;

        shutdown_msg = new Message();
        shutdown_msg.what = ConnectionMessage.DISCONNECTED.ordinal();
        send_manager(shutdown_msg);
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
        _device = new VirtualDevice(_context, _device_data_received);

        _connected = false;
        new Thread(_device).start();

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
        msg.what = VAMEDAMsgBak.INSTRUCTION.ordinal();
        msg.obj = instruction.Build();

        send_device(msg);
    }


    /**
     * Callback class for processing receiving messages from the virtual AMEDA.
     */
    class AMEDA_Handler implements Handler.Callback
    {
        /**
         * Handle messages received by the AMEDA.
         * Most of the time, messages received will be passed straight on to the connection
         * manager.
         *
         * @param msg The message received from the virtual AMEDA.
         * @return True!
         */
        @Override
        public boolean handleMessage(Message msg)
        {
            Globals.DebugToast.Send("Virtual connection receiving message "
                    + VirtualAMEDAMessage.toString(msg) + " from device");

            switch (VirtualAMEDAMessage.Message(msg))
            {
                case INSTRUCTION:
                    // get the message from the payload, and pass it upstream to the manager.
                    AMEDAResponse response = new AMEDAResponse(VirtualAMEDAMessage.Payload(msg));

                    msg = new Message();
                    msg.what = ConnectionMessage.RCVD.ordinal();
                    msg.obj = response;

                    send_manager(msg);
                    break;

                case MESSENGER_READY:
                    // get the device's messenger, and then
                    // let the manager know that we are now connected.
                    _device_data_sent = _device.GetMessenger();
                    _connected = true;

                    Message m = new Message();
                    m.what = ConnectionMessage.CONNECTED.ordinal();
                    send_manager(m);
                    break;

                default:
                    break;
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
        Looper.prepare();

        _connection_in = new Messenger(new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                handle_manager_message(msg);
                return true;
            }
        }));

        Message m = new Message();
        m.what = ConnectionMessage.MESSENGER_READY.ordinal();
        send_manager(m);

        Looper.loop();

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
            case CONNECT:
                create_device();
                break;
            case XMIT:
                // This instruction needs to be transmitted to the virtual AMEDA asap.
                AMEDAInstruction instruction = (AMEDAInstruction) msg.obj;
                send_instruction(instruction);
                break;
            case RCVD:
                break;
            case DISCONNECT:
                destroy_device();
                break;

            case SHUTDOWN:
                Shutdown();
                break;
            default:
                return false;
        }
        return true;
    }


    private void send_manager(Message msg)
    {
        Globals.DebugToast.Send("Virtual connection sending " + msg.what + " to manager");
        try
        {
            _connection_out.send (msg);
        }
        catch (RemoteException e)
        {
            // gkjh
        }
    }


    private void send_device(Message msg) {
        if (!_connected)
            Globals.DebugToast.Send("Virtual connection attempting to send msg to unconnected device");
        else
        {
            Globals.DebugToast.Send("Virtual connection sending " + msg.what + " to device.");
            try
            {
                _device_data_sent.send(msg);
            }
            catch (RemoteException e)
            {
                // gkjh
            }
        }
    }
}
