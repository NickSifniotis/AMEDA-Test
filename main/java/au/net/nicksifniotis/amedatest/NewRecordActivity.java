package au.net.nicksifniotis.amedatest;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.sql.SQLInput;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NewRecordActivity extends AppCompatActivity {
    private static TestRecordOpenHelper testRecordDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_new_record);

        Spinner genders = (Spinner)findViewById(R.id.spn_Gender);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.genders, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genders.setAdapter(adapter);

        testRecordDb = new TestRecordOpenHelper(this);

        Intent intent = getIntent();
        int record_id = intent.getIntExtra("id", -1);
        ((EditText)findViewById(R.id.txt_Name)).setText("record id is " + record_id);
        if (record_id == -1)
        {
            // setting up for a new record
            LinearLayout l;
            l = (LinearLayout)findViewById(R.id.newRec_hidden1);
            l.setVisibility(View.GONE);
            l = (LinearLayout)findViewById(R.id.newRec_hidden2);
            l.setVisibility(View.GONE);

            ((TextView)findViewById(R.id.txt_Name)).setText("record id is -1");
        }
        else
        {
            // loading data for an existing record
            SQLiteDatabase db = testRecordDb.getReadableDatabase();

            String query = "SELECT * FROM " + TestRecordContract.TestRecordEntry.TABLE_NAME + " WHERE " + TestRecordContract.TestRecordEntry._ID + " = " + record_id;
            Cursor c = db.rawQuery(query, null);

            c.moveToFirst();
            while (!c.isAfterLast())
            {
                String data = c.getString(c.getColumnIndexOrThrow(TestRecordContract.TestRecordEntry.COL_NAME));
                ((EditText)findViewById(R.id.txt_Name)).setText("record id is " + record_id);

                data = c.getString(c.getColumnIndexOrThrow(TestRecordContract.TestRecordEntry.COL_GENDER));
                ((Spinner)findViewById(R.id.spn_Gender)).setSelection(0);

                data = c.getString(c.getColumnIndexOrThrow(TestRecordContract.TestRecordEntry.COL_EDUCATION));
                ((EditText)findViewById(R.id.txt_Education)).setText(data);

                data = c.getString(c.getColumnIndexOrThrow(TestRecordContract.TestRecordEntry.COL_ADDRESS));
                ((EditText)findViewById(R.id.txt_Address)).setText(data);

                data = c.getString(c.getColumnIndexOrThrow(TestRecordContract.TestRecordEntry.COL_HOBBIES));
                ((EditText)findViewById(R.id.txt_Hobbies)).setText(data);

                data = c.getString(c.getColumnIndexOrThrow(TestRecordContract.TestRecordEntry.COL_NOTES));
                ((EditText)findViewById(R.id.txt_Notes)).setText(data);

                data = c.getString(c.getColumnIndexOrThrow(TestRecordContract.TestRecordEntry.COL_DATE));
                TextView date_box = (TextView)findViewById(R.id.record_date);
                date_box.setText(date_box.getText() + " " + data);
            }
        }
    }

    public void btn_Cancel(View view)
    {
        finish();
    }

    public void btn_Done(View view)
    {
        SQLiteDatabase db = testRecordDb.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put (TestRecordContract.TestRecordEntry.COL_NAME, ((EditText)findViewById(R.id.txt_Name)).getText().toString());
        values.put (TestRecordContract.TestRecordEntry.COL_GENDER, ((Spinner)findViewById(R.id.spn_Gender)).getSelectedItem().toString());
        values.put (TestRecordContract.TestRecordEntry.COL_EDUCATION, ((EditText)findViewById(R.id.txt_Education)).getText().toString());
        values.put (TestRecordContract.TestRecordEntry.COL_ADDRESS, ((EditText)findViewById(R.id.txt_Address)).getText().toString());
        values.put (TestRecordContract.TestRecordEntry.COL_HOBBIES, ((EditText)findViewById(R.id.txt_Hobbies)).getText().toString());
        values.put (TestRecordContract.TestRecordEntry.COL_NOTES, ((EditText)findViewById(R.id.txt_Notes)).getText().toString());

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();

        values.put (TestRecordContract.TestRecordEntry.COL_DATE, dateFormat.format(date));

        long newRowId;
        newRowId = db.insert(TestRecordContract.TestRecordEntry.TABLE_NAME, null, values);

        if (newRowId == -1)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Error");
            builder.setMessage("Unable to save record into the database.");
            AlertDialog diag = builder.create();

            diag.show();
        }

        finish();
    }
}
