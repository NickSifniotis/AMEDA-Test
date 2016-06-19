package au.net.nicksifniotis.amedatest.LocalDB;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AlertDialog;

import au.net.nicksifniotis.amedatest.LocalDB.DB;

/**
 * Database helper class that uses the contracts and strings inside of DB.
 *
 * Created by nsifniotis on 23/05/16.
 */
public class DBOpenHelper extends SQLiteOpenHelper
{
    private Context _my_context;


    public DBOpenHelper(Context context)
    {
        super(context, DB.filename, null, DB.version);
        _my_context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(DB.PersonTable.CreateSQL);
        db.execSQL(DB.TestTable.CreateSQL);
        db.execSQL(DB.QuestionTable.CreateSQL);
        db.execSQL(DB.StandardTestTable.CreateSQL);

        createStandardTests(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL(DB.PersonTable.DestroySQL);
        db.execSQL(DB.TestTable.DestroySQL);
        db.execSQL(DB.QuestionTable.DestroySQL);
        db.execSQL(DB.StandardTestTable.DestroySQL);

        onCreate(db);
    }


    private void createStandardTests(SQLiteDatabase db)
    {
        String [] standard_tests = new String[]{
                "24151334214252423111545551434543245121534532225423",
                "11413351545442552444123455511352155231525243544343",
                "12423331431235233353213322244234534521212124421124",
                "32531334511225423245113225224133354515551232221553",
                "25431521253324525553145444523121525353521131412522" };

        for (int i = 0, j = standard_tests.length; i < j; i ++)
        {
            ContentValues values = new ContentValues();
            values.put(DB.StandardTestTable.ANSWER_KEY, standard_tests[i]);
            long newRowId;
            newRowId = db.insert(DB.StandardTestTable.TABLE_NAME, null, values);

            if (newRowId == -1)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(_my_context);
                builder.setTitle("Error");
                builder.setMessage("Unable to save record into the database.");
                AlertDialog diag = builder.create();

                diag.show();
            }
        }
    }
}
