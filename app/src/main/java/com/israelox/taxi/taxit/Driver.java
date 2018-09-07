package com.israelox.taxi.taxit;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class Driver extends AppCompatActivity {


    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    EditText phoneed, codeed;
    ImageView fabbutton;
    String mVerificationId;
    TextView timertext;
    Timer timer;
    ImageView verifiedimg;
    Boolean mVerified = false;
    EditText firstname;
    EditText secondname;
    EditText numberplat;
    String display_name;
    String phonenumber;

    String numberplate;
    Button signIn;
    private PhoneAuthProvider.ForceResendingToken mResendToken;




    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(getApplicationContext(), DriverMapActivity.class));
            finish();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);


        phoneed = (EditText) findViewById(R.id.phone);

        codeed = (EditText) findViewById(R.id.phone_auth_code);
        fabbutton = (ImageView) findViewById(R.id.send_code);
        numberplat=(EditText)findViewById(R.id.numberplate);
        timertext = (TextView) findViewById(R.id.timertv);
        firstname=findViewById(R.id.firstname);
        secondname=findViewById(R.id.secondname);
        String fullnames=firstname+" "+secondname;
        phonenumber=phoneed.toString();

        numberplate=numberplat.toString();



        display_name=fullnames.toString();
        verifiedimg = (ImageView) findViewById(R.id.success);
        signIn = (Button) findViewById(R.id.logina);
        mAuth = FirebaseAuth.getInstance();

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verificaiton without
                //     user action.
                Log.d("TAG", "onVerificationCompleted:" + credential);

                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w("TAG", "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    Snackbar snackbar = Snackbar
                            .make((RelativeLayout) findViewById(R.id.parento), "Enter the correct number format", Snackbar.LENGTH_LONG);

                    snackbar.show();
                }
                else if (e instanceof FirebaseTooManyRequestsException) {
                    Snackbar snackbar = Snackbar
                            .make((RelativeLayout) findViewById(R.id.parento), "Verification Failed !! Too many request. Try after some time. ", Snackbar.LENGTH_LONG);

                    snackbar.show();
                }

            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d("TAG", "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
            }
        };

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (fabbutton.getTag().equals(getResources().getString(R.string.tag_send))) {
                    if (!phoneed.getText().toString().trim().isEmpty() && phoneed.getText().toString().trim().length() >= 10) {
                        startPhoneNumberVerification(phoneed.getText().toString().trim());
                        mVerified = false;
                        starttimer();
                        codeed.setVisibility(View.VISIBLE);
                        codeed.setVisibility(View.VISIBLE);
                        fabbutton.setImageResource(R.drawable.forward);
                        fabbutton.setTag(getResources().getString(R.string.tag_verify));
                    }
                    else {
                        phoneed.setError("Please enter valid mobile number");
                    }
                }

                if (fabbutton.getTag().equals(getResources().getString(R.string.tag_verify))) {
                    if (!codeed.getText().toString().trim().isEmpty() && !mVerified) {
                        Snackbar snackbar = Snackbar
                                .make((RelativeLayout) findViewById(R.id.parento), "Processing your request", Snackbar.LENGTH_LONG);

                        snackbar.show();
                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, codeed.getText().toString().trim());
                        signInWithPhoneAuthCredential(credential);
                    }
                    if (mVerified) {
                        FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                        String uid = current_user.getUid();

                        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);


                        String device_token = FirebaseInstanceId.getInstance().getToken();

                        HashMap<String, String> userMap = new HashMap<>();
                        userMap.put("name", display_name);
                        userMap.put("status", "Hi there I'm using TaxIT on the go App.");
                        userMap.put("image", "default");
                        userMap.put("points", "0");
                        userMap.put("numberplate", numberplate);
                        userMap.put("thumb_image", "default");
                        userMap.put("device_token", device_token);

                        mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                Snackbar snackbar = Snackbar
                                        .make((RelativeLayout) findViewById(R.id.parento), "Processing your request", Snackbar.LENGTH_LONG);

                                snackbar.show();

                                if(task.isSuccessful()){


                                    Snackbar snackbarw = Snackbar
                                            .make((RelativeLayout) findViewById(R.id.parento), "Done!", Snackbar.LENGTH_LONG);

                                    snackbarw.show();


                                    Intent mainIntent = new Intent(getApplicationContext(), DriverMapActivity.class);
                                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(mainIntent);
                                    finish();

                                }

                            }
                        });



//                        startActivity(new Intent(Passenger.this, PassengerMainActivity.class));
                    }

                }

            }
        });





        fabbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fabbutton.getTag().equals(getResources().getString(R.string.tag_send))) {
                    if (!phoneed.getText().toString().trim().isEmpty() && phoneed.getText().toString().trim().length() >= 10) {
                        startPhoneNumberVerification(phoneed.getText().toString().trim());
                        mVerified = false;
                        starttimer();
                        codeed.setVisibility(View.VISIBLE);
//                        signIn.setVisibility(View.VISIBLE);


                        fabbutton.setImageResource(R.drawable.forward);
                        fabbutton.setTag(getResources().getString(R.string.tag_verify));
                    }
                    else {
                        phoneed.setError("Please enter valid mobile number");
                    }
                }

                if (fabbutton.getTag().equals(getResources().getString(R.string.tag_verify))) {
                    if (!codeed.getText().toString().trim().isEmpty() && !mVerified) {
                        Snackbar snackbar = Snackbar
                                .make((RelativeLayout) findViewById(R.id.parento), "Processing your request", Snackbar.LENGTH_LONG);

                        snackbar.show();
                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, codeed.getText().toString().trim());
                        signInWithPhoneAuthCredential(credential);
                    }
                    if (mVerified) {

//                        String user_id = mAuth.getCurrentUser().getUid();
//////                        DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(user_id).child("name");
//////                        current_user_db.setValue(phonenumber);
////                        FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
////                        String uid = current_user.getUid();
////
////                        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
////
////
////                        String device_token = FirebaseInstanceId.getInstance().getToken();
////
////                        HashMap<String, String> userMap = new HashMap<>();
////                        userMap.put("name", display_name);
////                        userMap.put("status", "Hi there I'm using TaxIT on the go App.");
////                        userMap.put("image", "default");
////                        userMap.put("phonenumber", phonenumber);
////                        userMap.put("numberplate", numberplate);
////                        userMap.put("thumb_image", "default");
////                        userMap.put("device_token", device_token);
////
////                        mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
////                            @Override
////                            public void onComplete(@NonNull Task<Void> task) {
////
////
////
////                                if(task.isSuccessful()){
////
////
////                                    Snackbar snackbarw = Snackbar
////                                            .make((RelativeLayout) findViewById(R.id.parento), "Done!", Snackbar.LENGTH_LONG);
////
////                                    snackbarw.show();
////
////
////                                    Intent mainIntent = new Intent(getApplicationContext(), DriverMapActivity.class);
////                                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
////                                    startActivity(mainIntent);
////                                    finish();
////
////                                }
////                                else
////                                {
////
////                                    Toast.makeText(Driver.this, "Failed", Toast.LENGTH_SHORT).show();
////                                }
////
////                            }
////                        });


                        startActivity(new Intent(getApplicationContext(), DriverMapActivity.class));






                    }

                }


            }
        });

        timertext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!phoneed.getText().toString().trim().isEmpty() && phoneed.getText().toString().trim().length() == 10) {
                    resendVerificationCode(phoneed.getText().toString().trim(), mResendToken);
                    mVerified = false;
                    starttimer();
                    codeed.setVisibility(View.VISIBLE);
                    fabbutton.setImageResource(R.drawable.forward);
                    fabbutton.setTag(getResources().getString(R.string.tag_verify));
                    Snackbar snackbar = Snackbar
                            .make((RelativeLayout) findViewById(R.id.parento), "Resending verification code...", Snackbar.LENGTH_LONG);

                    snackbar.show();
                }
            }
        });

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();
                            mVerified = true;
                            timer.cancel();
                            verifiedimg.setImageResource(R.drawable.ic_check_circle_black_24dp);
                            timertext.setVisibility(View.INVISIBLE);
                            phoneed.setEnabled(false);
                            codeed.setVisibility(View.INVISIBLE);
                            Snackbar snackbar = Snackbar
                                    .make((RelativeLayout) findViewById(R.id.parento), "Successfully Verified", Snackbar.LENGTH_LONG);

                            snackbar.show();
                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                Snackbar snackbar = Snackbar
                                        .make((RelativeLayout) findViewById(R.id.parento), "Please enter the correct code", Snackbar.LENGTH_LONG);

                                snackbar.show();
                            }
                        }
                    }
                });
    }

    private void startPhoneNumberVerification(String phoneNumber) {
        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]

    }

    public void starttimer() {
        timer = new Timer();
        timer.schedule(new TimerTask() {

            int second = 60;

            @Override
            public void run() {
                if (second <= 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            timertext.setText("RESEND CODE");
                            timer.cancel();
                        }
                    });

                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            timertext.setText("00:" + second--);
                        }
                    });
                }

            }
        }, 0, 1000);
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

}