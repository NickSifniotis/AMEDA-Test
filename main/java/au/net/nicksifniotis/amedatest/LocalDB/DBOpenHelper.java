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


    /**
     * These standard tests have been extracted from the five Excel spreadsheets sent
     * through to me by Gordon.
     * @param db
     */
    private void createStandardTests(SQLiteDatabase db)
    {
        String [] standard_tests = new String[]{
                "12451212435213434345123551234513245145123234534512",
                "51235511242512124352123234534512343434345132451451",
                "34321121411444311342434524245244131424553452234443",
                "51352234454133344435345123555541124412223435541342",
                "34345325212323412234345132424512125355114534511451" };

        for (int i = 0, j = standard_tests.length; i < j; i ++)
        {
            ContentValues values = new ContentValues();
            values.put(DB.StandardTestTable.ANSWER_KEY, standard_tests[i]);
            long newRowId;
            newRowId = db.insert(DB.StandardTestTable.TABLE_NAME, null, values);

            if (newRowId == -1)
                databaseError ("Unable to save record into the database.");
        }
    }


    /**
     * A database error has occured. Let the user know that the program isn't behaving the way
     * it is supposed to.
     *
     * @param error The error message to display to the user.
     */
    public void databaseError (String error)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(_my_context);
        builder.setTitle("Error");
        builder.setMessage(error);
        AlertDialog diag = builder.create();

        diag.show();
    }
}
