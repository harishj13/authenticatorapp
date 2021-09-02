package com.example.authenticatorapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;


public class EditProfile extends AppCompatActivity {

    public static final String TAG = "TAG";
    EditText upadte_full_name,upadte_email,upadte_phone;
    Button update_info_btn;
    ImageView upadte_image;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth fAuth;
    FirebaseUser firebaseUser;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        upadte_image = (ImageView) findViewById(R.id.upadte_image);

        upadte_full_name = (EditText) findViewById(R.id.upadte_full_name);
        upadte_email = (EditText) findViewById(R.id.upadte_email);
        upadte_phone = (EditText) findViewById(R.id.upadte_phone);

        update_info_btn = (Button) findViewById(R.id.update_info_btn);

        firebaseFirestore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        firebaseUser = fAuth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();

        Intent data = getIntent();
        String fullname = data.getStringExtra("fullName");
        String email = data.getStringExtra("email");
        String phone = data.getStringExtra("phone");

        upadte_full_name.setText(fullname);
        upadte_email.setText(email);
        upadte_phone.setText(phone);

        Log.d(TAG," Data of user are " + fullname + " " + email + " " + phone );


        //only show the image
        StorageReference profileRef = storageReference.child("users/"+fAuth.getCurrentUser().getUid()+"/profile.jpg");
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(upadte_image);

            }
        });

        upadte_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //open gallery
                Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(openGalleryIntent,1000);
            }
        });

        update_info_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (upadte_full_name.getText().toString().isEmpty() || upadte_email.getText().toString().isEmpty() || upadte_phone.getText().toString().isEmpty())
                {
                    Toast.makeText(EditProfile.this, "please fill this field", Toast.LENGTH_SHORT).show();
                    return;
                }

                final String email_user = upadte_email.getText().toString();

                firebaseUser.updateEmail(email_user).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        DocumentReference docRef = firebaseFirestore.collection("Users").document(firebaseUser.getUid());
                        Map<String,Object> edit_user = new HashMap<>();
                        edit_user.put("FullName",upadte_full_name.getText().toString());
                        edit_user.put("Email",email_user);
                        edit_user.put("Phone",upadte_phone.getText().toString());
                        docRef.update(edit_user).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                Toast.makeText(EditProfile.this, "Profile is Updated", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(),Logout.class));
                                finish();
                            }
                        });

                        Toast.makeText(EditProfile.this, "Email is Changed", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditProfile.this, " Error ! " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


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

                uploadUpdatedImageToFirebase(imageUri);
            }
        }
    }

    private void uploadUpdatedImageToFirebase( Uri imageUri) {
        //upload image to firebase storage

        final StorageReference fileref = storageReference.child("users/"+fAuth.getCurrentUser().getUid()+"/profile.jpg");
        fileref.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //Toast.makeText(Logout.this, "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
                fileref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        Picasso.get().load(uri).into(upadte_image);

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
            }
        });

    }

}