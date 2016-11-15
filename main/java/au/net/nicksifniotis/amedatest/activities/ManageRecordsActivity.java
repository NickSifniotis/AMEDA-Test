package au.net.nicksifniotis.amedatest.activities;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
 * Lists the user records (includes a new_btn button) and launches a new_btn activity when the user makes
 * a selection.
 *
 * Note that the new_btn button will always launch NewRecord Activity!
 *
 * However selecting a record from the listview will open the activity that has been provided
 * when this very activity was intent-ed.
 */
public class ManageRecordsActivity extends NoConnectionActivity
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

        _adaptor = new User_RCA(this);

        if (_list != null)
        {
            _list.setAdapter(_adaptor);
            _list.setOnItemClickListener(new UserRecordAdapterView());
            registerForContextMenu(_list);
        }
    }


    /**
     * On resume - after the new_btn/edit activity finishes - refresh the data in the listview.
     */
    @Override
    protected void onResume()
    {
        super.onResume();

        _refresh_data();
    }


    private void _refresh_data()
    {
        String query = "SELECT * FROM " + DB.PersonTable.TABLE_NAME
                + " WHERE " + DB.PersonTable.ACTIVE + " = 1"
                + " ORDER BY " + DB.PersonTable.SURNAME
                + ", " + DB.PersonTable.FIRST_NAME;

        Cursor _record_cursor = _db.rawQuery(query, null);
        _record_cursor.moveToFirst();

        _adaptor.swapCursor(_record_cursor);
        _adaptor.notifyDataSetChanged();
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

        Toolbar bar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(bar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle("");

        if (bar != null)
        {
            bar.setNavigationIcon(R.drawable.toolbar_back);
            bar.setNavigationOnClickListener(new View.OnClickListener()
            {
                /**
                 * Clicking the back arrow is equivalent to saying 'stop the test, I wanna get off'
                 * So record it as an interrupted test.
                 *
                 * @param v Not used. Poor v :(
                 */
                @Override
                public void onClick(View v)
                {
                    finish();
                }
            });
        }
    }


    /**
     * Event handler for the 'new_btn record' button. Launches the 'new_btn record' activity with
     * a user_id of -1.
     *
     * @param view Unused.
     */
    public void amr_btn_new(View view)
    {
        _launch_newRecord_activity(-1, EditUserActivity.class);
    }

    public void amr_btn_close(View view)
    {
        _finish();
    }


    /**
     * Launches the 'new_btn / edit user record' activity over this one.
     * Passes the parameter user_id to the activity.
     *
     * @param user_id The database ID of the record to edit, or -1 if creating a new_btn record.
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


    public void manage_searchbar(View view)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.not_implemented_title))
                .setMessage(getString(R.string.not_implemented_desc))
                .setPositiveButton(getString(R.string.btn_done), null);

        builder.create().show();
    }

    /**
     * Call me crazy, but I am no fan of anonymous classes.
     */
    private class UserRecordAdapterView implements AdapterView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            view.showContextMenu();
        }
    }


    /**
     * Inflate the context menu.
     *
     * @param menu The menu to inflate.
     * @param v The view to inflate it in.
     * @param menuInfo Not sure what this does.
     */
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.manage_records, menu);
    }


    /**
     * Handler for the context menu selection.
     *
     * @param item The menu item that the user selected.
     * @return True on a successful event handling, false otherwise.
     */
    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        ListView lv = (ListView) findViewById(R.id.amr_list_records);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        SQLiteCursor entry = null;
        if (lv != null)
            entry = (SQLiteCursor) lv.getItemAtPosition(info.position);

        int record_id = -1;
        if (entry != null)
            record_id = entry.getInt(entry.getColumnIndex(DB.PersonTable._ID));

        if (record_id == -1)
            return false;


        switch (item.getItemId())
        {
            case R.id.manage_delete_user:
                _confirm_deletion(record_id);
                break;
            case R.id.manage_edit_user:
                _launch_newRecord_activity(record_id, ManageRecordsEnum.EDIT_RECORD.Activity());
                break;
            case R.id.manage_start_test:
                _launch_newRecord_activity(record_id, ManageRecordsEnum.START_TEST.Activity());
                break;
            case R.id.manage_view_user:
                _launch_newRecord_activity(record_id, ManageRecordsEnum.VIEW_RECORD.Activity());
                break;

            default:
                return super.onContextItemSelected(item);
        }
        return true;
    }


    /**
     * Delete the existing record from the system - including all tests that this person may
     * have taken.
     *
     */
    private void _delete_record(int _user_id)
    {
        if (_user_id == -1)
            _database_helper.databaseError("Error deleting non-existent user.");
        else
        {
            SQLiteDatabase db = _database_helper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DB.TestTable.ACTIVE, Integer.toString(0));

            db.update(DB.TestTable.TABLE_NAME, values,
                    DB.TestTable.PERSON_ID + " = " + _user_id, null);

            values = new ContentValues();
            values.put(DB.PersonTable.ACTIVE, Integer.toString(0));

            db.update(DB.PersonTable.TABLE_NAME, values,
                    DB.PersonTable._ID + " = " + _user_id, null);
        }

        _refresh_data();
    }


    /**
     * Deleting a user record is a big enough deal to warrant the creation of a confirmation
     * dialog box.
     */
    private void _confirm_deletion(final int _user_id)
    {
        if (_user_id <= 0)
            return;     // nothing to delete.

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.mpr_delete_title))
                .setMessage(getString(R.string.mpr_delete_desc))
                .setNegativeButton(getString(R.string.btn_no), null)
                .setPositiveButton(getString(R.string.btn_yes),
                        new DialogInterface.OnClickListener()
                        {
                            /**
                             * Delete the damn thing, then.
                             *
                             * @param dialog Not used.
                             * @param which Not used.
                             */
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                _delete_record(_user_id);
                            }
                        });

        builder.create().show();
    }
}
