package au.net.nicksifniotis.amedatest;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import au.net.nicksifniotis.amedatest.LocalDB.DB;
import au.net.nicksifniotis.amedatest.LocalDB.DBOpenHelper;

public class ManageRecordsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_records);

        DBOpenHelper testDB = new DBOpenHelper(this);
        final SQLiteDatabase db = testDB.getReadableDatabase();
        String query = "SELECT * FROM " + DB.PersonTable.TABLE_NAME + " WHERE " + DB.PersonTable.ACTIVE + " = 1";

        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();

        RecordCursorAdaptor rca = new RecordCursorAdaptor(this, c, 0);
        ListView list = (ListView) findViewById(R.id.list_Records);
        if (list != null)
        {
            list.setAdapter(rca);

            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    SQLiteCursor entry = (SQLiteCursor) parent.getAdapter().getItem(position);
                    int record_id = entry.getInt(entry.getColumnIndex(DB.PersonTable._ID));

                    Intent manage_record_intent = new Intent(parent.getContext(), NewRecordActivity.class);
                    manage_record_intent.putExtra("id", record_id);

                    entry.close();
                    db.close();

                    startActivity(manage_record_intent);
                }
            });
        }
        c.close();
    }


    /**
     * Event handler for the 'new record' button.
     *
     * @param view
     */
    public void btn_manage_record_new(View view)
    {
        Intent new_record_intent = new Intent (this, NewRecordActivity.class);
        startActivity(new_record_intent);
    }
}
