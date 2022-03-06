package com.app.gearvrcontrollerapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.AppOpsManager;
import android.app.Instrumentation;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.LinearLayout;

import com.app.gearvrcontrollerapp.New.ControllerInputManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;
import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;
import static android.view.KeyEvent.KEYCODE_ALL_APPS;
import static android.view.KeyEvent.KEYCODE_APP_SWITCH;
import static android.view.KeyEvent.KEYCODE_BACK;
import static android.view.KeyEvent.KEYCODE_HOME;
import static androidx.core.app.NotificationCompat.CATEGORY_SERVICE;

public class AppActivity extends AppCompatActivity {
    boolean mNotYetNotifying = true;
    private static final int GATT_INTERNAL_ERROR = 0x0081;
    private static final String CCC_DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805f9b34fb";
    public static final String TARGET_SERVICE_UUID = "4f63756c-7573-2054-6872-65656d6f7465";
    public static final String NOTIFICATIONS_CHARACTERISTIC_UUID = "c8c51726-81bc-483b-a052-f7a14ea3d281";
    public static final String WRITE_CHARACTERISTIC_UUID = "c8c51726-81bc-483b-a052-f7a14ea3d282";
    public static final String COMMAND_TO_WRITE = "00000800-0000-1000-8000-00805F9B34FB";
    public static final String COMMAND_TO_SENSOR = "00000100-0000-1000-8000-00805F9B34FB";

    private ControllerInputDisplay mControllerInputDisplay;
    private Context mContext;

    private BluetoothGatt gatt;

    private ControllerInputManager mControllerInputManager;
    private Instrumentation mSystemInstrumentation;

    private void log(String message){ Log.v("AppActivity",message); }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);
        mContext = this;

        //  START INITIAL ACTIVITY
        //Intent myIntent = new Intent(this, InitialActivity.class);
        //startActivity(myIntent);
        //Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        //startActivity(intent);

        checkOverlayPermission();
        stopService(new Intent(AppActivity.this, ControllerInputOverlayService.class));
        BroadcastReceiver mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                finish();
            }

        };
        IntentFilter filter = new IntentFilter("android.intent.CLOSE_ACTIVITY");
        registerReceiver(mReceiver, filter);
        showNotification();
        Intent svc = new Intent(this, ControllerInputOverlayService.class);
        //startService(svc);

        mSystemInstrumentation = new Instrumentation();
        //startService();

//        AppOpsManager appOpsManager = (AppOpsManager)mContext.getSystemService(Context.APP_OPS_SERVICE);
//        appOpsManager.


        mControllerInputDisplay = new ControllerInputDisplay(mContext, findViewById(R.id.app_activity_included_controller_values));
        mControllerInputDisplay.setListener(new ControllerInputDisplay.ControllerInputDisplayListener() {
            @Override
            public void onClickConnect() {
                beginConnect();
            }
            @Override
            public void onClickDisconnect() {
                gatt.disconnect();
            }
        });
        mControllerInputManager = new ControllerInputManager(mContext, createControllerInputManagerListener());
        //
        mControllerInputDisplay.onMacAddress(MAC_ADDRESS);
    }

    private static final String MAC_ADDRESS = "2C:BA:BA:2F:6C:8E";

    private void beginConnect(){
        //  SCAN FOR DEVICES
        log("beginConnect()");
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();

        List<ScanFilter> mArrayScanFilters = new ArrayList<ScanFilter>();
        mArrayScanFilters.add(new ScanFilter.Builder().setDeviceAddress(MAC_ADDRESS).build());

        ScanSettings mScanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .setReportDelay(0L)
                .build();
        log("start scan...");
        scanner.startScan(
                mArrayScanFilters,
                mScanSettings,
                new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                //  FOUND DEVICE BY MAC ADDRESS.
                foundDevice(result.getDevice());
            }
        });
    }
    private void foundDevice(BluetoothDevice bluetoothDevice){
        log("foundDevice with mac_address="+MAC_ADDRESS);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mControllerInputDisplay.onMacAddress(MAC_ADDRESS);
            }
        });
        actionConnectGatt(bluetoothDevice);
    }
    private void actionConnectGatt(BluetoothDevice bluetoothDevice){
        log("actionConnectGatt()");
        gatt = bluetoothDevice.connectGatt(mContext, false, createBluetoothGattCallback());
    }
    private BluetoothGattCallback createBluetoothGattCallback(){
        return new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                log("onConnectionStateChange____");
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    log("GATT_CALLBACK: onConnectionStateChange - STATE_CONNECTED");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mControllerInputDisplay.onConnectionStatus(true);
                        }
                    });
                    log("running discoverServices...");
                    gatt.discoverServices();
                } else {
                    log("GATT_CALLBACK: onConnectionStateChange - NOT CONNECTED! CLOSING GATT.");
                    gatt.close();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mControllerInputDisplay.onConnectionStatus(false);
                        }
                    });

                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                // Check if the service discovery succeeded. If not disconnect
                if (status == GATT_INTERNAL_ERROR) {
                    Log.e("AppActivity", "Service discovery failed");
                    gatt.disconnect();
                    return;
                }
                log("foundServices()");

                BluetoothGattCharacteristic bluetoothGattCharacteristicNotify =
                        gatt.getService(UUID.fromString(TARGET_SERVICE_UUID))
                                .getCharacteristic(UUID.fromString(NOTIFICATIONS_CHARACTERISTIC_UUID));
                gatt.setCharacteristicNotification(bluetoothGattCharacteristicNotify,true);

                List<BluetoothGattService> gattServices = gatt.getServices();
                for(int i=0;i<gattServices.size();i++){
                    log("Found Service: "+gattServices.get(i).getUuid());
                    if(gattServices.get(i).getUuid().toString().equals(TARGET_SERVICE_UUID)){
                        //  FOUND TARGET SERVICE
                        log("FOUND TARGET SERVICE!");
                        log("will now read characteristic for notifications...");
                        boolean mDidReadCharacteristic = gatt.readCharacteristic(
                                gattServices.get(i).getCharacteristic(UUID.fromString(WRITE_CHARACTERISTIC_UUID))
                        );
                        log("mDidReadCharacteristic = "+mDidReadCharacteristic);

                        //COMMAND_TO_WRITE

                        ////gattServices.get(i).getCharacteristic(UUID.fromString(NOTIFICATIONS_CHARACTERISTIC_UUID))
                    }
                }
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
                log("onCharacteristicRead____");
                if(characteristic.getUuid().toString().equals(WRITE_CHARACTERISTIC_UUID) && mNotYetNotifying){
                    BluetoothGattCharacteristic bluetoothGattCharacteristic = characteristic;
                    bluetoothGattCharacteristic.setValue(new byte[]{1,0});
                    //bluetoothGattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                    boolean didWriteCharacteristic = gatt.writeCharacteristic(bluetoothGattCharacteristic);
                    log("onCharacteristicRead - didWriteCharacteristic="+didWriteCharacteristic);
                    log("onCharacteristicRead - status="+status);
                    log("onCharacteristicRead - characteristic="+ Arrays.toString(characteristic.getValue()));
                }
//                if(characteristic.getUuid().toString().equals(NOTIFICATIONS_CHARACTERISTIC_UUID) && mNotYetNotifying){
//                    //  GET DESCRIPTOR (ccd, uuid=0x2902) | HAS NOTIFY & READ.
//                    byte[] properties = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
//                    BluetoothGattDescriptor bluetoothGattDescriptor = characteristic.getDescriptor(UUID.fromString(CCC_DESCRIPTOR_UUID));
//                    boolean didSetCharacteristicNotification = gatt.setCharacteristicNotification(characteristic,true);
//                    bluetoothGattDescriptor.setValue(properties);
//                    boolean didWriteDescriptor = gatt.writeDescriptor(bluetoothGattDescriptor);
//                    log("onCharacteristicRead - didSetCharacteristicNotification="+didSetCharacteristicNotification);
//                    log("onCharacteristicRead - didWriteDescriptor="+didWriteDescriptor);
//                    mNotYetNotifying = false;
//                }
            }


            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
                if(characteristic.getUuid().toString().equals(WRITE_CHARACTERISTIC_UUID) && mNotYetNotifying){
                    //  GET DESCRIPTOR (ccd, uuid=0x2902) | HAS NOTIFY & READ.
                    byte[] properties = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
                    BluetoothGattDescriptor bluetoothGattDescriptor =
                                    gatt.getService(UUID.fromString(TARGET_SERVICE_UUID))
                                    .getCharacteristic(UUID.fromString(NOTIFICATIONS_CHARACTERISTIC_UUID))
                                    .getDescriptor(UUID.fromString(CCC_DESCRIPTOR_UUID));
                    BluetoothGattCharacteristic bluetoothGattCharacteristicNotify =
                                    gatt.getService(UUID.fromString(TARGET_SERVICE_UUID))
                                    .getCharacteristic(UUID.fromString(NOTIFICATIONS_CHARACTERISTIC_UUID));
                    boolean didSetCharacteristicNotification =
                            gatt.setCharacteristicNotification(bluetoothGattCharacteristicNotify,true);

                    bluetoothGattDescriptor.setValue(properties);
                    log("onCharacteristicWrite - didSetCharacteristicNotification="+didSetCharacteristicNotification);
                    boolean didWriteDescriptor = gatt.writeDescriptor(bluetoothGattDescriptor);
                    log("onCharacteristicWrite - didWriteDescriptor="+didWriteDescriptor);
                    mNotYetNotifying = false;
                }
            }

            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorWrite(gatt, descriptor, status);
                if(descriptor.getUuid().equals(UUID.fromString(CCC_DESCRIPTOR_UUID))){
                    if(status == GATT_SUCCESS){
                        // Check if we were turning notify on or off
                        byte[] value = descriptor.getValue();
                        if(value != null){
                            boolean wasTurnedOn = (value[0] != 0);
                            log("onDescriptorWrite - wasTurnedOn="+wasTurnedOn);
                        }


                    }
                }
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
                //log("onCharacteristicChanged: characteristic="+ Arrays.toString(characteristic.getValue()));
//                boolean triggerPressed = (characteristic.getValue()[58] & (1 << 0)) == 1;
//                if(triggerPressed){
//                    incrementSystemVol(true);
//                }
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mControllerInputDisplay.onTrigger(triggerPressed);
//                    }
//                });
                mControllerInputManager.onEventData(characteristic.getValue());
            }


            @Override
            public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorRead(gatt, descriptor, status);
                log("onDescriptorRead_____");
            }
        };
    }

    private void runShellCommand(String command) throws Exception {
        Process process = Runtime.getRuntime().exec(command);
        process.waitFor();
    }

    private void incrementSystemVol(boolean up){
        AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.adjustVolume(up?AudioManager.ADJUST_RAISE:AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
    }

    private ControllerInputManager.ControllerInputManagerListener createControllerInputManagerListener(){
        return new ControllerInputManager.ControllerInputManagerListener() {
            @Override
            public void onTouchpad(boolean shouldClick, int posX, int posY) {
                Intent intent = new Intent("your_action_name");
                intent.putExtra("shouldClick",shouldClick);
                intent.putExtra("posX",posX);
                intent.putExtra("posY",posY);
                sendBroadcast(intent);

                Intent intent2 = new Intent("INTENT_FILTER_ACTION_FROM_ACTIVITY_TO_ACCESSIBILITY");
                intent2.putExtra("touchpadPosX",posX);
                intent2.putExtra("touchpadPosY",posY);
                intent2.putExtra("onClickTouchpad",shouldClick);
                sendBroadcast(intent2);


            }

            @Override
            public void onClickTrigger() {

            }

            @Override
            public void onClickBack() {
                //mSystemInstrumentation.sendKeyDownUpSync(KEYCODE_BACK);
                Intent intent = new Intent("INTENT_FILTER_ACTION_FROM_ACTIVITY_TO_ACCESSIBILITY");
                intent.putExtra("onClickBack",true);
                sendBroadcast(intent);
            }

            @Override
            public void onClickHome() {
                //mSystemInstrumentation.sendKeyDownUpSync(KEYCODE_HOME);
                Intent intent = new Intent("INTENT_FILTER_ACTION_FROM_ACTIVITY_TO_ACCESSIBILITY");
                intent.putExtra("onClickHome",true);
                sendBroadcast(intent);
            }

            @Override
            public void onClickVolUp() {
                //incrementSystemVol(true);
                Intent intent = new Intent("INTENT_FILTER_ACTION_FROM_ACTIVITY_TO_ACCESSIBILITY");
                intent.putExtra("onClickVolUp",true);
                sendBroadcast(intent);
            }

            @Override
            public void onClickVolDown() {
                //incrementSystemVol(false);
                Intent intent = new Intent("INTENT_FILTER_ACTION_FROM_ACTIVITY_TO_ACCESSIBILITY");
                intent.putExtra("onClickVolDown",true);
                sendBroadcast(intent);
            }

            @Override
            public void onLongClickHome() {
                Intent intent = new Intent("INTENT_FILTER_ACTION_FROM_ACTIVITY_TO_ACCESSIBILITY");
                intent.putExtra("onLongClickHome",true);
                sendBroadcast(intent);
            }

            @Override
            public void onLongClickBack() {
                Intent intent = new Intent("INTENT_FILTER_ACTION_FROM_ACTIVITY_TO_ACCESSIBILITY");
                intent.putExtra("onLongClickBack",true);
                sendBroadcast(intent);
            }
        };
    }


    /////////

    // method to ask user to grant the Overlay permission
    public void checkOverlayPermission(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                // send user to the device settings
                Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                startActivity(myIntent);
            }
        }
    }


    /////////

    private void showNotification(){

        Intent intent = new Intent("android.intent.CLOSE_ACTIVITY");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0 , intent, 0);


        createNotificationChannel();
//        PendingIntent pendingIntent =
//                PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Running")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Running")
                .setContentText("Tap to terminate service")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setOngoing(true)
                .setChannelId("channel1");
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Running";
            String description = "Shows an ongoing notification for ease of terminating service.";
            int importance = NotificationManager.IMPORTANCE_NONE;
            NotificationChannel channel = new NotificationChannel("channel1", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}