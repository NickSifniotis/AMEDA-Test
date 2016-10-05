package au.net.nicksifniotis.amedatest.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import au.net.nicksifniotis.amedatest.AMEDA.AMEDAInstruction;
import au.net.nicksifniotis.amedatest.AMEDA.AMEDAInstructionEnum;
import au.net.nicksifniotis.amedatest.AMEDA.AMEDAResponse;
import au.net.nicksifniotis.amedatest.R;


/**
 * Calibrates the AMEDA.
 *
 * Not sure exactly how the process is supposed to work here, this class remains a @TODO
 *
 * For now, just go with the wireframe as given. Manual control, buttons for 'calibrate' and
 * for every stop between 1 and 5.
 */
public class CalibrationActivity extends AMEDAActivity
{
    private ImageView[] _images;


    /**
     * Loads up the Activity.
     *
     * @param savedInstanceState Not used.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calibration_activity);

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
     * When this activity starts, make all the smiley status indicators invisible.
     */
    @Override
    public void onStart()
    {
        super.onStart();

        for (ImageView v: _images)
            v.setImageDrawable(getDrawable(R.drawable.calibrate_grey));
    }


    /**
     * Connects the GUI elements to the variables that represent them.
     */
    private void _connect_gui ()
    {
        _images = new ImageView[6];

        _images[0] = (ImageView)findViewById(R.id.c_img_calibrate);
        Resources r = getResources();
        for (int i = 1; i <= 5; i ++)
            _images[i] = (ImageView)findViewById(r.getIdentifier("c_img_position_" + i, "id", "au.net.nicksifniotis.amedatest"));
    }


    /**
     * Handle the response from the AMEDA device.
     *
     * @param instruction The instruction that was sent to the AMEDA.
     * @param response The AMEDA's response to that instruction.
     */
    @Override
    protected void ProcessAMEDAResponse(AMEDAInstruction instruction, AMEDAResponse response)
    {
        AMEDAInstructionEnum instruction_code = instruction.GetInstruction();

        if (!instruction_code.IsValidResponse(response))
            FailAndDieDialog(getString(R.string.error_ameda_fail_desc));

        if (instruction_code == AMEDAInstructionEnum.CALIBRATE && response.GetCode() == AMEDAResponse.Code.CALIBRATION_FAIL)
            CalibrationFailedDialog();
        else if (instruction_code == AMEDAInstructionEnum.MOVE_TO_POSITION && response.GetCode() == AMEDAResponse.Code.CANNOT_MOVE)
            CannotMoveDialog();
        else if (instruction_code == AMEDAInstructionEnum.MOVE_TO_POSITION && response.GetCode() == AMEDAResponse.Code.WOBBLE_NO_RESPONSE)
            WobbleDeadDialog();
        else
            _images[instruction_code == AMEDAInstructionEnum.CALIBRATE ? 0 : instruction.GetN()].setImageDrawable(getDrawable(R.drawable.calibrate_green));
    }


    /**
     * Button press event handers.
     *
     * @param v Not used.
     */
    public void c_btn_calibrate(View v)
    {
        _handle_command_button_press(0);
    }

    public void c_btn_position_1(View v)
    {
        _handle_command_button_press(1);
    }

    public void c_btn_position_2(View v)
    {
        _handle_command_button_press(2);
    }

    public void c_btn_position_3(View v)
    {
        _handle_command_button_press(3);
    }

    public void c_btn_position_4(View v)
    {
        _handle_command_button_press(4);
    }

    public void c_btn_position_5(View v)
    {
        _handle_command_button_press(5);
    }

    public void c_btn_done(View v)
    {
        finish();
    }


    /**
     * Handle the user pressing a calibration control button.
     *
     * @param pressed The step to move the AMEDA to, or 0 if the user requested a calibration action.
     */
    private void _handle_command_button_press (int pressed)
    {
        if (pressed == 0)
            Calibrate();
        else
            GoToPosition(pressed);

        ExecuteNextInstruction();
    }


    /**
     * Dialog box to prompt the user into action if the AMEDA reports an error calibrating itself.
     */
    private void CalibrationFailedDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.error_title))
        .setMessage(getString(R.string.c_calibration_failure))
                .setPositiveButton(R.string.btn_done, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        RepeatInstruction();
                    }
                })
                .setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        ClearInstructions();
                        finish();
                    }
                });
        builder.create().show();
    }


    /**
     * Dialog box reporting non-response from wobble board.
     */
    private void WobbleDeadDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.error_title))
                .setMessage("No response received from the wobble board. Please check to make sure that it is switched on.")
                .setPositiveButton("Try Again",new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        RepeatInstruction();
                    }
                })
                .setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        ClearInstructions();
                        finish();
                    }
                });
        builder.create().show();
    }
}
