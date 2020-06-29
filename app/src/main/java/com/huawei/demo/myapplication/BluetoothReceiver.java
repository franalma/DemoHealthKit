package com.huawei.demo.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.huawei.hms.kit.awareness.barrier.BarrierStatus;

import java.util.ArrayList;

public class BluetoothReceiver extends BroadcastReceiver {
    ArrayList<String> devices = new ArrayList<>();
    Bluetooth.BluetoothDelegate delegate;
//    String deviceAddress = "54:D3:62:73:25:DD";
//    String deviceAddress = "53:7D:8A:F1:A1:4D";//imac
    String deviceAddress = "74:5C:4B:33:D5:2F";//javra evolve
//    boolean enable = false;
    boolean enable = true;
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (BluetoothDevice.ACTION_FOUND.equals(action)){
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (!devices.contains(device.getAddress())){
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
                devices.add(device.getAddress());
                System.out.println("----device: "+device.getName()+" address: "+device.getAddress()+ " rssi: "+ rssi);
                if (enable && device.getAddress().equals(deviceAddress)){
                    Bluetooth.getInstance().pair(device);
                }

//                delegate.onNewDeviceDetected(device);

            }

            } else if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)){
                System.out.println("-- pairing requested");
            }
    }
}
