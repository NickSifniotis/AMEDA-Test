package au.net.nicksifniotis.amedatest.activities;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;
import java.util.Random;

import au.net.nicksifniotis.amedatest.AMEDA.AMEDAInstruction;
import au.net.nicksifniotis.amedatest.AMEDA.AMEDAResponse;
import au.net.nicksifniotis.amedatest.Globals;
import au.net.nicksifniotis.amedatest.R;


/**
 * Familiarisation task activity.
 */
public class FamiliarisationActivity extends AMEDAActivity
{
    private TextView[] _fields;
    private Button[] _buttons;


    /**
     * Launches the activity.
     *
     * Launch sounds so much better than create.
     *
     * @param savedInstanceState Restoration bundle from previous instance.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.familiarisation_activity);

        _connect_gui();

        Toolbar bar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(bar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle("");

        if (bar != null)
        {
            bar.setNavigationIcon(R.drawable.toolbar_back);
            bar.setNavigationOnClickListener(new View.OnClickListener()
            {
                /**
                 * Clicking the back arrow is equivalent to saying 'stop the test, I wanna get off'
                 * So record it as an interrupted test.
                 *
                 * @param v Not used. Poor v :(
                 */
                @Override
                public void onClick(View v)
                {
                    finish();
                }
            });
        }
    }


    /**
     * Connects the GUI elements to the local variables that represent them.
     */
    private void _connect_gui ()
    {
        Resources r = getResources();
        _fields = new TextView[6];
        _buttons = new Button[6];

        for (int i = 1; i <= 5; i ++)
        {
            int btn_id = r.getIdentifier("f_btn_" + i, "id", "au.net.nicksifniotis.amedatest");
            _buttons[i] = (Button) findViewById(btn_id);
            if (_buttons[i] != null)
                _buttons[i].setText(getString (R.string.f_goto_button, i));

            int txt_id = r.getIdentifier("f_txt_" + i, "id", "au.net.nicksifniotis.amedatest");
            _fields [i] = (TextView) findViewById(txt_id);

            if (_fields[i] != null)
                _fields[i].setText(getString(R.string.f_initial_count));
        }
    }


    /**
     * Button onClick event handlers.
     *
     * @param view Not used.
     */
    public void f_btn_1(View view)
    {
        execute (1);
    }

    public void f_btn_2(View view)
    {
        execute (2);
    }

    public void f_btn_3(View view)
    {
        execute (3);
    }

    public void f_btn_4(View view)
    {
        execute (4);
    }

    public void f_btn_5(View view)
    {
        execute (5);
    }

    public void f_btn_done(View view)
    {
        finish();
    }


    /**
     * Move the device to the requested position, for familiarisation_activity purposes.
     *
     * @param num The position to move the AMEDA to.
     */
    private void execute(int num)
    {
        if (_fields[num] != null)
        {
            int curr_value = Integer.parseInt(_fields[num].getText().toString());
            if (curr_value < 5)
            {
                GoToPosition(num);
                Beep(1);

                ExecuteNextInstruction();
            }
            else
                Globals.DebugToast.Send (getString(R.string.f_sorry_five));
        }
        else
            Globals.DebugToast.Send ("Strange error in that execute has been invoked with num=" + num);
    }


    /**
     * Interpret and respond to the AMEDA's response to the last instruction.
     *
     * @param instruction The instruction that was sent to the AMEDA.
     * @param response The AMEDA's response to that instruction.
     */
    @Override
    protected void ProcessAMEDAResponse (AMEDAInstruction instruction, AMEDAResponse response)
    {
        if (instruction.GetInstruction().IsValidResponse(response))
        {
            switch (response.GetCode())
            {
                case READY:
                    int n = instruction.GetN();
                    int curr_value = Integer.parseInt(_fields[n].getText().toString()) + 1;
                    _fields[n].setText(String.format(Locale.ENGLISH, "%d", curr_value));

                    if (curr_value >= 5)
                    {
                        _fields[n].setEnabled(false);
                        _buttons[n].setEnabled(false);
                    }

                    ExecuteNextInstruction();
                    break;
                case CANNOT_MOVE:
                    CannotMoveDialog();
                    break;
                default:
                    Globals.DebugToast.Send("Received unknown response code for current command.");
                    FailAndDieDialog(getString(R.string.error_ameda_fail_desc));
                    break;
            }
        }
        else
        {
            Globals.DebugToast.Send ("Received response " + response.toString() + " to command " + instruction.Build());
            FailAndDieDialog(getString(R.string.error_ameda_fail_desc));
        }
    }
}
