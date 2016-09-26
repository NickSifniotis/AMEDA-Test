package au.net.nicksifniotis.amedatest.Connection.VirtualAMEDA;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import au.net.nicksifniotis.amedatest.Globals;
import au.net.nicksifniotis.amedatest.Messages.ConnectionMessage;
import au.net.nicksifniotis.amedatest.Messages.Messages;
import au.net.nicksifniotis.amedatest.Messages.VirtualAMEDAMessage;
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
    private int              _virtual_position;        // The make-believe position of the device.
    private Messenger        _inbound_message_buffer;  // Inbound commands are queued here.
    private Messenger        _outbound_message_buffer; // The virtual device's responses
    private Context          _context;                 // The context to beep at


    /**
     * Constructor!
     *
     * Connect the two Messenger objects to the parent thread as well.
     */
    public VirtualDevice(Context c, Messenger outbound)
    {
        _context = c;
        _virtual_position = 1;
        _outbound_message_buffer = outbound;

        Globals.DebugToast.Send("Virtual AMEDA constructed");
    }


    /**
     * Gets a reference to this device's inbound message handler.
     *
     * @return A Messenger object that the connection can use to communicate with this device.
     */
    public Messenger GetMessenger()
    {
        Globals.DebugToast.Send("Virtual device surrendering messenger");
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
        Globals.DebugToast.Send ("Virtual AMEDA received " + ConnectionMessage.toString(msg));

        switch (ConnectionMessage.Message(msg))
        {
            case SHUTDOWN:
                // Shut down the virtual device.
                Shutdown();
                break;

            case XMIT:
                // The message is called 'transmit' but on this end it means receive.
                // Go figure.
                String byte_code = validate_command(Messages.GetString(msg));
                if (byte_code == null)
                    return false;   // Communications failure, which should never happen!

                String response = encode_response (generate_response (byte_code));
                if (response == null)
                    return true;    // Not every message received requires a response.

                // Transmit the data that's been received to the connection.
                send(Messages.Create(VirtualAMEDAMessage.INSTRUCTION, response));
                break;

            default:
                // Simply pass on.
                break;
        }
        return true;
    }


    /**
     * Runs this thread, until a request to stop is received from the parent.
     */
    @Override
    public void run()
    {
        Globals.DebugToast.Send("Virtual AMEDA entering run method.");

        Looper.prepare();
        Handler _my_handler = new Handler(this);
        _inbound_message_buffer = new Messenger(_my_handler);

        send(Messages.Create(VirtualAMEDAMessage.MESSENGER_READY));

        Looper.loop();

        Globals.DebugToast.Send("Virtual AMEDA exiting run method.");
        shutdown();
    }


    public void Shutdown()
    {
        if (Looper.myLooper() != null)
            Looper.myLooper().quitSafely();
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

        return ((chk % 256) == (int)payload.charAt(6)) ? instruction : null;
    }


    /**
     * Respond to the instruction that the virtual device has received.
     *
     * @param instruction_code The instruction sent from the app.
     * @return The response, if any, to that instruction.
     */
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
        {
            _virtual_position = Integer.parseInt(instruction_code.substring(4, 5));
            res = "READY";
        }
        else if (instruction_code.equals("RQAGL"))
        {
            String pos = Integer.toString(_virtual_position + 8);
            if (pos.length() == 1)
                pos = "0" + pos;

            res = "A" + pos + ".0";
        }

        try
        {
            Thread.sleep (500);     // not so fast, cowboy! Small delay here to make this
                                    // device seem realistically (non)responsive.
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
        if (response.equals(""))
            return null;

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
     * Safe transmission of message.
     *
     * @param msg The message to send.
     */
    private void send(Message msg)
    {
        try
        {
            _outbound_message_buffer.send (msg);
        }
        catch (RemoteException e)
        {
            // If the connection has been lost somehow, disconnect safely.
            Shutdown();
        }
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
