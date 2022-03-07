package com.app.gearvrcontrollerapp.Utils;

import android.bluetooth.BluetoothAdapter;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;

public class AppBluetoothUtil {
    private static final String CCC_DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805f9b34fb";
    private static final int GATT_INTERNAL_ERROR = 0x0081;

    private Context mContext;
    private String mMacAddress = "";

    private BluetoothGatt mGatt;

    private AppBluetoothUtilListener mListener;
    private TargetDeviceDefinitionBase mTargetDeviceDefinition;



    private boolean mNotYetNotifying = true;


    /** CONSTRUCTOR  **/
    public AppBluetoothUtil(Context mContext, TargetDeviceDefinitionBase mTargetDeviceDefinition, AppBluetoothUtilListener mListener){
        this.mContext = mContext;
        this.mTargetDeviceDefinition = mTargetDeviceDefinition;
        this.mListener = mListener;
    }

    /** CONNECT TO MAC ADDRESS  **/
    public void connectToMacAddress(String macAddress){
        //  SCAN FOR MAC ADDRESS
        this.mMacAddress = macAddress;
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothLeScanner mBluetoothScanner = mBluetoothAdapter.getBluetoothLeScanner();
        List<ScanFilter> mArrayScanFilters = new ArrayList<ScanFilter>();
        mArrayScanFilters.add(new ScanFilter.Builder().setDeviceAddress(mMacAddress).build());
        ScanSettings mScanSettings =
                new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                        .setReportDelay(0L)
                        .build();
        mBluetoothScanner.startScan(
                mArrayScanFilters,
                mScanSettings,
                new ScanCallback() {
                    @Override
                    public void onScanResult(int callbackType, ScanResult result) {
                        super.onScanResult(callbackType, result);
                        /** FOUND DEVICE BY MAC ADDRESS **/
                        //  CONNECT GATT
                        mGatt = result.getDevice().connectGatt(mContext, false, createGattCallback());
                    }
                });
    }
    /** GATT CALLBACK   **/
    private BluetoothGattCallback createGattCallback(){
        return new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    //  CONNECTED
                    //  RUN DISCOVER SERVICES
                    mListener.onConnected();
                    gatt.discoverServices();
                } else {
                    //  DISCONNECTED
                    mListener.onDisconnected();
                    gatt.close();
                }
            }
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);

                if (status == GATT_INTERNAL_ERROR) {
                    //  FAILED TO DISCOVER SERVICES
                    mListener.onError(AppBluetoothUtilListener.ERROR_DISCOVER_SERVICES_FAILED);
                    gatt.disconnect();
                    return;
                }

                BluetoothGattCharacteristic bluetoothGattCharacteristicNotify =
                        gatt.getService(UUID.fromString(mTargetDeviceDefinition.SERVICE_UUID))
                                .getCharacteristic(UUID.fromString(mTargetDeviceDefinition.NOTIFICATIONS_CHARACTERISTIC_UUID));
                gatt.setCharacteristicNotification(bluetoothGattCharacteristicNotify,true);

                List<BluetoothGattService> gattServices = gatt.getServices();
                for(int i=0;i<gattServices.size();i++){
                    //log("Found Service: "+gattServices.get(i).getUuid());
                    if(gattServices.get(i).getUuid().toString().equals(mTargetDeviceDefinition.SERVICE_UUID)){
                        //  FOUND TARGET SERVICE
                        //log("FOUND TARGET SERVICE!");
                        //log("will now read characteristic for notifications...");
                        boolean mDidReadCharacteristic = gatt.readCharacteristic(
                                gattServices.get(i).getCharacteristic(UUID.fromString(mTargetDeviceDefinition.WRITE_CHARACTERISTIC_UUID))
                        );
                        //log("mDidReadCharacteristic = "+mDidReadCharacteristic);
                    }
                }
            }
            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
                //log("onCharacteristicRead____");
                if(characteristic.getUuid().toString().equals(mTargetDeviceDefinition.WRITE_CHARACTERISTIC_UUID) && mNotYetNotifying){
                    BluetoothGattCharacteristic bluetoothGattCharacteristic = characteristic;
                    bluetoothGattCharacteristic.setValue(new byte[]{1,0});
                    //bluetoothGattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                    boolean didWriteCharacteristic = gatt.writeCharacteristic(bluetoothGattCharacteristic);
                    //log("onCharacteristicRead - didWriteCharacteristic="+didWriteCharacteristic);
                    //log("onCharacteristicRead - status="+status);
                    //log("onCharacteristicRead - characteristic="+ Arrays.toString(characteristic.getValue()));
                }
            }
            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
                if(characteristic.getUuid().toString().equals(mTargetDeviceDefinition.WRITE_CHARACTERISTIC_UUID) && mNotYetNotifying){
                    //  GET DESCRIPTOR (ccd, uuid=0x2902) | HAS NOTIFY & READ.
                    byte[] properties = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
                    BluetoothGattDescriptor bluetoothGattDescriptor =
                            gatt.getService(UUID.fromString(mTargetDeviceDefinition.SERVICE_UUID))
                                    .getCharacteristic(UUID.fromString(mTargetDeviceDefinition.NOTIFICATIONS_CHARACTERISTIC_UUID))
                                    .getDescriptor(UUID.fromString(CCC_DESCRIPTOR_UUID));
                    BluetoothGattCharacteristic bluetoothGattCharacteristicNotify =
                            gatt.getService(UUID.fromString(mTargetDeviceDefinition.SERVICE_UUID))
                                    .getCharacteristic(UUID.fromString(mTargetDeviceDefinition.NOTIFICATIONS_CHARACTERISTIC_UUID));
                    boolean didSetCharacteristicNotification =
                            gatt.setCharacteristicNotification(bluetoothGattCharacteristicNotify,true);

                    bluetoothGattDescriptor.setValue(properties);
                    //log("onCharacteristicWrite - didSetCharacteristicNotification="+didSetCharacteristicNotification);
                    boolean didWriteDescriptor = gatt.writeDescriptor(bluetoothGattDescriptor);
                    //log("onCharacteristicWrite - didWriteDescriptor="+didWriteDescriptor);
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
                        }
                    }
                }
            }
            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
                mListener.onEventData(characteristic.getUuid().toString(),characteristic.getValue());
            }
            @Override
            public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorRead(gatt, descriptor, status);
            }
        };
    }

    /** DEVICE DEFINITION BASE  **/
    public static class TargetDeviceDefinitionBase{
        public String SERVICE_UUID, NOTIFICATIONS_CHARACTERISTIC_UUID, WRITE_CHARACTERISTIC_UUID = "";

        public TargetDeviceDefinitionBase(String SERVICE_UUID, String NOTIFICATIONS_CHARACTERISTIC_UUID, String WRITE_CHARACTERISTIC_UUID){
            this.SERVICE_UUID = SERVICE_UUID;
            this.NOTIFICATIONS_CHARACTERISTIC_UUID = NOTIFICATIONS_CHARACTERISTIC_UUID;
            this.WRITE_CHARACTERISTIC_UUID = WRITE_CHARACTERISTIC_UUID;
        }
    }
    /** GEAR VR CONTROLLER - DEFINITION **/
    public static class TargetDeviceDefinitionGearVRController extends TargetDeviceDefinitionBase{
        public TargetDeviceDefinitionGearVRController(){
            super("4f63756c-7573-2054-6872-65656d6f7465",
                    "c8c51726-81bc-483b-a052-f7a14ea3d281",
                    "c8c51726-81bc-483b-a052-f7a14ea3d282");
        }
    }


    /** LISTENER  **/
    public interface AppBluetoothUtilListener{
        int ERROR_DISCOVER_SERVICES_FAILED = 0;

        void onConnected();
        void onDisconnected();
        void onError(int error);
        //mControllerInputManager.onEventData(characteristic.getValue());
        void onEventData(String characteristicUUID, byte[] eventData);
    }




}
