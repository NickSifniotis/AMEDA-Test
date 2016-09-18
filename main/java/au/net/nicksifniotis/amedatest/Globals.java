package au.net.nicksifniotis.amedatest;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AlertDialog;
import android.widget.ImageView;
import android.widget.Toast;

import au.net.nicksifniotis.amedatest.AMEDA.AMEDAInstruction;
import au.net.nicksifniotis.amedatest.AMEDA.AMEDAResponse;
import au.net.nicksifniotis.amedatest.Connection.AMEDAConnection;
import au.net.nicksifniotis.amedatest.Connection.Connection;
import au.net.nicksifniotis.amedatest.Connection.ConnectionMessage;
import au.net.nicksifniotis.amedatest.Connection.VirtualConnection;
import au.net.nicksifniotis.amedatest.ConnectionManager.ManagerMessages;


/**
 * Static global class for storing global config / build options.
 */
public class Globals
{
    /* TRUE if the app is being developed without access to the AMEDA device. */
    public static boolean AMEDA_FREE = true;

    /* TRUE if you want to dump a bunch of debug toasts to the device during execution. */
    public static volatile boolean DEBUG_MODE = true;

    /* TRUE if we want to save the addresses of users in the database. */
    public static boolean USING_ADDRESSES = true;

    /* TRUE if we are testing the app and want to cap test size to 10 questions */
    public static boolean SHORT_TESTS = false;


    /* Services! */
    public static DebugToastService DebugToast;
    public static Connection DeviceConnection;
    private static Messenger _data_sent;
    public static boolean Connected;
    public static ImageView ConnectionLamp;
    public static Messenger activity_sent;
    public static Messenger activity_received;

    public static Drawable green;
    public static Drawable yellow;
    public static Drawable red;


    public static void InitialiseServices (final HomeActivity base_activity)
    {
        green  = base_activity.getResources().getDrawable
                (R.drawable.liveness_green , base_activity.getTheme());
        yellow = base_activity.getResources().getDrawable
                (R.drawable.liveness_yellow, base_activity.getTheme());
        red    = base_activity.getResources().getDrawable
                (R.drawable.liveness_red   , base_activity.getTheme());

        Messenger debug_messenger = new Messenger(new Handler(new Handler.Callback()
        {
            @Override
            public boolean handleMessage(Message msg)
            {
                final String m = (String) msg.obj;
                base_activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Toast t = Toast.makeText(base_activity, m, Toast.LENGTH_SHORT);
                        t.show();
                    }
                });
                return true;
            }
        }));
        DebugToast = new DebugToastService(debug_messenger);

        new Thread(DebugToast).start();


        // Fire up the connection.
        DeviceConnection = (Globals.AMEDA_FREE) ?
                new VirtualConnection(base_activity) :
                new AMEDAConnection(base_activity);
        DeviceConnection.UpdateCallback(new Messenger(new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg)
            {
                ManagerCallback(msg);
                return true;
            }
        })));
        new Thread(DeviceConnection).start();

        Disconnected();


        activity_received = new Messenger(new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (Connected) {
                    Message new_msg = new Message();
                    new_msg.what = ConnectionMessage.XMIT.ordinal();
                    new_msg.obj = msg.obj;

                    send_connection(new_msg);
                }
                return true;
            }
        }));
    }


    /**
     * The master callback, that filters out heartbeat and connection related messages.
     * Passes through other messages to whichever activity is the registered callback receiver.
     *
     * @param m The message received.
     */
    public static void ManagerCallback (Message m)
    {
        DebugToast.Send("Manager received message " + m.what + " from device.");

        ConnectionMessage msg = ConnectionMessage.values()[m.what];

        switch (msg)
        {
            case RCVD:
                AMEDAResponse response = (AMEDAResponse) m.obj;
                if (response.GetCode() == AMEDAResponse.Code.UNKNOWN_COMMAND)
                    DebugToast.Send("Unknown response received from AMEDA: " + response.toString());
//                else if (response.GetCode() == AMEDAResponse.Code.READY)
//                {
//                  todo implement EHLLO
//                }
                else
                {
                    Message new_message = new Message();
                    new_message.what = ManagerMessages.RECEIVE.ordinal();
                    new_message.obj = m.obj;
                    try
                    {
                        activity_sent.send(new_message);
                    }
                    catch (RemoteException e)
                    {
                        // fhlkjh
                    }
                }
                break;
            case MESSENGER_READY:
                _data_sent = DeviceConnection.get_connection();
                break;
            case CONNECTED:
                Connected();
                break;
            case DISCONNECTED:
                Disconnected();
                break;
            case CONNECT:
            case SHUTDOWN:
            case XMIT:
                DebugToast.Send("Erroneous message received in master callback function: " + msg.toString());
                break;
        }
    }


    public static void onLampClick ()
    {
        DebugToast.Send("Lamp onclick triggered! connection status is " + Connected);
        if (Connected)
            Disconnect();
        else
            Connect();
    }


    public static void TerminateServices()
    {
        DebugToast.Shutdown();
        DeviceConnection.Shutdown();
    }


    public static void Connected()
    {
        Connected = true;
        ConnectionLamp.setImageDrawable(green);
    }


    public static void Disconnected()
    {
        Connected = false;
        ConnectionLamp.setImageDrawable(red);
    }


    public static void RefreshLamp()
    {
        ConnectionLamp.setImageDrawable((Connected) ? green : red);
    }


    /** Open up a connection to the device **/
    public static void Connect()
    {
        if (Connected)
            return;

        if (_data_sent == null)     // the connection has not signalled that it is ready yet.
            return;

        Message msg = new Message();
        msg.what = ConnectionMessage.CONNECT.ordinal();
        send_connection(msg);
    }


    /**
     * Ask the device to disconnect!
     */
    public static void Disconnect()
    {
        if (!Connected)
            return;

        if (_data_sent == null)
            return;

        Message msg = new Message();
        msg.what = ConnectionMessage.DISCONNECT.ordinal();
        send_connection(msg);
    }


    private static void send_connection (Message m)
    {
        try
        {
            _data_sent.send(m);
        }
        catch (RemoteException e)
        {
            ///lkdfhgdkjh
        }
    }


    public static void SetCallback(Handler.Callback callback)
    {
        activity_sent = new Messenger(new Handler(callback));
    }


    /**
     * Displays an error message dialog to the user. Only one button is provided to the user.
     * The calling function is responsible for shutting itself down correctly.
     *
     * @param activity The activity to display the dialog in.
     * @param error_message The message to display to the user.
     */
    public static void Error(final Activity activity, String error_message)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.error_title)
                .setMessage(error_message)
                .setPositiveButton(R.string.btn_done, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        activity.finish();
                    }
                });
        builder.create().show();
    }


    /**
     * Scores a recorded test. The score is something called a 'mean AUC', which I have had to
     * reverse engineer from undocumented Excel spreadsheet formulae.
     *
     * @param correct The set of correct responses to the test.
     * @param responses The responses as recorded by the user.
     * @return The 'mean AUC' for the test.
     */
    public static double ScoreTest(int [] correct, int [] responses)
    {
        double res = 0.0;
        int [] [] matrix = _get_matrix(correct, responses);

        for (int i = 1; i < 5; i ++)
            res += _get_auc(matrix, i, i + 1);

        return res / 4;
    }


    /**
     * I'd be lying if I said I understood this method.
     *
     * It's been tested against the Excel spreadsheet data and produces the correct results.
     *
     * @param matrix todo
     * @param row1
     * @param row2
     * @return
     */
    private static double _get_auc(int [] [] matrix, int row1, int row2)
    {
        double res = 0.0;
        int t_r1 = 0;
        int t_r2 = 0;

        for (int i = 1; i <= 5; i ++)
        {
            res += (matrix[row1][i] * matrix[row2][i]);
            t_r1 += matrix[row1][i];
            t_r2 += matrix[row2][i];
        }
        res /= 2;

        for (int i = 2; i <= 5; i ++)
        {
            int temp = 0;
            for (int j = 1; j < i; j ++)
                temp += matrix[row1][j];

            res += (temp * matrix[row2][i]);
        }

        return res / (t_r1 * t_r2);
    }


    /**
     * Generates the matrix used in the calculation of the mean AUC.
     *
     * @param correct todo
     * @param responses todo
     * @return todo
     */
    private static int [] [] _get_matrix(int [] correct, int [] responses)
    {
        int [] [] result_matrix = new int [6] [6];

        for (int i = 0, j = correct.length; i < j; i ++)
            result_matrix[correct[i]][responses[i]] ++;

        return result_matrix;
    }
}
