package au.net.nicksifniotis.amedatest.activities;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import au.net.nicksifniotis.amedatest.Globals;
import au.net.nicksifniotis.amedatest.LocalDB.DB;
import au.net.nicksifniotis.amedatest.LocalDB.DBOpenHelper;
import au.net.nicksifniotis.amedatest.R;

/**
 * Edit the user records activity.
 *
 * Things that you can do with this activity:
 *   - create a new user
 *   - edit the personal details of an existing user
 *   - delete a user
 *   - launch into a new test with a user
 */
public class EditUserActivity extends AppCompatActivity
{
    private EditText _first_name;
    private EditText _surname;
    private EditText _weight;
    private EditText _dob;
    private EditText _height;
    private EditText _notes;
    private EditText _address;
    private Spinner _gender;
    private ArrayAdapter<CharSequence> _gender_adapter;
    private Button _delete_record;

    private static DBOpenHelper _database_helper;
    private int _user_id;
    private boolean _launch_test_by_default;


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
        setContentView(R.layout.edit_user_activity);

        _gender = (Spinner)findViewById(R.id.mpr_spn_gender);
        _gender_adapter = ArrayAdapter.createFromResource(this,
                R.array.genders, android.R.layout.simple_spinner_item);
        _gender_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (_gender != null)
            _gender.setAdapter(_gender_adapter);


        _database_helper = new DBOpenHelper(this);

        Intent intent = getIntent();
        _user_id = intent.getIntExtra("id", -1);
        _launch_test_by_default = intent.getBooleanExtra("launch_test", false);
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
            _delete_record.setVisibility(View.GONE);
        else
        {
            SQLiteDatabase db = _database_helper.getReadableDatabase();
            Cursor c = db.query(DB.PersonTable.TABLE_NAME, null,
                    DB.PersonTable._ID + " = " + _user_id,
                    null, null, null, null);

            c.moveToFirst();

            _first_name.setText (c.getString(c.getColumnIndex(DB.PersonTable.FIRST_NAME)));
            _surname.setText    (c.getString(c.getColumnIndex(DB.PersonTable.SURNAME)));
            _dob.setText        (c.getString(c.getColumnIndex(DB.PersonTable.DOB)));
            _height.setText     (c.getString(c.getColumnIndex(DB.PersonTable.HEIGHT)));
            _weight.setText     (c.getString(c.getColumnIndex(DB.PersonTable.WEIGHT)));
            _address.setText    (c.getString(c.getColumnIndex(DB.PersonTable.ADDRESS)));
            _notes.setText      (c.getString(c.getColumnIndex(DB.PersonTable.NOTES)));

            _set_gender_spinner (c.getString(c.getColumnIndex(DB.PersonTable.GENDER)));

            c.close();
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
        _confirm_deletion();
    }

    public void mpr_btn_done(View view)
    {
        _save_data();
        if (_launch_test_by_default)
            _launch_test();

        finish();
    }


    /**
     * Create the links to the GUI components for easy referencing.
     */
    private void _get_gui_components()
    {
        _first_name = (EditText) findViewById(R.id.mpr_txt_first_name);
        _surname    = (EditText) findViewById(R.id.mpr_txt_surname);
        _dob        = (EditText) findViewById(R.id.mpr_txt_dob);
        _gender     = (Spinner)  findViewById(R.id.mpr_spn_gender);
        _height     = (EditText) findViewById(R.id.mpr_txt_height);
        _weight     = (EditText) findViewById(R.id.mpr_txt_weight);
        _address    = (EditText) findViewById(R.id.mpr_txt_address);
        _notes      = (EditText) findViewById(R.id.mpr_txt_notes);

        _delete_record = (Button) findViewById(R.id.mpr_btn_delete);


        // until the address situation is clarified, this is becoming a thing.
        LinearLayout _address_layout = (LinearLayout) findViewById(R.id.mpr_layout_address);
        if (_address_layout != null)
            _address_layout.setVisibility(
                    (Globals.USING_ADDRESSES) ? View.VISIBLE : View.GONE);

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
                    _confirm_exit();
                }
            });
        }
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
        inflater.inflate(R.menu.edit_user_menu, menu);
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
        switch (item.getItemId())
        {
            case R.id.mpr_mnu_save:
                _save_data();
                break;

            case R.id.mpr_mnu_launch_test:
                _save_data();
                _launch_test();
                finish();
                break;

            case R.id.mpr_mnu_delete:
                _confirm_deletion();
                break;

            default:
                super.onOptionsItemSelected(item);
        }

        return true;
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

        res.put(DB.PersonTable.FIRST_NAME, _first_name.getText().toString());
        res.put(DB.PersonTable.SURNAME   , _surname.getText().toString());
        res.put(DB.PersonTable.DOB       , _dob.getText().toString());
        res.put(DB.PersonTable.GENDER    , _gender.getSelectedItem().toString());
        res.put(DB.PersonTable.HEIGHT    , _height.getText().toString());
        res.put(DB.PersonTable.WEIGHT    , _weight.getText().toString());
        res.put(DB.PersonTable.ADDRESS   , _address.getText().toString());
        res.put(DB.PersonTable.NOTES     , _notes.getText().toString());

        res.put(DB.PersonTable.ACTIVE    , Integer.toString(1));

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
            else
                _user_id = (int)newRowId;
        }
    }


    /**
     * Launches the testing activity from within this one. user_id must be valid.
     */
    private void _launch_test()
    {
        if (_user_id <= 0)
            return;         // no user record exists to test.

        Intent intent = new Intent(this, TestActivity.class);
        intent.putExtra("id", _user_id);
        startActivity(intent);
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


    /**
     * Deleting a user record is a big enough deal to warrant the creation of a confirmation
     * dialog box.
     */
    private void _confirm_deletion()
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
                            _delete_record();
                        }
                    });

        builder.create().show();
    }


    /**
     * User is trying to exit the screen. Make sure that they won't lose any changes if they do.
     */
    private void _confirm_exit()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.mpr_unsaved_title))
                .setMessage(getString(R.string.mpr_unsaved_desc))
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
                                finish();
                            }
                        });

        builder.create().show();
    }
}
