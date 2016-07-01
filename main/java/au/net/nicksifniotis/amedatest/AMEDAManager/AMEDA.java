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

    public boolean GoToPosition (int position);

    public boolean Calibrate();

    public void Disconnect();

    public void Connect();

    public boolean Beep(int num_beeps);
}
