package com.app.gearvrcontrollerapp;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.UUID;


//usage: ControllerBluetoothManager.actionPair(bluetoothDevice);
public class ControllerBluetoothManager {
    private ControllerInputDisplay mControllerInputDisplay;

    private Context mContext;
    private PairInterface mPairListener;

    private BluetoothGattService mCustomService;
    private BluetoothGattCharacteristic mCharacteristicWrite, mCharacteristicRead;
    private BluetoothGatt mGatt;

    //        this.gattServer               = null;
    //        this.batteryService           = null;
    //        this.deviceInformationService = null;
    //        this.customService            = null;
    //        this.customServiceNotify      = null;
    //        this.customServiceWrite       = null;

    public ControllerBluetoothManager(Context mContext){
        this.mContext = mContext;
        mPairListener = new PairInterface() {
            @Override
            public void onGotDevice(BluetoothDevice bluetoothDevice) {
                //  GOT DEVICE -> GET GATT.
                bluetoothDevice.connectGatt(mContext, false, bluetoothGattCallback());
            }
            @Override
            public void onGotGatt(BluetoothGatt gatt) {
                //  GOT GATT -> STORE GATT -> GET CUSTOM SERVICE.
                mGatt = gatt;
                mGatt.discoverServices();
            }
            @Override
            public void onGotCustomService(BluetoothGattService bluetoothGattService) {
                //  STORE CUSTOM SERVICE -> GET WRITE CHARACTERISTIC
                mCustomService = bluetoothGattService;
                BluetoothGattCharacteristic bluetoothGattWriteCharacteristic = mCustomService.getCharacteristic(UUID.fromString(UUID_CUSTOM_SERVICE_WRITE));
                Log.v("LOG","bluetoothGattWriteCharacteristic isNull="+(bluetoothGattWriteCharacteristic==null));
                mPairListener.onGotWriteCharacteristic(bluetoothGattWriteCharacteristic);
            }
            @Override
            public void onGotWriteCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
                //  STORE WRITE CHARACTERISTIC -> GET READ CHARACTERISTIC
                mCharacteristicWrite = bluetoothGattCharacteristic;
                BluetoothGattCharacteristic bluetoothGattReadCharacteristic = mCustomService.getCharacteristic(UUID.fromString(UUID_CUSTOM_SERVICE_NOTIFY));
                mPairListener.onGotReadCharacteristic(bluetoothGattReadCharacteristic);
            }
            @Override
            public void onGotReadCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
                //  STORE READ CHARACTERISTIC -> START NOTIFICATIONS.
                mCharacteristicRead = bluetoothGattCharacteristic;

                mPairListener.startNotifications();
                Log.v("LOG","did start notifications!!!");
            }
            @Override
            public void startNotifications() {
                //payload=BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
//                for(int i=0;i<mCharacteristicWrite.getDescriptors().size();i++){
//                    Log.v("LOG","mCharacteristicWrite.getDescriptors()="+mCharacteristicWrite.getDescriptors().get(0));
//                }
                mGatt.readDescriptor(mCharacteristicRead.getDescriptors().get(0));
//                BluetoothGattDescriptor mDescriptor = mCharacteristicWrite.getDescriptor(UUID.fromString(UUID_CCCD_ID));
//                mDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//                mGatt.writeDescriptor(mDescriptor);
                Log.v("LOG","did start notifications!!!!!");


            }
        };
    }



    void actionPair(BluetoothDevice bluetoothDevice, ControllerInputDisplay mControllerInputDisplay){
        this.mControllerInputDisplay = mControllerInputDisplay;
        mPairListener.onGotDevice(bluetoothDevice);
    }
    void actionDisconnect(){

    }
    void actionRunCommand(){

    }


    BluetoothGattCallback bluetoothGattCallback(){
        return new BluetoothGattCallback() {

            @Override
            public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
                super.onMtuChanged(gatt, mtu, status);
                Log.v("LOG","onMtuChanged,status="+status+",mtu="+mtu);



                gatt.setCharacteristicNotification(gatt.getService(UUID.fromString(UUID_CUSTOM_SERVICE)).getCharacteristic(UUID.fromString(UUID_CUSTOM_SERVICE_NOTIFY)),true);



                BluetoothGattService bluetoothGattService = mGatt.getService(UUID.fromString(UUID_CUSTOM_SERVICE));
                mPairListener.onGotCustomService(bluetoothGattService);

                BluetoothGattDescriptor mDescriptor = gatt.getService(UUID.fromString(UUID_CUSTOM_SERVICE)).getCharacteristic(UUID.fromString(UUID_CUSTOM_SERVICE_NOTIFY)).getDescriptor(UUID.fromString(UUID_CCCD_ID));
                mDescriptor.setValue(new byte[]{8,0});
                mGatt.writeDescriptor(mDescriptor);
//                mDescriptor.setValue(new byte[]{1,0});
//                mGatt.writeDescriptor(mDescriptor);


            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
                Log.v("LOG","onCharacteristicRead:status="+status);
                for(int i=0;i<gatt.getServices().size();i++){
                    Log.v("LOG","onCharacteristicRead! gatt.getCharacteristics()="+gatt.getServices().get(i).getCharacteristics());
                    for(int a=0;a<gatt.getServices().get(i).getCharacteristics().size();a++){
                        Log.v("LOG","onCharacteristicRead! gatt.getCharacteristics()=INNER="+gatt.getServices().get(i).getCharacteristics().get(a).getUuid().toString());
                    }
                }
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
                Log.v("LOG","onCharacteristicWrite:status="+status);
            }

            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorWrite(gatt, descriptor, status);
                Log.v("LOG","onDescriptorWrite:status="+status);
//                BluetoothGattCharacteristic mCharacteristicActivateAlready = mCharacteristicWrite;
//                mCharacteristicActivateAlready.setValue(createBaseUUID("0800"));
//                mCharacteristicActivateAlready.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
//                gatt.writeCharacteristic(mCharacteristicActivateAlready);
            }

            @Override
            public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorRead(gatt, descriptor, status);
                Log.v("LOG","onDescriptorRead="+descriptor.getUuid());

                gatt.setCharacteristicNotification(mCharacteristicRead,true);

//                for(int i=0;i<descriptor.getDescriptors().size();i++){
//                    Log.v("LOG","descriptor)="+descriptor.getDescriptors().get(0));
//                }
                gatt.setCharacteristicNotification(gatt.getService(UUID.fromString(UUID_CUSTOM_SERVICE)).getCharacteristic(UUID.fromString(UUID_CUSTOM_SERVICE_NOTIFY)),true);
                BluetoothGattDescriptor mDescriptor = gatt.getService(UUID.fromString(UUID_CUSTOM_SERVICE)).getCharacteristic(UUID.fromString(UUID_CUSTOM_SERVICE_NOTIFY)).getDescriptor(UUID.fromString(UUID_CCCD_ID));//;mCharacteristicWrite.getDescriptor(UUID.fromString(UUID_CCCD_ID));
                mDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(mDescriptor);

                Log.v("LOG","mDescriptor.getValue()="+ Arrays.toString(mDescriptor.getValue()));//===== [1,0]
//                mDescriptor.setValue(new byte[]{8,0});
//                mGatt.writeDescriptor(mDescriptor);
//                Log.v("LOG","mDescriptor.getValue()="+ Arrays.toString(mDescriptor.getValue()));
//                Log.v("LOG","mDescriptor.getUuid().toString()="+ mDescriptor.getUuid().toString());
//                mDescriptor.setValue(new byte[]{1,0});
//                mGatt.writeDescriptor(mDescriptor);
//                mDescriptor.setValue(new byte[]{8,0});
//                mGatt.writeDescriptor(mDescriptor);
//                mDescriptor.setValue(new byte[]{1,0});
//                mGatt.writeDescriptor(mDescriptor);
//                mDescriptor.setValue(new byte[]{8,0});
//                mGatt.writeDescriptor(mDescriptor);
//                mDescriptor.setValue(new byte[]{1,0});
//                mGatt.writeDescriptor(mDescriptor);


            }

            @Override
            public void onServiceChanged(@NonNull BluetoothGatt gatt) {
                super.onServiceChanged(gatt);

                Log.v("LOG","onServiceChanged");
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                Log.v("LOG","onServicesDiscovered! status="+status);
                for(int i=0;i<gatt.getServices().size();i++){
                    Log.v("LOG","gatt.getServices()="+gatt.getServices().get(i).getUuid().toString());
                }

                //  request large mtu
                mGatt.requestMtu(GATT_MAX_MTU_SIZE);
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
                Log.v("LOG","onCharacteristicChanged!!!");
                for(int i=0;i<gatt.getServices().size();i++){
                    Log.v("LOG","gatt.getCharacteristics()="+gatt.getServices().get(i).getCharacteristics());
                    for(int a=0;a<gatt.getServices().get(i).getCharacteristics().size();a++){
                        Log.v("LOG","gatt.getCharacteristics()=INNER="+gatt.getServices().get(i).getCharacteristics().get(a).getUuid().toString());
                    }
                }
            }
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                switch(status){
                    case BluetoothGatt.GATT_SUCCESS:
                        Log.v("LOG","onConnectionStateChange >> GATT_SUCCESS");
                        mPairListener.onGotGatt(gatt);
                        break;
                    case BluetoothGatt.GATT_FAILURE:
                        Log.v("LOG","onConnectionStateChange >> GATT_FAILURE");
                        break;
                    default:
                        Log.v("LOG","onConnectionStateChange >> GATT_UNKNOWN");
                        break;
                }
                switch(newState){
                    case BluetoothGatt.STATE_DISCONNECTED:
                        Log.v("LOG","onConnectionStateChange > STATE_DISCONNECTED");
                        break;
                    case BluetoothGatt.STATE_CONNECTING:
                        Log.v("LOG","onConnectionStateChange > STATE_CONNECTING");
                        break;
                    case BluetoothProfile.STATE_CONNECTED:
                        Log.v("LOG","onConnectionStateChange > STATE_CONNECTED");
                        break;
                    case BluetoothProfile.STATE_DISCONNECTING:
                        Log.v("LOG","onConnectionStateChange > STATE_DISCONNECTING");
                        break;
                }
            }
        };
    }


    private static final String UUID_CUSTOM_SERVICE        = "4f63756c-7573-2054-6872-65656d6f7465";
    private static final String UUID_CUSTOM_SERVICE_WRITE  = "c8c51726-81bc-483b-a052-f7a14ea3d282";
    private static final String UUID_CUSTOM_SERVICE_NOTIFY = "c8c51726-81bc-483b-a052-f7a14ea3d281";

    private static final String UUID_CCCD_ID = "000002902-0000-1000-8000-00805f9b34fb";

    private static final int GATT_MAX_MTU_SIZE = 517;
    private static final int GATT_MIN_MTU_SIZE = 23;//although actually less, like 20 bytes.

    private String createBaseUUID(String uuid){
        return "0000"+uuid+"-0000-1000-8000-00805F9B34FB";
    }
}



interface PairInterface{
    void onGotDevice(BluetoothDevice bluetoothDevice);
    void onGotGatt(BluetoothGatt gatt);
    void onGotCustomService(BluetoothGattService bluetoothGattService);
    void onGotWriteCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic);
    void onGotReadCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic);
    void startNotifications();
}

