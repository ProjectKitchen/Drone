package at.technikum_wien.fhtw_drohne;

/**
 * Defines several constants used between {@link BluetoothControlService} and the UI.
 */
public interface Constants {

    // Message types sent from the BluetoothControlService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothControlService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Control Constants
    public static final int THROTTLE = 1;
    public static final int ROLL = 2;
    public static final int PITCH = 3;
    public static final int YAW = 4;

}
