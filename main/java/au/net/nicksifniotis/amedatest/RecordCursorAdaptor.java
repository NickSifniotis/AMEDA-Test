package au.net.nicksifniotis.amedatest;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * Created by nsifniotis on 23/05/16.
 */
public class RecordCursorAdaptor extends CursorAdapter
{
    public RecordCursorAdaptor(Context context, Cursor cursor, int flags) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.record_list_layout, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tv_Name = (TextView)view.findViewById(R.id.record_name);
        TextView tv_Date = (TextView)view.findViewById(R.id.record_date);

        String name = cursor.getString(cursor.getColumnIndexOrThrow(DB.PersonTable.NAME));
        String date = cursor.getString(cursor.getColumnIndexOrThrow(DB.PersonTable.COL_DATE));

        tv_Name.setText(name);
        tv_Date.setText(date);
    }
}
