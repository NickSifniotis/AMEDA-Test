package au.net.nicksifniotis.amedatest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

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
    }
}
