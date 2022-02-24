package com.app.gearvrcontrollerapp;

import androidx.appcompat.app.AppCompatActivity;

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
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;
import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;

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

    private void log(String message){ Log.v("AppActivity",message); }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);
        mContext = this;
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
                log("onCharacteristicChanged: characteristic="+ Arrays.toString(characteristic.getValue()));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        boolean triggerPressed = (characteristic.getValue()[58] & (1 << 0)) == 1;
                        mControllerInputDisplay.onTrigger(triggerPressed);
                        if(triggerPressed){
                            incrementSystemVol(true);
                        }
                    }
                });
            }


            @Override
            public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorRead(gatt, descriptor, status);
                log("onDescriptorRead_____");
            }
        };
    }

    private void incrementSystemVol(boolean up){
        AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.adjustVolume(up?AudioManager.ADJUST_RAISE:AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
    }
}