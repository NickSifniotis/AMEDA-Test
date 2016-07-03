package au.net.nicksifniotis.amedatest.LocalDB;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import au.net.nicksifniotis.amedatest.R;

/**
 * RecordCursorAdapter object for the User database table.
 *
 */
public class User_RCA extends CursorAdapter
{
    /**
     * Default constructor.
     *
     * @param context The ListView that this RCA will populate.
     * @param cursor The cursor pointing to the dataset to draw from.
     */
    public User_RCA(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }


    /**
     * I'm actually not sure what this method does.
     *
     * @param context The ListView to populate.
     * @param cursor The cursor pointing to the data. Wait up, this was passed through the constructor?
     * @param parent The Activity housing the ListView.
     * @return Returns an inflated Layout view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        return LayoutInflater.from(context).inflate(R.layout.record_list_layout, parent, false);
    }


    /**
     * Populates one row of the ListView with the dataset currently pointed to by the cursor.
     *
     * @param view The view describing one row of the dataset. These go into the ListView.
     * @param context The ListView itself? I'm not sure, it isn't used.
     * @param cursor The dataset to read from.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor)
    {
        TextView tv_Name = (TextView)view.findViewById(R.id.record_date);
        TextView tv_Date = (TextView)view.findViewById(R.id.record_score);

        String name = cursor.getString(cursor.getColumnIndexOrThrow(DB.PersonTable.NAME));
        long d = cursor.getLong(cursor.getColumnIndexOrThrow("test_count"));
        String date;

        if (d == 0)
            date = "< none >";
        else
        {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
            date = format.format(new Date(d));
        }

        tv_Name.setText(name);
        tv_Date.setText(date);
    }
}
