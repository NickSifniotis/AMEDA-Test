package au.net.nicksifniotis.amedatest.AMEDAManager;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Implements an 'instruction queue' data structure for use by the AMEDA controller state machines.
 */
public class AMEDAInstructionQueue
{
    final private Queue <AMEDAInstruction> _queue;
    private AMEDAInstruction _last_instruction;


    public AMEDAInstructionQueue()
    {
        _queue = new LinkedList<>();
        _last_instruction = null;
    }


    /**
     * Adds an instruction to the queue.
     *
     * @param i The instruction to add.
     */
    public void Enqueue (AMEDAInstruction i)
    {
        _queue.add(i);
    }


    /**
     * Empties out the queue. Used in the case of a failure or cancellation request.
     */
    public void Clear()
    {
        _queue.clear();
        _last_instruction = null;
    }


    /**
     * Advances the instruction pointer to the next item in the queue.
     * Set to null if the queue is empty.
     */
    public void Advance()
    {
        _last_instruction = _queue.poll();
    }


    /**
     * Gets the current instruction to execute.
     *
     * @return The current instruction.
     */
    public AMEDAInstruction Current()
    {
        return _last_instruction;
    }


    /**
     * Does the instruction queue hold any more instructions beyond the current one?
     *
     * @return True if it does, false if it doesn't.
     */
    public boolean HasNext() { return !_queue.isEmpty(); }
}
