package au.net.nicksifniotis.amedatest.AMEDAManager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.ArrayList;

import au.net.nicksifniotis.amedatest.BluetoothManager.BluetoothService;
import au.net.nicksifniotis.amedatest.BluetoothManager.BluetoothServiceImplementation;
import au.net.nicksifniotis.amedatest.BluetoothManager.VirtualBTImplementation;

/**
 * Created by nsifniotis on 9/06/16.
 *
 * Implementation of the AMEDA interface.
 */
public class AMEDAImplementation implements AMEDA {
    private AMEDAState _current_state;
    private Handler _message_handler;
    private BluetoothService _service;


    public AMEDAImplementation(boolean debug_mode) throws Exception {
        _current_state = AMEDAState.OFFLINE;


        // connect to the AMEDA device. Reset to position 1 and recalibrate.
        // throw an error if the device cannot be connected / read to

        String device_name = "AMEDA";
        _message_handler = new Handler(Looper.myLooper(), new Handler.Callback() {
            public boolean handleMessage(Message msg) {
                if (msg.what == 1)
                {
                    // this means READY so advance the AMEDA state.
                    _current_state = AMEDAState.READY;
                }
                else if (msg.what == 2)
                {
                    // this is an error state of some kind.
                    _current_state = AMEDAState.ERROR;
                }

                return true;
            }
        });

        if (debug_mode)
            _service = new VirtualBTImplementation(_message_handler);
        else
            _service = new BluetoothServiceImplementation(_message_handler);

        // try to find the AMEDA device.
        // TODO fix the error state reporting here
        BluetoothAdapter adaptor = BluetoothAdapter.getDefaultAdapter();
        if (adaptor == null)
            return;

        if (!adaptor.isEnabled())
            return;

        BluetoothDevice ameda = null;
        ArrayList<BluetoothDevice> pairedDevices = new ArrayList(adaptor.getBondedDevices());
        for (BluetoothDevice d: pairedDevices)
            if (d.getName() == device_name)
                ameda = d;

        // @TODO this
        if (ameda == null)
            return;

        _service.connect(ameda);
        _current_state = AMEDAState.READY;
    }


    @Override
    public boolean GoToPosition(int position)
    {
        try {
            AMEDAInstruction instruction = AMEDAInstructionFactory.Create()
                    .Instruction(AMEDAInstructionEnum.MOVE_TO_POSITION)
                    .N(position);
            _service.write(instruction.Build());

            _current_state = AMEDAState.TRANSITIONING;
        }
        catch (AMEDAException e)
        {
            return false;
        }

        return true;
    }

    @Override
    public boolean GoHome() {
        try {
            AMEDAInstruction instruction = AMEDAInstructionFactory.Create()
                    .Instruction(AMEDAInstructionEnum.MOVE_TO_POSITION)
                    .N(1);
            _service.write(instruction.Build());

            _current_state = AMEDAState.TRANSITIONING;
        }
        catch (AMEDAException e)
        {
            return false;
        }

        return true;
    }

    @Override
    public boolean Calibrate() {
        AMEDAInstruction instruction = AMEDAInstructionFactory.Create()
                .Instruction(AMEDAInstructionEnum.CALIBRATE);
        _service.write(instruction.Build());

        _current_state = AMEDAState.TRANSITIONING;

        return true;
    }

    @Override
    public AMEDAState Status() {
        return _current_state;
    }

}
