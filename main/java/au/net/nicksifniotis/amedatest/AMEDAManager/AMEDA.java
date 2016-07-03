package au.net.nicksifniotis.amedatest.AMEDAManager;

/**
 * Abstract interface for communicating with the AMEDA device.
 *
 * The implementation of the bluetooth connection and the AMEDA internals (such as the instruction
 * set) are completely hidden to the outside world.
 * Created by nsifniotis on 29/05/16.
 */
public interface AMEDA
{
    public static final int RESPONSE = 1;
    public static final int CONNECTED = 2;

    public boolean GoToPosition (int position);

    public boolean Calibrate();


    /**
     * Disconnects the device, if it's currently connected.
     */
    public void Disconnect();


    /**
     * Creates the connection to the device, if no such connection exists.
     */
    public void Connect();

    public boolean Beep(int num_beeps);


    /**
     * Transmits the given instruction to the AMEDA.
     *
     * @param instruction The instruction to transmit.
     */
    public void SendInstruction (AMEDAInstruction instruction);
}
