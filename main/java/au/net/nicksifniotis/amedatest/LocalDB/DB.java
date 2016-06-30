package au.net.nicksifniotis.amedatest.LocalDB;

import android.provider.BaseColumns;

/**
 * Holds all the database 'contracts' used by the application.
 *
 * Created by nsifniotis on 23/05/16.
 */
public class DB
{
    public static final String filename = "AMEDA.db";
    public static final int version = 7;

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
        public static final String HEIGHT = "Height";
        public static final String WEIGHT = "Weight";
        public static final String NOTES = "Notes";
        public static final String ACTIVE = "Active";


        public static final String CreateSQL = "CREATE TABLE " + TABLE_NAME + " ("
                                                + _ID + " INTEGER PRIMARY KEY, "
                                                + NAME + " TEXT, "
                                                + DOB + " TEXT, "
                                                + GENDER + " TEXT, "
                                                + HEIGHT + " INTEGER, "
                                                + WEIGHT + " INTEGER, "
                                                + NOTES + " TEXT, "
                                                + ACTIVE + " INTEGER)";
        public static final String DestroySQL = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class TestTable implements BaseColumns
    {
        public static final String TABLE_NAME = "_test";

        public static final String DATE = "Date";
        public static final String PERSON_ID = "PersonId";
        public static final String STANDARD_TEST_ID = "StandardId";
        public static final String INTERRUPTED = "Interrupted";
        public static final String FINISHED = "Finished";
        public static final String ACTIVE = "Active";

        public static final String CreateSQL = "CREATE TABLE " + TABLE_NAME + " ("
                                                + _ID + " INTEGER PRIMARY KEY, "
                                                + PERSON_ID + " INTEGER, "
                                                + STANDARD_TEST_ID + " INTEGER, "
                                                + INTERRUPTED + " INTEGER, "
                                                + FINISHED + " INTEGER, "
                                                + DATE + " TEXT, "
                                                + ACTIVE + " INTEGER)";
        public static final String DestroySQL = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class QuestionTable implements BaseColumns
    {
        public static final String TABLE_NAME = "_question";

        public static final String TEST_ID = "TestId";
        public static final String USER_ANSWER = "UserAnswer";
        public static final String QUESTION_NUMBER = "QuestionNumber";

        public static final String CreateSQL = "CREATE TABLE " + TABLE_NAME + " ("
                                                + _ID + " INTEGER PRIMARY KEY, "
                                                + TEST_ID + " INTEGER, "
                                                + USER_ANSWER + " INTEGER, "
                                                + QUESTION_NUMBER + " INTEGER)";
        public static final String DestroySQL = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class StandardTestTable implements BaseColumns
    {
        public static final String TABLE_NAME = "_standard_test";

        public static final String ANSWER_KEY = "AnswerKey";

        public static final String CreateSQL = "CREATE TABLE " + TABLE_NAME + " ("
                                                + _ID + " INTEGER PRIMARY KEY, "
                                                + ANSWER_KEY + " TEXT)";
        public static final String DestroySQL = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }
}
