package com.huawei.demo.myapplication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.huawei.hihealthkit.auth.HiHealthOpenPermissionType;
import com.huawei.hms.hihealth.DataController;
import com.huawei.hms.hihealth.HiHealthOptions;
import com.huawei.hms.hihealth.data.DataCollector;
import com.huawei.hms.hihealth.data.DataType;
import com.huawei.hms.hihealth.data.Field;
import com.huawei.hms.hihealth.data.SamplePoint;
import com.huawei.hms.hihealth.data.SampleSet;
import com.huawei.hms.hihealth.options.ReadOptions;
import com.huawei.hms.hihealth.result.ReadReply;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements LoginTools.LoginDelegate,
        HealthKitTools.HealthKitToolsDelegate, HealthAppBridge.HealthAppDelegate {

    private final static int REQUEST_LOGIN = 2001;
    private DataController dataController;
    int[] read = new int[] {HiHealthOpenPermissionType.HEALTH_OPEN_PERMISSION_TYPE_READ_DATA_POINT_CALORIES_SUM,
            HiHealthOpenPermissionType.HEALTH_OPEN_PERMISSION_TYPE_READ_USER_PROFILE_FEATURE,
            HiHealthOpenPermissionType.HEALTH_OPEN_PERMISSION_TYPE_READ_USER_PROFILE_INFORMATION};

    int[] write = new int[] {HiHealthOpenPermissionType.HEALTH_OPEN_PERMISSION_TYPE_WRITE_DATA_SET_WEIGHT};


    private SampleSet createData(){
        try{
            DataCollector collector = new DataCollector.Builder().setPackageName(this)
                    .setDataType(DataType.DT_CONTINUOUS_STEPS_DELTA)
                    .setDataCollectorName("STEPS_DELTA")
                    .setDataGenerateType(DataCollector.DATA_TYPE_RAW)
                    .build();
            Date startDate = new Date(System.currentTimeMillis()-10000);
            Date endtDate = new Date(System.currentTimeMillis());
            int stepDelta = 1000;
            SampleSet set = SampleSet.create(collector);
            SamplePoint samplePoint = set.createSamplePoint()
                    .setTimeInterval(startDate.getTime(), endtDate.getTime(), TimeUnit.MILLISECONDS);
            samplePoint.getFieldValue(Field.FIELD_STEPS_DELTA).setIntValue(stepDelta);
            set.addSample(samplePoint);
            return set;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private void initDataController(){
        HiHealthOptions options = HiHealthOptions.builder()
                .addDataType(DataType.DT_CONTINUOUS_STEPS_DELTA, HiHealthOptions.ACCESS_READ)
                .addDataType(DataType.DT_CONTINUOUS_STEPS_DELTA, HiHealthOptions.ACCESS_WRITE)
                .build();
        dataController =  HealthKitTools.getInstance().init(this, options);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LoginTools.signIn(this, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_LOGIN){
            this.onLoginSuccess();
        }
    }

    public void insertSample(View view){
        SampleSet sampleSet = createData();
        HealthKitTools.getInstance().insertData(dataController,sampleSet, this);
    }

    public void readValues(View view){
        DataCollector dataCollector = new DataCollector.Builder().setPackageName(this)
                .setDataType(DataType.DT_CONTINUOUS_STEPS_DELTA)
                .setDataGenerateType(DataCollector.DATA_TYPE_RAW)
                .build();
        ReadOptions readOptions = new ReadOptions.Builder()
                .read(dataCollector)
                .setTimeRange(System.currentTimeMillis()-24*60*60*1000, System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .build();
        HealthKitTools.getInstance().readDataFromCloud(dataController, readOptions, this);
    }

    public void sync(View view){

        HealthKitTools.getInstance().syncWithFitnessHealth(dataController, this);
    }

    public void syncWithHealthApp(View view){

        HealthAppBridge.getInstance().getGender(this,this);
        HealthAppBridge.getInstance().getBirthDay(this,this);
    }
    public void requestPersmission(View view){
        HealthAppBridge.getInstance().requestPermission(read,write, this,this);
    }

    @Override
    public void onLoginSuccess() {
        try{
            initDataController();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onSilentLoginFailure(Intent signInIntent) {
        startActivityForResult(signInIntent,REQUEST_LOGIN);
    }

    @Override
    public void onHandleSignInResult(boolean result) {
        if (result){
            this.onLoginSuccess();
        }
    }

    @Override
    public void onReadReplySuccess(ReadReply response) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("Response status: "+ response.getStatus());
        if (response.getStatus().getStatusCode() == 0){
            List<SampleSet> list =  response.getSampleSets();
            for (SampleSet set:list){
                for (SamplePoint point: set.getSamplePoints()){
                    String type = point.getDataType().getName();
                    String start = dateFormat.format(new Date(point.getStartTime(TimeUnit.MILLISECONDS)));
                    String end = dateFormat.format(new Date(point.getEndTime(TimeUnit.MILLISECONDS)));
                    System.out.println("--"+type+ " "+start +" "+end);
                    for(Field field:point.getDataType().getFields()){
                        System.out.println("*********"+point.getFieldValue(field));
                    }
                }
            }
        }
    }

    @Override
    public void onReadReplyFailure(Exception e) {
        System.out.println(e.getMessage());
    }

    @Override
    public void onDataInserted() {
        System.out.println("On data inserted!!!");
    }

    @Override
    public void onDataInsertedFailure(Exception e) {
        System.out.println("On data inserted failure:"+e.getMessage());
    }

    @Override
    public void onDataSyncSuccess() {
        System.out.println("---DataSync OK");
    }

    @Override
    public void onDataSyncFailure(Exception e) {
        System.out.println("---DataSync KO: "+e.getMessage());
    }

    @Override
    public void onRequestPermissionResult(int resultCode) {

    }

    @Override
    public void onCheckPermissionStatus(int permission, int resultCode) {

    }

    @Override
    public void onDataReadSuccess(Object data, HealthAppBridge.HealthAppDataType dataType) {

        switch (dataType){
            case BIRTHDATE:{
                System.out.println("---Birthday: "+(int)data);
                break;
            }
            case GENDER:{
                if ((int)data == 1){
                    System.out.println("----Gender is male");
                }else{
                    System.out.println("----Gender is female");
                }
            }
        }
    }

    @Override
    public void onDataReadError(int errorCode, HealthAppBridge.HealthAppDataType dataType) {
        System.out.println("---Data read error for: "+ dataType + " error code: "+errorCode);
    }

    @Override
    public void onDataWriteSuccess(Object data, HealthAppBridge.HealthAppDataType dataType) {

    }

    @Override
    public void onDataWriteError(HealthAppBridge.HealthAppDataType dataType, int errorCode) {

    }
}
