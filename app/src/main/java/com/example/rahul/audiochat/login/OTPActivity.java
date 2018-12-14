package com.example.rahul.audiochat.login;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rahul.audiochat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import org.w3c.dom.Text;

import java.util.concurrent.TimeUnit;

public class OTPActivity extends AppCompatActivity {

    private static final String TAG = "PhoneAuthActivity";
    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";

    private static final int STATE_INITIALIZED = 1;
    private static final int STATE_CODE_SENT = 2;
    private static final int STATE_VERIFY_FAILED = 3;
    private static final int STATE_VERIFY_SUCCESS = 4;
    private static final int STATE_SIGNIN_FAILED = 5;
    private static final int STATE_SIGNIN_SUCCESS = 6;

    EditText etPhone, etOTP;
    TextView tvStatus;
    CardView btnSendOTP,btnVerifyPhone, btnResendOtp,btnSignOut;
    ProgressBar progressBar;

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    private boolean mVerificationInProgress = false;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        // Restore instance state
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }

        etPhone = findViewById(R.id.et_phone);
        etOTP = findViewById(R.id.et_otp);
        btnSendOTP = findViewById(R.id.btn_send_otp);
        tvStatus = findViewById(R.id.tv_status);
        btnVerifyPhone = findViewById(R.id.btn_verify);
        btnResendOtp = findViewById(R.id.btn_resend_otp);
        progressBar = findViewById(R.id.progressBar2);
        btnSignOut = findViewById(R.id.btn_sign_out);

        mAuth = FirebaseAuth.getInstance();

        btnSendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validatePhoneNumber()){
                    startPhoneNumberVerification("+91"+etPhone.getText().toString());
                }else{
                    etPhone.setError("Incorrect Phone number");
                    enableViews(progressBar);
                }
            }
        });

        btnVerifyPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = etOTP.getText().toString();
                if (!TextUtils.isEmpty(code)){
                    verifyPhoneNumberWithCode(mVerificationId, code);
                }
                enableViews(progressBar);
            }
        });

        btnResendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resendVerificationCode("+91"+etPhone.getText().toString(), mResendToken);
                enableViews(progressBar);
            }
        });

        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:" + phoneAuthCredential);
                // [START_EXCLUDE silent]
                mVerificationInProgress = false;
                // [END_EXCLUDE]

                // [START_EXCLUDE silent]
                // Update the UI and attempt sign in with the phone credential
              //  updateUI(STATE_VERIFY_SUCCESS, phoneAuthCredential);
                // [END_EXCLUDE]

                // Verification has succeeded, proceed to firebase sign in
                disableViews(btnSendOTP, btnVerifyPhone, btnResendOtp, etPhone, progressBar, btnSignOut);
                enableViews(btnSendOTP);
                tvStatus.setText("Verification success");

                // Set the verification text based on the credential
                if (phoneAuthCredential != null) {
                    if (phoneAuthCredential.getSmsCode() != null) {
                        etOTP.setText(phoneAuthCredential.getSmsCode());
                    } else {
                        etOTP.setText("Instant Validation");
                    }
                }

                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e);
                // [START_EXCLUDE silent]
                mVerificationInProgress = false;
                // [END_EXCLUDE]

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // [START_EXCLUDE]
                    etPhone.setError("Invalid phone number.");
                    // [END_EXCLUDE]
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // [START_EXCLUDE]
                    Snackbar.make(findViewById(android.R.id.content), "Quota exceeded.",
                            Snackbar.LENGTH_SHORT).show();
                    // [END_EXCLUDE]
                }

                enableViews(btnVerifyPhone, btnResendOtp, etOTP);
                disableViews(btnSendOTP,etPhone, progressBar, btnSignOut);
                tvStatus.setText("Verification failed");

                // Show a message and update the UI
                // [START_EXCLUDE]
               // updateUI(STATE_VERIFY_FAILED);
// [END_EXCLUDE]
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + s);

                // Save verification ID and resending token so we can use them later
                mVerificationId = s;
                mResendToken = forceResendingToken;

                disableViews(btnSendOTP,etPhone, btnSignOut);
                enableViews(btnVerifyPhone, btnResendOtp, etOTP);
                tvStatus.setText("code sent");

                // [START_EXCLUDE]
                // Update UI
//                updateUI(STATE_CODE_SENT);
// [END_EXCLUDE]
            }
        };

    }

    // [START on_start_check_user]
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);

        // [START_EXCLUDE]
        if (mVerificationInProgress && validatePhoneNumber()) {
            startPhoneNumberVerification("+91"+etPhone.getText().toString());
        }
        // [END_EXCLUDE]
    }
// [END on_start_check_user]

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS);
    }

    private boolean validatePhoneNumber() {
        String phoneNumber = etPhone.getText().toString();
        if (TextUtils.isEmpty(phoneNumber)) {
            etPhone.setError("Invalid phone number.");
            return false;
        }

        return true;
    }

    private void updateUI(int uiState) {
        updateUI(uiState, mAuth.getCurrentUser(), null);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            updateUI(STATE_SIGNIN_SUCCESS, user);
        } else {
            updateUI(STATE_INITIALIZED);
        }
    }

    private void updateUI(int uiState, FirebaseUser user) {
        updateUI(uiState, user, null);
    }

    private void updateUI(int uiState, PhoneAuthCredential cred) {
        updateUI(uiState, null, cred);
    }


    private void updateUI(int uiState, FirebaseUser user, PhoneAuthCredential cred) {
        switch (uiState) {
            case STATE_INITIALIZED:
                // Initialized state, show only the phone number field and start button
                enableViews(etPhone, btnSendOTP);
                disableViews(btnVerifyPhone, btnResendOtp,etOTP, btnSignOut, progressBar);
                tvStatus.setText(null);
                break;
            case STATE_CODE_SENT:
                // Code sent state, show the verification field, the

                break;
            case STATE_VERIFY_FAILED:
                // Verification has failed, show all options

                break;
            case STATE_VERIFY_SUCCESS:


                break;
            case STATE_SIGNIN_FAILED:
                // No-op, handled by sign-in check
                disableViews(btnVerifyPhone, btnResendOtp,etOTP,etPhone,btnSendOTP, progressBar, btnSignOut);
                enableViews(etPhone, btnSendOTP);
                tvStatus.setText("Sign In Failed");
                break;
            case STATE_SIGNIN_SUCCESS:
                // Np-op, handled by sign-in check
                enableViews(btnSignOut);
                disableViews(btnVerifyPhone, btnResendOtp,etOTP,etPhone,btnSendOTP, progressBar);
                tvStatus.setText("Sign In Success");
                break;
        }

        if (user == null) {
            // Signed out
//            etPhone.setVisibility(View.VISIBLE);
            enableViews(etPhone, btnSendOTP);
            disableViews(btnVerifyPhone, btnResendOtp,etOTP, btnSignOut);
            tvStatus.setText(null);
            ///mSignedInViews.setVisibility(View.GONE);

            tvStatus.setText("Signed Out");
        } else {
            // Signed in
//            etPhone.setVisibility(View.GONE);
            //mSignedInViews.setVisibility(View.VISIBLE);
            disableViews(btnVerifyPhone, btnResendOtp,etOTP,etPhone,btnSendOTP);
            enableViews(btnSignOut);

            tvStatus.setText("Signed In");
            Toast.makeText(OTPActivity.this,"User id is: "+user.getUid(),Toast.LENGTH_SHORT).show();
        }
    }

    private void enableViews(View... views) {
        for (View v : views) {
            v.setVisibility(View.VISIBLE);
            v.setEnabled(true);
        }
    }

    private void disableViews(View... views) {
        for (View v : views) {
//            v.setVisibility(View.GONE);
            v.setEnabled(false);
            v.setVisibility(View.INVISIBLE);
        }
    }

    // [START sign_in_with_phone]
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();
                            // [START_EXCLUDE]
                            updateUI(STATE_SIGNIN_SUCCESS, user);
                            // [END_EXCLUDE]
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                // [START_EXCLUDE silent]
                                etOTP.setError("Invalid code.");
                                // [END_EXCLUDE]
                            }
                            // [START_EXCLUDE silent]
                            // Update UI
                            updateUI(STATE_SIGNIN_FAILED);
                            // [END_EXCLUDE]
                        }
                    }
                });
    }
// [END sign_in_with_phone]

    private void startPhoneNumberVerification(String phoneNumber) {
        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]

        mVerificationInProgress = true;
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        // [START verify_with_code]
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        // [END verify_with_code]
        signInWithPhoneAuthCredential(credential);
    }

    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }

    private void signOut() {
        mAuth.signOut();
        updateUI(STATE_INITIALIZED);
    }

}
