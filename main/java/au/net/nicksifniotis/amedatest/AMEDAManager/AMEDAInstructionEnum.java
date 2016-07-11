package au.net.nicksifniotis.amedatest.AMEDAManager;

/**
 * Enumeration class containing the commands that can be sent to the AMEDA,
 * along with associated data.
 */
public enum AMEDAInstructionEnum
{
    HELLO(new AMEDAResponse[]{ AMEDAResponse.READY }),
    BUZZER_SHORT(new AMEDAResponse[]{ }),
    BUZZER_LONG(new AMEDAResponse[]{ }),
    MOVE_TO_POSITION(new AMEDAResponse[]{ AMEDAResponse.READY, AMEDAResponse.CANNOT_MOVE }),
    CALIBRATE(new AMEDAResponse[]{ AMEDAResponse.READY, AMEDAResponse.CALIBRATION_FAIL });
//    REQUEST_ANGLE(new_btn AMEDAResponse[]{ AMEDAResponse.NO_RESPONSE_ANGLE, }); @TODO this

    final private AMEDAResponse [] _valid_responses;


    /**
     * Constructor for the enumerated objects.
     *
     * @param rs Array containing the set of response codes that the AMEDA generates for this instruction.
     */
    AMEDAInstructionEnum (AMEDAResponse[] rs)
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
        for (AMEDAResponse r: this._valid_responses)
            if (r == response)
                return true;

        return false;
    }
}
