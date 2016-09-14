package au.net.nicksifniotis.amedatest.AMEDA;

/**
 * Abstract interface for communicating with the AMEDA device.
 *
 * The implementation of the bluetooth connection and the AMEDA internals (such as the instruction
 * set) are completely hidden to the outside world.
 * Created by nsifniotis on 29/05/16.
 */
public interface AMEDA
{
    int RESPONSE = 1;
    int CONNECTED = 2;


    /**
     * Disconnects the device, if it's currently connected.
     */
    void Disconnect();


    /**
     * Creates the connection to the device, if no such connection exists.
     *
     * Returns true on 'we are connected' and false otherwise.
     */
    boolean Connect();


    /**
     * Transmits the given instruction to the AMEDA.
     *
     * @param instruction The instruction to transmit.
     */
    void SendInstruction(AMEDAInstruction instruction);
}
