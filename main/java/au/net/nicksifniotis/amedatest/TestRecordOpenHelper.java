package au.net.nicksifniotis.amedatest;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by nsifniotis on 23/05/16.
 */
public class TestRecordOpenHelper extends SQLiteOpenHelper
{
    private static String DATABASE_NAME = "ameda.db";
    private static int DATABASE_VERSION = 1;
    private static final String CREATE_SQL = "CREATE TABLE " + TestRecordContract.TestRecordEntry.TABLE_NAME
            + " (" + TestRecordContract.TestRecordEntry._ID + " INTEGER PRIMARY KEY, "
            + TestRecordContract.TestRecordEntry.COL_NAME + " TEXT, "
            + TestRecordContract.TestRecordEntry.COL_GENDER + " TEXT, "
            + TestRecordContract.TestRecordEntry.COL_EDUCATION + " TEXT, "
            + TestRecordContract.TestRecordEntry.COL_ADDRESS + " TEXT, "
            + TestRecordContract.TestRecordEntry.COL_HOBBIES + " TEXT, "
            + TestRecordContract.TestRecordEntry.COL_NOTES + " TEXT)";
    private static final String DELETE_SQL = "DROP TABLE IF EXISTS " + TestRecordContract.TestRecordEntry.TABLE_NAME;


    public TestRecordOpenHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(CREATE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL(DELETE_SQL);
        onCreate(db);
    }
}
