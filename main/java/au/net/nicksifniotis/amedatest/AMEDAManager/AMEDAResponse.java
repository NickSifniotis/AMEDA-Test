package au.net.nicksifniotis.amedatest.AMEDAManager;

/**
 * Enumeration class for the various response codes that the AMEDA device generates.
 *
 * @TODO: This setup is not going to work for 'angle request' responses. Refactor
 */
public enum AMEDAResponse
{
    READY("READY"),
    CANNOT_MOVE("ERR01"),
    NO_RESPONSE_ANGLE("ERR02"),
    CALIBRATION_FAIL("ERR03"),
    UNKNOWN_COMMAND("NEGAK");

    private String packet;

    AMEDAResponse(String p)
    {
        this.packet = p;
    }

    public static AMEDAResponse FindResponse(String p)
    {
        for (AMEDAResponse a: AMEDAResponse.values())
            if (a.packet.equals(p))
                return a;

        return null;
    }
}
