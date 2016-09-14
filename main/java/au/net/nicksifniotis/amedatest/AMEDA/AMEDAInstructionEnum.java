package au.net.nicksifniotis.amedatest.AMEDA;

/**
 * Enumeration class containing the commands that can be sent to the AMEDA,
 * along with associated data.
 */
public enum AMEDAInstructionEnum
{
    HELLO(new AMEDAResponse.Code[]{ AMEDAResponse.Code.READY }),
    BUZZER_SHORT(new AMEDAResponse.Code[]{ }),
    BUZZER_LONG(new AMEDAResponse.Code[]{ }),
    MOVE_TO_POSITION(new AMEDAResponse.Code[]{ AMEDAResponse.Code.READY, AMEDAResponse.Code.CANNOT_MOVE }),
    CALIBRATE(new AMEDAResponse.Code[]{ AMEDAResponse.Code.READY, AMEDAResponse.Code.CALIBRATION_FAIL }),
    REQUEST_ANGLE(new AMEDAResponse.Code[]{ AMEDAResponse.Code.NO_RESPONSE_ANGLE, AMEDAResponse.Code.ANGLE });

    final private AMEDAResponse.Code [] _valid_responses;


    /**
     * Constructor for the enumerated objects.
     *
     * @param rs Array containing the set of response codes that the AMEDA generates for this instruction.
     */
    AMEDAInstructionEnum (AMEDAResponse.Code[] rs)
    {
        this._valid_responses = rs;
    }


    /**
     * Checks to see whether the response received for the command that this enum represents is
     * a valid response / one that we were expecting.
     *
     * Generally, failing this test is very bad because it means that either the commands are being
     * sent and received in random order, or the AMEDA itself is malfunctioning.
     *
     * @param response The response received after transmitting this command.
     * @return True if the response is a valid response to this command, false otherwise.
     */
    public boolean IsValidResponse(AMEDAResponse response)
    {
        AMEDAResponse.Code code = response.GetCode();
        for (AMEDAResponse.Code r: this._valid_responses)
            if (r == code)
                return true;

        return false;
    }
}
