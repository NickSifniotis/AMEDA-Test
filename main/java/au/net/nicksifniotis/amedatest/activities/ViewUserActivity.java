package au.net.nicksifniotis.amedatest.activities;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import au.net.nicksifniotis.amedatest.Globals;
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
public class ViewUserActivity extends NoConnectionActivity
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

        _database_helper = new DBOpenHelper(this);
        _db = _database_helper.getReadableDatabase();

        String query = "SELECT * FROM " + DB.PersonTable.TABLE_NAME +
                " WHERE " + DB.PersonTable._ID + " = " + _user_id;

        Cursor resultSet = _db.rawQuery(query, null);
        resultSet.moveToNext();

        String username = resultSet.getString(resultSet.getColumnIndex(DB.PersonTable.FIRST_NAME))
                + " " + resultSet.getString(resultSet.getColumnIndex(DB.PersonTable.SURNAME));
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
                        _email_results(test_id);
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
     * Saves the test data into a CSV file, and emails that data out via some other application.
     *
     * @param test_id The test to email.
     */
    private void _email_results(int test_id)
    {
        String fileName = create_csv(test_id);

        // email the file out!
        File file = new File(fileName);
        Uri path = Uri.fromFile(file);
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("application/octet-stream");
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Test #" + test_id + " export");
        String to[] = { "u5809912@anu.edu.au" };
        intent.putExtra(Intent.EXTRA_EMAIL, to);
        intent.putExtra(Intent.EXTRA_TEXT, "CSV file of test results attached.");
        intent.putExtra(Intent.EXTRA_STREAM, path);
        startActivityForResult(Intent.createChooser(intent, "Send mail..."),
                1222);
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


    /**
     * Sends all this user's tests to an email address.
     */
    public void SendAllRecords()
    {
        //has to be an ArrayList
        ArrayList<Uri> uris = new ArrayList<Uri>();
        //convert from paths to Android friendly Parcelable Uri's
        for (String file : filePaths)
        {
            File fileIn = new File(file);
            Uri u = Uri.fromFile(fileIn);
            uris.add(u);
        }
        emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
    }


    private String create_csv(int test_id)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        String query = "SELECT t." + DB.StandardTestTable.ANSWER_KEY
                + ", d.* FROM " + DB.StandardTestTable.TABLE_NAME + " t, " +
                DB.TestTable.TABLE_NAME + " d " +
                " WHERE t." + DB.StandardTestTable._ID + " = d." + DB.TestTable.STANDARD_TEST_ID +
                " AND d." + DB.TestTable._ID + " = " + test_id;
        SQLiteDatabase db = _database_helper.getReadableDatabase();

        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();
        String answer_key = c.getString(c.getColumnIndexOrThrow(DB.StandardTestTable.ANSWER_KEY));
        int sequence_number = c.getInt(c.getColumnIndexOrThrow(DB.TestTable.STANDARD_TEST_ID));
        long date = c.getLong(c.getColumnIndexOrThrow(DB.TestTable.DATE));
        int person_id = c.getInt(c.getColumnIndexOrThrow(DB.TestTable.PERSON_ID));
        int interrupted = c.getInt(c.getColumnIndex(DB.TestTable.INTERRUPTED));
        c.close();

        // If we can't load the data from the database, whinge and close.
        if (answer_key == null)
        {
            _database_helper.databaseError("Error loading test " + test_id);
            return "";
        }

        // load up the answer key.
        int _num_questions = answer_key.length();
        int [] _test_questions = new int[_num_questions];
        int [] _times = new int[_num_questions];
        for (int i = 0; i < _num_questions; i ++)
            _test_questions[i] = Integer.parseInt(answer_key.substring(i, i + 1));


        int [] _answers = new int [_num_questions];

        query = "SELECT * FROM " + DB.QuestionTable.TABLE_NAME +
                " WHERE " + DB.QuestionTable.TEST_ID + " = " + test_id;

        Cursor resultSet = db.rawQuery(query, null);
        resultSet.moveToFirst();
        do
        {
            int time = resultSet.getInt(resultSet.getColumnIndex(DB.QuestionTable.TIME_TO_ANSWER));
            int answer = resultSet.getInt(resultSet.getColumnIndex(DB.QuestionTable.USER_ANSWER));
            int question_number = resultSet.getInt(resultSet.getColumnIndex(DB.QuestionTable.QUESTION_NUMBER));
            _answers[question_number] = answer;
            _times[question_number] = time;
        }
        while (resultSet.moveToNext());

        resultSet.close();

        String score = (interrupted == 1) ? "Interrupted" : String.valueOf(Globals.ScoreTest(_test_questions, _answers));

        // build up the CSV file.
        String newPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
        String fileName = test_id + ".csv";
        try {
            PrintWriter fos = new PrintWriter(newPath + fileName);

            fos.println("DateTime," + sdf.format(new Date(date)));
            fos.println("Person Id," + person_id);
            fos.println("Sequence Number," + sequence_number);
            fos.println("Score," + score);

            fos.println("Response Time (Milliseconds),Correct Response,User Response");
            for (int i = 0; i < _num_questions; i ++)
                fos.println(_times[i] + "," + _test_questions[i] + "," + (_answers[i] > 0 ? _answers[i] : ""));

            fos.close();

        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
            return "";
        }

        return newPath + fileName;
    }
}
