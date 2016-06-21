package au.net.nicksifniotis.amedatest.AMEDAManager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import java.util.ArrayList;

import au.net.nicksifniotis.amedatest.BluetoothManager.BTState;
import au.net.nicksifniotis.amedatest.BluetoothManager.BluetoothService;
import au.net.nicksifniotis.amedatest.BluetoothManager.BluetoothServiceImplementation;

/**
 * Created by nsifniotis on 9/06/16.
 *
 * Implementation of the AMEDA interface.
 */
public class AMEDAImplementation implements AMEDA {
    private AMEDAState _current_state;
    private Handler _test_handler;
    private BluetoothService _service;
    private Context _view;

    public AMEDAImplementation(Context view, Handler h, boolean debug_mode) throws Exception {
        _current_state = AMEDAState.OFFLINE;
        _view = view;
        _test_handler = h;

        _service = new BluetoothServiceImplementation(view);

        Connect();
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
            _service.write(instruction.Build());

//            _current_state = AMEDAState.TRANSITIONING;
//
//            // @TODO this override needs to be disabled when reading from the ameda is resolved
//            int delay = 1000; //_random.nextInt(5000) + 500;
//            Message m = _test_handler.obtainMessage(1);
//            _test_handler.sendMessageDelayed(m, delay);
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

    public AMEDAState Status() {
        return _current_state;
    }

    @Override
    public void Terminate()
    {
        _service.stop();
    }

    public void makeToast (String message)
    {
        Toast t = Toast.makeText(_view, message, Toast.LENGTH_SHORT);
        t.show();
    }

    public boolean BeepTest(int num_beeps) {
        try {
            AMEDAInstruction instruction = AMEDAInstructionFactory.Create()
                    .Instruction(AMEDAInstructionEnum.BUZZER_SHORT)
                    .N(num_beeps);

            _service.write(instruction.Build());
            return true;
        }
        catch (AMEDAException e)
        {
            makeToast(e.getMessage());
            return false;
        }
    }


    @Override
    public void Connect()
    {
        String device_name = "AMEDA";

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
            if (d.getName().equals(device_name))
                ameda = d;

        // @TODO this
        if (ameda == null) {
            makeToast("Could not find AMEDA in list of paired devices");
            return;
        }

        _service.connect(ameda);

        _current_state = (_service.getState() == BTState.CONNECTED) ? AMEDAState.READY : AMEDAState.CONNECTION_ERROR;
    }
}
