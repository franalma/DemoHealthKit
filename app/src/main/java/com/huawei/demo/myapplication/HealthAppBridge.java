package com.huawei.demo.myapplication;

import android.content.Context;
import com.huawei.hihealth.error.HiHealthError;
import com.huawei.hihealth.listener.ResultCallback;
import com.huawei.hihealthkit.auth.HiHealthAuth;
import com.huawei.hihealthkit.auth.IAuthorizationListener;
import com.huawei.hihealthkit.data.store.HiHealthDataStore;
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
