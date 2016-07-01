package au.net.nicksifniotis.amedatest.AMEDAManager;

/**
 * Created by nsifniotis on 9/06/16.
 *
 * This class essentially contains the communications protocols specified in
 * the technical manual. Note that this class converts packets to and from the instruction set,
 * however it does not completely specify the instruction set itself - to change the set of
 * instructions that the AMEDA controller works with, you will need to modify the AMEDAInstructionEnum
 * enumeration, and then edit this code to implement those changes.
 *
 */
public class AMEDAInstruction {
    private AMEDAInstructionEnum _instruction;
    private int _n;


    /**
     * Null constructor.
     */
    public AMEDAInstruction()
    {
        this._instruction = AMEDAInstructionEnum.NONE;
        this._n = 0;
    }


    public AMEDAInstruction CreatePacket()
    {
        return new AMEDAInstruction();
    }

    public AMEDAInstruction Instruction (AMEDAInstructionEnum in)
    {
        this._instruction = in;
        return this;
    }

    public AMEDAInstruction N (int n) throws AMEDAException
    {
        if (n < 1 || n > 9)
            throw new AMEDAException("Invalid n passed to packet factory.");

        if (_instruction == AMEDAInstructionEnum.NONE
                || _instruction == AMEDAInstructionEnum.HELLO
                || _instruction == AMEDAInstructionEnum.REQUEST_ANGLE
                || _instruction == AMEDAInstructionEnum.CALIBRATE)
            throw new AMEDAException("Attempting to set data byte on instruction" +
                    " that doesn't use it");

        this._n = n;
        return this;
    }

    public String Build()
    {
        String res = "XXXXX";
        switch (_instruction)
        {
            case HELLO:
                res = "HELLO";
                break;
            case MOVE_TO_POSITION:
                res = "GOPN" + Integer.toString(_n);
                break;
            case BUZZER_LONG:
                res = "BZLG" + Integer.toString(_n);
                break;
            case BUZZER_SHORT:
                res = "BZSH" + Integer.toString(_n);
                break;
            case REQUEST_ANGLE:
                res = "RQAGL";
                break;
            case CALIBRATE:
                res = "CALHZ";
                break;
        }

        //return res;
        return packetize(res);
    }


    /**
     * Private member function that encodes and checksums the instruction according to the
     * AMEDA's technical specifications.
     *
     * @param ins The raw instruction to be transmitted to the device.
     * @return A string representing the 8 byte packet to be sent to the device.
     */
    private String packetize(String ins)
    {
        int chk = 0;
        for (char c: ins.toCharArray())
            chk += c;

        String res = "[" + ins + (char)(chk % 256) + "]";

        return res;
    }


    /**
     * Converts a packet received from the device into a response that the AMEDA controller
     * can understand.
     *
     * @param ins The packet received from the device.
     * @throws AMEDAException If the packet is unreadable or contains an unrecognised instruction.
     */
    private void depacketise (String ins) throws AMEDAException
    {
        // test for the easy wins first
        if (ins.length() == 8 && ins.charAt(0) == '[' && ins.charAt(7) == ']')
        {
            String instruction = ins.substring(1, 6);
            int checksum = (int) ins.charAt(6);

            int chk = 0;
            for (char c: instruction.toCharArray())
                chk += (int) c;

            if (checksum == (chk % 256))
            {
                // passes the checksum test, so now lets see what we have received.
                // @TODO this needs to be processed properly, not using throws here and there
                if (instruction == "READY")
                {}
                else if (instruction == "NEGAK")
                    throw new AMEDAException ("AMEDA reports NEGAK - instruction transmission failure.");
                else if (instruction == "ERR01")
                    throw new AMEDAException ("AMEDA reports ERR01 - failed to set new position.");
                else if (instruction == "ERR02")
                    throw new AMEDAException ("AMEDA reports ERR02 - failed to contact wobble board.");
                else if (instruction == "ERR03")
                    throw new AMEDAException ("AMEDA reports ERR03 - failed to calibrate.");

            }
            else throw new AMEDAException("Invalid packet received from device: " + ins);
        }
        else throw new AMEDAException("Invalid packet received from device: " + ins);
    }
}
