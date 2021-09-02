package com.example.authenticatorapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {

    EditText login_email,login_password;
    Button login_button;
    ProgressBar progressBar_login;
    FirebaseAuth firebaseAuth;
    TextView text,forgot_password;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        text = (TextView) findViewById(R.id.text);
        forgot_password = (TextView) findViewById(R.id.forgot_password);
        login_email = (EditText) findViewById(R.id.login_email);
        login_password = (EditText) findViewById(R.id.login_password);
        login_button = (Button) findViewById(R.id.login_button);
        progressBar_login = (ProgressBar) findViewById(R.id.progressBar_login);

        firebaseAuth = FirebaseAuth.getInstance();

        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
            }
        });


     forgot_password.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
             //startActivity(new Intent(getApplicationContext(),Logout.class));
             //finish();
             final EditText resetMail = new EditText(view.getContext());
             final AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(view.getContext());
             passwordResetDialog.setTitle("Reset Password");
             passwordResetDialog.setMessage("Enter Email To Received The Reset Link");
             passwordResetDialog.setView(resetMail);


             passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialogInterface, int i) {

                     String email = resetMail.getText().toString();
                     if (TextUtils.isEmpty(email))
                     {
                         Toast.makeText(Login.this, "please provide email", Toast.LENGTH_LONG).show();
                         return;
                     }
                     firebaseAuth.sendPasswordResetEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
                         @Override
                         public void onSuccess(Void aVoid) {

                             Toast.makeText(getApplicationContext()," Reset Link for Email is Sent Check Your Email",Toast.LENGTH_LONG).show();
                         }
                     }).addOnFailureListener(new OnFailureListener() {
                         @Override
                         public void onFailure(@NonNull Exception e) {
                             Toast.makeText(Login.this, " Error for sending Reset Link " + e.getMessage(), Toast.LENGTH_SHORT).show();
                         }
                     });
                 }
             });

             passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialogInterface, int i) {

                 }
             });

             passwordResetDialog.create().show(); // <--- this statement creates dialog box
         }
     });





        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = login_email.getText().toString().trim();
                String password = login_password.getText().toString().trim();

                if (TextUtils.isEmpty(email))
                {
                    login_email.setError("Email is Required..");
                    return;
                }

                if (TextUtils.isEmpty(password))
                {
                    login_password.setError("Password is Required..");
                    return;
                }

                if (password.length() < 6)
                {
                    login_password.setError("Please Enter Password Greater than 6 Characters");
                    return;
                }

                progressBar_login.setVisibility(View.VISIBLE);


                firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful())
                        {
                            Toast.makeText(Login.this, "User is Successfully Logged in", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(),Logout.class));
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext()," Error Occured " + task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            progressBar_login.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });

    }
}