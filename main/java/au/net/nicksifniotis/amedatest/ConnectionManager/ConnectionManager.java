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

import au.net.nicksifniotis.amedatest.AMEDA.AMEDAResponse;
import au.net.nicksifniotis.amedatest.Connection.AMEDAConnection;
import au.net.nicksifniotis.amedatest.Connection.Connection;
import au.net.nicksifniotis.amedatest.Connection.ConnectionMessage;
import au.net.nicksifniotis.amedatest.Connection.VirtualConnection;
import au.net.nicksifniotis.amedatest.Globals;
import au.net.nicksifniotis.amedatest.R;


/**
 * Created by Nick Sifniotis on 14/09/16.
 *
 * The mid-tier layer that interfaces with implementations of Connection on the one hand,
 * and the application-level API on the other.
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
     * @param callback todo get rid of this hey
     */
    public void UpdateActivity (Activity activity, Handler.Callback callback)
    {
        too_many_variables = activity;
        activity_sent = new Messenger(new Handler(callback));

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

    private void open_virtual ()
    {
        if (DeviceConnection != null)
        {
            // shut down the old one first hey.
            Message msg = new Message();
            msg.what = ConnectionMessage.SHUTDOWN.ordinal();
            send_message(connection_sent, msg);
        }

        // Fire up the connection.
        DeviceConnection = new VirtualConnection(too_many_variables);
        DeviceConnection.UpdateCallback(new Messenger(new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg)
            {
                connection_callback(msg);
                return true;
            }
        })));
        new Thread(DeviceConnection).start();

        Disconnected();
    }


    public void open_bluetooth (String device_name)
    {
        if (DeviceConnection != null)
        {
            // shut down the old one first hey.
            Message msg = new Message();
            msg.what = ConnectionMessage.SHUTDOWN.ordinal();
            send_message(connection_sent, msg);
        }

        // Fire up the connection.
        DeviceConnection = new AMEDAConnection(too_many_variables, device_name);
        DeviceConnection.UpdateCallback(new Messenger(new Handler(new Handler.Callback() {
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
     *
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
        Globals.DebugToast.Send("Manager received message " + m.what + " from device.");

        ConnectionMessage msg = ConnectionMessage.values()[m.what];

        Message new_message;
        switch (msg)
        {
            case RCVD:
                AMEDAResponse response = (AMEDAResponse) m.obj;
                if (response.GetCode() == AMEDAResponse.Code.UNKNOWN_COMMAND)
                    Globals.DebugToast.Send("Unknown response received from AMEDA: " + response.toString());
//                else if (response.GetCode() == AMEDAResponse.Code.READY)
//                {
//                  todo implement EHLLO
//                }
                else
                {
                    new_message = new Message();
                    new_message.what = ManagerMessages.RECEIVE.ordinal();
                    new_message.obj = m.obj;
                    send_message(activity_sent, new_message);
                }
                break;
            case MESSENGER_READY:
                connection_sent = DeviceConnection.get_connection();

                new_message = new Message();
                new_message.what = ConnectionMessage.CONNECT.ordinal();
                send_message(connection_sent, new_message);

                break;
            case CONNECTED:
                new_message = new Message();
                new_message.what = ManagerMessages.CONNECTION_RESTORED.ordinal();
                new_message.obj = m.obj;
                send_message(activity_sent, new_message);

                Connected();
                break;
            case DISCONNECTED:
                new_message = new Message();
                new_message.what = ManagerMessages.CONNECTION_DROPPED.ordinal();
                new_message.obj = m.obj;
                send_message(activity_sent, new_message);

                Disconnected();
                break;

            case CONNECT_FAILED:
                _connect_progress.dismiss();
                Globals.DebugToast.Send ("Connection attempt failed. Please try again.");
                Disconnected();
                break;

            case CONNECT:
            case SHUTDOWN:
            case XMIT:
                Globals.DebugToast.Send("Erroneous message received in master callback function: " + msg.toString());
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
            Message new_msg = new Message();
            new_msg.what = ConnectionMessage.XMIT.ordinal();
            new_msg.obj = msg.obj;

            send_message(connection_sent, new_msg);
        }
        else
            Globals.DebugToast.Send ("Cannot transmit packet at this time. Please try reconnecting.");
    }


    public void RefreshLamp()
    {
        ConnectionLamp.setImageDrawable((Connected) ? green : red);
    }


    public void Connected()
    {
        if (_connect_progress != null)
            _connect_progress.dismiss();

        Connected = true;
        ConnectionLamp.setImageDrawable(green);
    }


    public void Disconnected()
    {
        Connected = false;
        ConnectionLamp.setImageDrawable(red);
    }


    public void Disconnect()
    {
        if (!Connected)
            return;

        if (connection_sent == null)
            return;

        Message msg = new Message();
        msg.what = ConnectionMessage.DISCONNECT.ordinal();
        send_message(connection_sent, msg);
    }


    /**
     * Safe transmission of a message through the given messenger object.
     *
     * @param stream The messenger to send to.
     * @param m The message to send.
     */
    private void send_message (Messenger stream, Message m)
    {
        try
        {
            stream.send(m);
        }
        catch (RemoteException e)
        {
            Globals.DebugToast.Send ("Error transmitting a message within ConnectionManager.");
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
