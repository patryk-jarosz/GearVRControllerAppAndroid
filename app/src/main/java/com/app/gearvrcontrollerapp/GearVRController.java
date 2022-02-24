package com.app.gearvrcontrollerapp;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class GearVRController {
    /** CONSTANTS   **/
    public static final String UUID_CUSTOM_SERVICE        = "4f63756c-7573-2054-6872-65656d6f7465";
    public static final String UUID_CUSTOM_SERVICE_WRITE  = "c8c51726-81bc-483b-a052-f7a14ea3d282";
    public static final String UUID_CUSTOM_SERVICE_NOTIFY = "c8c51726-81bc-483b-a052-f7a14ea3d281";

    public static final String CMD_OFF                          = "0000";
    public static final String CMD_SENSOR                       = "0100";
    public static final String CMD_UNKNOWN_FIRMWARE_UPDATE_FUNC = "0200";
    public static final String CMD_CALIBRATE                    = "0300";
    public static final String CMD_KEEP_ALIVE                   = "0400";
    public static final String CMD_UNKNOWN_SETTING              = "0500";
    public static final String CMD_LPM_ENABLE                   = "0600";
    public static final String CMD_LPM_DISABLE                  = "0700";
    public static final String CMD_VR_MODE                      = "0800";

    public static final double GYRO_FACTOR      = 0.0001; // to radians / s
    public static final double ACCEL_FACTOR     = 0.00001; // to g (9.81 m/s**2)
    public static final double TIMESTAMP_FACTOR = 0.001; // to seconds

    /** UTILS   **/
    //  TODO: Convert.
//    public Float32Array getAccelerometerFloatWithOffsetFromArrayBufferAtIndex(arrayBuffer, offset, index){
//        Int16Array arrayOfShort = new Int16Array(arrayBuffer.slice(16 * index + offset, 16 * index + offset + 2));
//        return (new Float32Array([arrayOfShort[0] * 10000.0 * 9.80665 / 2048.0]))[0];
//    };
//
//    public Float32Array getGyroscopeFloatWithOffsetFromArrayBufferAtIndex(arrayBuffer, offset, index){
//        Int16Array arrayOfShort = new Int16Array(arrayBuffer.slice(16 * index + offset, 16 * index + offset + 2));
//        return (new Float32Array([arrayOfShort[0] * 10000.0 * 0.017453292 / 14.285]))[0];
//    };
//
//    public Float32Array getMagnetometerFloatWithOffsetFromArrayBufferAtIndex(arrayBuffer, offset){
//        Int16Array arrayOfShort = new Int16Array(arrayBuffer.slice(32 + offset, 32 + offset + 2));
//        return (new Float32Array([arrayOfShort[0] * 0.06]))[0];
//    };

    //  ALREADY CONVERTED.
    public double getLength(float f1, float f2, float f3){ return Math.sqrt(Math.pow(f1, 2) + Math.pow(f2, 2) + Math.pow(f3, 2)); }
    public byte[] getLittleEndianUint8Array(String hexString){
        int val = Integer.parseInt(hexString, 16);
        BigInteger big = BigInteger.valueOf(val);
        return (big.toByteArray());
    }


    public void setBluetoothDevice(BluetoothDevice bluetoothDevice){
        mCurrentInstance = new GearVRControllerInstance();
        mCurrentInstance.setDevice(bluetoothDevice);
    }
    public void didClickGetGATT(){
        mCurrentInstance.setGatt(mCurrentInstance.mDevice.connectGatt(mContext,false,createBluetoothGattCallback()));
    }
    public void didClickDiscoverServices(){
        mCurrentInstance.actionDiscoverServices();
    }
    public void didClickRequestMTU(){
        mCurrentInstance.requestMtu();
        //mCurrentInstance.startSensorMode();
    }
    public void didClickActionSingleRead(){
        mCurrentInstance.actionSingleRead();
    }
    public void didClickCMDSensor(){
        mCurrentInstance.actionWriteCharacteristic(CMD_SENSOR,BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
    }
    public void didClickCMDVR(){
        mCurrentInstance.actionDisconnect();
        //mCurrentInstance.actionWriteCharacteristic(CMD_VR_MODE,BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
    }

    //actionWriteCharacteristic


    public interface GearVRControllerListener{
        void onBluetoothError(String e);
        void runCommand(String commandValue);
        void onNotificationReceived(String e);
        void disconnect();
        void pair();
        void onDeviceConnected(BluetoothDevice device);
    }


    private GearVRControllerListener mListener;//console.warn('Error: ' + e);

    private Context mContext;

    public GearVRController(Context mContext){//GearVRControllerListener mListener){
        //this.mListener = mListener;
        this.mContext = mContext;

    }

    private BluetoothGattServer gattServer;
    private BluetoothGattService customService,customServiceNotify,customServiceWrite;

    private BluetoothDevice mCurrentDevice;

    private void actionPair(BluetoothDevice device){
        mCurrentDevice = device;
        //customService = mCurrentDevice.
        //gattServer = mCurrentDevice.connectGatt(null,false,createBluetoothGattCallback());

    }
    private void actionDisconnect(){}
    private void actionCalibrate(){}
    private void actionSensor(){
        //  AKA -> START.
    }

    /** GATT CALLBACK   **/
    private BluetoothGattCallback createBluetoothGattCallback(){
        return new BluetoothGattCallback() {
            /** ON CONNECTION STATE CHANGE **/
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);

                //  STORE GATT REF.
                mCurrentInstance.setGatt(gatt);

                switch(status){
                    case BluetoothGatt.GATT_SUCCESS:
                        log("onConnectionStateChange >> GATT_SUCCESS");
                        break;
                    case BluetoothGatt.GATT_FAILURE:
                        log("onConnectionStateChange >> GATT_FAILURE");
                        break;
                    default:
                        log("onConnectionStateChange >> GATT_UNKNOWN");
                        break;
                }
                switch(newState){
                    case BluetoothGatt.STATE_DISCONNECTED:
                        log("onConnectionStateChange > STATE_DISCONNECTED");
                        break;
                    case BluetoothGatt.STATE_CONNECTING:
                        log("onConnectionStateChange > STATE_CONNECTING");
                        break;
                    case BluetoothProfile.STATE_CONNECTED:
                        log("onConnectionStateChange > STATE_CONNECTED");
                        break;
                    case BluetoothProfile.STATE_DISCONNECTING:
                        log("onConnectionStateChange > STATE_DISCONNECTING");
                        break;
                }
            }
            /** ON SERVICES DISCOVERED **/
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);

                //  STORE GATT REF.
                mCurrentInstance.setGatt(gatt);

                mCurrentInstance.setArrayServices(gatt.getServices());
            }
            /** ON MTU CHANGED  **/
            @Override
            public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
                super.onMtuChanged(gatt, mtu, status);
                log("onMtuChanged:" + mtu);
                //  STORE GATT REF.
                mCurrentInstance.setGatt(gatt);
            }

            /** ON CHARACTERISTIC WRITE    **/
            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
                log("onCharacteristicWrite: STATUS="+status);
                //  ref: https://developer.android.com/reference/android/bluetooth/BluetoothGatt#GATT_INVALID_ATTRIBUTE_LENGTH
                //  STORE GATT REF.
                mCurrentInstance.setGatt(gatt);
            }
            /** ON CHARACTERISTIC READ  **/
            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
                log("onCharacteristicRead: STATUS="+status);
                //  STORE GATT REF.
                mCurrentInstance.setGatt(gatt);
                switch(status){
                    case BluetoothGatt.GATT_SUCCESS:
                        log("onCharacteristicRead >> GATT_SUCCESS");
                        mCurrentInstance.onGotReadValue();
                        break;
                    case BluetoothGatt.GATT_READ_NOT_PERMITTED:
                        log("onCharacteristicRead >> GATT_READ_NOT_PERMITTED");
                        break;
                    default:
                        log("BluetoothGattCallback >> GATT_UNKNOWN",true);
                        break;
                }
            }
            /** ON CHARACTERISTIC CHANGE    **/
            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
                log("onCharacteristicChanged: characteristic="+ Arrays.toString(characteristic.getValue()));
                //  STORE GATT REF.
                mCurrentInstance.setGatt(gatt);
            }

            @Override
            public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
                super.onPhyUpdate(gatt, txPhy, rxPhy, status);
                log("onPhyUpdate");
            }

            @Override
            public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
                super.onPhyRead(gatt, txPhy, rxPhy, status);
                log("onPhyRead");
            }

            @Override
            public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorRead(gatt, descriptor, status);
                log("onDescriptorRead");
            }

            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorWrite(gatt, descriptor, status);
                log("onDescriptorWrite:status="+status);
            }

            @Override
            public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
                super.onReliableWriteCompleted(gatt, status);
                log("onReliableWriteCompleted");
            }

            @Override
            public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                super.onReadRemoteRssi(gatt, rssi, status);
                log("onReadRemoteRssi");
            }

            @Override
            public void onServiceChanged(@NonNull BluetoothGatt gatt) {
                super.onServiceChanged(gatt);
                log("onServiceChanged");
            }
        };
    }

    public GearVRControllerInstance mCurrentInstance = new GearVRControllerInstance();


    private void log(String message){ log(message,false); }
    private void log(String message, boolean isError){
        if(isError){
            Log.e(TAG, message);
            return;
        }
        Log.v(TAG, message);
    }
    private static final String TAG = "GearVRController";


    //  SERVICE ->
    //              CHARACTERISTICS ->
    //                                  DESCRIPTORS.
    public class GearVRControllerInstance{
        private BluetoothDevice mDevice;
        private BluetoothGatt mGatt = null;
        private List<BluetoothGattService> mArrayServices;
        public GearVRControllerInstance(){

        }

        public void actionDisconnect(){
            mGatt.disconnect();
            mGatt = null;
        }

        public void setDevice(BluetoothDevice bluetoothDevice){
            mDevice = bluetoothDevice;
        }
        public void setGatt(BluetoothGatt gatt){
            if(this.mGatt != null){
                return;
            }
            this.mGatt = gatt;
        }

        public void actionDiscoverServices(){
            mGatt.discoverServices();
            //  -> onServicesDiscovered().
        }

        public void setArrayServices(List<BluetoothGattService> services){
            mArrayServices = services;
        }

        public void requestMtu(){
            mGatt.requestMtu(GATT_MAX_MTU_SIZE);
            //  -> onMtuChanged().
        }

        public boolean isCharacteristicWritable(BluetoothGattCharacteristic pChar) { return (pChar.getProperties() & (BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) != 0; }
        public boolean isCharacteristicReadable(BluetoothGattCharacteristic pChar) { return ((pChar.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) != 0); }
        public boolean isCharacteristicNotifiable(BluetoothGattCharacteristic pChar) { return (pChar.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0; }

        public void startSensorMode(){
            //CMD_SENSOR
            BluetoothGattService mGattCustomService = mGatt.getService( UUID.fromString(UUID_CUSTOM_SERVICE) );
            BluetoothGattCharacteristic mGattCustomServiceWrite = mGattCustomService.getCharacteristic( UUID.fromString(UUID_CUSTOM_SERVICE_WRITE) );
            mGattCustomServiceWrite.setValue(generateBaseUUID(CMD_VR_MODE));
            mGatt.writeCharacteristic(mGattCustomServiceWrite);
        }

        //  SINGLE READ CHARACTERISTIC FROM 'UUID_CUSTOM_SERVICE'.
        public void actionSingleRead(){
            log("actionSingleRead: getting service...");
            BluetoothGattService mGattCustomService = mGatt.getService( UUID.fromString(UUID_CUSTOM_SERVICE) );
            log("actionSingleRead: Got Service, isNull? -> "+(mGattCustomService==null));
            log("actionSingleRead: getting characteristic...");
            BluetoothGattCharacteristic mGattCustomServiceNotify = mGattCustomService.getCharacteristic( UUID.fromString(UUID_CUSTOM_SERVICE_NOTIFY) );
            log("actionSingleRead: Got Characteristic, isNull? -> "+(mGattCustomServiceNotify==null));
            log("actionSingleRead: is characteristic writable? -> "+isCharacteristicWritable(mGattCustomServiceNotify));
            log("actionSingleRead: is characteristic readable? -> "+isCharacteristicReadable(mGattCustomServiceNotify));
            log("actionSingleRead: is characteristic notifiable? -> "+isCharacteristicNotifiable(mGattCustomServiceNotify));
            log("actionSingleRead: performing read...");
            mGatt.readCharacteristic(mGattCustomServiceNotify);
            //  ->  onCharacteristicRead().
        }

        //  GOT READ VALUE - SUCCESSFULLY.
        public void onGotReadValue(){
            log("onGotReadValue: trying to read...");
            BluetoothGattService mGattCustomService = mGatt.getService( UUID.fromString(UUID_CUSTOM_SERVICE) );
            BluetoothGattCharacteristic mGattCustomServiceNotify = mGattCustomService.getCharacteristic( UUID.fromString(UUID_CUSTOM_SERVICE_NOTIFY) );
            byte[] mByteArrayValue = mGattCustomServiceNotify.getValue();
            log("onGotReadValue: did read value!!!");
            log("onGotReadValue: ==========BYTE ARRAY VALUE==========");
            for(byte b: mByteArrayValue){
                //Log.i(TAG, String.format("0x%20x", b));
                Log.i(TAG, getHexString(b));
//                Log.i(TAG, String.valueOf(b));
            }
            log("onGotReadValue: ==========BYTE ARRAY VALUE==========");
            boolean mIsTriggerPressed = false;
            mIsTriggerPressed = Boolean.parseBoolean(String.valueOf(mByteArrayValue[58] & (1 << 0)));
            log("triggerPressed="+mIsTriggerPressed);
        }

        //  WRITE CHARACTERISTIC (TO START RECEIVING NOTIFICATIONS).
        public void actionWriteCharacteristic(String command, byte[] payload){
//            BluetoothGattService mGattCustomService = mGatt.getService( UUID.fromString(UUID_CUSTOM_SERVICE) );
////            BluetoothGattCharacteristic mGattCustomServiceWrite = mGattCustomService.getCharacteristic( UUID.fromString(UUID_CUSTOM_SERVICE_NOTIFY) );
////            mGattCustomServiceWrite.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
////            mGattCustomServiceWrite.setValue(payload);
////            mGatt.writeCharacteristic(mGattCustomServiceWrite);
//
//
//
//            for(int i=0;i<mGatt.getService(UUID.fromString(UUID_CUSTOM_SERVICE)).getCharacteristic(UUID.fromString(UUID_CUSTOM_SERVICE_WRITE)).getDescriptors().size();i++){
//                log(mGatt.getService(UUID.fromString(UUID_CUSTOM_SERVICE)).getCharacteristic(UUID.fromString(UUID_CUSTOM_SERVICE_WRITE)).getDescriptors().get(i).getUuid().toString());
//            }
//            UUID cccdUuid = UUID.fromString("00000800-0000-1000-8000-00805f9b34fb");
//
//            mGatt.setCharacteristicNotification(mGattCustomService.getCharacteristic( UUID.fromString(UUID_CUSTOM_SERVICE_WRITE) ),true);
//            //BluetoothGattDescriptor mDescriptor = mGatt.getService( UUID.fromString(UUID_CUSTOM_SERVICE) ).getCharacteristic( UUID.fromString(UUID_CUSTOM_SERVICE_WRITE) ).getDescriptors().get(0);
//            //Toast.makeText(mContext, "d: "+mDescriptor.getUuid().toString(), Toast.LENGTH_SHORT).show();
//            //byte[] aaa = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
//            //mDescriptor.setValue(aaa);
            //mGatt.writeDescriptor(mDescriptor);


//            BluetoothGattCharacteristic characteristic = mGatt.getService(SERIAL_SERVICE).getCharacteristic(SERIAL_VALUE);
//
//            mGatt.setCharacteristicNotification(characteristic,true);
//
//
//            BluetoothGattDescriptor desc = characteristic.getDescriptor(CONFIG_DESCRIPTOR);
//            desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//            mGatt.writeDescriptor(desc);

            mGatt.setCharacteristicNotification(mGatt.getService(SERIAL_SERVICE).getCharacteristic(SERIAL_VALUE), true);
            BluetoothGattDescriptor descriptor = mGatt.getService(SERIAL_SERVICE).getCharacteristic(SERIAL_VALUE).getDescriptor(CONFIG_DESCRIPTOR);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            mGatt.writeDescriptor(descriptor);

            for(int i=0;i<mGatt.getServices().size();i++){
                log("uuid="+mGatt.getServices().get(i).getUuid());
            }
        }

        public final UUID SERIAL_SERVICE = UUID.fromString(UUID_CUSTOM_SERVICE);
        public final UUID SERIAL_VALUE  = UUID.fromString(UUID_CUSTOM_SERVICE_NOTIFY);

        private final UUID CONFIG_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");



//        public void enableNotifications(){
//            generateBaseUUID(CMD_VR_MODE)
//            mGatt.getService( UUID.fromString(UUID_CUSTOM_SERVICE) ).getCharacteristic( UUID.fromString(UUID_CUSTOM_SERVICE_WRITE) );
//        }
    }

    //  1 GET BT DEVICE
    //  2 GET GATT
    //  3 DISCOVER SERVICES ON GATT

    //  16-bit (2-byte) values == 0x180F

    //ParcelUuid pUuid = new ParcelUuid(UUID.fromString("00001234-0000-1000-8000-00805F9B34FB");

    //  REQUIRED WHEN USING 16-BIT VALUES ON ANDROID. [0000xxxx-0000-1000-8000-00805F9B34FB]
    public String generateBaseUUID(String uuid){
        return "0000"+uuid+"-0000-1000-8000-00805F9B34FB";
    }

    private static final int GATT_MAX_MTU_SIZE = 517;
    private static final int GATT_MIN_MTU_SIZE = 23;//although actually less, like 20 bytes.

    public void performNotificationSingleRead(){
        //UUID_CUSTOM_SERVICE
        //UUID_CUSTOM_SERVICE_NOTIFY
    }

    private String getHexString(Byte it){
        return String.format("%02X", it);
    }

}

