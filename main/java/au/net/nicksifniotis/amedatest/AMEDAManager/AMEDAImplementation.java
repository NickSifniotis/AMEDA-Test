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
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Created by nsifniotis on 30/06/16.
 */
public class AMEDAImplementation implements AMEDA
{
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private Context _parent;
    private String dataReceived;
    private boolean blocked;

    private BluetoothAdapter btAdaptor = null;
    private BluetoothSocket btSocket = null;

    Handler handler;
    private ConnectedThread mConnectedThread;


    public AMEDAImplementation(Context context)
    {
        _parent = context;
        dataReceived = "";

        handler = new Handler() {
            /**
             *
             * @param msg
             */
            public void handleMessage (Message msg) {
                String readMessage = (String) msg.obj;

                if (msg.what != 1)
                    return;

                if (msg.arg1 > 0)
                    addMessage(readMessage);
            }
        };
    }


    /**
     * Add data received by the AMEDA to this list.
     *
     * @param msg
     */
    private void addMessage(String msg)
    {
        dataReceived += msg;
        if (dataReceived.length() > 8)
        {
            String result = dataReceived.substring(0, 8);
            dataReceived = dataReceived.substring (8);

            makeToast(result);
            blocked = false;
        }
    }


    /**
     * Fire up the Bluetooth adapter if not already active.
     */
    private void checkBTState()
    {
        if (btAdaptor == null)
        {
            errorExit("Fatal Error", "Bluetooth not supported");
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
     * @param device
     * @return
     * @throws IOException
     */
    private BluetoothSocket createBluetoothSocket (BluetoothDevice device) throws IOException
    {
        Toast.makeText(_parent, "createBluetoothSocket", Toast.LENGTH_SHORT).show();

        BluetoothSocket res;

        if (Build.VERSION.SDK_INT >= 10)
        {

            try
            {
//                Class<?> c = device.getClass();
//                Method m = c.getMethod("createInsecureRfcommSocketToServiceRecord", c);
//
//                res = (BluetoothSocket) m.invoke(device, MY_UUID);
                Method m = device.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
                res = (BluetoothSocket) m.invoke(device, 1);

                return res;
            }
            catch (Exception e)
            {
                Toast.makeText(_parent, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        res = device.createRfcommSocketToServiceRecord(MY_UUID);

        return res;
    }


    private void errorExit (String title, String message)
    {
        Toast.makeText(_parent, title + " - " + message, Toast.LENGTH_LONG).show();
    }


    class ConnectedThread extends Thread {
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
            {}

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }


        @Override
        public void run()
        {
            byte[] buffer = new byte[0x100];

            while (!interrupted())
            {
                try
                {
                    int bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    handler.obtainMessage(1, bytes, -1, readMessage).sendToTarget();
                }
                catch (Exception e)
                {
                    return;
                }
            }
        }


        public void write(String message)
        {
            byte[] msgBuffer = message.getBytes();

            try
            {
                mmOutStream.write(msgBuffer);
                blocked = true;
            }
            catch (Exception e)
            {
                Toast.makeText(_parent, "DEVICE UNAVAILABLE", Toast.LENGTH_SHORT).show();
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

        ArrayList<BluetoothDevice> pairedDevices = new ArrayList(btAdaptor.getBondedDevices());
        for (BluetoothDevice d: pairedDevices)
            if (d.getName().equals(device_name))
                device = d;

        try
        {
            btSocket = createBluetoothSocket(device);
        }
        catch (Exception e)
        {
            errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
            return;
        }

        btAdaptor.cancelDiscovery();

        try
        {
            Toast.makeText(_parent, "Attempting to connect socket", Toast.LENGTH_SHORT).show();
            if (btSocket == null)
                Toast.makeText(_parent, "Socket is null but.", Toast.LENGTH_SHORT).show();
            btSocket.connect();
        }
        catch (Exception e)
        {
            try
            {
                btSocket.close();
            }
            catch (Exception e2)
            {
                errorExit ("Fatal Error", "In onResume() and unable to close socket during connection failure: " + e2.getMessage() + ".");
            }
        }

        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();
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
            errorExit ("Fatal error", e.getMessage());
            return false;
        }
    }


    public void makeToast (String message)
    {
        Toast t = Toast.makeText(_parent, message, Toast.LENGTH_SHORT);
        t.show();
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


    @Override
    public boolean GoToPosition(int position)
    {
        try {
//            AMEDAInstruction instruction = AMEDAInstructionFactory.Create()
//                    .Instruction(AMEDAInstructionEnum.MOVE_TO_POSITION)
//                    .N(5);
//            _service.write(instruction.Build());
//
//            instruction = AMEDAInstructionFactory.Create()
//                    .Instruction(AMEDAInstructionEnum.MOVE_TO_POSITION)
//                    .N(1);
//            _service.write(instruction.Build());

            AMEDAInstruction instruction = AMEDAInstructionFactory.Create()
                    .Instruction(AMEDAInstructionEnum.MOVE_TO_POSITION)
                    .N(position);
            mConnectedThread.write(instruction.Build());

//            _current_state = AMEDAState.TRANSITIONING;
//
//            // @TODO this override needs to be disabled when reading from the ameda is resolved
//            int delay = 1000; //_random.nextInt(5000) + 500;
//            Message m = _test_handler.obtainMessage(1);
//            _test_handler.sendMessageDelayed(m, delay);

            while (blocked) {
                try {
                    Thread.sleep(500);
                } catch (Exception e) {

                }
            }
        }
        catch (AMEDAException e)
        {
            return false;
        }

        return true;
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

        }
    }
}
