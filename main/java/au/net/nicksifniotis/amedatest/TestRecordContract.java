package au.net.nicksifniotis.amedatest;

import android.provider.BaseColumns;

/**
 * Created by nsifniotis on 23/05/16.
 */
public class TestRecordContract
{
    public TestRecordContract() {}

    public static abstract class TestRecordEntry implements BaseColumns
    {
        public static final String TABLE_NAME = "TestRecord";
        public static final String COL_NAME = "Name";
        public static final String COL_GENDER = "Gender";
        public static final String COL_EDUCATION = "Education";
        public static final String COL_ADDRESS = "Address";
        public static final String COL_HOBBIES = "Hobbies";
        public static final String COL_NOTES = "Notes";
    }
}
