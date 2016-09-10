package au.net.nicksifniotis.amedatest.AMEDAManager.VirtualAMEDA;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import au.net.nicksifniotis.amedatest.R;

/**
 * Created by Nick Sifniotis on 10/09/16.
 *
 * A virtual AMEDA that listens to instructions sent out by the app, and responds
 * the same way that the actual AMEDA does.
 *
 * The virtual device does not use the same codebase for interpreting instructions etc
 * as the main app, in order to preserve a certain separation of logic between the two.
 * After all, this app doesn't share code with the *actual* AMEDA device.
 */
public class VirtualDevice implements Runnable, Handler.Callback
{
    private Context _context;                       // A connection to the application context.
    private Messenger _inbound_message_buffer;      // Inbound commands are queued here.
    private Messenger _outbound_message_buffer;     // The virtual device's responses


    /**
     * Constructor!
     *
     * Connect the two Messenger objects to the parent thread as well.
     */
    public VirtualDevice(Context context, Messenger outbound)
    {
        _context = context;
        _outbound_message_buffer = outbound;

        Handler _my_handler = new Handler(this);
        _inbound_message_buffer = new Messenger(_my_handler);
    }


    /**
     * Gets a reference to this device's inbound message handler.
     *
     * @return A Messenger object that the connection can use to communicate with this device.
     */
    public Messenger GetMessenger()
    {
        return _inbound_message_buffer;
    }


    /**
     * Handler for instructions sent through to this virtual device.
     *
     * @param msg The message that we have received from the app.
     * @return True.
     */
    @Override
    public boolean handleMessage(Message msg)
    {
        if (msg.what == 2)
        {
            // A shutdown signal!
            Thread.currentThread().interrupt();
            return true;
        }

        if (msg.what != 1)
            return false;   // I'll only be transmitting messages of type 1 (ordinary) or 2 (shutdown)

        String byte_code = validate_command((String) msg.obj);
        if (byte_code == null)
            return false;   // Communications failure, which should never happen!

        String response = encode_response (generate_response (byte_code));
        if (response == null)
            return true;    // Not every message received requires a response.

        Message message = new Message();
        message.what = 1;
        message.obj = response;

        try
        {
            _outbound_message_buffer.send(message);
        }
        catch (RemoteException e)
        {
            // What the hell is going on? If the outbound buffer has disappeared, it can
            // only mean that the virtual AMEDA connection has gone down.
            // So shut this thing down!

            Thread.currentThread().interrupt();
            return true;
        }

        return true;
    }


    /**
     * Runs this thread, until a request to stop is received from the parent.
     */
    @Override
    public void run()
    {
        while (!Thread.interrupted())
        {
            try
            {
                Thread.sleep (500);
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
        }

        shutdown();
    }


    /**
     * Strip out the checksum stuff and the unnecessary parts of the string.
     * Return either the instruction component of the string, or null if the checksum test failed.
     *
     * @param payload The instruction received from the app.
     * @return The instruction, or NULL if no instruction could be decoded from the payload.
     */
    private String validate_command(String payload)
    {
        if (payload.length() != 8)
            return null;

        if (payload.charAt(0) != '[' || payload.charAt(7) != ']')
            return null;

        String instruction = payload.substring(1, 6);
        int chk = 0;
        for (char c: instruction.toCharArray())
            chk += c;

        return (chk == (int)payload.charAt(6)) ? instruction : null;
    }


    private String generate_response(String instruction_code)
    {
        String res = "";
        if (instruction_code.equals ("HELLO"))
            res = "EHLLO";
        else if (instruction_code.equals("CALHZ"))
            res = "READY";
        else if (instruction_code.substring (0, 4).equals ("BZSH"))
        {
            int num_beeps = Integer.parseInt(instruction_code.substring(4, 5));
            for (int i = 0; i < num_beeps; i ++)
                beep();

            res = "";
        }
        else if (instruction_code.substring (0, 4).equals ("BZLG"))
        {
            int num_beeps = Integer.parseInt(instruction_code.substring(4, 5));
            for (int i = 0; i < num_beeps; i ++)
                beep();

            res = "";
        }
        else if (instruction_code.substring (0, 4).equals ("GOPN"))
            res = "READY";
        else if (instruction_code.equals("RQAGL"))
            res = "A10.0";          // todo implement some logic to remember the stopper position
                                    // todo and return it, for a more realistic simulation.

        try
        {
            Thread.sleep (500);     // not so fast, cowboy!
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }

        return res;
    }


    /**
     * Encodes this instruction into the communications protocol specified by Leon in his
     * technical manual.
     *
     * @param response The instruction to encode.
     * @return The packetised instruction, ready for transmission.
     */
    private String encode_response(String response)
    {
        int chk = 0;
        for (char c: response.toCharArray())
            chk += c;

        return "[" + response + (char)(chk % 256) + "]";
    }


    /**
     * Not much to do really, when I'm asked to shut down.
     * Just make it easier for the garbage collector to do its thing.
     */
    private void shutdown()
    {
        _inbound_message_buffer = null;
        _outbound_message_buffer = null;
    }


    /**
     * Beeps.
     */
    private void beep()
    {
        MediaPlayer mp = MediaPlayer.create(_context, R.raw.virtual_ameda);
        mp.start();
    }
}
