package com.example.authenticatorapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {


    public static final String TAG = "TAG";
    EditText reg_full_name,reg_email,reg_password,reg_phone;
    Button reg_button,reg_log_button;
    ProgressBar progressBar_register;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    String userID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        reg_full_name = (EditText) findViewById(R.id.reg_full_name);
        reg_email = (EditText) findViewById(R.id.reg_email);
        reg_password = (EditText) findViewById(R.id.reg_password);
        reg_phone = (EditText) findViewById(R.id.reg_phone);
        reg_button = (Button) findViewById(R.id.reg_button);
        reg_log_button = (Button) findViewById(R.id.reg_log_button);
        progressBar_register = (ProgressBar) findViewById(R.id.progressBar_register);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        //add this firebasefirestore settings
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firebaseFirestore.setFirestoreSettings(settings);

        if (firebaseAuth.getCurrentUser() != null)
        {
            startActivity(new Intent(getApplicationContext(),Logout.class));
            finish();
        }

        reg_log_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,Login.class));
            }
        });

        reg_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String fname = reg_full_name.getText().toString();
                final String email = reg_email.getText().toString().trim();
                final String password = reg_password.getText().toString().trim();
                final String phone = reg_phone.getText().toString();

                if (TextUtils.isEmpty(email))
                {
                    reg_email.setError("Email is Required..");
                    return;
                }

                if (TextUtils.isEmpty(password))
                {
                    reg_password.setError("Password is Required..");
                    return;
                }

                if (password.length() < 6)
                {
                    reg_password.setError("Password must be Greater than 6 Characters");
                    return;
                }


                progressBar_register.setVisibility(View.VISIBLE);

                firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            //send verification link for we need firebaseuser object
                            FirebaseUser fuser = firebaseAuth.getCurrentUser();
                            fuser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(MainActivity.this, "Verification email has ben sent", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                               Log.d("tag","email is not send "+ e.getMessage());
                                }
                            });

                            Toast.makeText(MainActivity.this, "User is Successfully Registered", Toast.LENGTH_SHORT).show();

                            userID = firebaseAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = firebaseFirestore.collection("Users").document(userID);
                            Map<String,Object> user = new HashMap<>();
                            user.put("FullName",fname);
                            user.put("Email",email);
                            user.put("Phone",phone);
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG," user is created for " + userID);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG," user is failed to create " + e.toString());
                                }
                            });

                            startActivity(new Intent(getApplicationContext(),Logout.class));
                        }
                        else
                        {
                            Toast.makeText(MainActivity.this, " Error Occured " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar_register.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
    }
}