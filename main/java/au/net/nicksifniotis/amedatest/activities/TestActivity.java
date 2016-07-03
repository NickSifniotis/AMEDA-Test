package au.net.nicksifniotis.amedatest.activities;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Random;

import au.net.nicksifniotis.amedatest.AMEDAManager.AMEDAInstruction;
import au.net.nicksifniotis.amedatest.AMEDAManager.AMEDAInstructionEnum;
import au.net.nicksifniotis.amedatest.AMEDAManager.AMEDAResponse;
import au.net.nicksifniotis.amedatest.LocalDB.DB;
import au.net.nicksifniotis.amedatest.LocalDB.DBOpenHelper;
import au.net.nicksifniotis.amedatest.R;
import au.net.nicksifniotis.amedatest.TestState;


/**
 * TestActivity activity. Launches a test and runs it from go to whoa.
 *
 * Created by nsifniotis on 29/05/16.
 */
public class TestActivity extends AMEDAActivity
{
    private static final int NUM_TESTS = 5;

    private int [] _test_questions;
    private int _current_question;
    private int _num_questions;
    private Random randomiser;
    private ProgressDialog _setting_progress;

    private TestState current_state;
    private int _current_test_id;
    private DBOpenHelper _database_helper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity);


        // Get the user id of the person who is taking this test.
        // If no user_id has been passed, abort.
        Intent intent = getIntent();
        int user_id = intent.getIntExtra("id", -1);
        if (user_id == -1)
        {
            makeToast("Unable to launch test. No user_id received.");
            finish();
            return;
        }

        _database_helper = new DBOpenHelper(this);
        randomiser = new Random();

        // randomly pick a standard test for this user, and load the answer key from the database.
        int test_number = randomiser.nextInt(NUM_TESTS) + 1;
        String query = "SELECT * FROM " + DB.StandardTestTable.TABLE_NAME + " WHERE " + DB.StandardTestTable._ID + "=" + test_number;
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
        values.put(DB.TestTable.FINISHED, 0);
        values.put(DB.TestTable.INTERRUPTED, 0);
        values.put(DB.TestTable.PERSON_ID, user_id);
        values.put(DB.TestTable.STANDARD_TEST_ID, test_number);

        db = _database_helper.getWritableDatabase();
        _current_test_id = (int)db.insert(DB.TestTable.TABLE_NAME, null, values);

        if (_current_test_id == -1)
        {
            _database_helper.databaseError("Error saving new test into database.");
            finish();
            return;
        }


        // create the 'Setting AMEDA please wait' progress dialog.
        // it gets shown and hidden as the activity changes from one state to another.
        _setting_progress = new ProgressDialog(this);
        _setting_progress.setTitle (getString(R.string.t_setting_title));
        _setting_progress.setMessage(getString(R.string.t_setting_desc));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.test_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle item selection
        switch (item.getItemId())
        {
            case R.id.test_pause_mnu:
                makeToast("Pause test selected.");
                return true;
            case R.id.test_stop_mnu:
                _abort_test();
                return true;
            case R.id.help_mnu:
                makeToast("Help option selected.");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * Disconnect from the AMEDA each time the user exits the activity. We don't want to be
     * holding on to this resource.
     *
     */
    @Override
    protected void onStop()
    {
        super.onStop();

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

        updateState(TestState.STARTING);
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
            DebugToast("Unknown response " + response.toString() + " received for instruction " + instruction_code.toString());
            FailAndDieDialog(getString(R.string.error_ameda_fail_desc));
        }

        if (instruction_code == AMEDAInstructionEnum.MOVE_TO_POSITION)
            if (response == AMEDAResponse.CANNOT_MOVE)
                CannotMoveDialog();
            else
                if (HasMoreInstructions())
                    ExecuteNextInstruction();
                else
                    updateState(TestState.STEPPING);
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
            updateState(TestState.MIDDLE);
    }


    /**
     * Move the AMEDA to the next position (block until the device reports ready) and advance
     * to the STEP state.
     */
    private void _move_to_next_pos()
    {
        updateState(TestState.SETTING);
        makeToast("Setting device to position " + _test_questions[_current_question]);

        for (int i = 0; i < 5; i ++)
            GoToPosition(randomiser.nextInt(5) + 1);
        GoToPosition(_test_questions[_current_question]);

        ExecuteNextInstruction();
    }


    /**
     * Update the GUI components to match the current state of the test state machine.
     *
     * @param new_state The new state to advance to.
     */
    private void updateState(TestState new_state)
    {
        current_state = new_state;

        for (TestState t: TestState.values())
        {
            View layout = findViewById(t.Layout());
            if (layout != null)
                layout.setVisibility ((current_state == t) ? View.VISIBLE : View.GONE);
        }

        if (current_state == TestState.SETTING)
            _setting_progress.show();
        else
            _setting_progress.dismiss();
    }


    /**
     * Button press event handlers.
     *
     * @param view Not used.
     */
    public void t_btn_step(View view)
    {
        updateState(TestState.ANSWERING);
    }

    public void t_btn_middle(View view)
    {
        _move_to_next_pos();
    }

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


    public void makeToast (String message)
    {
        Toast t = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        t.show();
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
            makeToast("Impossible state - record_user_response where state is " + current_state.toString());
            return;
        }

        if (response < 1 || response > 5)
        {
            // another impossible state. This function cannot be called with values outside this range.
            // Buttons to do that simply don't exist.
            makeToast("Impossible state - response passed to record_user_response is " + response);
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
            DebugToast("Error on database response save.");
            _database_helper.databaseError(getString(R.string.t_error_database_on_response));
            finish();
            return;
        }

        // advance to the next question.
        _next_question();
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
        values.put(DB.TestTable.FINISHED, 1);

        SQLiteDatabase db = _database_helper.getWritableDatabase();
        int success = db.update(DB.TestTable.TABLE_NAME, values, DB.TestTable._ID + " = " + _current_test_id, null);
        db.close();

        if (success == 0)
        {
            _database_helper.databaseError(getString(R.string.t_error_database_on_end));
        }
    }
}
