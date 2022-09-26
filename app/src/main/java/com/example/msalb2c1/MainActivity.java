package com.example.msalb2c1;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.widget.Button;
import android.widget.TextView;

import com.microsoft.identity.client.AcquireTokenParameters;
import com.microsoft.identity.client.AuthenticationCallback;
import com.microsoft.identity.client.IAccount;
import com.microsoft.identity.client.IAuthenticationResult;
import com.microsoft.identity.client.IMultipleAccountPublicClientApplication;
import com.microsoft.identity.client.IPublicClientApplication;
import com.microsoft.identity.client.Prompt;
import com.microsoft.identity.client.PublicClientApplication;
import com.microsoft.identity.client.exception.MsalException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView mTextView;
    Button mMsalButton;
    IMultipleAccountPublicClientApplication mMsalClient;
    IAccount mFirstAccount = null;

    String mScope = "<type in the scope you need here>";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = findViewById(R.id.text_view_name);
        mMsalButton = findViewById(R.id.msal_button);

        PublicClientApplication.createMultipleAccountPublicClientApplication(this,
                R.raw.msal_config,
                new IPublicClientApplication.IMultipleAccountApplicationCreatedListener() {
                    @Override
                    public void onCreated(IMultipleAccountPublicClientApplication application) {
                        mMsalClient = application;
                        if (mMsalClient != null) {
                            mMsalClient.getAccounts(new IPublicClientApplication.LoadAccountsCallback() {
                                @Override
                                public void onTaskCompleted(List<IAccount> result) {
                                    if (result.size() > 0) {
                                        mFirstAccount = result.get(0);
                                    }
                                }

                                @Override
                                public void onError(MsalException exception) {
                                    mTextView.setText(exception.getMessage());
                                }
                            });
                        }
                        mMsalButton.setText("Edit Profile");
                        mMsalButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                login();
                            }
                        });
                    }

                    @Override
                    public void onError(MsalException exception) {
                        mTextView.setText(exception.getMessage());
                    }
                });
    }

    private void login() {
        mMsalClient.acquireToken(getParameters());
    }

    private AcquireTokenParameters getParameters() {
        if (mFirstAccount == null) {
            return new AcquireTokenParameters.Builder()
                    .startAuthorizationFromActivity(this)
                    .withScopes(Arrays.asList(mScope))
                    .withCallback(getAuthCallback())
                    .build();
        }
        else {
            return new AcquireTokenParameters.Builder()
                    .startAuthorizationFromActivity(this)
                    .withScopes(Arrays.asList(mScope))
                    //.withLoginHint(mFirstAccount.getUsername())
                    .withCallback(getAuthCallback())
                    .build();
        }
    }

    private AuthenticationCallback getAuthCallback() {
        return new AuthenticationCallback() {
            @Override
            public void onCancel() {
                mTextView.setText("User cancelled Authentication.");
            }

            @Override
            public void onSuccess(IAuthenticationResult authenticationResult) {
                mFirstAccount = authenticationResult.getAccount();
                mTextView.setText(mFirstAccount.getClaims().toString());
            }

            @Override
            public void onError(MsalException exception) {
                mTextView.setText(exception.getMessage());
            }
        };
    }
}