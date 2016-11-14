package au.net.nicksifniotis.amedatest.ConnectionManager;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import au.net.nicksifniotis.amedatest.AMEDA.AMEDAInstruction;
import au.net.nicksifniotis.amedatest.AMEDA.AMEDAInstructionEnum;
import au.net.nicksifniotis.amedatest.AMEDA.AMEDAResponse;
import au.net.nicksifniotis.amedatest.Connection.AMEDAConnection;
import au.net.nicksifniotis.amedatest.Connection.Connection;
import au.net.nicksifniotis.amedatest.Connection.VirtualConnection;
import au.net.nicksifniotis.amedatest.Globals;
import au.net.nicksifniotis.amedatest.Messages.ActivityMessage;
import au.net.nicksifniotis.amedatest.Messages.ConnectionMessage;
import au.net.nicksifniotis.amedatest.Messages.ManagerMessage;
import au.net.nicksifniotis.amedatest.Messages.Messages;
import au.net.nicksifniotis.amedatest.R;
import au.net.nicksifniotis.amedatest.activities.AMEDAActivity;


/**
 * Created by Nick Sifniotis on 14/09/16.
 *
 * The mid-tier layer that interfaces with implementations of Connection on the one hand,
 * and the application-level API on the other.
 *
 */
public class ConnectionManager implements Runnable
{
    // connection data
    public  boolean        Connected;
    private Connection     _device_connection;
    private ProgressDialog _connect_progress;
    private ImageView      _connection_lamp;
    private Activity       _current_activity;

    private Messenger activity_sent;
    private Messenger activity_received;
    private Messenger connection_sent;
    private Messenger connection_received;

    private Heartbeat heart;

    private Drawable green;
    private Drawable yellow;
    private Drawable red;

    private volatile boolean _alive;


    public ConnectionManager(Activity base_activity)
    {
        green  = base_activity.getResources().getDrawable
                (R.drawable.green_connect , base_activity.getTheme());
        yellow = base_activity.getResources().getDrawable
                (R.drawable.green_pump, base_activity.getTheme());
        red    = base_activity.getResources().getDrawable
                (R.drawable.red_disconnect   , base_activity.getTheme());


        activity_received = new Messenger(new Handler(new Handler.Callback()
        {
            /**
             * Simple method to call the callback function for messages received from Activities.
             *
             * @param msg The message received.
             * @return True, always.
             */
            @Override
            public boolean handleMessage(Message msg)
            {
                activity_callback(msg);
                return true;
            }
        }));


        connection_received = new Messenger(new Handler(new Handler.Callback()
        {
            /**
             * Simple method to call the callback function for Connection messages.
             *
             * @param msg The message received.
             * @return True, always.
             */
            @Override
            public boolean handleMessage(Message msg)
            {
                connection_callback(msg);
                return true;
            }
        }));
    }


    /**
     * Update the reference to the current activity.
     *
     * UI interactions have to be run through an activity. Since this object exists for the
     * lifetime of the application, it needs to be notified whenever a child activity is launced.
     * Otherwise, what happens is that a child activity might be active, but messages or UI
     * requests are being sent back to the parent - which may have stopped.
     *
     * @param activity The activity to interact with.
     */
    public Messenger UpdateActivity (final AMEDAActivity activity)
    {
        _current_activity = activity;
        activity_sent = new Messenger(new Handler(new Handler.Callback()
        {
            /**
             * Simple event handler to call the callback function.
             *
             * @param msg The message received from this connection manager.
             * @return True, always.
             */
            @Override
            public boolean handleMessage(Message msg)
            {
                activity.HandleManagerMessage(msg);
                return true;
            }
        }));

        _connection_lamp = (ImageView)activity.findViewById(R.id.heartbeat_liveness);
        _connection_lamp.setOnClickListener(new View.OnClickListener()
        {
            /**
             * Simple method to call the onClick event handler.
             *
             * @param v Not used!
             */
            public void onClick(View v)
            {
                onLampClick();
            }
        });
        RefreshLamp();

        return activity_received;
    }


    /**
     * Terminate this service!
     */
    public void Shutdown()
    {
        Disconnect();
        _alive = false;
    }


    /**
     * Callback function that handles requests to display the connection status dialog box.
     */
    public void onLampClick()
    {
        if (Connected)
            Disconnect();
        else
            SelectDeviceToConnect(_current_activity);
    }


    /**
     * The main loop for this service.
     */
    @Override
    public void run()
    {
        _alive = true;

        while (_alive)
        {
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                _alive = false;
            }
        }
    }


    /**
     * Opens up a new connection to the virtual device.
     */
    private void open_virtual ()
    {
        if (_device_connection != null)
            send_connection(Messages.Create(ManagerMessage.SHUTDOWN));

        // Fire up the connection.
        _device_connection = new VirtualConnection(_current_activity, Globals.BUGGY_CONNECTIONS);
        _device_connection.UpdateCallback(connection_received);

        new Thread(_device_connection).start();
        show_progress_dialog();
        Disconnected();
    }


    /**
     * Open up a new connection via the Bluetooth interface.
     *
     * @param device_name The name of the device to try and open a connection with.
     */
    public void open_bluetooth (String device_name)
    {
        if (_device_connection != null)
            send_connection(Messages.Create(ManagerMessage.SHUTDOWN));

        // Fire up the connection.
        _device_connection = new AMEDAConnection(_current_activity, device_name);
        _device_connection.UpdateCallback(connection_received);

        new Thread(_device_connection).start();
        show_progress_dialog();
        Disconnected();
    }


    /**
     * Opens up the 'Connecting ...' dialog box while the connection is being made.
     */
    private void show_progress_dialog()
    {
        if (_connect_progress != null)
            _connect_progress.dismiss();

        _connect_progress = new ProgressDialog(_current_activity);
        _connect_progress.setTitle(_current_activity.getString(R.string.connecting_title));
        _connect_progress.setMessage(_current_activity.getString(R.string.connecting_desc));
        _connect_progress.setCancelable(false);
        _connect_progress.show();
    }


    /**
     * The master callback, that filters out heartbeat and connection related messages.
     * Passes through other messages to whichever activity is the registered callback receiver.
     *
     * @param m The message received.
     */
    private void connection_callback (Message m)
    {
        Globals.DebugToast.Send("Manager received "
                + ConnectionMessage.toString(m) + " from device.");

        switch (ConnectionMessage.Message(m))
        {
            case RCVD:
                // Data has been received from the AMEDA. Process it, and forward on to
                // the right place.
                AMEDAResponse response = Messages.GetResponse(m);

                if (response.GetCode() == AMEDAResponse.Code.UNKNOWN_COMMAND)
                    Globals.DebugToast.Send("Unknown response received from AMEDA: " + response.toString());
                else if (response.GetCode() == AMEDAResponse.Code.EHLLO)
                    heart_diastole();
                else
                    send_activity(Messages.Create(ManagerMessage.RCVD, response));
                break;

            case MESSENGER_READY:
                // Grab the connection object's communications line, and then instruct it
                // to try and connect to its device.
                connection_sent = _device_connection.get_connection();

                send_connection (Messages.Create (ManagerMessage.CONNECT));
                break;

            case CONNECTED:
                // Inform the activity that the connection has been established.
                send_activity (Messages.Create (ManagerMessage.CONNECTION_RESUMED));
                Connected();
                break;

            case DISCONNECTED:
                // Inform the activity that the connection is lost ...
                send_activity (Messages.Create (ManagerMessage.CONNECTION_DROPPED));
                Disconnected();
                break;

            case CONNECT_FAILED:
                // Need to inform the user that the connection was unsuccessful.
                _connect_progress.dismiss();
                Globals.Alert (_current_activity, "Connection attempt failed. Please try again.");
                Disconnected();
                break;

            default:
                break;
        }
    }


    /**
     * Callback function for messages received from activities.
     *
     * @param msg The message that has been received.
     */
    public void activity_callback (Message msg)
    {
        if (Connected)
        {
            switch (ActivityMessage.Message(msg))
            {
                case SEND:
                    AMEDAInstruction instruction = Messages.GetInstruction(msg);
                    if (instruction.GetInstruction() == AMEDAInstructionEnum.HELLO)
                        heart_systole();

                    send_connection(Messages.Create(ManagerMessage.XMIT, instruction));
                    break;

                default:
                    break;
            }
        }
        else
            Globals.DebugToast.Send ("Cannot transmit packet at this time. Please try reconnecting.");
    }


    /**
     * Ping sent!
     */
    private void heart_systole()
    {
        _connection_lamp.setImageDrawable(yellow);
    }


    /**
     * Ping received.
     */
    private void heart_diastole()
    {
        _connection_lamp.setImageDrawable(green);
    }


    public void RefreshLamp()
    {
        _connection_lamp.setImageDrawable((Connected) ? green : red);
    }


    /**
     * Received confirmation that we are connected to something!
     */
    public void Connected()
    {
        if (_connect_progress != null)
            _connect_progress.dismiss();

        Connected = true;
        _connection_lamp.setImageDrawable(green);

        // fire up a heartbeat thread!
        heart = new Heartbeat(activity_received);
        new Thread(heart).start();
    }


    /**
     * Connection lost! So kill off any hearts that might be alive.
     */
    public void Disconnected()
    {
        Connected = false;
        _connection_lamp.setImageDrawable(red);

        if (heart != null)
            heart.die();
        heart = null;
    }


    /**
     * Send a message to the connection requesting a disconnection.
     */
    public void Disconnect()
    {
        if (!Connected)
            return;

        if (connection_sent == null)
            return;

        send_connection(Messages.Create (ManagerMessage.DISCONNECT));
    }


    /**
     * Safe transmission of a message to the current connection object.
     *
     * @param msg The message to transmit.
     */
    private void send_connection(Message msg)
    {
        try
        {
            connection_sent.send (msg);
        }
        catch (RemoteException e)
        {
            // disconnected for some reason.
            Globals.DebugToast.Send ("Error transmitting a message to connection within ConnectionManager.");
        }
    }


    /**
     * Safe transmission of a message to the current listening activity.
     *
     * -@param msg The message to send.
     */
    private void send_activity(Message msg)
    {
        try
        {
            activity_sent.send (msg);
        }
        catch (RemoteException e)
        {
            // disconnected for some reason.
            Globals.DebugToast.Send ("Error transmitting a message to activity within ConnectionManager.");
        }
    }


    public void SelectDeviceToConnect (final Activity activity)
    {
        List<BluetoothDevice> pairedDevices = new ArrayList<>(BluetoothAdapter
                .getDefaultAdapter().getBondedDevices());
        List<String> list_names = new LinkedList<>();
        list_names.add("Virtual Device");

        for (BluetoothDevice d: pairedDevices)
            list_names.add (d.getName().trim());

        final Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.bluetooth_selector);

        final ListView lv = (ListView) dialog.findViewById(R.id.bt_sel_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity,
                android.R.layout.simple_list_item_1, list_names);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0)
                {
                    // open up a new virtual connection.
                    open_virtual();
                }
                else
                {
                    // go for the real thing!
                    String device_selected = parent.getAdapter().getItem(position).toString();
                    open_bluetooth(device_selected);
                }

                dialog.dismiss();
            }
        });

        dialog.setCancelable(true);
        dialog.setTitle("Select Device");

        dialog.show();
    }
}
