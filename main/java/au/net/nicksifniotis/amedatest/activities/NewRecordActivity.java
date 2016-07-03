package au.net.nicksifniotis.amedatest.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import au.net.nicksifniotis.amedatest.LocalDB.DB;
import au.net.nicksifniotis.amedatest.LocalDB.DBOpenHelper;
import au.net.nicksifniotis.amedatest.R;


public class NewRecordActivity extends AppCompatActivity
{
    private EditText _name;
    private EditText _weight;
    private EditText _dob;
    private EditText _height;
    private EditText _notes;
    private Spinner _gender;
    private ArrayAdapter<CharSequence> _gender_adapter;
    private LinearLayout _tests_taken_layout;
    private Button _delete_record;

    private static DBOpenHelper _database_helper;
    private int _user_id;


    /**
     * OnCreation of the activity, do set up stuff.
     *
     * This includes initialising those parts of the GUI that need to be initialised, establishing
     * a database connection and extracting whatever information has been passed through to
     * this activity.
     *
     * @param savedInstanceState Any data that might be passed to this method by the caller.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_person_record);

        _gender = (Spinner)findViewById(R.id.mpr_spn_gender);
        _gender_adapter = ArrayAdapter.createFromResource(this,
                R.array.genders, android.R.layout.simple_spinner_item);
        _gender_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (_gender != null)
            _gender.setAdapter(_gender_adapter);


        _database_helper = new DBOpenHelper(this);

        Intent intent = getIntent();
        _user_id = intent.getIntExtra("id", -1);
    }


    /**
     * OnStart method call - populate the GUI components with relevant data.
     * Link the GUI components to this class's member variables.
     */
    @Override
    public void onStart()
    {
        super.onStart();
        _get_gui_components();


        // If we have been given an existing user record to edit, load the data from the 'base
        // and populate the GUI components.
        if (_user_id == -1)
        {
            _tests_taken_layout.setVisibility(View.GONE);
            _delete_record.setVisibility(View.GONE);
        }
        else
        {
            SQLiteDatabase db = _database_helper.getReadableDatabase();
            Cursor c = db.query(DB.PersonTable.TABLE_NAME, null,
                    DB.PersonTable._ID + " = " + _user_id,
                    null, null, null, null);

            c.moveToFirst();

            _name.setText     (c.getString(c.getColumnIndex(DB.PersonTable.NAME)));
            _dob.setText      (c.getString(c.getColumnIndex(DB.PersonTable.DOB)));
            _height.setText(c.getString(c.getColumnIndex(DB.PersonTable.HEIGHT)));
            _weight.setText  (c.getString(c.getColumnIndex(DB.PersonTable.WEIGHT)));
            _notes.setText    (c.getString(c.getColumnIndex(DB.PersonTable.NOTES)));

            _set_gender_spinner(c.getString(c.getColumnIndex(DB.PersonTable.GENDER)));

            c.close();

            _tests_taken_layout.setVisibility(View.VISIBLE);
        }
    }


    /**
     * Button press event handlers.
     */
    public void mpr_btn_cancel(View view)
    {
        finish();
    }

    public void mpr_btn_delete(View view)
    {
        _delete_record();
    }

    public void mpr_btn_done(View view)
    {
        _save_data();
    }


    /**
     * Create the links to the GUI components for easy referencing.
     */
    private void _get_gui_components()
    {
        _name   = (EditText) findViewById(R.id.mpr_txt_name);
        _dob    = (EditText) findViewById(R.id.mpr_txt_dob);
        _gender = (Spinner)  findViewById(R.id.mpr_spn_gender);
        _height = (EditText) findViewById(R.id.mpr_txt_height);
        _weight = (EditText) findViewById(R.id.mpr_txt_weight);
        _notes  = (EditText) findViewById(R.id.mpr_txt_notes);

        _tests_taken_layout = (LinearLayout) findViewById(R.id.newRec_tests_taken_list);
        _delete_record = (Button) findViewById(R.id.mpr_btn_delete);
    }


    /**
     * Set the gender spinner to have selected a particular value.
     *
     * @param value The value to set. If this value doesn't exist in the spinner, this function
     *              does nothing.
     */
    private void _set_gender_spinner (String value)
    {
        int position = _gender_adapter.getPosition(value);

        if (position >= 0)
            _gender.setSelection(position);
    }


    /**
     * Creates and returns a ContentValues object that contains the stack of data that
     * is to be saved into the database.
     *
     * Replicating code is really fucking stupid so both the insert() and update() methods
     * call this code.
     *
     * @return A ContentValues object that has been populated with the data that the user provided.
     */
    private ContentValues _get_responses_for_saving()
    {
        ContentValues res = new ContentValues();

        res.put(DB.PersonTable.NAME  , _name.getText().toString());
        res.put(DB.PersonTable.DOB   , _dob.getText().toString());
        res.put(DB.PersonTable.GENDER, _gender.getSelectedItem().toString());
        res.put(DB.PersonTable.HEIGHT, _height.getText().toString());
        res.put(DB.PersonTable.WEIGHT, _weight.getText().toString());
        res.put(DB.PersonTable.NOTES , _notes.getText().toString());
        res.put(DB.PersonTable.ACTIVE, Integer.toString(1));

        return res;
    }


    /**
     * Save the data to the persistent storage device.
     *
     * It's an INSERT for a new_btn record and UPDATE for existing.
     */
    private void _save_data()
    {
        SQLiteDatabase db = _database_helper.getWritableDatabase();
        ContentValues values = _get_responses_for_saving();

        if (_user_id > 0)
        {
            int victory = db.update(DB.PersonTable.TABLE_NAME, values,
                    DB.PersonTable._ID + " = " + _user_id, null);

            if (victory == 0)       // Victory? What victory???
                _database_helper.databaseError("Unable to update user data in the database.");
        }
        else
        {
            Toast.makeText(this, "Saving..", Toast.LENGTH_LONG).show();
            long newRowId = db.insert(DB.PersonTable.TABLE_NAME, null, values);

            if (newRowId == -1)
                _database_helper.databaseError("Unable to save new_btn person record in database.");
        }

        finish();
    }


    /**
     * Delete the existing record from the system - including all tests that this person may
     * have taken.
     *
     */
    private void _delete_record()
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

            db.close();
        }

        finish();
    }
}
