package au.net.nicksifniotis.amedatest;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Database helper class that uses the contracts and strings inside of DB.
 *
 * Created by nsifniotis on 23/05/16.
 */
public class DBOpenHelper extends SQLiteOpenHelper
{
    public DBOpenHelper(Context context)
    {
        super(context, DB.filename, null, DB.version);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(DB.PersonTable.CreateSQL);
        db.execSQL(DB.TestTable.CreateSQL);
        db.execSQL(DB.QuestionTable.CreateSQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL(DB.PersonTable.DestroySQL);
        db.execSQL(DB.TestTable.DestroySQL);
        db.execSQL(DB.QuestionTable.DestroySQL);
        
        onCreate(db);
    }
}
