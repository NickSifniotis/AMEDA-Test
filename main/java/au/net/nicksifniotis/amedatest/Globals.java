package au.net.nicksifniotis.amedatest;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;

/**
 * Static global class for storing global config / build options.
 */
public class Globals
{
    /* TRUE if the app is being developed without access to the AMEDA device. */
    public static boolean AMEDA_FREE = true;

    /* TRUE if you want to dump a bunch of debug toasts to the device during execution. */
    public static boolean DEBUG_MODE = true;

    /* TRUE if we want to save the addresses of users in the database. */
    public static boolean USING_ADDRESSES = true;

    /* TRUE if we are testing the app and want to cap test size to 10 questions */
    public static boolean SHORT_TESTS = false;


    /**
     * Displays an error message dialog to the user. Only one button is provided to the user.
     * The calling function is responsible for shutting itself down correctly.
     *
     * @param context The activity to display the dialog in.
     * @param error_message The message to display to the userf.
     */
    public static void Error(Context context, String error_message, DialogInterface.OnClickListener callback)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.error_title)
                .setMessage(error_message)
                .setPositiveButton(R.string.btn_done, callback);
        builder.create().show();
    }
}
