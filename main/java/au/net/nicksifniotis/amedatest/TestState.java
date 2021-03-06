package au.net.nicksifniotis.amedatest;


/**
 * Simple set of states for the AMEDA test state machine.
 *
 */
public enum TestState
{
    STARTING(R.id.t_layout_starting),
    MIDDLE(R.id.t_layout_middle),
    ANSWERING(R.id.t_layout_answering),
    FINISHING(R.id.t_layout_finishing);

    final private int _my_layout;


    TestState (int i)
    {
        this._my_layout = i;
    }

    /**
     * Accessor function for the layout_id that this state accesses.
     *
     * @return The layout_id for direct injection into findViewById
     */
    public int Layout()
    {
        return this._my_layout;
    }
}
