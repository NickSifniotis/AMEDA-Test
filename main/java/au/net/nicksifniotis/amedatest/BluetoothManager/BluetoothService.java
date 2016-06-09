package au.net.nicksifniotis.amedatest.BluetoothManager;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

/**
 * Created by nsifniotis on 9/06/16.
 *
 * Public interface for the BlueTooth manager classes.
 *
 * The only access that any methods or objects have to items within this subpackage
 * should be through this interface.
 *
 */
public interface BluetoothService {
    BTState getState();

    void start();

    void connect(BluetoothDevice device);

    void connected(BluetoothSocket socket, BluetoothDevice
            device);

    void stop();

    void write(byte[] out);
}
