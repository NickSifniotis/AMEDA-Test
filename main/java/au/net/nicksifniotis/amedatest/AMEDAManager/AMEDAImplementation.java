package au.net.nicksifniotis.amedatest.AMEDAManager;

/**
 * Created by nsifniotis on 9/06/16.
 *
 * Implementation of the AMEDA interface.
 */
public class AMEDAImplementation implements AMEDA {
    private AMEDAState _current_state;

    public AMEDAImplementation() throws Exception {
        _current_state = AMEDAState.OFFLINE;

        // connect to the AMEDA device. Reset to position 1 and recalibrate.
        // throw an error if the device cannot be connected / read to

        String device_name = "AMEDA";
        String device_address = "";
    }

    @Override
    public boolean GoToPosition(int position) {
        return false;
    }

    @Override
    public boolean GoHome() {
        return false;
    }

    @Override
    public boolean Calibrate() {
        return false;
    }

    @Override
    public AMEDAState Status() {
        return _current_state;
    }
}
