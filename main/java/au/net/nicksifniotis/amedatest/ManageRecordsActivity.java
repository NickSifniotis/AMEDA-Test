package au.net.nicksifniotis.amedatest;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import au.net.nicksifniotis.amedatest.LocalDB.DB;
import au.net.nicksifniotis.amedatest.LocalDB.DBOpenHelper;


public class ManageRecordsActivity extends AppCompatActivity
{
    private DBOpenHelper _database_helper;
    private SQLiteDatabase _db;
    private RecordCursorAdaptor _adaptor;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_records);

        _database_helper = new DBOpenHelper(this);
        _db = _database_helper.getReadableDatabase();

        _adaptor = new RecordCursorAdaptor(this, null, 0);
        ListView list = (ListView) findViewById(R.id.list_Records);
        if (list != null)
        {
            list.setAdapter(_adaptor);
            list.setOnItemClickListener(new UserRecordAdapterView());
        }
    }


    /**
     * On resume - after the new/edit activity finishes - refresh the data in the listview.
     */
    @Override
    protected void onResume()
    {
        super.onResume();

        String query = "SELECT * FROM " + DB.PersonTable.TABLE_NAME + " WHERE " + DB.PersonTable.ACTIVE + " = 1";

        Cursor _record_cursor = _db.rawQuery(query, null);
        _record_cursor.moveToFirst();

        _adaptor.swapCursor(_record_cursor);
    }


    /**
     * Make sure all database connections are closed off.
     */
    @Override
    public void onDestroy()
    {
        super.onDestroy();

        _db.close();
        _database_helper.close();
    }


    /**
     * Event handler for the 'new record' button. Launches the 'new record' activity with
     * a user_id of -1.
     *
     * @param view Unused.
     */
    public void btn_manage_record_new(View view)
    {
        _launch_newRecord_activity(-1);
    }


    /**
     * Launches the 'new / edit user record' activity over this one.
     * Passes the parameter user_id to the activity.
     *
     * @param user_id The database ID of the record to edit, or -1 if creating a new record.
     */
    private void _launch_newRecord_activity(int user_id)
    {
        Intent new_record_intent = new Intent (this, NewRecordActivity.class);
        new_record_intent.putExtra("id", user_id);
        startActivity(new_record_intent);
    }


    /**
     * Call me crazy, but I am no fan of anonymous classes.
     */
    private class UserRecordAdapterView implements AdapterView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            SQLiteCursor entry = (SQLiteCursor) parent.getAdapter().getItem(position);
            int record_id = entry.getInt(entry.getColumnIndex(DB.PersonTable._ID));

            _launch_newRecord_activity(record_id);
        }
    }
}
