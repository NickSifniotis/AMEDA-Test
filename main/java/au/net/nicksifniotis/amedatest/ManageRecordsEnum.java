package au.net.nicksifniotis.amedatest;

import au.net.nicksifniotis.amedatest.activities.NewRecordActivity;
import au.net.nicksifniotis.amedatest.activities.TestActivity;

/**
 * An enumeration class that controls the activities that selecting an entry in ManageRecord
 * triggers.
 *
 * At the time of writing, the only two activities that are triggered are New/Edit Record
 * and Test. But who knows, others may be implemented in the future.
 */
public enum ManageRecordsEnum
{
    EDIT_RECORD(NewRecordActivity.class, R.string.amr_edit_descriptor),
    START_TEST(TestActivity.class, R.string.amr_test_descriptor);

    private Class _activity;
    private int _descriptor_resource;


    /**
     * Private constructor that sets the instance fields for the elements in this enumeration.
     *
     * @param c The Activity class that this instance launches.
     * @param s The resource id for the string resource that describes this instance.
     */
    ManageRecordsEnum(Class c, int s)
    {
        this._activity = c;
        this._descriptor_resource = s;
    }


    /**
     * Public accessor methods.
     *
     * @return The value being sought.
     */
    public Class Activity()
    {
        return this._activity;
    }

    public int Descriptor()
    {
        return this._descriptor_resource;
    }
}
