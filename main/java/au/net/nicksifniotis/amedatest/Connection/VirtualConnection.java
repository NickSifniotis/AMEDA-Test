package au.net.nicksifniotis.amedatest.Connection;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import au.net.nicksifniotis.amedatest.AMEDA.AMEDAResponse;
import au.net.nicksifniotis.amedatest.Connection.VirtualAMEDA.BuggyVirtualDevice;
import au.net.nicksifniotis.amedatest.Connection.VirtualAMEDA.VirtualDevice;
import au.net.nicksifniotis.amedatest.Globals;
import au.net.nicksifniotis.amedatest.Messages.ConnectionMessage;
import au.net.nicksifniotis.amedatest.Messages.ManagerMessage;
import au.net.nicksifniotis.amedatest.Messages.Messages;
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
    private boolean _buggy;
    private Context _context;
    private VirtualDevice _device;
    private Messenger _device_data_received;
    private Messenger _device_data_sent;
    private Looper _looper;


    /**
     * Create a connection to the virtual AMEDA device..
     *
     * @param c The context to send UI requests to.
     */
    public VirtualConnection(Context c, boolean buggy)
    {
        _context = c;
        _buggy = buggy;
    }


    /**
     * Don't just _shutdown - remember to disconnect too.
     */
    public void shutdown()
    {
        if (_device != null)
            send_device(Messages.Create(ConnectionMessage.SHUTDOWN));

        if (_looper != null)
            _looper.quitSafely();
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

        send_device(Messages.Create(ConnectionMessage.SHUTDOWN));

        _device_data_sent = null;
        _device_data_received = null;
        _device = null;

        send_manager(Messages.Create(ConnectionMessage.DISCONNECTED));
    }


    /**
     * Connect to the virtual device by creating an instance of the task, and swapping messengers
     * and handlers.
     */
    private boolean create_device()
    {
        _device_data_received = new Messenger(new Handler(new Handler.Callback()
        {
            /**
             * Callback function to the message handling routine.
             *
             * @param msg The message to handle.
             * @return True, always.
             */
            @Override
            public boolean handleMessage(Message msg)
            {
                handle_ameda_message(msg);
                return true;
            }
        }));

        if (_buggy)
            _device = new BuggyVirtualDevice(_context, _device_data_received);
        else
            _device = new VirtualDevice(_context, _device_data_received);

        _connected = false;
        new Thread(_device).start();

        return true;
    }


    /**
     * Handle messages received by the AMEDA.
     * Most of the time, messages received will be passed straight on to the connection
     * manager.
     *
     * @param msg The message received from the virtual AMEDA.
     */
    public void handle_ameda_message(Message msg)
    {
        Globals.DebugToast.Send("Virtual connection receiving message "
                + VirtualAMEDAMessage.toString(msg) + " from device");

        switch (VirtualAMEDAMessage.Message(msg))
        {
            case RCV_PACKET:
                // get the message from the payload, and pass it upstream to the manager.
                AMEDAResponse response = new AMEDAResponse(Messages.GetString(msg));
                send_manager(Messages.Create(ConnectionMessage.RCVD, response));
                break;

            case MESSENGER_READY:
                // get the device's messenger, and then
                // let the manager know that we are now connected.
                _device_data_sent = _device.GetMessenger();
                _connected = true;

                send_manager(Messages.Create(ConnectionMessage.CONNECTED));
                break;

            default:
                break;
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
        _looper = Looper.myLooper();

        _connection_in = new Messenger(new Handler(new Handler.Callback()
        {
            /**
             * Simple callback method for message handling.
             *
             * @param msg The message to handle.
             * @return True, always.
             */
            @Override
            public boolean handleMessage(Message msg)
            {
                handle_manager_message(msg);
                return true;
            }
        }));

        send_manager(Messages.Create(ConnectionMessage.MESSENGER_READY));
        Looper.loop();

        destroy_device();
    }


    /**
     * Handles messages received from the connection manager.
     *
     * @param msg The message received.
     */
    @Override
    public void handle_manager_message(Message msg)
    {
        Globals.DebugToast.Send("Virtual connection receiving message "
                + ManagerMessage.toString(msg) + " from manager");

        switch (ManagerMessage.Message(msg))
        {
            case CONNECT:
                create_device();
                break;

            case XMIT:
                // This instruction needs to be transmitted to the virtual AMEDA asap.
                send_device(Messages.Create(ConnectionMessage.XMIT, Messages.GetInstruction(msg).Build()));
                break;

            case DISCONNECT:
                destroy_device();
                break;

            case SHUTDOWN:
                shutdown();
                break;

            default:
                break;
        }
    }


    /**
     * Forward a message on to the connection manager.
     *
     * @param msg The message to send.
     */
    private void send_manager(Message msg)
    {
        Globals.DebugToast.Send("Virtual connection sending "
                + ConnectionMessage.toString(msg) + " to manager");
        try
        {
            _connection_out.send (msg);
        }
        catch (RemoteException e)
        {
            // gkjh
        }
    }


    /**
     * Forward a message on to the virtual device.
     *
     * @param msg The message to send.
     */
    private void send_device(Message msg)
    {
        if (!_connected)
            Globals.DebugToast.Send("Virtual connection attempting to send msg to unconnected device");
        else
        {
            Globals.DebugToast.Send("Virtual connection sending "
                    + ConnectionMessage.toString(msg) + " to device.");
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
