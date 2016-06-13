package au.net.nicksifniotis.amedatest.BluetoothManager;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Created by nsifniotis on 13/06/16.
 *
 * Another crack at writing a Bluetooth connection interface that works.
 */
public class BluetoothServiceImplementation implements BluetoothService
{
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private Context _parent_context;
    private ReaderThread _thread;
    private Handler _handler;


    public BluetoothServiceImplementation(Context p)
    {
        _parent_context = p;
    }


    @Override
    public BTState getState() {
        return null;
    }

    @Override
    public void connect(BluetoothDevice device) {

    }

    @Override
    public void connected(BluetoothSocket socket, BluetoothDevice device) {

    }

    @Override
    public void stop() {

    }

    @Override
    public void write(byte[] out) {

    }


    /**
     * Creates a Bluetooth Socket to the device using dodgy af undocumented API calls.
     *
     * @param device The device to connect to.
     * @return The bluetooth socket, or null if shit's hit the fan.
     * @throws IOException If different shit hits the fan.
     */
    private BluetoothSocket createBluetoothSocket (BluetoothDevice device) throws IOException
    {
        BluetoothSocket res;

        if (Build.VERSION.SDK_INT >= 10)
        {
            try
            {
                Method m = device.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
                res = (BluetoothSocket) m.invoke(device, 1);

                return res;
            }
            catch (Exception e)
            {
                Toast.makeText(_parent_context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        res = device.createRfcommSocketToServiceRecord(MY_UUID);

        return res;
    }


    /**
     * Slave thread that reads incoming data from the Bluetooth connection and pumps them through to the controlling
     * object via a message handler.
     *
     */
    class ReaderThread extends Thread
    {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;


        public ReaderThread (BluetoothSocket socket)
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

            while (true) {
                try {
                    int bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    _handler.obtainMessage(1, bytes, -1, readMessage).sendToTarget();
                } catch (Exception e) {

                }
            }
        }


        public void write(String message)
        {
            byte[] msgBuffer = message.getBytes();

            try
            {
                mmOutStream.write(msgBuffer);
            }
            catch (Exception e)
            {
                Toast.makeText(_parent_context, "DEVICE UNAVAILABLE", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
