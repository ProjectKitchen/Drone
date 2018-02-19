package at.technikum_wien.fhtw_drohne;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


public class SettingsActivity extends ActionBarActivity {

    // Shared Preferences for global variables
    SharedPreferences mSharedPreferences;
    public static final String myPREFERENCES = "PrefSettings";
    public static final String mTSens = "Throttle_Sensitivity";
    public static final String mSSens = "Steering_Sensitivity";
    public static final String mRoll = "RollCal";
    public static final String mPitch = "PitchCal";
    public static final String mYaw = "YawCal";

    private SeekBar m_SeekTSens, m_SeekSSens, m_SeekRoll, m_SeekPitch, m_SeekYaw;
    private TextView sensTTextView, sensSTextView, rollTextView, pitchTextView, yawTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mSharedPreferences = getSharedPreferences(myPREFERENCES, Context.MODE_PRIVATE);

        m_SeekTSens = (SeekBar)findViewById(R.id.seek_throttle_sens);
        m_SeekSSens = (SeekBar)findViewById(R.id.seek_steer_sens);
        m_SeekRoll = (SeekBar)findViewById(R.id.seek_roll);
        m_SeekPitch = (SeekBar)findViewById(R.id.seek_pitch);
        m_SeekYaw = (SeekBar)findViewById(R.id.seek_yaw);

        /**
         * The SeekBars for the setting of the sensitivities and steering calibration
         * are set to the value stored in the SharedPreferences.
         */
        m_SeekTSens.setProgress((int)(mSharedPreferences.getFloat(mTSens, 1)*1000));
        m_SeekSSens.setProgress((int)(mSharedPreferences.getFloat(mSSens, 1)*1000));
        m_SeekRoll.setProgress((int)Math.round((double)mSharedPreferences.getInt(mRoll, 0)/100*500+500));
        m_SeekPitch.setProgress((int)Math.round((double)mSharedPreferences.getInt(mPitch, 0)/100*500+500));
        m_SeekYaw.setProgress((int)Math.round((double)mSharedPreferences.getInt(mYaw, 0)/100*500+500));

        sensTTextView = (TextView)findViewById(R.id.txt_throttle_sens);
        sensSTextView = (TextView)findViewById(R.id.txt_steer_sens);
        rollTextView = (TextView)findViewById(R.id.txt_roll);
        pitchTextView = (TextView)findViewById(R.id.txt_pitch);
        yawTextView = (TextView)findViewById(R.id.txt_yaw);

        /**
         * The TextVies showing the setting of the sensitivities and steering calibration
         * are set to the value stored in the SharedPreferences.
         */
        sensTTextView.setText(String.valueOf(mSharedPreferences.getFloat(mTSens, 1)));
        sensSTextView.setText(String.valueOf(mSharedPreferences.getFloat(mSSens, 1)));
        rollTextView.setText(String.valueOf(mSharedPreferences.getInt(mRoll, 0)));
        pitchTextView.setText(String.valueOf(mSharedPreferences.getInt(mPitch, 0)));
        yawTextView.setText(String.valueOf(mSharedPreferences.getInt(mYaw, 0)));
    }

    @Override
    public void onStart() {
        super.onStart();

        /**
         * Listeners for all SeekBars in the settings menu.
         */
        m_SeekTSens.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                /**
                 *  SharedPreferences are written here. Data that is saved here
                 *  will be available in other activities of this Application, and after
                 *  Application restart as well.
                 */
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putFloat(mTSens, (float) i / 1000);
                editor.commit();
                sensTTextView.setText(String.valueOf((double) i / 1000));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putFloat(mTSens, (float)seekBar.getProgress()/1000);
                editor.commit();
                sensTTextView.setText(String.valueOf((double)seekBar.getProgress()/1000));
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putFloat(mTSens, (float)seekBar.getProgress()/1000);
                editor.commit();
                sensTTextView.setText(String.valueOf((double) seekBar.getProgress() / 1000));
            }
        });
        m_SeekSSens.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putFloat(mSSens, (float) i / 1000);
                editor.commit();
                sensSTextView.setText(String.valueOf((double) i / 1000));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putFloat(mSSens, (float)seekBar.getProgress()/1000);
                editor.commit();
                sensSTextView.setText(String.valueOf((double)seekBar.getProgress()/1000));
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putFloat(mSSens, (float)seekBar.getProgress()/1000);
                editor.commit();
                sensSTextView.setText(String.valueOf((double) seekBar.getProgress() / 1000));
            }
        });
        m_SeekRoll.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putInt(mRoll, (int) Math.round(((double) i - 500) / 500 * 100));
                editor.commit();
                rollTextView.setText(String.valueOf(Math.round(((double)i-500)/500 * 100)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putInt(mRoll, (int)Math.round(((double)seekBar.getProgress()-500)/500 * 100));
                editor.commit();
                rollTextView.setText(String.valueOf(Math.round(((double)seekBar.getProgress()-500)/500 * 100)));
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putInt(mRoll, (int)Math.round(((double)seekBar.getProgress()-500)/500 * 100));
                editor.commit();
                rollTextView.setText(String.valueOf(Math.round(((double)seekBar.getProgress()-500)/500 * 100)));
            }
        });
        m_SeekPitch.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putInt(mPitch, (int)Math.round(((double)i-500)/500 * 100));
                editor.commit();
                pitchTextView.setText(String.valueOf(Math.round(((double)i-500)/500 * 100)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putInt(mPitch, (int)Math.round(((double)seekBar.getProgress()-500)/500 * 100));
                editor.commit();
                pitchTextView.setText(String.valueOf(Math.round(((double)seekBar.getProgress()-500)/500 * 100)));
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putInt(mPitch, (int)Math.round(((double)seekBar.getProgress()-500)/500 * 100));
                editor.commit();
                pitchTextView.setText(String.valueOf(Math.round(((double)seekBar.getProgress()-500)/500 * 100)));
            }
        });
        m_SeekYaw.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putInt(mYaw, (int)Math.round(((double)i-500)/500 * 100));
                editor.commit();
                yawTextView.setText(String.valueOf(Math.round(((double)i-500)/500 * 100)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putInt(mYaw, (int)Math.round(((double)seekBar.getProgress()-500)/500 * 100));
                editor.commit();
                yawTextView.setText(String.valueOf(Math.round(((double)seekBar.getProgress()-500)/500 * 100)));
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putInt(mYaw, (int)Math.round(((double)seekBar.getProgress()-500)/500 * 100));
                editor.commit();
                yawTextView.setText(String.valueOf(Math.round(((double)seekBar.getProgress()-500)/500 * 100)));
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
