package com.example.bamboo.demoweek1.service;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.IntDef;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.bamboo.demoweek1.MainActivity;
import com.libelium.mysignalsconnectkit.BluetoothManagerHelper;
import com.libelium.mysignalsconnectkit.BluetoothManagerService;
import com.libelium.mysignalsconnectkit.callbacks.BluetoothManagerCharacteristicsCallback;
import com.libelium.mysignalsconnectkit.callbacks.BluetoothManagerHelperCallback;
import com.libelium.mysignalsconnectkit.callbacks.BluetoothManagerQueueCallback;
import com.libelium.mysignalsconnectkit.callbacks.BluetoothManagerServicesCallback;
import com.libelium.mysignalsconnectkit.pojo.LBSensorObject;
import com.libelium.mysignalsconnectkit.utils.BitManager;
import com.libelium.mysignalsconnectkit.utils.LBValueConverter;
import com.libelium.mysignalsconnectkit.utils.StringConstants;
import com.libelium.mysignalsconnectkit.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BluetoothConnectionService extends Service implements BluetoothManagerHelperCallback, BluetoothManagerServicesCallback, BluetoothManagerCharacteristicsCallback, BluetoothManagerQueueCallback {
    private final IBinder mBinder = new LocalBinder();

    private static String kMySignalsId = "mysignals 000062"; // MySignals advertising name
    private static int RESTING_HEART_BEAT = 0;

    private static BluetoothManagerService mService = null;

    private SensorResult mActivity;

    private boolean mIsPause = false;

    private BluetoothGattService selectedService;
    private BluetoothDevice selectedDevice;
    private ArrayList<LBSensorObject> selectedSensors;
    private ArrayList<BluetoothGattCharacteristic> notifyCharacteristics;
    private boolean writtenService;
    private BluetoothGattCharacteristic characteristicSensorList;

    private BluetoothManagerHelper bluetoothManager;

    public BluetoothConnectionService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            mService = BluetoothManagerService.getInstance();
            mService.initialize(this);
            mService.setServicesCallback(this);
            mService.setCharacteristicsCallback(this);
            mService.setQueueCallback(this);
        } catch (Exception e) {

        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        scanBluetoothDevices();
        createInterface();
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onListDevicesFound(ArrayList<BluetoothDevice> devices) {
        for (BluetoothDevice deviceItem : devices) {

            String name = deviceItem.getName();

            if (name != null) {
                if (name.toLowerCase().contains(kMySignalsId)) {
                    Log.d("DEBUG", "Address: " + name);
                    this.selectedDevice = deviceItem;
                    break;
                }
            }
        }

        if (selectedDevice != null) {
            bluetoothManager.stopLeScan();
            boolean bonded = mService.startBonding(selectedDevice);
            if (bonded) {
                Log.d("DEBUG", "Bonding starting...");
            }
        }
    }



    @Override
    public void onManagerDidNotFoundDevices() {
        Log.d("DEBUG", "Device MySignals not found!!!");
    }

    @Override
    public void onBondAuthenticationError(BluetoothGatt gatt) {
        Log.d("DEBUG", "Bonding authentication error!!!");
    }

    @Override
    public void onBonded() {
        performConnection();
    }

    @Override
    public void onBondedFailed() {

        Log.d("DEBUG", "Bonded failed!!!");
    }

    @Override
    public void onConnectedToDevice(BluetoothDevice device, int status) {
        Log.d("DEBUG", "Device connected!!");
    }

    @Override
    public void onServicesFound(List<BluetoothGattService> services) {
        if (services != null) {
            selectedService = null;
            for (BluetoothGattService service : services) {
                String uuidService = service.getUuid().toString().toUpperCase();
                if (uuidService.equals(StringConstants.kServiceMainUUID)) {
                    selectedService = service;
                    break;
                }
            }
            if (selectedService != null) {
                writtenService = false;
                mService.readCharacteristicsForService(selectedService);
            }
        }
    }

    @Override
    public void onDisconnectFromDevice(BluetoothDevice device, int newState) {
        Log.d("DEBUG", "Device disconnected!!");
    }

    @Override
    public void onReadRemoteRssi(int rssi, int status) {
        Log.d("DEBUG", "RSSI: " + rssi + " dBm - Status: " + status);
    }

    @Override
    public void onCharacteristicsFound(List<BluetoothGattCharacteristic> characteristics, BluetoothGattService service) {
        if (service.getUuid().toString().toUpperCase().equals(StringConstants.kServiceMainUUID)) {
            if (!writtenService) {
                characteristicSensorList = null;
                writtenService = true;

                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    String uuid = characteristic.getUuid().toString().toUpperCase();

                    if (characteristic.getUuid().toString().toUpperCase().equals(StringConstants.kSensorList)) {
                        Log.d("DEBUG", "characteristic: " + uuid);
                        Log.d("DEBUG", "characteristic uuid: " + characteristic.getUuid().toString().toUpperCase());
                        Log.d("DEBUG", "characteristic getWriteType: " + characteristic.getWriteType());

                        characteristicSensorList = characteristic;
                        break;
                    }
                }
                if (characteristicSensorList != null) {
                    BitManager bitManager = BitManager.newObject();
                    bitManager.objectByte = BitManager.createByteObjectFromSensors(selectedSensors, BitManager.BLUETOOTH_DISPLAY_MODE.BLUETOOTH_DISPLAY_MODE_GENERAL, this);

                    byte[] data = BitManager.convertToData(bitManager.objectByte);

                    String dataString = data.toString();
                    String hexByte = Utils.toHexString(data);

                    Log.d("DEBUG", "hex dataString value: " + hexByte);
                    Log.d("DEBUG", "dataString: " + dataString);

                    mService.writeCharacteristicQueue(characteristicSensorList, data);

                    Log.d("DEBUG", "Writting characteristic: " + characteristicSensorList.getUuid().toString().toUpperCase());
                }
            }
        }
    }

    @Override
    public void onCharacteristicWritten(BluetoothGattCharacteristic characteristic, int status) {

    }

    @Override
    public void onCharacteristicChanged(BluetoothGattCharacteristic characteristic) {
        readCharacteristic(characteristic);
    }

    @Override
    public void onCharacteristicSubscribed(BluetoothGattCharacteristic characteristic, boolean isUnsubscribed) {
        if (isUnsubscribed) {
            Log.d("DEBUG", "unsubscribed from characteristic!!");
        } else {
            Log.d("DEBUG", "subscribed to characteristic!!");
        }
    }

    @Override
    public void onFinishWriteAllCharacteristics() {

    }

    @Override
    public void onStartWriteCharacteristic(BluetoothGattCharacteristic characteristic, int status) {
        if (status != BluetoothGatt.GATT_SUCCESS) {
            Log.d("DEBUG", "writing characteristic error: " + status + " - " + characteristic.getUuid().toString().toUpperCase());
        } else {
            String uuid = characteristic.getService().getUuid().toString().toUpperCase();
            if (uuid.equals(StringConstants.kServiceMainUUID)) {
                Log.d("DEBUG", "pasa aquiasdadds");

                for (BluetoothGattCharacteristic charac : notifyCharacteristics) {
                    mService.writeCharacteristicSubscription(charac, false);
                }

                notifyCharacteristics.clear();

                for (BluetoothGattCharacteristic charac : selectedService.getCharacteristics()) {
                    for (LBSensorObject sensor : selectedSensors) {
                        if (sensor.uuidString.toUpperCase().equals(charac.getUuid().toString().toUpperCase()) && sensor.tickStatus) {
                            notifyCharacteristics.add(charac);

                            mService.writeCharacteristicSubscription(charac, true);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onStartReadCharacteristic(BluetoothGattCharacteristic characteristic) {

    }

    @Override
    public void onStartWriteQueueDescriptor(BluetoothGattDescriptor descriptor) {

    }

    @Override
    public void onFinishWriteAllDescriptors() {
        if (characteristicSensorList != null) {
            BitManager bitManager = BitManager.newObject();
            bitManager.objectByte = BitManager.createByteObjectFromSensors(selectedSensors, BitManager.BLUETOOTH_DISPLAY_MODE.BLUETOOTH_DISPLAY_MODE_GENERAL, this);

            byte[] data = BitManager.convertToData(bitManager.objectByte);

            String dataString = data.toString();
            String hexByte = Utils.toHexString(data);

            Log.d("DEBUG", "hex dataString value: " + hexByte);
            Log.d("DEBUG", "dataString: " + dataString);

            mService.writeCharacteristicQueue(characteristicSensorList, data);

            Log.d("DEBUG", "Writting characteristic: " + characteristicSensorList.getUuid().toString().toUpperCase());
        }
    }

    @Override
    public void onFinishReadAllCharacteristics() {

    }

    @Override
    public void onCharacteristicChangedQueue(BluetoothGattCharacteristic characteristic) {

    }

    private void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (!mIsPause) {
            try {
                String uuid = characteristic.getUuid().toString().toUpperCase();

                byte[] value = characteristic.getValue();

                if (value == null) {
                    return;
                }

                if (uuid.equals(StringConstants.kUUIDAirflowSensor)) {
                    HashMap<String, String> dataDict = LBValueConverter.manageValueAirflow(value);
                    Log.d("DEBUG", "kUUIDAirflowSensor dict: " + dataDict.get("1"));
                    if (Integer.parseInt(dataDict.get("1") ) == 0) {
                        mActivity.goDown();
                    } else {
                        mActivity.goUp();
                    }
                }

                if (uuid.equals(StringConstants.kUUIDPulsiOximeterSensor) || uuid.equals(StringConstants.kUUIDPulsiOximeterBLESensor)) {
                    HashMap<String, String> dataDict = LBValueConverter.manageValuePulsiOximeter(value);
                    Log.d("DEBUG", "kUUIDPulsiOximeterSensor dict: " + dataDict.get("1"));
                    if (RESTING_HEART_BEAT == 0) {
                        RESTING_HEART_BEAT = Integer.parseInt(dataDict.get("1"));
                    }
                    int difference = Integer.parseInt(dataDict.get("1")) - RESTING_HEART_BEAT;
                    if (difference > 0) {
                        if (Math.random() * 100 < difference) {
                            mActivity.addObstacle();
                        }
                    }
                }
            } catch (Exception e) {

            }
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        try {
            mService.unregisterBondNotification();
        } catch (Exception e1) {

        }

        try {
            if (mService != null) {
                mService.disconnectDevice();
                mService.serviceDestroy();
                mService.close();
                mService = null;
            }
        } catch (Exception e) {

        }
        return super.onUnbind(intent);
    }

    private void createInterface() {
        if (BluetoothManagerHelper.hasBluetooth(this)) {
            writtenService = false;
            notifyCharacteristics = new ArrayList<BluetoothGattCharacteristic>();
            selectedSensors = createSensorsDisplay();
            selectedDevice = null;
        } else {
            Log.d("DEBUG", "The device does not have BLE technology, please use an Android device with BLE technology.");
        }
    }

    private ArrayList<LBSensorObject> createSensorsDisplay() {
        int maxNotifications = Utils.getMaxNotificationNumber();

        ArrayList<LBSensorObject> sensors = new ArrayList<LBSensorObject>();

        LBSensorObject object;

        object = LBSensorObject.newInstance();

        object.tag = 5;
        object.tickStatus = (maxNotifications > 4) ? true : false;
        object.uuidString = StringConstants.kUUIDAirflowSensor;

        LBSensorObject.preloadValues(object);
        sensors.add(object);

        object = LBSensorObject.newInstance();

        object.tag = 8;
        object.tickStatus = (maxNotifications > 7) ? true : false;
        object.uuidString = StringConstants.kUUIDPulsiOximeterSensor;

        LBSensorObject.preloadValues(object);
        sensors.add(object);
        return sensors;
    }

    private void scanBluetoothDevices() {
        bluetoothManager = BluetoothManagerHelper.getInstance();
        bluetoothManager.setInitParameters(this, this);

        List<BluetoothDevice> devicesBonded = bluetoothManager.getBondedDevices();

        if (devicesBonded.size() > 0) {
            selectedDevice = null;

            for (BluetoothDevice deviceItem : devicesBonded) {
                String name = deviceItem.getName();

                if (name != null) {
                    if (name.toLowerCase().contains(kMySignalsId)) {
                        Log.d("DEBUG", "Address: " + name);
                        this.selectedDevice = deviceItem;
                        break;
                    }
                }
            }

            if (selectedDevice != null) {
                performConnection();
            } else {
                bluetoothManager.startLEScan(true);
            }
        } else {

            bluetoothManager.startLEScan(true);
        }
    }

    private void performConnection() {

        final Handler handler = new Handler();

        final Runnable postExecution = new Runnable() {

            @Override
            public void run() {
                try {
                    if (mService != null) {
                        if (mService.discoverServices()) {
                            Log.d("DEBUG", "Device discoverServices: " + selectedDevice.getAddress());
                        }
                    }
                } catch (Exception e) {

                }
            }
        };

        if (mService.connectToDevice(selectedDevice, this)) {
            Log.d("DEBUG", "Device connected!!");
            handler.postDelayed(postExecution, 2000);
        }
    }

    public void setClient (AppCompatActivity activity) {
        mActivity = (SensorResult) activity;
    }

    public void pauseService () {
        mIsPause = true;
    }

    public void unpauseService () {
        mIsPause = false;
    }

    public class LocalBinder extends Binder {
        public BluetoothConnectionService getInstance() { return BluetoothConnectionService.this; }
    }

    public interface SensorResult {
        void goUp();
        void goDown();
        void addObstacle();
    }
}
