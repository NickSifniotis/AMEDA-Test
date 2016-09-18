package au.net.nicksifniotis.amedatest.Connection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
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

import au.net.nicksifniotis.amedatest.AMEDA.AMEDA;
import au.net.nicksifniotis.amedatest.AMEDA.AMEDAInstruction;
import au.net.nicksifniotis.amedatest.AMEDA.AMEDAResponse;
import au.net.nicksifniotis.amedatest.Globals;
import au.net.nicksifniotis.amedatest.R;
import au.net.nicksifniotis.amedatest.activities.AMEDAActivity;


/**
 * New AMEDA implementation class using code reverse engineered from other bluetooth projects.
 *
 */
public class AMEDAConnection extends Connection
{
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private String _data_received_buffer;
    private boolean _connected;

    private BluetoothAdapter _bt_adaptor = null;
    private BluetoothSocket _bt_sockets = null;

    final private Handler _read_handler;
    final private Messenger _response_handler;
    final private AMEDAActivity _parent;

    private ConnectedThread _read_thread;


    /**
     * Constructor for the AMEDA connection object.
     *
     * @param context The activity to send UI requests to todo this will not work, revisit this
     * @param responses The messenger to use to send messages back to the connection manager.
     */
    public AMEDAConnection(AMEDAActivity context, Messenger responses)
    {
        super (responses);

        _connected = false;
        _response_handler = responses;
        _parent = context;
        _data_received_buffer = "";

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

                Message message = new Message();
                message.what = 1;
                message.obj = response;

                try
                {
                    _response_handler.send(message);
                }
                catch (RemoteException e)
                {
                    // ha ha
                }
            }
        }
    }


    /**
     * Fire up the Bluetooth adapter if not already active.
     */
    private void checkBTState()
    {
        if (_bt_adaptor == null)
            Globals.Error(_parent, _parent.getString(R.string.error_ameda_no_bluetooth));
        else if (!_bt_adaptor.isEnabled())
        {
            Intent enableBtIntent = new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE");
            _parent.startActivityForResult(enableBtIntent, 1);
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
                    return;
                }
            }
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
    public boolean Connect()
    {
        _bt_adaptor = BluetoothAdapter.getDefaultAdapter();
        checkBTState();

        BluetoothDevice device = null;
        String device_name = "AMEDA";

        List<BluetoothDevice> pairedDevices = new ArrayList<>(_bt_adaptor.getBondedDevices());
        for (BluetoothDevice d: pairedDevices)
            if (d.getName().trim().equals(device_name))
                device = d;

        if (device == null)
        {
            Globals.DebugToast.Send("Unable to find AMEDA. Has it been paired to this device?");
            return false;
        }

        try
        {
            _bt_sockets = createBluetoothSocket(device);
        }
        catch (Exception e)
        {
            Globals.DebugToast.Send("Socket create failed: " + e.getMessage());
            return false;
        }

        if (_bt_sockets == null)
        {
            Globals.DebugToast.Send("Socket created was null.");
            return false;
        }

        _bt_adaptor.cancelDiscovery();

        try
        {
            _bt_sockets.connect();
        }
        catch (Exception e)
        {
            Globals.DebugToast.Send("Socket connection failure: " + e.getMessage());
            return false;
        }

        _read_thread = new ConnectedThread(_bt_sockets);
        _read_thread.start();

        _connected = true;


        // Signal the success of the connection by sending a message back to the parent UI
        Message message = new Message();
        message.what = AMEDA.CONNECTED;

        try
        {
            _response_handler.send(message);
        }
        catch (RemoteException e)
        {
            // ha ha
        }

        return true;
    }


    /**
     * Sends the instruction to the device.
     *
     * @param i The instruction to execute.
     */
    public void SendInstruction (AMEDAInstruction i)
    {
        _read_thread.write(i.Build());
    }


    /**
     * Close the connection to the AMEDA device.
     */
    public void Disconnect()
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
    }

    @Override
    public void run()
    {

    }

    @Override
    public boolean handle_manager_message(Message msg) {
        return false;
    }
}
