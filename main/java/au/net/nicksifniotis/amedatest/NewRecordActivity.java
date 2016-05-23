package au.net.nicksifniotis.amedatest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class NewRecordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_record);

        Spinner genders = (Spinner)findViewById(R.id.spn_Gender);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.genders, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genders.setAdapter(adapter);
    }

    public void btn_Cancel(View view)
    {
        finish();
    }

    public void btn_Done(View view)
    {
        String name = ((EditText)findViewById(R.id.txt_Name)).getText().toString();
        String gender = ((Spinner)findViewById(R.id.spn_Gender)).getSelectedItem().toString();
        String education = ((EditText)findViewById(R.id.txt_Education)).getText().toString();
        String address = ((EditText)findViewById(R.id.txt_Address)).getText().toString();
        String hobbies = ((EditText)findViewById(R.id.txt_Hobbies)).getText().toString();
        String notes = ((EditText)findViewById(R.id.txt_Notes)).getText().toString();
    }
}
