package au.net.nicksifniotis.amedatest.activities;

/**
 * Date Picker fragment for use in the 'select DOB' field (and possibly others in due course)
 *
 * Created by Nick Sifniotis on 10/09/16 with thanks to StackOverflow.
 */
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

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
        String [] parts = _dob_text.getText().toString().split("/");
        int starting_year = Integer.parseInt(parts[2]);
        int starting_month = Integer.parseInt(parts[1]);
        int starting_day = Integer.parseInt(parts[0]);

        Calendar cal = Calendar.getInstance();
        starting_year = (starting_year == 0) ? cal.get(Calendar.YEAR) : starting_year;
        starting_month = (starting_month == 0) ? cal.get(Calendar.MONTH) : starting_month - 1;
        starting_day = (starting_day == 0) ? cal.get(Calendar.DAY_OF_MONTH) : starting_day;

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
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy"); // todo improve date local stuff
        _dob_text.setText(sdf.format(cal.getTime()));
    }
}