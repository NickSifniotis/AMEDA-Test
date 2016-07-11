package au.net.nicksifniotis.amedatest.activities;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import au.net.nicksifniotis.amedatest.LocalDB.DB;
import au.net.nicksifniotis.amedatest.LocalDB.DBOpenHelper;
import au.net.nicksifniotis.amedatest.LocalDB.Test_RCA;
import au.net.nicksifniotis.amedatest.R;

/**
 * View User activity.
 *
 * View a user's details including their test history. Retake or delete unfinished tests.
 * Watch the user's progress improve (or otherwise) with repeated practice on the AMEDA
 * device.
 *
 * Only a few things todo:
 *   - make 'view results' DO something
 *   - the user interface really could use some work
 */
public class ViewUserActivity extends AppCompatActivity
{
    private int _user_id;
    private DBOpenHelper _database_helper;
    private SQLiteDatabase _db;
    private TextView _title;
    private TextView _notes;
    private Test_RCA _adaptor;


    /**
     * Create the activity, set up the GUI and look up the person
     * who's record we're seeing.
     *
     * @param savedInstanceState Not used.
     */
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_user_activity);

        _database_helper = new DBOpenHelper(this);
        _db = _database_helper.getReadableDatabase();
        _adaptor = new Test_RCA(this);

        _connect_gui();


        // get the user_id of the person we are looking at.
        Intent intent = getIntent();
        _user_id = intent.getIntExtra("id", -1);
        if (_user_id == -1)
            finish();
    }


    /**
     * Loading the person deets happens at this level so the data is automatically
     * refreshed once we come out of Edit User and the like.
     */
    @Override
    public void onStart()
    {
        super.onStart();

        String query = "SELECT * FROM " + DB.PersonTable.TABLE_NAME +
                " WHERE " + DB.PersonTable._ID + " = " + _user_id;

        Cursor resultSet = _db.rawQuery(query, null);
        resultSet.moveToNext();

        String username = resultSet.getString(resultSet.getColumnIndex(DB.PersonTable.NAME));
        _title.setText(getString(R.string.vu_string_page_title, username));

        String notes = resultSet.getString(resultSet.getColumnIndex(DB.PersonTable.NOTES));
        _notes.setText(notes);

        resultSet.close();


        query = "SELECT * FROM " + DB.TestTable.TABLE_NAME +
                " WHERE " + DB.TestTable.PERSON_ID + " = " + _user_id +
                " AND " + DB.TestTable.ACTIVE + " = 1" +
                " ORDER BY " + DB.TestTable.DATE + " DESC";

        resultSet = _db.rawQuery(query, null);
        resultSet.moveToFirst();
        _adaptor.swapCursor(resultSet);
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
     * Set up the GUI elements.
     */
    private void _connect_gui()
    {
        _title = (TextView)findViewById(R.id.vu_txt_title);
        _notes = (TextView)findViewById(R.id.vu_txt_notes);

        ListView list = (ListView)findViewById(R.id.vu_list_tests);
        if (list != null)
        {
            list.setAdapter(_adaptor);
            list.setOnItemClickListener(new TestRecordAdapterView());
        }

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
     * Button click handlers.
     *
     * @param view Sadly this goes unused.
     */
    public void vu_btn_edit(View view)
    {
        Intent intent = new Intent (this, EditUserActivity.class);
        intent.putExtra("id", _user_id);
        startActivity(intent);
    }

    public void vu_btn_new(View view)
    {
        Intent intent = new Intent (this, TestActivity.class);
        intent.putExtra("id", _user_id);
        startActivity(intent);
    }

    public void vu_btn_close(View view)
    {
        finish();
    }


    /**
     * Handle the situation where a user has selected one of the tests from the list.
     * What do we do? Delete is always an option.
     *
     * We can complete the test, if it's interrupted.
     *
     * Otherwise we can just look at how well the user did.
     *
     * @param test_id The test that has been selected.
     * @param interrupted True if the test was interrupted and completion is an option.
     */
    private void _handle_test_selection(final int test_id, boolean interrupted)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage (getString(R.string.vu_selected_resume))
                .setPositiveButton(getString(R.string.vu_selected_view), new DialogInterface.OnClickListener()
                {
                    /**
                     * View results. 'Not implemented yet'
                     *
                     * @param dialog Not used.
                     * @param which Not used.
                     */
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        _view_results(test_id);
                    }
                })
                .setNegativeButton(getString(R.string.vu_selected_delete), new DialogInterface.OnClickListener()
                {
                    /**
                     * Delete this test!
                     *
                     * @param dialog Not used.
                     * @param which Not used.
                     */
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        _delete_test(test_id);
                    }
                });

        if (interrupted)
            builder.setNeutralButton(getString(R.string.vu_selected_resume), new DialogInterface.OnClickListener()
            {
                /**
                 * Resume the test.
                 *
                 * @param dialog Not used.
                 * @param which Not used.
                 */
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    _resume_test(test_id);
                }
            });

        builder.create().show();
    }


    /**
     * Launches a new activity that displays the results of the test in detail.
     *
     * @param test_id The test who's results must be viewed.
     */
    private void _view_results (int test_id)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.not_implemented_title)
                .setMessage(R.string.not_implemented_desc)
                .setPositiveButton(R.string.btn_done, null);

        builder.create().show();
    }


    /**
     * Attempts to resume an interrupted test.
     *
     * @param test_id The test to resume.
     */
    private void _resume_test (int test_id)
    {
        Intent intent = new Intent (this, TestActivity.class);
        intent.putExtra("id", 0);
        intent.putExtra("test_id", test_id);
        startActivity(intent);
    }


    /**
     * Marks this test as deleted.
     *
     * @param test_id The test to delete.
     */
    private void _delete_test (int test_id)
    {
        SQLiteDatabase db_w = _database_helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DB.TestTable.ACTIVE, 0);

        int success = db_w.update(DB.TestTable.TABLE_NAME, values, DB.TestTable._ID + " = " + test_id, null);
        db_w.close();

        if (success == 0)
            _database_helper.databaseError(getString(R.string.vu_error_delete_fail));
    }


    /**
     * OnClick handler for selecting an entry in thr list of tests that the user has taken.
     */
    private class TestRecordAdapterView implements AdapterView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            SQLiteCursor entry = (SQLiteCursor) parent.getAdapter().getItem(position);
            int record_id = entry.getInt(entry.getColumnIndex(DB.TestTable._ID));
            int interrupted = entry.getInt(entry.getColumnIndex(DB.TestTable.INTERRUPTED));

            _handle_test_selection(record_id, (interrupted == 1));
        }
    }
}
