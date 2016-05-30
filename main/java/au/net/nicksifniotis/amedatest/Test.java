package au.net.nicksifniotis.amedatest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.View;
import android.widget.LinearLayout;

import java.util.Random;

/**
 * Test activity. Launches a test and runs it from go to whoa.
 *
 * Created by nsifniotis on 29/05/16.
 */
public class Test extends AppCompatActivity
{
    private static final int NUM_QUESTIONS = 50;

    private int [] test_questions;
    private int [] user_responses;
    private int current_question;
    private TestState current_state;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // righto, generate a test
        // @TODO replace this random number generator with the actual tests provided by the team
        test_questions = new int[50];
        user_responses = new int[50];

        Random r = new Random();
        for (int i = 0; i < 50; i ++)
            test_questions[i] = r.nextInt(5);

        current_question = 0;
        current_state = TestState.STARTING;
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
}
