package au.net.nicksifniotis.amedatest;

import android.provider.BaseColumns;

import java.util.Date;

/**
 * Created by nsifniotis on 23/05/16.
 */
public class DB
{
    public DB() {}

    public static abstract class PersonTable implements BaseColumns
    {
        public static final String TABLE_NAME = "_person";
        
        public static final String NAME = "Name";
        public static final String DOB = "date_of_birth";
        public static final String GENDER = "Gender";
        public static final String EDUCATION = "Education";
        public static final String ADDRESS = "Address";
        public static final String HOBBIES = "Hobbies";
        public static final String NOTES = "Notes";
    }

    public static abstract class TestTable implements BaseColumns
    {
        public static final String TABLE_NAME = "_test";

        public static final String DATE = "Date";
        public static final String PERSON_ID = "PersonId";
    }

    public static abstract class QuestionTable implements BaseColumns
    {
        public static final String TABLE_NAME = "_question";

        public static final String TEST_ID = "TestId";
        public static final String USER_ANSWER = "UserAnswer";
        public static final String CORRECT_ANSWER = "CorrectAnswer";
    }
}
