package au.net.nicksifniotis.amedatest;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import au.net.nicksifniotis.amedatest.ConnectionManager.ConnectionManager;
import au.net.nicksifniotis.amedatest.activities.HomeActivity;


/**
 * Static global class for storing global config / build options.
 */
public class Globals
{
    /* TRUE if you want to dump a bunch of debug toasts to the device during execution. */
    public static volatile boolean DEBUG_MODE = false;

    /* TRUE if we want to save the addresses of users in the database. */
    public static boolean USING_ADDRESSES = true;

    /* TRUE if we are testing the app and want to cap test size to 10 questions */
    public static boolean SHORT_TESTS = false;


    /* Services! */
    public static DebugToastService DebugToast;
    public static ConnectionManager ConnectionManager;


    /**
     * Start up the life-of-app services.
     *
     * - DebugToast service :  For debugging by printf ;)
     * - ConnectionManager  :  Service that manages the bluetooth connection to the device.
     *
     * @param base_activity The 'home' activity of the application.
     */
    public static void InitialiseServices (final HomeActivity base_activity)
    {
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

        ConnectionManager = new ConnectionManager(base_activity);
        new Thread(ConnectionManager).start();
    }


    /**
     * Shut down the service threads; we don't need em no more.
     */
    public static void TerminateServices()
    {
        DebugToast.Shutdown();
        ConnectionManager.Shutdown();
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
     * Displays an alert / non-fatal error message to the user. Only one button is provided
     * to the user, to dismiss the notification.
     *
     * @param activity The activity to display the notification in.
     * @param alert_message The message to display to the user.
     */
    public static void Alert(final Activity activity, String alert_message)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.error_title)
                .setMessage(alert_message)
                .setPositiveButton(R.string.btn_done, null);
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
     * I'd be lying if I said I understood this method. It compares two rows in the score
     * matrix and returns a value (between zero and one) that represents something about the
     * data in that matrix.
     *
     * It's been tested against the Excel spreadsheet data and produces the correct results.
     *
     * @param matrix The matrix containing the score data.
     * @param row1 The first row in the matrix to calculate.
     * @param row2 The second row in the matrix to calculate.
     * @return The AUC calculation, whatever that is.
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
     * @param correct The correct answers to each question in the test.
     * @param responses The answers provided by the user to each question in the test.
     * @return A matrix containing the score data for this test.
     */
    private static int [] [] _get_matrix(int [] correct, int [] responses)
    {
        int [] [] result_matrix = new int [6] [6];

        for (int i = 0, j = correct.length; i < j; i ++)
            result_matrix[correct[i]][responses[i]] ++;

        return result_matrix;
    }
}
