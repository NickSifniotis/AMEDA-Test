package au.net.nicksifniotis.amedatest.LocalDB;

import android.provider.BaseColumns;

/**
 * Holds all the database 'contracts' used by the application.
 *
 * Created by Nick Sifniotis on 23/05/16.
 */
public class DB
{
    public static final String filename = "AMEDA.db";
    public static final int version = 12;

    /**
     * Blank constructor, to make sure nobody accidentally instantiates a copy of this non-object.
     */
    public DB() {}

    public static abstract class PersonTable implements BaseColumns
    {
        public static final String TABLE_NAME = "_person";

        public static final String FIRST_NAME = "FirstName" + TABLE_NAME;
        public static final String SURNAME = "Surname" + TABLE_NAME;
        public static final String DOB = "DateOfBirth" + TABLE_NAME;
        public static final String GENDER = "Gender" + TABLE_NAME;
        public static final String HEIGHT = "Height" + TABLE_NAME;
        public static final String WEIGHT = "Weight" + TABLE_NAME;
        public static final String LAST_TEST_DATE = "LastTestDate" + TABLE_NAME;
        public static final String NOTES = "Notes" + TABLE_NAME;
        public static final String ADDRESS = "Address" + TABLE_NAME;
        public static final String ACTIVE = "Active" + TABLE_NAME;


        public static final String CreateSQL = "CREATE TABLE " + TABLE_NAME + " ("
                                                + _ID + " INTEGER PRIMARY KEY, "
                                                + FIRST_NAME + " TEXT, "
                                                + SURNAME + " TEXT, "
                                                + DOB + " TEXT, "
                                                + GENDER + " TEXT, "
                                                + HEIGHT + " INTEGER, "
                                                + WEIGHT + " INTEGER, "
                                                + NOTES + " TEXT, "
                                                + LAST_TEST_DATE + " INTEGER, "
                                                + ADDRESS + " TEXT, "
                                                + ACTIVE + " INTEGER)";
        public static final String DestroySQL = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class TestTable implements BaseColumns
    {
        public static final String TABLE_NAME = "_test";

        public static final String DATE = "Date" + TABLE_NAME;
        public static final String PERSON_ID = "PersonId" + TABLE_NAME;
        public static final String STANDARD_TEST_ID = "StandardId" + TABLE_NAME;
        public static final String INTERRUPTED = "Interrupted" + TABLE_NAME;
        public static final String SCORE = "Score" + TABLE_NAME;
        public static final String NUM_QUESTIONS = "NumQuestions" + TABLE_NAME;
        public static final String ACTIVE = "Active" + TABLE_NAME;

        public static final String CreateSQL = "CREATE TABLE " + TABLE_NAME + " ("
                                                + _ID + " INTEGER PRIMARY KEY, "
                                                + PERSON_ID + " INTEGER, "
                                                + STANDARD_TEST_ID + " INTEGER, "
                                                + INTERRUPTED + " INTEGER, "
                                                + SCORE + " INTEGER, "
                                                + NUM_QUESTIONS + " INTEGER, "
                                                + DATE + " INTEGER, "
                                                + ACTIVE + " INTEGER)";
        public static final String DestroySQL = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class QuestionTable implements BaseColumns
    {
        public static final String TABLE_NAME = "_question";

        public static final String TEST_ID = "TestId" + TABLE_NAME;
        public static final String USER_ANSWER = "UserAnswer" + TABLE_NAME;
        public static final String QUESTION_NUMBER = "QuestionNumber" + TABLE_NAME;

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

        public static final String ANSWER_KEY = "AnswerKey" + TABLE_NAME;

        public static final String CreateSQL = "CREATE TABLE " + TABLE_NAME + " ("
                                                + _ID + " INTEGER PRIMARY KEY, "
                                                + ANSWER_KEY + " TEXT)";
        public static final String DestroySQL = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }
}
