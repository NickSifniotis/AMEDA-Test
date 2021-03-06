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
 * RecordCursorAdaptor structure for the Test database table.
 *
 */
public class Test_RCA extends CursorAdapter
{
    /**
     * Default constructor.
     *
     * @param context The ListView that this RCA will populate.
     */
    public Test_RCA(Context context)
    {
        super(context, null, 0);
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
        return LayoutInflater.from(context).inflate(R.layout.test_rca_layout, parent, false);
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
        TextView txt_test_date = (TextView)view.findViewById(R.id.test_rca_date);
        TextView txt_test_score = (TextView)view.findViewById(R.id.test_rca_score);

        long d = cursor.getLong(cursor.getColumnIndexOrThrow(DB.TestTable.DATE));
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        String date = format.format(new Date(d));

        double score = cursor.getDouble(cursor.getColumnIndex(DB.TestTable.SCORE));
        int interrupted = cursor.getInt(cursor.getColumnIndex(DB.TestTable.INTERRUPTED));

        String score_text = (interrupted == 1)
                ? context.getString(R.string.test_rca_incomplete)
                : context.getString(R.string.test_rca_score_template, score);
        ///String score_text = context.getString(R.string.test_rca_score_template, score);

        txt_test_date.setText(date);
        txt_test_score.setText(score_text);
    }
}
