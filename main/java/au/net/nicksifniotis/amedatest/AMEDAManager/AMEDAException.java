package au.net.nicksifniotis.amedatest.AMEDAManager;

/**
 * Created by nsifniotis on 9/06/16.
 *
 * This class is used to flag logical errors in the AMEDA code, as distinct from I/O errors
 * or other standard Java fails.
 */
public class AMEDAException extends Exception {
    public AMEDAException (String error) {
        super(error);
    }
}
