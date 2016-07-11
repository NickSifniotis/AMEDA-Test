package au.net.nicksifniotis.amedatest.AMEDAManager;


/**
 * Class that represents the various response codes that the AMEDA device generates.
 */
public class AMEDAResponse
{
    final private Code _code;
    final private int _angle;


    /**
     * Constructors for the AMEDA responses.
     *
     * @param p The packet string received from the AMEDA device.
     */
    public AMEDAResponse(String p)
    {
        p = _validate_packet(p);

        if (p != null)
        {
            this._code = Code.FindCode(p);
            if (this._code == Code.ANGLE)
                this._angle = Integer.parseInt(p.substring(1, 5));
            else
                this._angle = 0;
        }
        else
        {
            _code = null;
            _angle = 0;
        }
    }


    /**
     * Gets the angle received from the AMEDA device, if the response code was ANGLE.
     * If it wasn't, just return zero.
     *
     * @return The angle
     */
    public int GetAngle()
    {
        return this._angle;
    }


    /**
     * Gets the instruction code.
     *
     * @return The enumerated instruction code extracted from this packet.
     */
    public Code GetCode()
    {
        return this._code;
    }


    /**
     * Verifies that the 8-byte packet received from the device is in the correct format, and
     * returns the 5-byte instruction code if validation is successful.
     *
     * @param p The 8-byte packet received from the AMEDA.
     * @return The 5-byte instruction code extracted from the packet.
     */
    private String _validate_packet(String p)
    {
        if (p.length() != 8)
            return null;
        if (p.charAt(0) != '[' || p.charAt(7) != ']')
            return null;

        String ins = p.substring(1, 6);
        int checksum = (int)p.charAt(6);
        int chk = 0;
        for (char c: ins.toCharArray())
            chk += c;

        return (checksum == (chk % 256)) ? ins : null;
    }


    /**
     * An enumeration containing the response codes that the AMEDA can respond with.
     */
    public enum Code
    {
        READY("READY"),
        CANNOT_MOVE("ERR01"),
        NO_RESPONSE_ANGLE("ERR02"),
        CALIBRATION_FAIL("ERR03"),
        WOBBLE_NO_RESPONSE("ERR04"),
        ANGLE("A"),
        UNKNOWN_COMMAND("NEGAK");

        final private String packet;

        Code (String p)
        {
            this.packet = p;
        }


        /**
         * Returns the code corresponding to the packet received by the device.
         *
         * @param p The 5-byte instruction received from the AMEDA.
         * @return The Code corresponding to that instruction.
         */
        public static Code FindCode(String p)
        {
            for (Code c: Code.values())
                if (c.packet.equals(p.substring(0, c.packet.length())))
                    return c;

            return null;
        }
    }
}
