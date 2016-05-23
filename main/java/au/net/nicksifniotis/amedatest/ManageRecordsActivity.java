package au.net.nicksifniotis.amedatest;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

public class ManageRecordsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_records);

        String projection[] = {TestRecordContract.TestRecordEntry._ID,
                TestRecordContract.TestRecordEntry.COL_NAME,
                TestRecordContract.TestRecordEntry.COL_DATE };

        TestRecordOpenHelper testDB = new TestRecordOpenHelper(this);
        SQLiteDatabase db = testDB.getReadableDatabase();

        Cursor c = db.query(TestRecordContract.TestRecordEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null);

        c.moveToFirst();

        RecordCursorAdaptor rca = new RecordCursorAdaptor(this, c, 0);
        ListView list = (ListView) findViewById(R.id.list_Records);
        list.setAdapter(rca);
    }
}
