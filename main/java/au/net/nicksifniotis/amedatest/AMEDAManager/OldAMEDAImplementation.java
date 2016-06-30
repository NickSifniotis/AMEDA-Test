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
public class OldAMEDAImplementation implements AMEDA
{
    private BluetoothService _service;
    private Context _view;


    /**
     * Construct the AMEDA device - don't attempt to connect though, that's something
     * that the activites direct.
     *
     * @param view The activity that this AMEDA implementation lives in.
     * @throws Exception if the shit hits the fan.
     */
    public OldAMEDAImplementation(Context view) throws Exception
    {
        _view = view;
        _service = new BluetoothServiceImplementation(view);
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
        //    _service.write(instruction.Build());

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


    /**
     * Returns the AMEDA to its horizontal / home position.
     *
     * @return True if the operation was successful, false if the AMEDA couldn't behave as directed.
     */
    public boolean GoHome()
    {
        try {
            AMEDAInstruction instruction = AMEDAInstructionFactory.Create()
                    .Instruction(AMEDAInstructionEnum.MOVE_TO_POSITION)
                    .N(1);
          //  _service.write(instruction.Build());
        }
        catch (AMEDAException e)
        {
            return false;
        }

        return true;
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
   //     _service.write(instruction.Build());

        return true;
    }

    /**
     * Disconnect from the AMEDA device.
     *
     * This method could probably use a more accurate name.
     */
    @Override
    public void Disconnect()
    {
        _service.stop();
    }

    public void makeToast (String message)
    {
        Toast t = Toast.makeText(_view, message, Toast.LENGTH_SHORT);
        t.show();
    }


    @Override
    public boolean Beep(int num_beeps)
    {
        try {
            AMEDAInstruction instruction = AMEDAInstructionFactory.Create()
                    .Instruction(AMEDAInstructionEnum.BUZZER_SHORT)
                    .N(num_beeps);

      //      _service.write(instruction.Build());
            return true;
        }
        catch (AMEDAException e)
        {
            makeToast(e.getMessage());
            return false;
        }
    }


    /**
     * Connects to the AMEDA device.
     *
     */
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
    }
}
