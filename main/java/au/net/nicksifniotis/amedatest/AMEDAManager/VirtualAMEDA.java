package au.net.nicksifniotis.amedatest.AMEDAManager;

/**
 * Implementation of a virtual AMEDA device, for testing purposes.
 *
 * This fully implements the AMEDA interface to enable testing and integration
 * with the remainder of the system.
 */
public class VirtualAMEDA implements AMEDA
{
    /**
     * Moves the AMEDA plate to the given position.
     *
     * As the virtual AMEDA doesn't have a plate to move, every call to this function returns
     * a true.
     *
     * @param position The position to move the device to. Valid range is from 1 to 5.
     * @return True if the device moved successfully, false otherwise.
     */
    @Override
    public boolean GoToPosition(int position)
    {
        return true;
    }


    /**
     * Returns the AMEDA device to it's home position. Effectively, this is a call to
     * GoToPosition(1).
     *
     * @return True on success, false if the AMEDA is unable to execute the task.
     */
    @Override
    public boolean GoHome()
    {
        return GoToPosition(1);
    }


    /**
     * Calibration should occur before the AMEDA device is used.
     * The virtual AMEDA doesn't need to calibrate, so always indicate success
     * by returning a true.
     * @return True if the calibration was successful, false if the AMEDA is returning an
     * error that it cannot recover from.
     */
    @Override
    public boolean Calibrate()
    {
        return true;
    }


    /**
     * Implementation of the terminate connection method.
     * The Virtual AMEDA has no connection to terminate.
     * So do nothing.
     */
    @Override
    public void Terminate()
    {
    }


    /**
     * Implementation of the open connection method.
     * The virtual AMEDA has nothing to connect to, so do nothing.
     */
    @Override
    public void Connect()
    {
    }


    /**
     * Implementation of 'beep'.
     *
     * It should actually play a 'beep' sound I reckon.
     *
     * @param num_beeps The number of beeps to emit.
     * @return True if successful, false otherwise.
     */
    @Override
    public boolean Beep(int num_beeps)
    {
        return true;
    }
}
