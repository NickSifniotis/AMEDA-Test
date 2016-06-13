package au.net.nicksifniotis.amedatest;

import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import au.net.nicksifniotis.amedatest.AMEDAManager.AMEDA;
import au.net.nicksifniotis.amedatest.AMEDAManager.AMEDAImplementation;

public class FamiliarisationActivity extends AppCompatActivity {
    private Handler _handler;
    private AMEDA _device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.familiarisation);

        _handler = new Handler(Looper.getMainLooper());
        try
        {
            _device = new AMEDAImplementation(this, _handler, false);
        }
        catch (Exception e)
        {
            makeToast("Unable to connect to AMEDA in constructor");
        }
    }


    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        _device.Terminate();
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        _device.Terminate();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        _device.Connect();
    }

    public void btn_famil_1(View view)
    {
        execute (1, R.id.famil_text_1);
    }

    public void btn_famil_2(View view)
    {
        execute (2, R.id.famil_text_2);
    }

    public void btn_famil_3(View view)
    {
        execute (3, R.id.famil_text_3);
    }

    public void btn_famil_4(View view)
    {
        execute (4, R.id.famil_text_4);
    }

    public void btn_famil_5(View view)
    {
        execute (5, R.id.famil_text_5);
    }

    public void makeToast (String message)
    {
        Toast t = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        t.show();
    }

    private void execute(int num, int id)
    {
        try {
            _device.GoToPosition(num);

            TextView text = (TextView)(findViewById(id));
            text.setText (Integer.toString(Integer.parseInt(text.getText().toString()) + 1));
        }
        catch (Exception e)
        {
            makeToast ("Error: " + e.getMessage());
        }
    }
}
