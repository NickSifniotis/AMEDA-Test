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

        DBOpenHelper testDB = new DBOpenHelper(this);
        SQLiteDatabase db = testDB.getReadableDatabase();

//        String query = "SELECT p." + DB.PersonTable.NAME + " AS name, p._ID AS id, COUNT(t." + DB.TestTable.DATE + ") AS count"
//            + " FROM " + DB.PersonTable.TABLE_NAME + " p, " + DB.TestTable.TABLE_NAME + " t"
//            + " WHERE p._ID = t._ID";

        String query = "SELECT * FROM " + DB.PersonTable.TABLE_NAME;

        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();

        RecordCursorAdaptor rca = new RecordCursorAdaptor(this, c, 0);
        ListView list = (ListView) findViewById(R.id.list_Records);
        list.setAdapter(rca);
    }
}
