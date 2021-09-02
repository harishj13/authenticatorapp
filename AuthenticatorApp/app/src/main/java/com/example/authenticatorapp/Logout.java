package com.example.authenticatorapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import javax.annotation.Nullable;

public class Logout extends AppCompatActivity {

    private static final int GALLERY_INTENT_CODE = 1023;
    Button logout;
FirebaseAuth firebaseAuth;
TextView profile_name,profile_email,profile_phone;
FirebaseFirestore firestore;
String userId;
TextView email_not_verify,email_verify;
Button verify_btn,change_profile,update_profile;
ImageView profile_image;
StorageReference storageReference;
//FirebaseStorage firebaseStorage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logout);
        logout = (Button) findViewById(R.id.logout);
        profile_name = (TextView) findViewById(R.id.profile_name);
        profile_email = (TextView) findViewById(R.id.profile_email);
        profile_phone = (TextView) findViewById(R.id.profile_phone);

        email_not_verify = (TextView) findViewById(R.id.email_not_verify);
        email_verify = (TextView) findViewById(R.id.email_verify);
        verify_btn = (Button) findViewById(R.id.verify_btn);

        update_profile = (Button) findViewById(R.id.update_profile);

        change_profile = (Button) findViewById(R.id.change_profile);
        profile_image = (ImageView) findViewById(R.id.profile_image);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        //firebaseStorage = FirebaseStorage.getInstance();
        //storageReference = FirebaseStorage.getInstance().getReference();

        //FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
              .setTimestampsInSnapshotsEnabled(true)
                .build();
            firestore.setFirestoreSettings(settings);


        storageReference = FirebaseStorage.getInstance().getReference();

        StorageReference profileRef = storageReference.child("users/"+firebaseAuth.getCurrentUser().getUid()+"/profile.jpg");
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(profile_image);
            }
        });



        /*StorageReference profile_image_set = storageReference.child("users/"+firebaseAuth.getCurrentUser().getUid()+"profile.jpg");
            profile_image_set.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {

                    Picasso.get().load(uri).into(profile_image);

                }
            }); */


        userId = firebaseAuth.getCurrentUser().getUid();

        final FirebaseUser user = firebaseAuth.getCurrentUser();

        if (!user.isEmailVerified())
        {
            email_not_verify.setVisibility(View.VISIBLE);
            verify_btn.setVisibility(View.VISIBLE);

            verify_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getApplicationContext()," Verification link sent to your email ",Toast.LENGTH_LONG).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("tag"," Email : on failure " + e.getMessage());
                        }
                    });
                }
            });
        }

        if (user.isEmailVerified())
        {
            email_verify.setVisibility(View.VISIBLE);
            email_not_verify.setVisibility(View.GONE);
            verify_btn.setVisibility(View.GONE);
        }

        DocumentReference documentReference = firestore.collection("Users").document(userId);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                if (e == null) {
                    if (documentSnapshot.exists()) {
                        profile_name.setText(documentSnapshot.getString("FullName"));
                        profile_email.setText(documentSnapshot.getString("Email"));
                        profile_phone.setText(documentSnapshot.getString("Phone"));
                    } else {
                        Log.d("Tag", "OnEvent : Document does not exists");
                    }
                  }
                }
        });


        update_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Logout.this,EditProfile.class);
                i.putExtra("fullName",profile_name.getText().toString());
                i.putExtra("email",profile_email.getText().toString());
                i.putExtra("phone",profile_phone.getText().toString());
                startActivity(i);
            }
        });








        change_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //open gallery
                Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(openGalleryIntent,1000);

            }
        });



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1000)
        {
            if (resultCode == Activity.RESULT_OK)
            {
                         Uri imageUri =data.getData();
                        // profile_image.setImageURI(imageUri);

                         uploadImageToFirebase(imageUri);
            }
        }
    }

    private void uploadImageToFirebase( Uri imageUri) {
        //upload image to firebase storage

        final StorageReference fileref = storageReference.child("users/"+firebaseAuth.getCurrentUser().getUid()+"/profile.jpg");
        fileref.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //Toast.makeText(Logout.this, "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
                fileref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        Picasso.get().load(uri).into(profile_image);

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Logout.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void logout(View view)
    {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(),Login.class));
        finish();
    }
}