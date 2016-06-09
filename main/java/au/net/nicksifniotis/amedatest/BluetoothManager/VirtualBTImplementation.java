package au.net.nicksifniotis.amedatest.BluetoothManager;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;

import java.util.Random;

/**
 * Created by nsifniotis on 9/06/16.
 *
 * The 'virtual Bluetooth connection' that only pretends to connect to the AMEDA
 * For debugging purposes only.
 */
public class VirtualBTImplementation implements BluetoothService
{
    private BTState _my_state;
    private Handler _handler;
    private Random _random;


    public VirtualBTImplementation (Handler handler)
    {
        _my_state = BTState.NONE;
        _handler = handler;
        _random = new Random();
    }


    @Override
    public BTState getState() {
        return _my_state;
    }

    @Override
    public void start() {
        // do not very much.
        _my_state = BTState.LISTEN;
    }

    @Override
    public void connect(BluetoothDevice device) {
        _my_state = BTState.CONNECTING;
    }

    @Override
    public void connected(BluetoothSocket socket, BluetoothDevice device) {
        _my_state = BTState.CONNECTED;
    }

    @Override
    public void stop() {
        _my_state = BTState.NONE;
    }

    @Override
    public void write(byte[] out) {
        // to simulate the AMEDA device, delay the transmission of the response message
        // by some random amount from 500 - 5000ms

        int delay = 1000; //_random.nextInt(5000) + 500;
        Message m = _handler.obtainMessage(1);
        _handler.sendMessageDelayed(m, delay);
    }
}
