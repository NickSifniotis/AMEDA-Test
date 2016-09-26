package au.net.nicksifniotis.amedatest.AMEDA;


/**
 * Created by Nick Sifniotis on 9/06/16.
 *
 * This class essentially contains the communications protocols specified in
 * the technical manual. Note that this class converts packets to and from the instruction set,
 * however it does not completely specify the instruction set itself - to change the set of
 * instructions that the AMEDA controller works with, you will need to modify the AMEDAInstructionEnum
 * enumeration, and then edit this code to implement those changes.
 *
 */
public class AMEDAInstruction
{
    private AMEDAInstructionEnum _instruction;
    private int _n;


    /**
     * Null constructor.
     */
    private AMEDAInstruction()
    {
        this._instruction = AMEDAInstructionEnum.HELLO;
        this._n = 0;
    }


    /**
     * Factory constructor method.
     *
     * @return A new Instruction object.
     */
    public static AMEDAInstruction Create()
    {
        return new AMEDAInstruction();
    }


    /**
     * Factory method.
     * Sets the sort of instruction that this is.
     *
     * @param in The instruction code to use.
     * @return A reference to this factory object.
     */
    public AMEDAInstruction Instruction (AMEDAInstructionEnum in)
    {
        this._instruction = in;
        return this;
    }


    /**
     * Factory method.
     * Sets the instruction's payload.
     *
     * @param n The value to store along with the instruction.
     * @return A reference to the instruction factory object.
     */
    public AMEDAInstruction N (int n)
    {
        this._n = n;
        return this;
    }


    /**
     * Converts this instruction factory into a string.
     *
     * @return A string representation of the instruction form held by this factory.
     */
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
     * Accessor methods.
     *
     * @return The requested data.
     */
    public AMEDAInstructionEnum GetInstruction()
    {
        return _instruction;
    }

    public int GetN()
    {
        return this._n;
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

        return "[" + ins + (char)(chk % 256) + "]";
    }
}
