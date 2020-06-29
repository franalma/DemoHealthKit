package com.huawei.demo.myapplication;

import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;

import androidx.annotation.RequiresApi;

import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hms.kit.awareness.Awareness;
import com.huawei.hms.kit.awareness.barrier.AwarenessBarrier;
import com.huawei.hms.kit.awareness.barrier.BarrierUpdateRequest;
import com.huawei.hms.kit.awareness.barrier.BluetoothBarrier;
import com.huawei.hms.kit.awareness.capture.BluetoothStatusResponse;
import com.huawei.hms.kit.awareness.status.BluetoothStatus;

public class Bluetooth {

    ScanCallback callback;
    private static Bluetooth instance;


    public void connect(){

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void scanBle(Context context){
        BluetoothManager btManager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
        assert btManager != null;

        boolean isSupported = context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
        System.out.println("----BLE supported: "+isSupported);
        BluetoothAdapter adapter = btManager.getAdapter();
        final BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
        callback = new ScanCallback() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                System.out.println("----result: "+result.getScanRecord());
                //System.out.println("---Scan result: "+result.getDevice().getName());
            }
        };
//        ScanSettings.Builder settings = new ScanSettings.Builder()
//                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
//                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
//                .build();

        scanner.startScan(callback);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                System.out.println("---Stopping BLE scanning");
                scanner.stopScan(callback);
            }
        },10000);
    }


    public void scanAwaranness(Context context, String barrierReceiverAction){
        Awareness.getCaptureClient(context).getBluetoothStatus(0).addOnSuccessListener(new OnSuccessListener<BluetoothStatusResponse>() {
            @Override
            public void onSuccess(BluetoothStatusResponse bluetoothStatusResponse) {
                System.out.println("----bluetooth response: "+bluetoothStatusResponse.getBluetoothStatus().getStatus());
            }
        });

        AwarenessBarrier barrier = BluetoothBarrier.connecting(0);
        BarrierUpdateRequest.Builder builder = new BarrierUpdateRequest.Builder();
        String bluetoothBarrierLabel = "bluetooth connecting barrier";
        Intent intent = new Intent(barrierReceiverAction);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context
                , 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        BarrierUpdateRequest request = builder.addBarrier(bluetoothBarrierLabel, barrier,pendingIntent).build();


    }
    public void scan(){
        System.out.println("--- enabling discovering");
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        adapter.cancelDiscovery();
        adapter.startDiscovery();
    }

    public void connect(String address){

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void pair(BluetoothDevice device){
        System.out.println("---pairing to "+device.getAddress());
        device.createBond();

    }



    public static Bluetooth getInstance(){
        if (instance == null){
            instance = new Bluetooth();
        }
        return instance;
    }

    public interface BluetoothDelegate{
        void onNewDeviceDetected(BluetoothDevice device);
    }
}
