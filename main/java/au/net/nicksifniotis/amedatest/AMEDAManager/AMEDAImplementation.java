package au.net.nicksifniotis.amedatest.AMEDAManager;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * New AMEDA implementation class using code reverse engineered from other bluetooth projects.
 *
 */
public class AMEDAImplementation implements AMEDA
{
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private Context _parent;
    private String dataReceived;
    private boolean _blocked;
    private boolean _connected;

    private BluetoothAdapter btAdaptor = null;
    private BluetoothSocket btSocket = null;

    private Handler _read_handler;
    private Handler _response_handler;
    private ConnectedThread mConnectedThread;


    /**
     * Constructor for the AMEDA Implementation object.
     * Note that connection to the device is not automatic. You need to
     * call connect() on this object after instantiating it.
     *
     * @param context The context that this instance serves.
     */
    public AMEDAImplementation(Context context, Handler responses)
    {
        _blocked = false;
        _connected = false;
        _response_handler = responses;
        _parent = context;
        dataReceived = "";

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
        makeToast("Reading " + msg);
        dataReceived += msg;
        makeToast("New buffer contents: " + dataReceived);

        if (dataReceived.length() >= 8)
        {
            String result = dataReceived.substring(1, 6);
            dataReceived = dataReceived.substring (8);

            AMEDAResponse response = AMEDAResponse.FindResponse(result);
            makeToast("Unblocking: " + response.toString());
          //  _blocked = false;
        }
    }


    /**
     * Fire up the Bluetooth adapter if not already active.
     */
    private void checkBTState()
    {
        if (btAdaptor == null)
        {
            makeToast ("Bluetooth not supported");
            return;
        }

        if (!btAdaptor.isEnabled())
        {
            Intent enableBtIntent = new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE");
            ((Activity)_parent).startActivityForResult(enableBtIntent, 1);
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
                makeToast ("Error: " + e.getMessage());
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
                makeToast("Error connecting to i/o streams. " + e.getMessage());
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
                    makeToast("Error reading from AMEDA device. " + e.getMessage());
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
            makeToast("Write called: " + message);

//            if (_blocked)
//            {
//                makeToast ("Unable to post message - AMEDA blocked. " + _blocked + ": " + message);
//                return;
//            }

            if (!_connected)
            {
                makeToast ("Unable to post message - AMEDA not connected.");
                return;
            }


            byte[] msgBuffer = message.getBytes();
            try
            {
                mmOutStream.write(msgBuffer);
                _blocked = true;
                makeToast("Blocked set to true, message " + message);
            }
            catch (Exception e)
            {
                makeToast("Error writing to device. " + e.getMessage());
            }
        }
    }


    /**
     * Connects to the AMEDA device.
     *
     */
    @Override
    public void Connect()
    {
        btAdaptor = BluetoothAdapter.getDefaultAdapter();
        checkBTState();

        BluetoothDevice device = null;
        String device_name = "AMEDA";

        List<BluetoothDevice> pairedDevices = new ArrayList<>(btAdaptor.getBondedDevices());
        for (BluetoothDevice d: pairedDevices)
            if (d.getName().equals(device_name))
                device = d;

        if (device == null)
        {
            makeToast ("Unable to find AMEDA. Has it been paired to this device?");
            return;
        }

        try
        {
            btSocket = createBluetoothSocket(device);
        }
        catch (Exception e)
        {
            makeToast("Socket create failed: " + e.getMessage());
            return;
        }

        btAdaptor.cancelDiscovery();

        try
        {
            if (btSocket == null)
                makeToast("Socket is null but.");
            btSocket.connect();
        }
        catch (Exception e)
        {
            try
            {
                if (btSocket != null)
                    btSocket.close();
            }
            catch (Exception e2)
            {
                makeToast ("In onResume() and unable to close socket during connection failure: " + e2.getMessage());
            }
        }

        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        _connected = true;
    }


    @Override
    public boolean Beep(int num_beeps)
    {
        try {
            AMEDAInstruction instruction = AMEDAInstructionFactory.Create()
                    .Instruction(AMEDAInstructionEnum.BUZZER_SHORT)
                    .N(num_beeps);

            mConnectedThread.write(instruction.Build());
            return true;
        }
        catch (AMEDAException e)
        {
            makeToast ("Fatal error: " + e.getMessage());
            return false;
        }
    }


    /**
     * Very fancy way to make thread-safe calls to the Toaster.
     *
     * @param message The message to send back to the user.
     */
    public void makeToast (String message)
    {
        final String msg = message;
        ((Activity)_parent).runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                Toast t = Toast.makeText(_parent, msg, Toast.LENGTH_SHORT);
                t.show();
            }
        });
    }


    /**
     * Instructs the AMEDA device to calibrate itself.
     *
     * @return True if the AMEDA reports everything ok, false otherwise.
     */
    @Override
    public boolean Calibrate()
    {
        AMEDAInstruction instruction = AMEDAInstructionFactory.Create()
                .Instruction(AMEDAInstructionEnum.CALIBRATE);
        mConnectedThread.write(instruction.Build());

        return true;
    }


    /**
     * Sets the AMEDA device to a new position, and awaits confirmation that the new position is
     * set. This is a blocking call @TODO it's probably blocking the UI thread, fuck
     *
     * @param position The position to set the AMEDA to.
     * @return True if successful, false otherwise.
     */
    @Override
    public boolean GoToPosition(int position)
    {
        makeToast("Moving to position " + position);

        AMEDAInstruction instruction = null;
        boolean result = true;

//        if (_blocked)
//            result = false;

        if (result)
        {
            try
            {
                instruction = AMEDAInstructionFactory.Create()
                        .Instruction(AMEDAInstructionEnum.MOVE_TO_POSITION)
                        .N(position);
            }
            catch (AMEDAException e)
            {
                result = false;
            }
        }

        if (result)
        {
            mConnectedThread.write(instruction.Build());

            // this is dangerous as all hell. Block until some other thread changes the value of
            // _blocked.
//            while (_blocked)
//            {
//                try
//                {
//                    Thread.sleep(500);
//                }
//                catch (Exception e)
//                {
//                    break;
//                }
//            }
        }

        return result;
    }


    /**
     * Close the connection to the AMEDA device.
     */
    @Override
    public void Disconnect()
    {
        mConnectedThread.interrupt();
        try
        {
            btSocket.close();
        }
        catch (Exception e)
        {
            makeToast("Error closing AMEDA connection. " + e.getMessage());
        }
    }
}
