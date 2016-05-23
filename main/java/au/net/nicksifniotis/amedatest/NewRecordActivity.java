package au.net.nicksifniotis.amedatest;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

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
