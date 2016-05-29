package au.net.nicksifniotis.amedatest;

import android.provider.BaseColumns;

import java.util.Date;

/**
 * Holds all the database 'contracts' used by the application.
 *
 * Created by nsifniotis on 23/05/16.
 */
public class DB
{
    public static final String filename = "AMEDA.db";
    public static final int version = 3;

    /**
     * Blank constructor, to make sure nobody accidentally instantiates a copy of this non-object.
     */
    public DB() {}

    public static abstract class PersonTable implements BaseColumns
    {
        public static final String TABLE_NAME = "_person";

        public static final String NAME = "Name";
        public static final String DOB = "DateOfBirth";
        public static final String GENDER = "Gender";
        public static final String EDUCATION = "Education";
        public static final String ADDRESS = "Address";
        public static final String HOBBIES = "Hobbies";
        public static final String NOTES = "Notes";


        public static final String CreateSQL = "CREATE TABLE " + TABLE_NAME + " ("
                                                + _ID + " INTEGER PRIMARY KEY, "
                                                + NAME + " TEXT, "
                                                + DOB + " TEXT, "
                                                + GENDER + " TEXT, "
                                                + EDUCATION + " TEXT, "
                                                + ADDRESS + " TEXT, "
                                                + HOBBIES + " TEXT, "
                                                + NOTES + " TEXT)";
        public static final String DestroySQL = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class TestTable implements BaseColumns
    {
        public static final String TABLE_NAME = "_test";

        public static final String DATE = "Date";
        public static final String PERSON_ID = "PersonId";

        public static final String CreateSQL = "CREATE TABLE " + TABLE_NAME + " ("
                                                + _ID + " INTEGER PRIMARY KEY, "
                                                + PERSON_ID + " INTEGER, "
                                                + DATE + " TEXT)";
        public static final String DestroySQL = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class QuestionTable implements BaseColumns
    {
        public static final String TABLE_NAME = "_question";

        public static final String TEST_ID = "TestId";
        public static final String USER_ANSWER = "UserAnswer";
        public static final String CORRECT_ANSWER = "CorrectAnswer";

        public static final String CreateSQL = "CREATE TABLE " + TABLE_NAME + " ("
                                                + _ID + " INTEGER PRIMARY KEY, "
                                                + TEST_ID + " INTEGER, "
                                                + USER_ANSWER + " INTEGER, "
                                                + CORRECT_ANSWER + " INTEGER)";
        public static final String DestroySQL = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }
}
