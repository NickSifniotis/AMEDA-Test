package au.net.nicksifniotis.amedatest;

/**
 * Abstract interface for communicating with the AMEDA device.
 *
 * The implementation of the bluetooth connection and the AMEDA's data language are both buried in this class.
 * To the rest of the app, this is a black box that weaves special magic.
 *
 * The only time that this layering is broken is when there is an irrepairable break in the connection and the test
 * or procedure currently underway has to be terminated.
 *
 * Created by nsifniotis on 29/05/16.
 */
public class AMEDA
{
    public static boolean GoToPosition (int position)
    {
        return true;
    }

    public static boolean GoHome()
    {
        return true;
    }

    public static boolean Calibrate()
    {
        return true;
    }
}
