package au.net.nicksifniotis.amedatest.activities;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import au.net.nicksifniotis.amedatest.LocalDB.DB;
import au.net.nicksifniotis.amedatest.LocalDB.DBOpenHelper;
import au.net.nicksifniotis.amedatest.ManageRecordsEnum;
import au.net.nicksifniotis.amedatest.R;
import au.net.nicksifniotis.amedatest.LocalDB.User_RCA;


/**
 * ManageRecordsActivity
 *
 * Lists the user records (includes a new button) and launches a new activity when the user makes
 * a selection.
 *
 * Note that the new button will always launch NewRecord Activity!
 *
 * However selecting a record from the listview will open the activity that has been provided
 * when this very activity was intent-ed.
 */
public class ManageRecordsActivity extends AppCompatActivity
{
    private DBOpenHelper _database_helper;
    private SQLiteDatabase _db;
    private User_RCA _adaptor;
    private ManageRecordsEnum _activity;

    private TextView _title;
    private ListView _list;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_records);

        _connect_gui();

        Intent intent = getIntent();
        _activity = ManageRecordsEnum.values()[intent.getIntExtra("activity", 0)];
        _title.setText(String.format(
                getString(R.string.amr_title),
                getString(R.string.amr_title_stub),
                getString(_activity.Descriptor())));

        _database_helper = new DBOpenHelper(this);
        _db = _database_helper.getReadableDatabase();

        _adaptor = new User_RCA(this, null);

        if (_list != null)
        {
            _list.setAdapter(_adaptor);
            _list.setOnItemClickListener(new UserRecordAdapterView());
        }
    }


    /**
     * On resume - after the new/edit activity finishes - refresh the data in the listview.
     */
    @Override
    protected void onResume()
    {
        super.onResume();

        String query = "SELECT d.*, t." + DB.TestTable.DATE +
                " FROM " + DB.PersonTable.TABLE_NAME + " d" +
                " LEFT JOIN " + DB.TestTable.TABLE_NAME + " t" +
                " ON d." + DB.PersonTable._ID + " = t." + DB.TestTable.PERSON_ID +
                " WHERE d." + DB.PersonTable.ACTIVE + " = 1";

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
     * Connects the GUI components to the variables that reference them.
     */
    private void _connect_gui()
    {
        _title = (TextView)findViewById(R.id.amr_txt_title);
        _list = (ListView) findViewById(R.id.amr_list_records);
    }


    /**
     * Event handler for the 'new record' button. Launches the 'new record' activity with
     * a user_id of -1.
     *
     * @param view Unused.
     */
    public void amr_btn_new(View view)
    {
        _launch_newRecord_activity(-1, NewRecordActivity.class);
    }

    public void amr_btn_close(View view)
    {
        _finish();
    }


    /**
     * Launches the 'new / edit user record' activity over this one.
     * Passes the parameter user_id to the activity.
     *
     * @param user_id The database ID of the record to edit, or -1 if creating a new record.
     */
    private void _launch_newRecord_activity(int user_id, Class activity)
    {
        Intent intent = new Intent (this, activity);
        intent.putExtra("id", user_id);
        startActivity(intent);
    }


    private void _finish()
    {
        finish();
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

            _launch_newRecord_activity(record_id, _activity.Activity());
        }
    }
}
