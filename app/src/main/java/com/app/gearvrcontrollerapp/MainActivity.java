package com.app.gearvrcontrollerapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
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
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
//    private static final int BLUETOOTH_PERMISSION_REQUEST_CODE = 102030;
//    private static final int ACCESS_FINE_LOCATION_PERMISSION_REQUEST_CODE = 405060;
    private static final int ALL_PERMISSION_REQUEST_CODE = 708090;

    private Context mContext;
    private BluetoothGatt mBluetoothGatt = null;
    private BluetoothAdapter mBluetoothAdapter = null;


    private TextView mTxtPermissionBluetooth, mTxtPermissionLocation, mTxtScanStatus;
    private RecyclerView mRecyclerDevices;
    private BluetoothDeviceListAdapter mBluetoothDeviceListAdapter;

    private ScanCallback mScanCallback;

    private MaterialCardView mCardOverlayGatt;
    //private EditText mEditTextStatusOverlayGatt;

    private Button
            mBtnGetGatt,
            mBtnDiscoverServices,
            mBtnRequestMTU,
            mBtnActionSingleRead,
            mBtnCMDSensor,
            mBtnCMDVR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        inflate();
        //init();
    }

    private ControllerInputDisplay mControllerInputDisplay;


    private void inflate(){
        mControllerInputDisplay = new ControllerInputDisplay(this,findViewById(R.id.included_controller_values_linear));

        mTxtPermissionBluetooth = findViewById(R.id.txt_permission_status_bluetooth);
        mTxtPermissionLocation = findViewById(R.id.txt_permission_status_location);
        mTxtScanStatus = findViewById(R.id.txt_scan_status);
        mRecyclerDevices = findViewById(R.id.recycler_devices);
        mCardOverlayGatt = findViewById(R.id.overlay_card_gattserver);
        //mEditTextStatusOverlayGatt = findViewById(R.id.status_overlay_edittext);
        mBluetoothDeviceListAdapter = new BluetoothDeviceListAdapter(new ArrayList<>());
        mRecyclerDevices.setAdapter(mBluetoothDeviceListAdapter);
        mRecyclerDevices.setLayoutManager(new LinearLayoutManager(this,RecyclerView.VERTICAL,false));

        mBtnGetGatt = findViewById(R.id.btn_get_gatt);
        mBtnDiscoverServices = findViewById(R.id.btn_discover_services);
        mBtnRequestMTU = findViewById(R.id.btn_request_mtu);
        mBtnActionSingleRead = findViewById(R.id.btn_action_single_read);

        mBtnCMDSensor = findViewById(R.id.btn_action_cmd_sensor);
        mBtnCMDVR = findViewById(R.id.btn_action_cmd_vr);

        mBtnCMDSensor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGearVRController.didClickCMDSensor();
            }
        });
        mBtnCMDVR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGearVRController.didClickCMDVR();
            }
        });
        mBtnGetGatt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGearVRController.didClickGetGATT();
            }
        });
        mBtnDiscoverServices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGearVRController.didClickDiscoverServices();
            }
        });
        mBtnRequestMTU.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGearVRController.didClickRequestMTU();
            }
        });
        mBtnActionSingleRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGearVRController.didClickActionSingleRead();
            }
        });


        onPermissionChange();
        //  CHECK PERMISSIONS
        if(!getPermissionGrantedBluetooth() || !getPermissionGrantedAccessFineLocation()){
            ActivityCompat.requestPermissions(MainActivity.this, getArrayPermissions(), ALL_PERMISSION_REQUEST_CODE);
            return;
        }
        //init();
        initNew();
    }

    private void initNew(){
        //  GET DEVICE BLUETOOTH ADAPTER
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null){
            Toast.makeText(mContext, "Bluetooth adapter could not be found.", Toast.LENGTH_SHORT).show();
            return;
        }
        //  CHECK IF BLUETOOTH IS ENABLED.
        if(!mBluetoothAdapter.isEnabled()){
            Toast.makeText(mContext, "Bluetooth is turned off.", Toast.LENGTH_SHORT).show();
            //  START DEVICE BLUETOOTH ADAPTER
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
            //  CHECK RESULT.
            if(mBluetoothAdapter.isEnabled()){
                Toast.makeText(mContext, "Bluetooth was turned on!", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(mContext, "Failed to turn bluetooth on.", Toast.LENGTH_SHORT).show();
                return;
            }
        }else{
            Toast.makeText(mContext, "Bluetooth is already on.", Toast.LENGTH_SHORT).show();
        }
        mScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                mBluetoothDeviceListAdapter.addDevice(new BluetoothDeviceListObject(result.getDevice()));
            }
        };
        //  SCAN FOR BLUETOOTH LE DEVICES
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        actionScan();
    }

    private void init(){
        //  GET DEVICE BLUETOOTH ADAPTER
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null){
            Toast.makeText(mContext, "Bluetooth adapter could not be found.", Toast.LENGTH_SHORT).show();
            return;
        }
        //  CHECK IF BLUETOOTH IS ENABLED.
        if(!mBluetoothAdapter.isEnabled()){
            Toast.makeText(mContext, "Bluetooth is turned off.", Toast.LENGTH_SHORT).show();
            //  START DEVICE BLUETOOTH ADAPTER
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
            //  CHECK RESULT.
            if(mBluetoothAdapter.isEnabled()){
                Toast.makeText(mContext, "Bluetooth was turned on!", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(mContext, "Failed to turn bluetooth on.", Toast.LENGTH_SHORT).show();
                return;
            }
        }else{
            Toast.makeText(mContext, "Bluetooth is already on.", Toast.LENGTH_SHORT).show();
        }

        mScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                mBluetoothDeviceListAdapter.addDevice(new BluetoothDeviceListObject(result.getDevice()));
                //leDeviceListAdapter.addDevice(result.getDevice());
                //leDeviceListAdapter.notifyDataSetChanged();
                //Toast.makeText(mContext, "FOUND DEVICE", Toast.LENGTH_SHORT).show();
            }
        };

        //  SCAN FOR BLUETOOTH LE DEVICES
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        actionScan();


    }
    private BluetoothLeScanner mBluetoothLeScanner = null;
    private boolean scanning;
    private Handler handler = new Handler();
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    private void actionScan(){
        if (!scanning) {
            // Stops scanning after a predefined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanning = false;
                    mBluetoothLeScanner.stopScan(mScanCallback);
                    onScanChange(scanning);
                }
            }, SCAN_PERIOD);

            scanning = true;
            mBluetoothLeScanner.startScan(mScanCallback);
        } else {
            scanning = false;
            mBluetoothLeScanner.stopScan(mScanCallback);
        }
        onScanChange(scanning);
    }

    private boolean getPermissionGrantedBluetooth(){
        boolean mGrantedOther = true;
        if(Build.VERSION.SDK_INT >= 31){ mGrantedOther = getPermissionGrantedBluetoothOther(); }
        return mGrantedOther && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED;
    }
    private boolean getPermissionGrantedAccessFineLocation(){ return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED; }
    @RequiresApi(api = 31)
    private boolean getPermissionGrantedBluetoothOther(){
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        onPermissionChange();
    }

    private void onPermissionChange(){
        String mTxtBluetooth, mTxtLocation;
        mTxtBluetooth = "Bluetooth: " + getPermissionGrantedBluetooth();
        mTxtLocation = "Location: " + getPermissionGrantedAccessFineLocation();
        mTxtPermissionBluetooth.setText(mTxtBluetooth);
        mTxtPermissionLocation.setText(mTxtLocation);
    }


    private String[] getArrayPermissions(){
        if(Build.VERSION.SDK_INT >= 30){
            return new String[] {
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_PRIVILEGED
            };
        }
        return new String[] {
                Manifest.permission.BLUETOOTH,
                Manifest.permission.ACCESS_FINE_LOCATION
        };
    }

    private void onScanChange(boolean isScanning){
        String mStatus = "Status: " + ((isScanning) ? "Scanning..." : "Idle");
        mTxtScanStatus.setText(mStatus);
    }

    /** RECYCLERVIEW - OBJECT    **/
    private class BluetoothDeviceListObject{
        private String deviceName = "[bt device]";
        private String deviceAddress = "[bt address]";
        private BluetoothDevice bluetoothDevice = null;

        private BluetoothDeviceListObject(BluetoothDevice bluetoothDevice){
            this.bluetoothDevice = bluetoothDevice;
            this.deviceName = "("+bluetoothDevice.getName()+")";
            this.deviceAddress = bluetoothDevice.getAddress();
        }

    }

    /** RECYCLERVIEW - ADAPTER  **/
    private class BluetoothDeviceListAdapter extends RecyclerView.Adapter<BluetoothDeviceListAdapter.ViewHolder>{
        private ArrayList<BluetoothDeviceListObject> mArrayDataset;

        private BluetoothDeviceListAdapter(ArrayList<BluetoothDeviceListObject> mArrayDataset){
            this.mArrayDataset = mArrayDataset;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.listitem_device, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder mViewHolder, int position) {
            BluetoothDeviceListObject mObject = mArrayDataset.get(position);
            //mViewHolder.applyObject(mObject);
            mViewHolder.mTxtDeviceName.setText(mObject.deviceName);
            mViewHolder.mTxtDeviceAddress.setText(mObject.deviceAddress);
        }

        @Override
        public int getItemCount() {
            return mArrayDataset.size();
        }

        public void addDevice(BluetoothDeviceListObject mObject){
//            for(int i=0;i<mArrayDataset.size();i++){
//                if(mArrayDataset.get(i).bluetoothDevice.getAddress().equals(mObject.deviceAddress)){
//                    mArrayDataset.set(i,mObject);
//                    notifyDataSetChanged();
//                    return;
//                }
//            }
//            if(mArrayDataset.contains(mObject)){
//                mArrayDataset.set(mArrayDataset.indexOf(mObject),mObject);
//            }else{
//                mArrayDataset.add(mObject);
//            }
            mArrayDataset.add(mObject);
            notifyDataSetChanged();
        }
        /** VIEW HOLDER **/
        private class ViewHolder extends RecyclerView.ViewHolder{
            private TextView mTxtDeviceName,mTxtDeviceAddress;
            private MaterialCardView mCard;
            private ViewHolder(View itemView){
                super(itemView);
                mCard = itemView.findViewById(R.id.listitem_card);
                mTxtDeviceName = itemView.findViewById(R.id.listitem_device_txt_name);
                mTxtDeviceAddress = itemView.findViewById(R.id.listitem_device_txt_address);
                mCard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //connectGattServer(mArrayDataset.get(getAdapterPosition()).bluetoothDevice);
//                        connectGattServerNew(mArrayDataset.get(getAdapterPosition()).bluetoothDevice);
                        ControllerBluetoothManager mControllerBluetoothManager = new ControllerBluetoothManager(mContext);
                        Log.v("LOG","DID START PAIR.(from MainActivity).");
                        mControllerBluetoothManager.actionPair(mArrayDataset.get(getAdapterPosition()).bluetoothDevice,mControllerInputDisplay);
                        mCardOverlayGatt.setVisibility(View.VISIBLE);
                    }
                });
            }
        }
    }

    private GearVRController mGearVRController;

    private void connectGattServerNew(BluetoothDevice bluetoothDevice){
        mCardOverlayGatt.setVisibility(View.VISIBLE);
        mGearVRController = new GearVRController(this);
        mGearVRController.setBluetoothDevice(bluetoothDevice);
        //mGearVRController.

    }

    private void connectGattServer(BluetoothDevice bluetoothDevice){
        mCardOverlayGatt.setVisibility(View.VISIBLE);

        //Toast.makeText(mContext, "bluetoothDeviceClicked="+bluetoothDevice.getName(), Toast.LENGTH_SHORT).show();

        mBluetoothGatt = bluetoothDevice.connectGatt(this, false, new BluetoothGattCallback(){
            @Override
            public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
                super.onPhyUpdate(gatt, txPhy, rxPhy, status);
                addLineStatusGatt("onPhyUpdate: txPhy="+txPhy+",rxPhy="+rxPhy+",status="+status);
            }
            @Override
            public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
                super.onPhyRead(gatt, txPhy, rxPhy, status);
                addLineStatusGatt("onPhyRead: txPhy="+txPhy+",rxPhy="+rxPhy+",status="+status);
            }
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                addLineStatusGatt("onConnectionStateChange: status="+status+",newState="+newState);

                if(newState == BluetoothProfile.STATE_CONNECTED){
                    gatt.discoverServices();
                }
            }
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                addLineStatusGatt("onServicesDiscovered: status="+status);

                BluetoothGattService mBTService = gatt.getService(UUID.fromString(UUID_CUSTOM_SERVICE));
                BluetoothGattCharacteristic mBTServiceCharacteristic = mBTService.getCharacteristic(UUID.fromString(UUID_CUSTOM_SERVICE_NOTIFY));
                BluetoothGattDescriptor mBTServiceCharacteristicDescriptor = mBTServiceCharacteristic.getDescriptors().get(0);
                mBTServiceCharacteristicDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(mBTServiceCharacteristicDescriptor);
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
                addLineStatusGatt("onCharacteristicRead: characteristic="+characteristic+",status="+status);
                addLineStatusGatt("onCharacteristicRead: characteristic="+ Arrays.toString(characteristic.getValue()) +",status="+status);
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
                addLineStatusGatt("onCharacteristicWrite: characteristic="+characteristic+",status="+status);
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
                addLineStatusGatt("onCharacteristicChanged: characteristic="+characteristic);
            }

            @Override
            public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorRead(gatt, descriptor, status);
                addLineStatusGatt("onDescriptorRead: descriptor="+descriptor+",status="+status);
            }

            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorWrite(gatt, descriptor, status);
                addLineStatusGatt("onDescriptorWrite: descriptor="+descriptor+",status="+status);
            }

            @Override
            public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
                super.onReliableWriteCompleted(gatt, status);
                addLineStatusGatt("onReliableWriteCompleted: status="+status);
            }

            @Override
            public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                super.onReadRemoteRssi(gatt, rssi, status);
                addLineStatusGatt("onReadRemoteRssi: rssi="+rssi+",status="+status);
            }

            @Override
            public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
                super.onMtuChanged(gatt, mtu, status);
                addLineStatusGatt("onMtuChanged: mtu="+mtu+",status="+status);
            }

            @Override
            public void onServiceChanged(@NonNull BluetoothGatt gatt) {
                super.onServiceChanged(gatt);
                addLineStatusGatt("onServiceChanged.");
            }
        });




//        Log.v("MainActivity","mBluetoothGatt.getServices().size()="+mBluetoothGatt.getServices().size());
//        for(int i=0;i<mBluetoothGatt.getServices().size();i++){
//            Log.v("MainActivity","mBluetoothGatt.getServices()="+mBluetoothGatt.getServices().get(i));
//        }

//
//        BluetoothGattService mService = mBluetoothGatt.getService(UUID.fromString(UUID_CUSTOM_SERVICE));
//        BluetoothGattCharacteristic mServiceCharacteristicWrite = mService.getCharacteristic(UUID.fromString(UUID_CUSTOM_SERVICE_WRITE));
//        BluetoothGattCharacteristic mServiceCharacteristicNotify = mService.getCharacteristic(UUID.fromString(UUID_CUSTOM_SERVICE_NOTIFY));
//
//        mBluetoothGatt.setCharacteristicNotification(mServiceCharacteristicNotify,true);
        //gatt.setCharacteristicNotification(characteristic, true);
//        mBluetoothGatt.
    }

    private byte[] getLittleEndianUint8Array(String hexString){
        return hexStringToByteArray(hexString);
    }

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len/2];

        for(int i = 0; i < len; i+=2){
            data[i/2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        }

        return data;
    }

    private static final String UUID_CUSTOM_SERVICE = "4f63756c-7573-2054-6872-65656d6f7465";
    private static final String UUID_CUSTOM_SERVICE_WRITE = "c8c51726-81bc-483b-a052-f7a14ea3d282";
    private static final String UUID_CUSTOM_SERVICE_NOTIFY = "c8c51726-81bc-483b-a052-f7a14ea3d281";

    private static final String CMD_VR_MODE = "0800";

    private void addLineStatusGatt(String strToAdd){
//        String mValue = mEditTextStatusOverlayGatt.getText().toString()+"\n\n"+strToAdd;
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                mEditTextStatusOverlayGatt.setText(mValue);
//            }
//        });
//        mEditTextStatusOverlayGatt.setText(mValue);
        Log.v("MainActivity","addLineStatusGatt: "+strToAdd);
    }
}