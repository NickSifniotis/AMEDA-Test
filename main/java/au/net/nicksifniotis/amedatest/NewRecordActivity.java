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

import java.text.SimpleDateFormat;
import java.util.Date;

public class NewRecordActivity extends AppCompatActivity {
    private static DBOpenHelper testRecordDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_new_record);

        Spinner genders = (Spinner)findViewById(R.id.spn_Gender);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.genders, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genders.setAdapter(adapter);

        testRecordDb = new DBOpenHelper(this);

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

            String query = "SELECT * FROM " + DB.PersonTable.TABLE_NAME + " WHERE " + DB.PersonTable._ID + " = " + record_id;
            Cursor c = db.rawQuery(query, null);

            c.moveToFirst();
            while (!c.isAfterLast())
            {
                String data = c.getString(c.getColumnIndexOrThrow(DB.PersonTable.NAME));
                ((EditText)findViewById(R.id.txt_Name)).setText("record id is " + record_id);

                data = c.getString(c.getColumnIndexOrThrow(DB.PersonTable.GENDER));
                ((Spinner)findViewById(R.id.spn_Gender)).setSelection(0);

                data = c.getString(c.getColumnIndexOrThrow(DB.PersonTable.EDUCATION));
                ((EditText)findViewById(R.id.txt_Education)).setText(data);

                data = c.getString(c.getColumnIndexOrThrow(DB.PersonTable.ADDRESS));
                ((EditText)findViewById(R.id.txt_Address)).setText(data);

                data = c.getString(c.getColumnIndexOrThrow(DB.PersonTable.HOBBIES));
                ((EditText)findViewById(R.id.txt_Hobbies)).setText(data);

                data = c.getString(c.getColumnIndexOrThrow(DB.PersonTable.NOTES));
                ((EditText)findViewById(R.id.txt_Notes)).setText(data);

                data = c.getString(c.getColumnIndexOrThrow(DB.PersonTable.COL_DATE));
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
        values.put (DB.PersonTable.NAME, ((EditText)findViewById(R.id.txt_Name)).getText().toString());
        values.put (DB.PersonTable.GENDER, ((Spinner)findViewById(R.id.spn_Gender)).getSelectedItem().toString());
        values.put (DB.PersonTable.EDUCATION, ((EditText)findViewById(R.id.txt_Education)).getText().toString());
        values.put (DB.PersonTable.ADDRESS, ((EditText)findViewById(R.id.txt_Address)).getText().toString());
        values.put (DB.PersonTable.HOBBIES, ((EditText)findViewById(R.id.txt_Hobbies)).getText().toString());
        values.put (DB.PersonTable.NOTES, ((EditText)findViewById(R.id.txt_Notes)).getText().toString());

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();

        values.put (DB.PersonTable.COL_DATE, dateFormat.format(date));

        long newRowId;
        newRowId = db.insert(DB.PersonTable.TABLE_NAME, null, values);

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
