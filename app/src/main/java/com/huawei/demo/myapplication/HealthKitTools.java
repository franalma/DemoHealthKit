package com.huawei.demo.myapplication;

import android.content.Context;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.hihealth.DataController;
import com.huawei.hms.hihealth.HiHealthOptions;
import com.huawei.hms.hihealth.HuaweiHiHealth;
import com.huawei.hms.hihealth.data.DataCollector;
import com.huawei.hms.hihealth.data.DataType;
import com.huawei.hms.hihealth.data.Field;
import com.huawei.hms.hihealth.data.SamplePoint;
import com.huawei.hms.hihealth.data.SampleSet;
import com.huawei.hms.hihealth.options.ReadOptions;
import com.huawei.hms.hihealth.result.ReadReply;
import com.huawei.hms.support.hwid.HuaweiIdAuthManager;
import com.huawei.hms.support.hwid.result.AuthHuaweiId;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class HealthKitTools {

    private static HealthKitTools instance;

    private HealthKitTools(){};

    public DataController init(Context context, HiHealthOptions options){
        AuthHuaweiId sign = HuaweiIdAuthManager.getExtendedAuthResult(options);
        DataController dataController = HuaweiHiHealth.getDataController(context, sign);
        return dataController;
    }


    public void readDataFromCloud( DataController dataController, ReadOptions readOptions,
                        final HealthKitToolsDelegate delegate){

        Task<ReadReply> readReplyTask = dataController.read(readOptions);
        readReplyTask.addOnSuccessListener(new OnSuccessListener<ReadReply>() {
            @Override
            public void onSuccess(ReadReply readReply) {
                delegate.onReadReplySuccess(readReply);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                delegate.onReadReplyFailure(e);
            }
        });
    }

    public void syncWithFitnessHealth(DataController dataController, final HealthKitToolsDelegate delegate){
        Task<Void> synkTask = dataController.syncAll();
        synkTask.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                delegate.onDataSyncSuccess();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                delegate.onDataSyncFailure(e);
            }
        });

    }


    public static HealthKitTools getInstance(){
        if (instance == null){
            instance = new HealthKitTools();
        }
        return instance;
    }


    public void insertData(DataController dataController, SampleSet set, final HealthKitToolsDelegate delegate){
        try{
            Task<Void> insertTasks = dataController.insert(set);
            insertTasks.addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    delegate.onDataInserted();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    delegate.onDataInsertedFailure(e);
                }
            });
        }catch(Exception e){
            e.printStackTrace();
        }
    }




    public interface HealthKitToolsDelegate{
        void onReadReplySuccess(ReadReply values);
        void onReadReplyFailure(Exception e);
        void onDataInserted();
        void onDataInsertedFailure(Exception e);
        void onDataSyncSuccess();
        void onDataSyncFailure(Exception e);
    }


}
