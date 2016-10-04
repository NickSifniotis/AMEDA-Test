package au.net.nicksifniotis.amedatest.activities;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Random;

import au.net.nicksifniotis.amedatest.AMEDA.AMEDAInstruction;
import au.net.nicksifniotis.amedatest.AMEDA.AMEDAInstructionEnum;
import au.net.nicksifniotis.amedatest.AMEDA.AMEDAResponse;
import au.net.nicksifniotis.amedatest.Globals;
import au.net.nicksifniotis.amedatest.LocalDB.DB;
import au.net.nicksifniotis.amedatest.LocalDB.DBOpenHelper;
import au.net.nicksifniotis.amedatest.R;
import au.net.nicksifniotis.amedatest.TestState;


/**
 * TestActivity activity. Launches a test and runs it from go to whoa.
 *
 * Outstanding issues todo
 * - it would be great if the tests that were loaded into the database were in their own
 *   static class
 */
public class TestActivity extends AMEDAActivity
{
    private static final int NUM_TESTS = 5;

    private int [] _test_questions;
    private int _current_question;
    private int _num_questions;
    private Random randomiser;
    private TextView _questions_progress;

    private TestState current_state;
    private int _current_test_id;
    private DBOpenHelper _database_helper;
    private ProgressDialog _please_wait;


    /**
     * Create the GUI elements, data structures etc that this activity will use.
     *
     * @param savedInstanceState Not used.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity);

        _connect_gui();

        _database_helper = new DBOpenHelper(this);
        randomiser = new Random();


        // Get the user id of the person who is taking this test.
        // If no user_id has been passed, abort.
        Intent intent = getIntent();
        int user_id = intent.getIntExtra("id", -1);
        if (user_id == -1)
        {
            Globals.DebugToast.Send("Unable to launch test. No user_id received.");
            FailAndDieDialog(getString(R.string.t_error_no_uid));
            return;
        }


        // It's possible that we are resuming a test that had been interrupted.
        // Load the test id from the intent as well, if it exists.
        int test_id = intent.getIntExtra("test_id", -1);
        if (test_id == -1)
            _create_new_test(user_id);
        else
            _resume_test(test_id);
    }


    /**
     * Abort the test before shutting down completely.
     */
    @Override
    protected void onStop()
    {
        super.onStop();

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        _abort_test();
    }


    /**
     * (Re)connect to the AMEDA device when the activity is (resumed)shown.
     *
     */
    @Override
    protected void onStart()
    {
        super.onStart();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        updateState(TestState.STARTING);
    }

    /**
     * Sets up the GUI-y things.
     *
     */
    private void _connect_gui()
    {
        _questions_progress = (TextView) findViewById(R.id.t_progress_counter);

        Resources r = getResources();
        for (int i = 1; i <= 5; i ++)
        {
            ImageButton button = (ImageButton) findViewById(r.getIdentifier("t_btn_excursion_" + i, "id", "au.net.nicksifniotis.amedatest"));
            if (button != null)
                button.setContentDescription(getString(R.string.t_excursion_button, i));
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
                    _confirm_abort_test();
                }
            });
        }

        _please_wait = new ProgressDialog(this);
        _please_wait.setTitle ("One moment");
        _please_wait.setMessage ("Please wait while the AMEDA device moves to the next position.");
    }


    /**
     * Launching a new_btn test from scratch. Pick one randomly from the database, read the correct answers
     * and record the beginning of the new_btn test.
     */
    private void _create_new_test(int user_id)
    {
        // randomly pick a standard test for this user, and load the answer key from the database.
        int test_number = randomiser.nextInt(NUM_TESTS) + 1;
        String query = "SELECT * FROM " + DB.StandardTestTable.TABLE_NAME +
                " WHERE " + DB.StandardTestTable._ID + "=" + test_number;
        SQLiteDatabase db = _database_helper.getReadableDatabase();

        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();
        String answer_key = c.getString(c.getColumnIndexOrThrow(DB.StandardTestTable.ANSWER_KEY));
        c.close();
        db.close();


        // If we can't load the data from the database, whinge and close.
        if (answer_key == null)
        {
            _database_helper.databaseError("Error retrieving standard test with ID " + test_number);
            finish();
            return;
        }

        _num_questions = answer_key.length();
        _test_questions = new int[_num_questions];
        for (int i = 0; i < _num_questions; i ++)
            _test_questions[i] = Integer.parseInt(answer_key.substring(i, i + 1));
        _current_question = -1;


        // having made it this far, prepare to begin the test.
        ContentValues values = new ContentValues();
        values.put(DB.TestTable.DATE, Calendar.getInstance().getTimeInMillis());
        values.put(DB.TestTable.SCORE, -1);
        values.put(DB.TestTable.ACTIVE, 1);
        values.put(DB.TestTable.NUM_QUESTIONS, _num_questions);
        values.put(DB.TestTable.INTERRUPTED, 0);
        values.put(DB.TestTable.PERSON_ID, user_id);
        values.put(DB.TestTable.STANDARD_TEST_ID, test_number);

        db = _database_helper.getWritableDatabase();
        _current_test_id = (int)db.insert(DB.TestTable.TABLE_NAME, null, values);

        if (_current_test_id == -1)
        {
            _database_helper.databaseError("Error saving new test into database.");
            finish();
        }

        values = new ContentValues();
        values.put(DB.PersonTable.LAST_TEST_DATE, Calendar.getInstance().getTimeInMillis());
        db.update(DB.PersonTable.TABLE_NAME, values, DB.PersonTable._ID + " = " + user_id, null);

        db.close();

        // todo integrate properly
        if (Globals.SHORT_TESTS)
            _num_questions = 10;
    }


    /**
     * Resuming a test that had been interrupted. As well as loading up the test, need to establish
     * how many positions have been 'answered' and how many more there are to go to finish the test.
     *
     * @param test_id The interrupted test_id
     */
    private void _resume_test(int test_id)
    {
        _current_test_id = test_id;

        String query = "SELECT t.* FROM " + DB.StandardTestTable.TABLE_NAME + " t, " +
                DB.TestTable.TABLE_NAME + " d " +
                " WHERE t." + DB.StandardTestTable._ID + " = d." + DB.TestTable.STANDARD_TEST_ID +
                " AND d." + DB.TestTable._ID + " = " + test_id +
                " AND d." + DB.TestTable.INTERRUPTED + " = 1";
        SQLiteDatabase db = _database_helper.getReadableDatabase();

        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();
        String answer_key = c.getString(c.getColumnIndexOrThrow(DB.StandardTestTable.ANSWER_KEY));
        c.close();

        // If we can't load the data from the database, whinge and close.
        if (answer_key == null)
        {
            _database_helper.databaseError("Either test doesn't exist or it's not interrupted: " + test_id);
            finish();
            return;
        }

        // load up the answer key.
        _num_questions = answer_key.length();
        _test_questions = new int[_num_questions];
        for (int i = 0; i < _num_questions; i ++)
            _test_questions[i] = Integer.parseInt(answer_key.substring(i, i + 1));


        // Which was the last question that the user answered?
        query = "SELECT COUNT (*) AS answer_count FROM " + DB.QuestionTable.TABLE_NAME +
                " WHERE " + DB.QuestionTable.TEST_ID + " = " + test_id;
        c = db.rawQuery(query, null);
        c.moveToFirst();
        _current_question = c.getInt(c.getColumnIndexOrThrow("answer_count")) - 1;
        c.close();
        db.close();


        // Un-interrupt the test.
        ContentValues values = new ContentValues();
        values.put(DB.TestTable.INTERRUPTED, 0);

        db = _database_helper.getWritableDatabase();
        int success = db.update(DB.TestTable.TABLE_NAME, values, DB.TestTable._ID + " = " + _current_test_id, null);
        db.close();

        if (success == 0)
            _database_helper.databaseError(getString(R.string.t_error_database_on_abort));

        // todo integrate properly
        if (Globals.SHORT_TESTS)
            _num_questions = 10;
    }


    /**
     * Create the context menu for this activity.
     *
     * @param menu The menu to inflate.
     * @return True on success but also true otherwise.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.test_activity_menu, menu);
        return true;
    }


    /**
     * Menu click event handler for the activity's context menu.
     *
     * @param item The item that was selected
     * @return True if the operation was a success but also true otherwise.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        AlertDialog.Builder builder;
        switch (item.getItemId())
        {
            case R.id.t_mnu_pause:
                builder = new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.t_paused_title))
                        .setMessage(getString(R.string.t_paused_desc))
                        .setPositiveButton(getString(R.string.btn_done), new DialogInterface.OnClickListener()
                        {
                            /**
                             * Reconnect.
                             *
                             * @param dialog Not used.
                             * @param which Not used.
                             */
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                               // Connect();
                            }
                        });
                builder.create().show();
                break;

            case R.id.t_mnu_stop:
                _confirm_abort_test();
                break;

            case R.id.t_mnu_help:
                builder = new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.not_implemented_title))
                        .setMessage(getString(R.string.not_implemented_desc))
                        .setPositiveButton(getString(R.string.btn_done), null);
                builder.create().show();
                break;

            default:
                super.onOptionsItemSelected(item);
        }

        return true;
    }


    /**
     * Processes the response codes received from the AMEDA device.
     *
     * In this implementation of the AMEDA activity, the only commands that are being sent to
     * the device are 'Move to position' commands. So that significantly narrows the set of
     * possible responses being received, and the appropriate actions to take for them.
     *
     * @param instruction The instruction that was sent to the AMEDA.
     * @param response The AMEDA's response to that instruction.
     */
    @Override
    protected void ProcessAMEDAResponse(AMEDAInstruction instruction, AMEDAResponse response)
    {
        AMEDAInstructionEnum instruction_code = instruction.GetInstruction();

        if (!instruction_code.IsValidResponse(response))
        {
            Globals.DebugToast.Send("Unknown response " + response.toString() + " received for instruction " + instruction_code.toString());
            FailAndDieDialog(getString(R.string.error_ameda_fail_desc));
        }

        if (instruction_code == AMEDAInstructionEnum.MOVE_TO_POSITION)
            if (response.GetCode() == AMEDAResponse.Code.CANNOT_MOVE)
                CannotMoveDialog();
            else if (response.GetCode() == AMEDAResponse.Code.READY)
                if (HasMoreInstructions())
                    ExecuteNextInstruction();
                else
                    updateState(TestState.ANSWERING);
    }


    /**
     * Proceed to the next question in the test.
     *
     */
    private void _next_question()
    {
        _current_question++;

        if (_current_question >= _num_questions)
            _end_of_test();
        else
        {
            _questions_progress.setText (getString(
                    R.string.t_progress_counter, (_current_question + 1), _num_questions));
            updateState(TestState.MIDDLE);

            _move_to_next_pos();
        }
    }


    /**
     * Move the AMEDA to the next position (block until the device reports ready) and advance
     * to the STEP state.
     *
     * Updated 10/9 to move to one random position before next step, instead of five.
     * Done to speed up the cam moving to the next position.
     */
    private void _move_to_next_pos()
    {
        Globals.DebugToast.Send("Setting device to position " + _test_questions[_current_question]);

        GoToPosition(randomiser.nextInt(5) + 1);
        GoToPosition(_test_questions[_current_question]);

        ExecuteNextInstruction();
    }


    /**
     * Update the GUI components to match the current state of the test state machine.
     *
     * @param new_state The new_btn state to advance to.
     */
    private void updateState(TestState new_state)
    {
        Globals.DebugToast.Send("Setting new state to " + new_state.toString());
        current_state = new_state;

        if (current_state == TestState.MIDDLE)
            _please_wait.show();
        else
        {
            _please_wait.dismiss();
            for (TestState t : TestState.values())
            {
                View layout = findViewById(t.Layout());
                if (layout != null)
                    layout.setVisibility((current_state == t) ? View.VISIBLE : View.GONE);
            }
        }
    }


    /**
     * Button press event handlers.
     *
     * @param view Not used.
     */
    public void t_btn_excursion_1(View view)
    {
        record_user_response(1);
    }

    public void t_btn_excursion_2(View view)
    {
        record_user_response(2);
    }

    public void t_btn_excursion_3(View view)
    {
        record_user_response(3);
    }

    public void t_btn_excursion_4(View view)
    {
        record_user_response(4);
    }

    public void t_btn_excursion_5(View view)
    {
        record_user_response(5);
    }


    public void t_btn_start_test(View view)
    {
        _next_question();
    }


    public void t_btn_end_test(View view)
    {
        finish();
    }


    /**
     * Saves the user's response to the test question in the database.
     *
     * @param response An integer that corresponds to the button that the user pressed.
     */
    private void record_user_response(int response)
    {
        if (current_state != TestState.ANSWERING)
        {
            // this is impossible. The response buttons are invisible in every state except ANSWERING.
            Globals.DebugToast.Send("Impossible state - record_user_response where state is " + current_state.toString());
            return;
        }

        if (response < 1 || response > 5)
        {
            // another impossible state. This function cannot be called with values outside this range.
            // Buttons to do that simply don't exist.
            Globals.DebugToast.Send("Impossible state - response passed to record_user_response is " + response);
            return;
        }

        // Ok then. Save the user's response to the database.
        ContentValues values = new ContentValues();
        values.put(DB.QuestionTable.QUESTION_NUMBER, _current_question);
        values.put(DB.QuestionTable.USER_ANSWER, response);
        values.put(DB.QuestionTable.TEST_ID, _current_test_id);

        SQLiteDatabase db = _database_helper.getWritableDatabase();
        long record_id = db.insert(DB.QuestionTable.TABLE_NAME, null, values);

        if (record_id == -1)
        {
            // something's gone wrong saving to the database.
            // this is the sort of error that can't be explained and needs to reset the system.
            Globals.DebugToast.Send("Error on database response save.");
            _database_helper.databaseError(getString(R.string.t_error_database_on_response));
            finish();
            return;
        }

        // advance to the next question.
        _next_question();
    }


    /**
     * Aborting a test is kind of a big deal. So make sure that the user is really sure that
     * they want to abort the test, before doing so.
     */
    private void _confirm_abort_test()
    {
        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.t_abort_title))
                .setMessage(getString(R.string.t_abort_desc))
                .setPositiveButton(getString(R.string.btn_yes),
                        new DialogInterface.OnClickListener()
                {
                    /**
                     * User wants to abort the test. So, do that.
                     *
                     * @param dialog Unused.
                     * @param which Unused.
                     */
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        _abort_test();
                    }
                })
                .setNegativeButton(getString(R.string.btn_no), null);

        builder.create().show();
    }


    /**
     * For whatever reason - except a database problem - the test has had to be aborted.
     * So abort, save the interruption into the database, and disconnect the AMEDA device.
     */
    private void _abort_test()
    {
        ContentValues values = new ContentValues();
        values.put(DB.TestTable.INTERRUPTED, 1);

        SQLiteDatabase db = _database_helper.getWritableDatabase();
        int success = db.update(DB.TestTable.TABLE_NAME, values, DB.TestTable._ID + " = " + _current_test_id, null);
        db.close();

        if (success == 0)
            _database_helper.databaseError(getString(R.string.t_error_database_on_abort));

        finish();
    }


    /**
     * Function that is called when the end of the test is reached.
     */
    private void _end_of_test()
    {
        updateState(TestState.FINISHING);

        ContentValues values = new ContentValues();
        values.put(DB.TestTable.INTERRUPTED, 0);
        values.put(DB.TestTable.SCORE, _compute_score());

        SQLiteDatabase db = _database_helper.getWritableDatabase();
        int success = db.update(DB.TestTable.TABLE_NAME, values, DB.TestTable._ID + " = " + _current_test_id, null);
        db.close();

        if (success == 0)
        {
            _database_helper.databaseError(getString(R.string.t_error_database_on_end));
        }
    }


    /**
     * Scores the test.
     *
     * @return The number of questions that the user got correct in this test.
     */
    private double _compute_score()
    {
        int [] _answers = new int [_test_questions.length];

        SQLiteDatabase db = _database_helper.getReadableDatabase();
        String query = "SELECT * FROM " + DB.QuestionTable.TABLE_NAME +
                " WHERE " + DB.QuestionTable.TEST_ID + " = " + _current_test_id;

        Cursor resultSet = db.rawQuery(query, null);
        resultSet.moveToFirst();
        while (resultSet.moveToNext())
        {
            int answer = resultSet.getInt(resultSet.getColumnIndex(DB.QuestionTable.USER_ANSWER));
            int question_number = resultSet.getInt(resultSet.getColumnIndex(DB.QuestionTable.QUESTION_NUMBER));
            _answers[question_number] = answer;
        }
        resultSet.close();
        db.close();

        return Globals.ScoreTest(_test_questions, _answers);
    }
}
