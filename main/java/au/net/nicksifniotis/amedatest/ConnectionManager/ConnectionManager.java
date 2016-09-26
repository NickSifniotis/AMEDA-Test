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
 * The connection manager takes care of packing and unpacking AMEDA instructions into a format that
 * can be transmitted to the devices. Top-level activities cannot see the instructions at all,
 * the commands are codified into the messaging system itself.
 */
public class ConnectionManager implements Runnable
{
    // connection data
    public boolean Connected;
    private Connection DeviceConnection;
    private ProgressDialog _connect_progress;
    private ImageView ConnectionLamp;
    private Activity too_many_variables;

    public Messenger activity_sent;
    public Messenger activity_received;
    public Messenger connection_sent;
    public Messenger connection_received;

    private Heartbeat heart;

    public Drawable green;
    public Drawable yellow;
    public Drawable red;


    // message passing mapping

    // me, myself and I
    private volatile boolean _alive;


    public ConnectionManager(Activity base_activity)
    {
        green  = base_activity.getResources().getDrawable
                (R.drawable.liveness_green , base_activity.getTheme());
        yellow = base_activity.getResources().getDrawable
                (R.drawable.liveness_yellow, base_activity.getTheme());
        red    = base_activity.getResources().getDrawable
                (R.drawable.liveness_red   , base_activity.getTheme());


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
    }


    /**
     * Update the reference to the current activity.
     *
     * UI interactions have to be run through an activity. Since this object exists for the
     * lifetime of the application, it needs to be notified whenever a child activity is launced.
     * Otherwise, what happens is that a child activity might be active, but messages or UI
     * requests are being sent back to the parent - which may have stopped.
     *
     * todo make all activities in this app children of AMEDAActivity, which will implement a callback
     *
     * @param activity The activity to interact with.
     */
    public Messenger UpdateActivity (final AMEDAActivity activity)
    {
        too_many_variables = activity;
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
                activity.handleManagerMessage(msg);
                return true;
            }
        }));

        ConnectionLamp = (ImageView)activity.findViewById(R.id.heartbeat_liveness);
        ConnectionLamp.setOnClickListener(new View.OnClickListener()
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


    public void onLampClick()
    {
        Globals.DebugToast.Send("Lamp onclick triggered! connection status is " + Connected);
        if (Connected)
            Disconnect();
        else
            SelectDeviceToConnect(too_many_variables);
    }


    @Override
    public void run() {
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
        if (DeviceConnection != null)
            send_connection(Messages.Create(ManagerMessage.SHUTDOWN));

        // Fire up the connection.
        DeviceConnection = new VirtualConnection(too_many_variables);
        DeviceConnection.UpdateCallback(new Messenger(new Handler(new Handler.Callback()
        {
            @Override
            public boolean handleMessage(Message msg)
            {
                connection_callback(msg);
                return true;
            }
        })));
        new Thread(DeviceConnection).start();
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
        if (DeviceConnection != null)
            send_connection(Messages.Create(ManagerMessage.SHUTDOWN));

        // Fire up the connection.
        DeviceConnection = new AMEDAConnection(too_many_variables, device_name);
        DeviceConnection.UpdateCallback(new Messenger(new Handler(new Handler.Callback()
        {
            @Override
            public boolean handleMessage(Message msg)
            {
                connection_callback(msg);
                return true;
            }
        })));
        new Thread(DeviceConnection).start();
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

        _connect_progress = new ProgressDialog(too_many_variables);
        _connect_progress.setTitle(too_many_variables.getString(R.string.connecting_title));
        _connect_progress.setMessage(too_many_variables.getString(R.string.connecting_desc));
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
                AMEDAResponse.Code code = response.GetCode();

                switch (code)
                {
                    case READY:
                        send_activity(Messages.Create(ManagerMessage.READY));
                        break;

                    case EHLLO:
                        heart_diastole();
                        break;

                    case UNKNOWN_COMMAND:
                        Globals.DebugToast.Send("Unknown response received from AMEDA: " + response.toString());
                        break;

                    case CANNOT_MOVE:
                        send_activity(Messages.Create(ManagerMessage.CANNOT_MOVE));
                        break;

                    case NO_RESPONSE_ANGLE:
                        send_activity(Messages.Create(ManagerMessage.NO_RESPONSE_ANGLE));
                        break;

                    case CALIBRATION_FAIL:
                        send_activity(Messages.Create(ManagerMessage.CALIBRATION_FAIL));
                        break;

                    case WOBBLE_NO_RESPONSE:
                        send_activity(Messages.Create(ManagerMessage.WOBBLE_NO_RESPONSE));
                        break;

                    case ANGLE:
                        send_activity(Messages.Create(ManagerMessage.ANGLE));
                        break;
                }

                break;

            case MESSENGER_READY:
                // Grab the connection object's communications line, and then instruct it
                // to try and connect to its device.
                connection_sent = DeviceConnection.get_connection();

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
                _connect_progress.dismiss();
                Globals.DebugToast.Send ("Connection attempt failed. Please try again.");
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
        ConnectionLamp.setImageDrawable(yellow);
    }


    /**
     * Ping received.
     */
    private void heart_diastole()
    {
        ConnectionLamp.setImageDrawable(green);
    }


    public void RefreshLamp()
    {
        ConnectionLamp.setImageDrawable((Connected) ? green : red);
    }


    /**
     * Received confirmation that we are connected to something!
     */
    public void Connected()
    {
        if (_connect_progress != null)
            _connect_progress.dismiss();

        Connected = true;
        ConnectionLamp.setImageDrawable(green);

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
        ConnectionLamp.setImageDrawable(red);

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
