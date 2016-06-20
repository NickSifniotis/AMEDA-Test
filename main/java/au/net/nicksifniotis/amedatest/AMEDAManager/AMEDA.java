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
    public boolean GoToPosition (int position);

    public boolean GoHome();

    public boolean Calibrate();

  //  public AMEDAState Status();

    public void Terminate();

    public void Connect();

  //  public boolean BeepTest(int num_beeps);
}
