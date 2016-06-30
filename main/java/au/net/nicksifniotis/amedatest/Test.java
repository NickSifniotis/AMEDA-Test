package au.net.nicksifniotis.amedatest;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.Date;
import java.util.Random;

import au.net.nicksifniotis.amedatest.AMEDAManager.AMEDA;
import au.net.nicksifniotis.amedatest.AMEDAManager.AMEDAImplementation;
import au.net.nicksifniotis.amedatest.AMEDAManager.AMEDAState;
import au.net.nicksifniotis.amedatest.AMEDAManager.NewAmeda;
import au.net.nicksifniotis.amedatest.AMEDAManager.VirtualAMEDA;
import au.net.nicksifniotis.amedatest.LocalDB.DB;
import au.net.nicksifniotis.amedatest.LocalDB.DBOpenHelper;

/**
 * Test activity. Launches a test and runs it from go to whoa.
 *
 * Created by nsifniotis on 29/05/16.
 */
public class Test extends AppCompatActivity
{
    private static final int NUM_TESTS = 5;

    private int [] _test_questions;
    private int _current_question;
    private int _num_questions;

    private TestState current_state;
    private AMEDA device;
    private int _current_test_id;
    private DBOpenHelper _database_helper;

    private LinearLayout[] _state_layouts;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_layout);


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
        Random r = new Random();

        // randomly pick a standard test for this user, and load the answer key from the database.
        int test_number = r.nextInt(NUM_TESTS) + 1;
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
        values.put(DB.TestTable.DATE, new Date().toString());
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

        try
        {
            device = (Globals.AMEDA_FREE) ? new VirtualAMEDA(this) : new NewAmeda(this);
            device.Connect();
        }
        catch (Exception e)
        {
            makeToast("Unable to create connection to AMEDA device, aborting.");
            finish();
        }

        _connect_to_layouts();


        updateState(TestState.STARTING);
    }


    /**
     * Finds the different layout controls on the UI and connects them to a bunch of local vars.
     * This has been split into it's own function, so that the state controller can call it directly
     * if it ever detects that a connection to one of these layouts is lost.
     */
    private void _connect_to_layouts()
    {
        _state_layouts = new LinearLayout[TestState.values().length];
        _state_layouts[TestState.STARTING.ordinal()] = (LinearLayout)findViewById(R.id.test_starting_state);
        _state_layouts[TestState.MIDDLE.ordinal()] = (LinearLayout)findViewById(R.id.test_middle_state);
        _state_layouts[TestState.SETTING.ordinal()] = (LinearLayout)findViewById(R.id.test_setting_state);
        _state_layouts[TestState.STEPPING.ordinal()] = (LinearLayout)findViewById(R.id.test_stepping_state);
        _state_layouts[TestState.ANSWERING.ordinal()] = (LinearLayout)findViewById(R.id.test_answering_state);
        _state_layouts[TestState.FINISHING.ordinal()] = (LinearLayout)findViewById(R.id.test_finishing_state);
    }


    /**
     * Safely sets the 'visibility' parameter of the given LinearLayout.
     *
     * This function is called by updateState() only. It contains safety code to ensure that
     * calls to null pointers are never made. It will try to 'reload' the pointers to the layouts,
     * and only if it can't do that, will it crap out.
     *
     * @param target Which state layout to target.
     * @param visibility True if the layout is to be made visible, false otherwise.
     */
    private void _toggle_layout (TestState target, boolean visibility)
    {
        if (_state_layouts[target.ordinal()] == null)
            _connect_to_layouts();

        // try it again.
        if (_state_layouts[target.ordinal()] == null)
        {
            makeToast("Catastrophic failure connecting to layouts in the UI. This error should never ever be seen.");
            finish();
            return;
        }

        _state_layouts[target.ordinal()].setVisibility((visibility) ? View.VISIBLE : View.GONE);
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

        boolean success;
        success = device.GoHome();
        if (!success)
            _abort_test();

        success = device.GoToPosition(_test_questions[_current_question]);
        if (!success)
            _abort_test();

        updateState(TestState.STEPPING);
    }


    /**
     * Update the GUI components to match the current state of the test state machine.
     *@TODO tomorrow morning add the 'middle' state to this
     * @param new_state The new state to advance to.
     */
    private void updateState(TestState new_state)
    {
        current_state = new_state;
        switch (new_state)
        {
            case STARTING:
                _toggle_layout(TestState.STARTING, true);
                _toggle_layout(TestState.MIDDLE, false);
                _toggle_layout(TestState.SETTING, false);
                _toggle_layout(TestState.STEPPING, false);
                _toggle_layout(TestState.ANSWERING, false);
                _toggle_layout(TestState.FINISHING, false);
                break;
            case MIDDLE:
                _toggle_layout(TestState.STARTING, false);
                _toggle_layout(TestState.MIDDLE, true);
                _toggle_layout(TestState.SETTING, false);
                _toggle_layout(TestState.STEPPING, false);
                _toggle_layout(TestState.ANSWERING, false);
                _toggle_layout(TestState.FINISHING, false);
                break;
            case SETTING:
                _toggle_layout(TestState.STARTING, false);
                _toggle_layout(TestState.MIDDLE, false);
                _toggle_layout(TestState.SETTING, true);
                _toggle_layout(TestState.STEPPING, false);
                _toggle_layout(TestState.ANSWERING, false);
                _toggle_layout(TestState.FINISHING, false);
                break;
            case STEPPING:
                _toggle_layout(TestState.STARTING, false);
                _toggle_layout(TestState.MIDDLE, false);
                _toggle_layout(TestState.SETTING, false);
                _toggle_layout(TestState.STEPPING, true);
                _toggle_layout(TestState.ANSWERING, false);
                _toggle_layout(TestState.FINISHING, false);
                break;
            case ANSWERING:
                _toggle_layout(TestState.STARTING, false);
                _toggle_layout(TestState.MIDDLE, false);
                _toggle_layout(TestState.SETTING, false);
                _toggle_layout(TestState.STEPPING, false);
                _toggle_layout(TestState.ANSWERING, true);
                _toggle_layout(TestState.FINISHING, false);
                break;
            case FINISHING:
                _toggle_layout(TestState.STARTING, false);
                _toggle_layout(TestState.MIDDLE, false);
                _toggle_layout(TestState.SETTING, false);
                _toggle_layout(TestState.STEPPING, false);
                _toggle_layout(TestState.ANSWERING, false);
                _toggle_layout(TestState.FINISHING, true);
                break;
        }
    }


    /**
     * Button press event handlers.
     *
     * @param view
     */
    public void btn_Next (View view)
    {
        updateState(TestState.ANSWERING);
    }

    public void btn_Next_Middle (View view)
    {
        _move_to_next_pos();
    }

    public void btn_1(View view)
    {
        record_user_response(1);
    }

    public void btn_2(View view)
    {
        record_user_response(2);
    }

    public void btn_3(View view)
    {
        record_user_response(3);
    }

    public void btn_4(View view)
    {
        record_user_response(4);
    }

    public void btn_5(View view)
    {
        record_user_response(5);
    }


    public void btn_begin_test(View view)
    {
        _next_question();
    }


    public void btn_end_test (View view)
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
            _database_helper.databaseError("Unable to save your response to the database. " + values.toString());
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
        {
            _database_helper.databaseError("Error updating test during abort operation. You will not be able to resume this test.");
        }

        // @TODO reference to ameda here.
        device.Terminate();

        finish();
        return;
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
            _database_helper.databaseError("There has been an error recording that this test is finished.");
        }
    }
}
