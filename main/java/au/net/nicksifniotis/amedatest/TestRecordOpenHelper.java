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
    private static int DATABASE_VERSION = 2;
    private static final String CREATE_SQL = "CREATE TABLE " + DB.PersonTable.TABLE_NAME
            + " (" + DB.PersonTable._ID + " INTEGER PRIMARY KEY, "
            + DB.PersonTable.NAME + " TEXT, "
            + DB.PersonTable.GENDER + " TEXT, "
            + DB.PersonTable.EDUCATION + " TEXT, "
            + DB.PersonTable.ADDRESS + " TEXT, "
            + DB.PersonTable.HOBBIES + " TEXT, "
            + DB.PersonTable.NOTES + " TEXT, "
            + DB.PersonTable.COL_DATE + " TEXT)";
    private static final String DELETE_SQL = "DROP TABLE IF EXISTS " + DB.PersonTable.TABLE_NAME;


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
