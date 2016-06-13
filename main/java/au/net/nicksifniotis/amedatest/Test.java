package au.net.nicksifniotis.amedatest;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.Random;

import au.net.nicksifniotis.amedatest.AMEDAManager.AMEDA;
import au.net.nicksifniotis.amedatest.AMEDAManager.AMEDAImplementation;
import au.net.nicksifniotis.amedatest.AMEDAManager.AMEDAState;

/**
 * Test activity. Launches a test and runs it from go to whoa.
 *
 * Created by nsifniotis on 29/05/16.
 */
public class Test extends AppCompatActivity
{
    private static final int NUM_QUESTIONS = 5;

    private int [] test_questions;
    private int [] user_responses;
    private int current_question;
    private TestState current_state;
    private AMEDA device;
    private Handler _my_handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_layout);

        // righto, generate a test
        // @TODO replace this random number generator with the actual tests provided by the team
        test_questions = new int[NUM_QUESTIONS];
        user_responses = new int[NUM_QUESTIONS];

        Random r = new Random();
        for (int i = 0; i < NUM_QUESTIONS; i ++)
            test_questions[i] = r.nextInt(5) + 1;

        current_question = -1;
        current_state = TestState.STARTING;
        updateState();
        _my_handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                makeToast("Message received: " + msg.what);
                ameda_updated();
                return true;
            }
        });
    }


    /**
     * Disconnect from the AMEDA each time the user exits the activity. We don't want to be
     * holding on to this resource.
     *
     */
    @Override
    protected void onPause()
    {
        super.onPause();

        device.Terminate();
    }


    /**
     * (Re)connect to the AMEDA device when the activity is (resumed)shown.
     *
     */
    @Override
    protected void onResume()
    {
        super.onResume();

        try
        {
            device = new AMEDAImplementation(this, _my_handler, false);
        }
        catch (Exception e)
        {
            makeToast("Unable to create connection to AMEDA device, aborting.");
            finish();
        }

        nextQuestion();
    }


    private void ameda_updated()
    {
        if (device.Status() == AMEDAState.ERROR)
        {
            // we have a frikkin problem.
            makeToast("AMEDA is reporting an error. Aborting test.");
            finish();
        }

        switch (current_state)
        {
            case STARTING:
                // wtf?
                break;
            case SETTING:
                // have just finished moving to the position so set the next state.
                current_state = TestState.STEPPING;
                updateState();
                break;
            case STEPPING:
                break;
            case ANSWERING:
                break;
        }
    }

    private void nextQuestion ()
    {
        if (device.Status() == AMEDAState.READY)
        {
            current_question++;

            if (current_question >= NUM_QUESTIONS)
            {
                device.Terminate();
                finish();
                return;
            }

            setNewPosition(test_questions[current_question]);
        }
        else
        {
            makeToast("AMEDA is not able to advance to the next question.");
        }
    }


    private void setNewPosition (int pos)
    {
        makeToast("Setting device to position " + pos);
        current_state = TestState.SETTING;
        updateState();

        device.GoToPosition(pos);
    }


    private void updateState()
    {
        LinearLayout l;
        switch (current_state)
        {
            case STARTING:
                l = (LinearLayout)findViewById(R.id.test_plz_wait);
                l.setVisibility(View.GONE);

                l = (LinearLayout)findViewById(R.id.test_step_on);
                l.setVisibility(View.GONE);

                l = (LinearLayout)findViewById(R.id.test_question);
                l.setVisibility(View.GONE);

//                l = (LinearLayout)findViewById(R.id.test_plz_wait);
//                l.setVisibility(View.GONE);
                break;
            case SETTING:
                l = (LinearLayout)findViewById(R.id.test_plz_wait);
                l.setVisibility(View.VISIBLE);

                l = (LinearLayout)findViewById(R.id.test_step_on);
                l.setVisibility(View.GONE);

                l = (LinearLayout)findViewById(R.id.test_question);
                l.setVisibility(View.GONE);

                break;
            case STEPPING:
                l = (LinearLayout)findViewById(R.id.test_plz_wait);
                l.setVisibility(View.GONE);

                l = (LinearLayout)findViewById(R.id.test_step_on);
                l.setVisibility(View.VISIBLE);

                l = (LinearLayout)findViewById(R.id.test_question);
                l.setVisibility(View.GONE);
                break;
            case ANSWERING:
                l = (LinearLayout)findViewById(R.id.test_plz_wait);
                l.setVisibility(View.GONE);

                l = (LinearLayout)findViewById(R.id.test_step_on);
                l.setVisibility(View.GONE);

                l = (LinearLayout)findViewById(R.id.test_question);
                l.setVisibility(View.VISIBLE);

                break;
            case FINISHING:
                break;
        }
    }


    public void btn_Next (View view)
    {
        current_state = TestState.ANSWERING;
        updateState();
    }


    public void btn_1(View view)
    {
        // @TODO this should be a wtf
        if (current_state != TestState.ANSWERING)
            return;

        user_responses[current_question] = 1;

        nextQuestion();
    }


    public void btn_2(View view)
    {
        // @TODO this should be a wtf
        if (current_state != TestState.ANSWERING)
            return;

        user_responses[current_question] = 2;

        nextQuestion();
    }

    public void btn_3(View view)
    {
        // @TODO this should be a wtf
        if (current_state != TestState.ANSWERING)
            return;

        user_responses[current_question] = 3;

        nextQuestion();
    }

    public void btn_4(View view)
    {
        // @TODO this should be a wtf
        if (current_state != TestState.ANSWERING)
            return;

        user_responses[current_question] = 4;

        nextQuestion();
    }

    public void btn_5(View view)
    {
        // @TODO this should be a wtf
        if (current_state != TestState.ANSWERING)
            return;

        user_responses[current_question] = 5;

        nextQuestion();
    }

    public void makeToast (String message)
    {
        Toast t = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        t.show();
    }
}
