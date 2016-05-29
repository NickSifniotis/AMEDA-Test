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

        String projection[] = {DB.PersonTable._ID,
                DB.PersonTable.NAME,
                DB.PersonTable.COL_DATE };

        DBOpenHelper testDB = new DBOpenHelper(this);
        SQLiteDatabase db = testDB.getReadableDatabase();

        Cursor c = db.query(DB.PersonTable.TABLE_NAME,
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
