package com.huawei.demo.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.hihealth.data.Scopes;
import com.huawei.hms.support.api.entity.auth.Scope;
import com.huawei.hms.support.hwid.HuaweiIdAuthAPIManager;
import com.huawei.hms.support.hwid.HuaweiIdAuthManager;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper;
import com.huawei.hms.support.hwid.result.AuthHuaweiId;
import com.huawei.hms.support.hwid.result.HuaweiIdAuthResult;
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService;
import java.util.ArrayList;
import java.util.List;

public class LoginTools {

    final static String TAG ="LogInfo";
    /**
     * Sign-in and authorization method. The authorization screen will display if the current account has not granted authorization.
     */
    public static void signIn(Activity activity, final LoginDelegate delegate) {
        Log.i(TAG, "begin sign in");
        List<Scope> scopeList = new ArrayList<>();

        // Add scopes to apply for. The following only shows an example. Developers need to add scopes according to their specific needs.
        scopeList.add(new Scope(Scopes.HEALTHKIT_STEP_BOTH)); // View and save step counts in HUAWEI Health Kit.
        scopeList.add(new Scope(Scopes.HEALTHKIT_HEIGHTWEIGHT_BOTH)); // View and save height and weight in HUAWEI Health Kit.
        scopeList.add(new Scope(Scopes.HEALTHKIT_HEARTRATE_BOTH)); // View and save the heart rate data in HUAWEI Health Kit.

        // Configure authorization parameters.
        HuaweiIdAuthParamsHelper authParamsHelper = new HuaweiIdAuthParamsHelper(
                HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM);
        HuaweiIdAuthParams authParams = authParamsHelper.setIdToken()
                .setAccessToken()
                .setScopeList(scopeList)
                .createParams();

        // Initialize the HuaweiIdAuthService object.
        final HuaweiIdAuthService authService = HuaweiIdAuthManager.getService(activity, authParams);

        // Silent sign-in. If authorization has been granted by the current account, the authorization screen will not display. This is an asynchronous method.
        Task<AuthHuaweiId> authHuaweiIdTask = authService.silentSignIn();

        // Add the callback for the call result.
        authHuaweiIdTask.addOnSuccessListener(new OnSuccessListener<AuthHuaweiId>() {
            @Override
            public void onSuccess(AuthHuaweiId huaweiId) {
                // The silent sign-in is successful.
                Log.i(TAG, "silentSignIn success");
                delegate.onLoginSuccess();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception exception) {
                // The silent sign-in fails. This indicates that the authorization has not been granted by the current account.
                if (exception instanceof ApiException) {
                    ApiException apiException = (ApiException) exception;
                    Log.i(TAG, "sign failed status:" + apiException.getStatusCode());
                    Log.i(TAG, "begin sign in by intent");

                    // Call the sign-in API using the getSignInIntent() method.
                    Intent signInIntent = authService.getSignInIntent();

                    // Display the authorization screen by using the startActivityForResult() method of the activity.
                    // Developers can change HihealthKitMainActivity to the actual activity.
                    delegate.onSilentLoginFailure(signInIntent);
                }
            }
        });
    }

    public static void handleSignInResult(LoginDelegate delegate, Intent data){
        // Obtain the authorization response from the intent.
        HuaweiIdAuthResult result = HuaweiIdAuthAPIManager.HuaweiIdAuthAPIService.parseHuaweiIdFromIntent(data);
        Log.d(TAG, "handleSignInResult status = " + result.getStatus() + ", result = " + result.isSuccess());
        if (result.isSuccess()) {
            Log.d(TAG, "sign in is success");
            // Obtain the authorization result.
            HuaweiIdAuthResult authResult = HuaweiIdAuthAPIManager.HuaweiIdAuthAPIService.parseHuaweiIdFromIntent(data);
            if (authResult.isSuccess()){
                delegate.onHandleSignInResult(true);
            }

        }
        delegate.onHandleSignInResult(false);
    }

    public interface LoginDelegate{
        void onLoginSuccess();
        void onSilentLoginFailure(Intent signInIntent);
        void onHandleSignInResult(boolean result);
    }

}
