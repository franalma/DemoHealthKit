package com.huawei.demo.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.huawei.hihealth.error.HiHealthError;
import com.huawei.hihealth.listener.ResultCallback;
import com.huawei.hihealthkit.auth.HiHealthAuth;
import com.huawei.hihealthkit.auth.IAuthorizationListener;
import com.huawei.hihealthkit.data.HiHealthKitConstant;
import com.huawei.hihealthkit.data.store.HiHealthDataStore;
import com.huawei.hihealthkit.data.store.HiRealTimeListener;
import com.huawei.hihealthkit.data.store.HiSportDataCallback;

import org.json.JSONObject;

import java.util.List;

public class HealthAppBridge {

    private static HealthAppBridge instance;
    private final String TAG = "HealthAppBridge";

    public void requestPermission(int [] readPermission, int [] writePermission,
                                  Context context, final HealthAppDelegate delegate){
        HiHealthAuth.requestAuthorization(context, writePermission, readPermission, new IAuthorizationListener() {
            @Override
            public void onResult(int resultCode, Object resultDesc) {
                delegate.onRequestPermissionResult(resultCode);
            }
        });
    }

    public void checkPermissionStatus(final int permission, Context context, final HealthAppDelegate delegate){
        HiHealthAuth.getDataAuthStatus(context, permission, new IAuthorizationListener() {
            @Override
            public void onResult(int resultCode, Object result) {
                if (resultCode != HiHealthError.SUCCESS) {

                }else{
                    List<Integer> list = (List) result;
                    delegate.onCheckPermissionStatus(permission,list.get(0));
                }
            }
        });
    }


    public void getGender(Context context, final HealthAppDelegate delegate){
        HiHealthDataStore.getGender(context, new ResultCallback() {
            @Override
            public void onResult(int errorCode, Object gender) {
                if (errorCode == HiHealthError.SUCCESS){
                    delegate.onDataReadSuccess(gender, HealthAppDataType.GENDER);
                }else{
                    delegate.onDataReadError(errorCode, HealthAppDataType.GENDER);
                }
            }
        });
    }

    public void getBirthDay(Context context, final HealthAppDelegate delegate){
        HiHealthDataStore.getBirthday(context, new ResultCallback() {
            @Override
            public void onResult(int resultCode, Object birthday) {
                if (resultCode == HiHealthError.SUCCESS) {
                    delegate.onDataReadSuccess(birthday,HealthAppDataType.BIRTHDATE);
                }else{
                    delegate.onDataReadError(resultCode,HealthAppDataType.BIRTHDATE);
                }
            }
        });
    }

    public void readRealTimeData(Context context){
//        HiHealthDataStore.startReadingHeartRate(context, new HiRealTimeListener() {
//            @Override
//            public void onResult(int state) {
//                System.out.println("---result state: "+state);
//            }
//
//            @Override
//            public void onChange(int resultCode, String value) {
//                try{
//                    System.out.println("----value: "+value);
//                    JSONObject joc = new JSONObject(value);
//                    int hearRate = Integer.parseInt(joc.getString("hr_info"));
//
//                }catch(Exception e){
//                    e.printStackTrace();
//                }
//            }
//        });

        HiHealthDataStore.startReadingRri(context, new HiRealTimeListener() {
            @Override
            public void onResult(int i) {
                System.out.println("---state: "+i);
            }

            @Override
            public void onChange(int resultCode, String value) {
                System.out.println("--resultcode: "+resultCode);
                System.out.println("---value: "+value);
            }
        });
    }

    public void startReadingRealTimeSportData(Context context){
        HiHealthDataStore.startRealTimeSportData(context, new HiSportDataCallback() {
            @Override
            public void onResult(int state) {
                Log.i(TAG, "startRealTimeSport onResult state:" + state);
            }
            @Override
            public void onDataChanged(int resultCode, Bundle value) {
                Log.i(TAG, "startRealTimeSport onChange resultCode: " + resultCode);
                if (value != null) {
                    // Here we only extract distance and duration as an example, find more in
                    // HiHealthKitConstant
                    StringBuilder builder = new StringBuilder("RealTimeSport: ");
                    builder.append(HiHealthKitConstant.BUNDLE_KEY_DISTANCE)
                            .append(":")
                            .append(value.getInt(HiHealthKitConstant.BUNDLE_KEY_DISTANCE));
                    builder.append(HiHealthKitConstant.BUNDLE_KEY_DURATION)
                            .append(":")
                            .append(value.getInt(HiHealthKitConstant.BUNDLE_KEY_DURATION));
                    Log.i(TAG, builder.toString());
                }
            }
        });
    }


    public static HealthAppBridge getInstance(){
        if (instance == null){
            instance = new HealthAppBridge();
        }
        return instance;
    }

    public enum HealthAppDataType{
        BIRTHDATE,
        GENDER
    }

    public interface HealthAppDelegate {

        void onRequestPermissionResult(int resultCode);
        void onCheckPermissionStatus(int permission, int resultCode);
        void onDataReadSuccess(Object data, HealthAppDataType dataType);
        void onDataReadError (int errorCode,HealthAppDataType dataType);
        void onDataWriteSuccess(Object data, HealthAppDataType dataType);
        void onDataWriteError (HealthAppDataType dataType, int errorCode);

    }
}
