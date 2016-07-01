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


    public AMEDAInstruction Instruction (AMEDAInstructionEnum in)
    {
        this._instruction = in;
        return this;
    }

    public AMEDAInstruction N (int n)
    {
//        if (n < 1 || n > 9)
//            throw new AMEDAException("Invalid n passed to packet factory.");
//
//        if (_instruction == AMEDAInstructionEnum.NONE
//                || _instruction == AMEDAInstructionEnum.HELLO
//                || _instruction == AMEDAInstructionEnum.REQUEST_ANGLE
//                || _instruction == AMEDAInstructionEnum.CALIBRATE)
//            throw new AMEDAException("Attempting to set data byte on instruction" +
//                    " that doesn't use it");

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

        return packetize(res);
    }


    /**
     * Accessor method for the instruction enumeration.
     *
     * @return This instance's instruction enum.
     */
    public AMEDAInstructionEnum GetInstruction()
    {
        return _instruction;
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
}
