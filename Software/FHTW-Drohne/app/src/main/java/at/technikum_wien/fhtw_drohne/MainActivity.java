package at.technikum_wien.fhtw_drohne;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import at.technikum_wien.fhtw_drohne.ViewJoystick.OnJoystickMoveListener;
import at.technikum_wien.fhtw_drohne.ViewSlider.OnSliderMoveListener;


public class MainActivity extends ActionBarActivity {

    // Shared Preferences for global variables
    SharedPreferences mSharedPreferences;
    public static final String myPREFERENCES = "PrefSettings";
    public static final String mTSens = "Throttle_Sensitivity";
    public static final String mSSens = "Steering_Sensitivity";
    public static final String mRoll = "RollCal";
    public static final String mPitch = "PitchCal";
    public static final String mYaw = "YawCal";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    private ViewSlider mSlider;
    private ViewJoystick mJoystick;
    private TextView valueTTextView, valueXTextView, valueYTextView, valueZTextView;
    private ToggleButton mToggleArm, mToggleHold;

    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;

    /**
     * String buffer for outgoing messages
     */
    private StringBuffer mOutStringBuffer;

    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * Member object for the chat services
     */
    private BluetoothControlService mControlService = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // initialization of SharedPreferences
        mSharedPreferences = getSharedPreferences(myPREFERENCES, Context.MODE_PRIVATE);

        mSlider = (ViewSlider)findViewById(R.id.throttleView);
        mJoystick = (ViewJoystick)findViewById(R.id.joystickView);
        valueTTextView = (TextView)findViewById(R.id.txt_t);
        valueXTextView = (TextView)findViewById(R.id.txt_x);
        valueYTextView = (TextView)findViewById(R.id.txt_y);
        valueZTextView = (TextView)findViewById(R.id.txt_z);

        mToggleArm = (ToggleButton)findViewById(R.id.toggle_arm);
        mToggleHold = (ToggleButton)findViewById(R.id.toggle_hold);

        // If the adapter is null, then Bluetooth is not supported
        if(mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        /**
         * If BT is not on, request that it be enabled.
         * setupControl() will then be called during onActivityResult
         */
        if(!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the control session
        } else if (mControlService == null) {
            setupControl();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mControlService != null) {
            mControlService.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        /**
         * Performing this check in onResume() covers the case in which BT was
         * not enabled during onStart(), so we were paused to enable it ...
         * onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
         */
        if(mControlService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if(mControlService.getState() == BluetoothControlService.STATE_NONE) {
                // Start the Bluetooth chat service
                mControlService.start();
            }
        }

        /**
         * When returning to the main activity steering initial commands are sent,
         * but only if a device is connected.
         */
        if (mControlService.getState() == BluetoothControlService.STATE_CONNECTED) {
            int roll = mSharedPreferences.getInt(mRoll, 0);
            int pitch = mSharedPreferences.getInt(mPitch, 0);
            int yaw = mSharedPreferences.getInt(mYaw, 0);

            valueXTextView.setText("*2|" + String.valueOf(roll) + "#");
            valueYTextView.setText("*3|" + String.valueOf(pitch) + "#");
            valueZTextView.setText("*4|" + String.valueOf(yaw) + "#");
            sendCommand("*2|" + String.valueOf(roll) + "#");
            sendCommand("*3|" + String.valueOf(pitch) + "#");
            sendCommand("*4|" + String.valueOf(yaw) + "#");
        }
    }

    /**
     * Set up the UI and background operations for control
     */
    private void setupControl() {

        mSlider.setOnSliderMoveListener(new OnSliderMoveListener() {
            @Override
            public void onValueChanged(float value) {
                /**
                 * Read throttle sensitivity form SharedPreferences and the slider position.
                 * Then construct the control message that is sent via bluetooth.
                 */
                float sens = mSharedPreferences.getFloat(mTSens, 1);
                valueTTextView.setText("*1|"+String.valueOf(Math.round((value)*sens))+"#");
                sendCommand("*1|"+String.valueOf(Math.round((value)*sens))+"#");
            }
        }, ViewSlider.DEFAULT_LOOP_INTERVAL);

        mJoystick.setOnJoystickMoveListener(new OnJoystickMoveListener() {
            @Override
            public void onValueChanged(float valueX, float valueY) {
                /**
                 * Read sensitivity settings and calibration settings from SharedPreferences.
                 * Then calculate pitch and yaw from the joystick position.
                 */
                float sens = mSharedPreferences.getFloat(mSSens, 1);
                int roll_cal = mSharedPreferences.getInt(mRoll, 0);
                int pitch_cal = mSharedPreferences.getInt(mPitch, 0);
                int roll = Math.round(valueX*sens)+roll_cal;
                int pitch = Math.round(valueY*sens)+pitch_cal;

                // This is to ensure roll and pitch to be within their value range (-500, 500)
                if (roll > 500)
                    roll = 500;
                if (roll < -500)
                    roll = -500;

                if (pitch > 500)
                    pitch = 500;
                if (pitch < -500)
                    pitch = -500;

                /**
                 * Write control commands into textviews (for debugging purposes) and
                 * send the command via bluetooth.
                 */
                valueXTextView.setText("*2|"+String.valueOf(roll)+"#");
                valueYTextView.setText("*3|"+String.valueOf(pitch)+"#");
                sendCommand("*2|" + String.valueOf(roll) + "#");
                sendCommand("*3|"+String.valueOf(pitch)+"#");
            }
        }, ViewJoystick.DEFAULT_LOOP_INTERVAL);

        mToggleArm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked) {
                    // Send control command via bluetooth
                    sendCommand("*11#");
                }
                else {
                    // Send control command via bluetooth
                    sendCommand("*12#");
                }
            }
        });
        mToggleHold.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked) {
                    // Send control command via bluetooth
                    sendCommand("*13#");
                }
                else {
                    // Send control command via bluetooth
                    sendCommand("*14#");
                }
            }
        });

        // Initialize the BluetoothControlService to perform bluetooth connections
        mControlService = new BluetoothControlService(getApplicationContext(), mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    /**
     * Makes this device discoverable.
     */
    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a command.
     *
     * @param command A command-string to send
     */
    private void sendCommand(String command) {
        // Check that we're actually connected before trying anything
        if (mControlService.getState() != BluetoothControlService.STATE_CONNECTED) {
            Toast.makeText(getApplicationContext(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (command.length() > 0) {
            // Get the command bytes to tell the BluetoothControlService to write
            byte[] send = command.getBytes();
            mControlService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
        }
    }

    /**
     * Updates the status on the action bar
     *
     * @param resId a string resource ID
     */
    private void setStatus(int resId) {
        if (null == this) {
            return;
        }
        final ActionBar actionBar = this.getSupportActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(resId);
    }

    /**
     * Updates the status on the action bar
     *
     * @param subTitle status
     */
    private void setStatus(CharSequence subTitle) {
        if (null == this) {
            return;
        }
        final ActionBar actionBar = this.getSupportActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(subTitle);
    }

    /**
     * The Handler that gets information back from the BluetoothControlService
     */
    String mMsgBuf = "";
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothControlService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            break;
                        case BluetoothControlService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothControlService.STATE_LISTEN:
                        case BluetoothControlService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    mMsgBuf += new String(readBuf, 0, msg.arg1);

                    if (mMsgBuf.indexOf("#") >= 0){
                        String readMessage = mMsgBuf.substring(mMsgBuf.indexOf("*")+1, mMsgBuf.indexOf("#"));
                        mMsgBuf = mMsgBuf.substring(mMsgBuf.indexOf("#")+1, mMsgBuf.length());
                        Toast.makeText(getApplicationContext(), readMessage, Toast.LENGTH_LONG).show();
                    }
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != this) {
                        Toast.makeText(getApplicationContext(), "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != this) {
                        Toast.makeText(getApplicationContext(), msg.getData().getString(Constants.TOAST), Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a control session
                    setupControl();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Toast.makeText(getApplicationContext(), R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    /**
     * Establish connection with other device
     *
     * @param data An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true), Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mControlService.connect(device, secure);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_btsearching: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                return true;
            }
            case R.id.action_settings: {
                // Launch the SettingsActivity
                Intent serverIntent = new Intent(this, SettingsActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }
}
