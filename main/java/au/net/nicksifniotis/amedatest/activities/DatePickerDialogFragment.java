package au.net.nicksifniotis.amedatest.activities;

/**
 * Date Picker fragment for use in the 'select DOB' field (and possibly others in due course)
 *
 * Created by Nick Sifniotis on 10/09/16 with thanks to StackOverflow.
 */
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.DatePicker;
import android.widget.TextView;


public class DatePickerDialogFragment extends DialogFragment implements OnDateSetListener
{
    private TextView _dob_text;


    public DatePickerDialogFragment()
    {
        // nothing to see here, move along
    }


    /**
     * Use a factory method to instantiate this fragment.
     *
     * @param text The TextView GUI element that contains the selected date.
     * @return A new dialog fragment.
     */
    public static DatePickerDialogFragment newInstance(TextView text)
    {
        DatePickerDialogFragment fragment = new DatePickerDialogFragment();
        fragment._dob_text = text;

        return fragment;
    }


    /**
     * Create the fragment.
     *
     * @param savedInstanceState Not used.
     * @return A new dialog to display to the user.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Calendar cal = Calendar.getInstance();
        int starting_year = cal.get(Calendar.YEAR);
        int starting_month = cal.get(Calendar.MONTH);
        int starting_day = cal.get(Calendar.DAY_OF_MONTH);

        String [] parts = _dob_text.getText().toString().split("/");
        if (tryParseInt(parts[0]) > 0)
            starting_day = tryParseInt(parts[0]);
        if (parts.length > 1 && tryParseInt(parts[1]) > 0)
            starting_month = tryParseInt(parts[1]) - 1;
        if (parts.length > 2 && tryParseInt(parts[2]) > 0)
            starting_year = tryParseInt(parts[2]);

        return new DatePickerDialog(getActivity(), this, starting_year, starting_month, starting_day);
    }


    /**
     * Callback method to handle the setting of a new date.
     *
     * @param view Probs not used.
     * @param year Should be self evident.
     * @param monthOfYear Likewise.
     * @param dayOfMonth Once more.
     */
    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
    {
        Calendar cal = new GregorianCalendar(year, monthOfYear, dayOfMonth);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        _dob_text.setText(sdf.format(cal.getTime()));
    }


    /**
     * Trust Java to make this method necessary!!
     *
     * @param value The string to try and convert to an integer.
     * @return An integer, or zero if the string is unconvertable.
     */
    private int tryParseInt(String value)
    {
        try
        {
            return Integer.parseInt(value);
        }
        catch(NumberFormatException nfe)
        {
            return 0;
        }
    }
}