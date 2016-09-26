package au.net.nicksifniotis.amedatest.Connection;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import au.net.nicksifniotis.amedatest.AMEDA.AMEDAInstruction;
import au.net.nicksifniotis.amedatest.AMEDA.AMEDAResponse;
import au.net.nicksifniotis.amedatest.Globals;
import au.net.nicksifniotis.amedatest.Messages.ConnectionMessage;
import au.net.nicksifniotis.amedatest.Messages.ManagerMessage;
import au.net.nicksifniotis.amedatest.Messages.Messages;
import au.net.nicksifniotis.amedatest.R;


/**
 * New AMEDA implementation class using code reverse engineered from other bluetooth projects.
 *
 */
public class AMEDAConnection extends Connection
{
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private String _data_received_buffer;
    private String device_name;
    private boolean _connected;

    private BluetoothAdapter _bt_adaptor = null;
    private BluetoothSocket _bt_sockets = null;

    final private Handler _read_handler;
    final private Activity _parent;

    private ConnectedThread _read_thread;


    /**
     * Constructor for the AMEDA connection object.
     *
     * @param context The activity to send UI requests to.
     */
    public AMEDAConnection(Activity context, String d_n)
    {
        Globals.DebugToast.Send ("AMEDA Connection constructor called!");

        device_name = d_n;
        _connected = false;
        _parent = context;
        _data_received_buffer = "";
        _bt_adaptor = BluetoothAdapter.getDefaultAdapter();

        _read_handler = new Handler(new Handler.Callback()
        {
            /**
             * Message handling callback. Adds the message to the input buffer string.
             *
             * @param msg The message to process.
             */
            @Override
            public boolean handleMessage (Message msg)
            {
                String readMessage = (String) msg.obj;

                if (msg.what == 1 && msg.arg1 > 0)
                    addMessage(readMessage);

                return true;
            }
        });
    }


    @Override
    public void Shutdown()
    {
        Globals.DebugToast.Send("Shutting down AMEDA connection");

        if (Looper.myLooper() != null)
            Looper.myLooper().quitSafely();
    }


    /**
     * Add data received by the AMEDA to the input 'buffer' string.
     *
     * @param msg The data received from the AMEDA device.
     */
    private void addMessage(String msg)
    {
        Globals.DebugToast.Send("Reading " + msg);
        _data_received_buffer += msg;

        if (_data_received_buffer.length() >= 8)
        {
            String result = _data_received_buffer.substring(0, 8);
            _data_received_buffer = _data_received_buffer.substring (8);

            AMEDAResponse response = new AMEDAResponse(result);

            if (response.GetCode() == null)
            {
                Globals.DebugToast.Send("Unable to parse message received: " + result);
            }
            else
            {
                Globals.DebugToast.Send("Sending message: " + response.toString());
                send_manager (Messages.Create (ConnectionMessage.RCVD, response));
            }
        }
    }


    /**
     * Create the connection to the device.
     *
     * @param device The device to connect to.
     * @return A _connected socket to that device.
     * @throws IOException If it fails to open a socket to the thing.
     */
    private BluetoothSocket createBluetoothSocket (BluetoothDevice device) throws IOException
    {
        Toast.makeText(_parent, "createBluetoothSocket", Toast.LENGTH_SHORT).show();

        BluetoothSocket res;

        if (Build.VERSION.SDK_INT >= 10)
        {
            try
            {
                Method m = device.getClass().getMethod("createRfcommSocket", int.class);
                res = (BluetoothSocket) m.invoke(device, 1);

                return res;
            }
            catch (Exception e)
            {
                Globals.DebugToast.Send("Error: " + e.getMessage());
            }
        }

        res = device.createRfcommSocketToServiceRecord(MY_UUID);

        return res;
    }


    class ConnectedThread extends Thread
    {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;


        public ConnectedThread (BluetoothSocket socket)
        {
            super();

            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try
            {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            }
            catch (Exception e)
            {
                Globals.DebugToast.Send("Error connecting to i/o streams. " + e.getMessage());
                Globals.Error(_parent, _parent.getString(R.string.error_ameda_cannot_connect));
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }


        /**
         * Input listener. Keep going until interrupted by the main controller to indicate
         * a disconnect.
         */
        @Override
        public void run()
        {
            Globals.DebugToast.Send("AMEDA slave thread entering run phase");
            byte[] buffer = new byte[0x100];

            while (!interrupted() && _connected)
            {
                try
                {
                    int bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    _read_handler.obtainMessage(1, bytes, -1, readMessage).sendToTarget();
                }
                catch (Exception e)
                {
                    Globals.DebugToast.Send("Error reading from AMEDA device. " + e.getMessage());
                }
            }

            Globals.DebugToast.Send("AMEDA slave thread leaving run phase");
        }


        /**
         * Writes some bytes to the AMEDA device.
         *
         * @param message - The data to send to the device.
         */
        public void write(String message)
        {
            if (!_connected)
                return;

            Globals.DebugToast.Send("Write called: " + message);

            byte[] msgBuffer = message.getBytes();
            try
            {
                mmOutStream.write(msgBuffer);
            }
            catch (Exception e)
            {
                Globals.DebugToast.Send("Error writing to device. " + e.getMessage());
            }
        }
    }


    /**
     * Connects to the AMEDA device.
     *
     */
    private void _handle_connect()
    {
        BluetoothDevice device = null;

        List<BluetoothDevice> pairedDevices = new ArrayList<>(_bt_adaptor.getBondedDevices());
        for (BluetoothDevice d: pairedDevices)
            if (d.getName().trim().equals(device_name))
                device = d;

        if (device == null)
        {
            Globals.DebugToast.Send("Unable to find AMEDA. Has it been paired to this device?");
            return;
        }

        try
        {
            _bt_sockets = createBluetoothSocket(device);
        }
        catch (Exception e)
        {
            Globals.DebugToast.Send("Socket create failed: " + e.getMessage());
            connect_failed();
            return;
        }

        if (_bt_sockets == null)
        {
            Globals.DebugToast.Send("Socket created was null.");
            connect_failed();
            return;
        }

        _bt_adaptor.cancelDiscovery();

        try
        {
            _bt_sockets.connect();
        }
        catch (Exception e)
        {
            Globals.DebugToast.Send("Socket connection failure: " + e.getMessage());
            connect_failed();
            return;
        }

        _read_thread = new ConnectedThread(_bt_sockets);
        _read_thread.start();

        _connected = true;


        // Signal the success of the connection by sending a message back to the parent UI
        send_manager(Messages.Create(ConnectionMessage.CONNECTED));
    }


    /**
     * Sends the instruction to the device.
     *
     * @param i The instruction to execute.
     */
    private void _handle_transmission(AMEDAInstruction i)
    {
        _read_thread.write(i.Build());
    }


    /**
     * Close the connection to the AMEDA device.
     */
    private void _handle_disconnect()
    {
        // because this method will be called whether or not we are connected.
        if (!_connected)
            return;

        _read_thread.interrupt();
        try
        {
            _read_thread.mmOutStream.close();
            _read_thread.mmInStream.close();
            _bt_sockets.close();
        }
        catch (Exception e)
        {
            Globals.DebugToast.Send("Error closing AMEDA connection. " + e.getMessage());
        }

        // Signal the success of the disconnection by sending a message back to the manager
        send_manager(Messages.Create(ConnectionMessage.DISCONNECTED));
    }


    @Override
    public void run()
    {
        Globals.DebugToast.Send("AMEDA Connection entering run phase");

        Looper.prepare();

        _connection_in = new Messenger(new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                handle_manager_message(msg);
                return true;
            }
        }));

        send_manager(Messages.Create(ConnectionMessage.MESSENGER_READY));
        Looper.loop();

        _handle_disconnect();

        Globals.DebugToast.Send("AMEDA Connection leaving run phase");
    }


    /**
     * Callback function for handling messages that are received from the connection manager.
     *
     * @param msg The message received.
     * @return True, always.
     */
    @Override
    public boolean handle_manager_message(Message msg)
    {
        switch (ManagerMessage.Message(msg))
        {
            case XMIT:
                _handle_transmission(Messages.GetInstruction(msg));
                break;

            case CONNECT:
                _handle_connect();
                break;

            case DISCONNECT:
                _handle_disconnect();
                break;

            case SHUTDOWN:
                _handle_disconnect();
                Shutdown();
                break;

            default:
                break;
        }

        return true;
    }


    /**
     * Dispatches a message to the connection manager.
     *
     * @param m The message to send.
     */
    private void send_manager(Message m)
    {
        if (_connection_out != null)
        {
            try
            {
                _connection_out.send (m);
            }
            catch (RemoteException e)
            {
                // jkdfkjdf
            }
        }
    }


    /**
     * Attempt to connect to the bluetooth device has failed. Notify the connection manager.
     */
    private void connect_failed()
    {
        send_manager (Messages.Create (ConnectionMessage.CONNECT_FAILED));
    }
}
