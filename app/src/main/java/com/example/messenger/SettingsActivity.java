package com.example.messenger;


import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    private Button UpdateAccountSettings;
    private TextView userName , userStatus;
    private CircleImageView userProfileImage;
    private String currentUserID;
    private FirebaseAuth mauth;
    private DatabaseReference RootRef;
    private static final int GalleryPic=1;
    private StorageReference UserProfileImageRef;
    private ProgressDialog loadingbar;
    private Toolbar SettingsToolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings2);
        mauth=FirebaseAuth.getInstance() ;
        currentUserID= mauth.getCurrentUser().getUid();
        RootRef= FirebaseDatabase.getInstance().getReference();
        UserProfileImageRef= FirebaseStorage.getInstance().getReference().child("Profile Images");

        InitializeFields();
        UpdateAccountSettings.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view)
            {
                UpdateSettings();

            }
        });
        RetrieveUserInfo();

        userProfileImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent galleryIntent=new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GalleryPic);
            }

        });

    }


    private void InitializeFields()
    {
        UpdateAccountSettings=(Button)findViewById(R.id.update_settings_buttton);
        userName=(TextView) findViewById(R.id.set_username);
        userStatus=(TextView)findViewById(R.id.set_profilestatus);
        userProfileImage=(CircleImageView) findViewById(R.id.set_profile_image);
        loadingbar =new ProgressDialog(this);
        SettingsToolBar=(Toolbar)findViewById(R.id.settings_toolbar);
        setSupportActionBar(SettingsToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Settings");

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GalleryPic && resultCode==RESULT_OK && data!=null)
        {
            Uri ImageUri=data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode== RESULT_OK)
            {
                loadingbar.setTitle("set Profile Image");
                loadingbar.setMessage("Please wait,your Profile Image is updating...");
                loadingbar.setCanceledOnTouchOutside(false);
                loadingbar.show();
                Uri resultUri= result.getUri();
                StorageReference filePath=  UserProfileImageRef.child(currentUserID +".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                    {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(SettingsActivity.this, "Profile Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
                            final String downloadUri=task.getResult().getStorage().getDownloadUrl().toString();
                            RootRef.child("Users").child(currentUserID).child("image")
                                    .setValue(downloadUri)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                Toast.makeText(SettingsActivity.this, "Image save in Database,Successfully....", Toast.LENGTH_SHORT).show();
                                                Glide.with(SettingsActivity.this).load(downloadUri).into(userProfileImage);
                                            }
                                            else
                                            {
                                                String message=task.getException().toString();
                                                Toast.makeText(SettingsActivity.this, "ERROR : "+ message, Toast.LENGTH_SHORT).show();
                                            }
                                            loadingbar.dismiss();
                                        }
                                    });
                        }
                        else
                        {
                            String message= task.getException().toString();
                            Toast.makeText(SettingsActivity.this, "ERROR : " + message, Toast.LENGTH_SHORT).show();
                            loadingbar.dismiss();
                        }
                    }
                });

            }
        }
    }

    private void UpdateSettings()
    {
        String setUserName=userName.getText().toString();
        String setStatus =userStatus.getText().toString();
        if(TextUtils.isEmpty(setUserName))
        {
            Toast.makeText(this, "Please write your user name..", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(setStatus))
        {
            Toast.makeText(this, "Please write your Status..", Toast.LENGTH_SHORT).show();
        }
        else
        {
            HashMap<String,Object> profileMap=new HashMap<>();
            profileMap.put("uid",currentUserID);
            profileMap.put("name",setUserName);
            profileMap.put("status",setStatus);
            RootRef.child("Users").child(currentUserID).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                SendUserToMainActivity();
                                Toast.makeText(SettingsActivity.this, "Profile Updated Successfully....", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                String message=task.getException().toString();
                                Toast.makeText(SettingsActivity.this, "ERROR :" + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


        }
    }
    public void RetrieveUserInfo()
    {
        RootRef.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        Toast.makeText(SettingsActivity.this, "error", Toast.LENGTH_SHORT).show();
                        if((snapshot.exists())&&(snapshot.hasChild("name")&&(snapshot.hasChild("image"))))
                        {
                            Toast.makeText(SettingsActivity.this, " ", Toast.LENGTH_SHORT).show();
                            String retrieveUserName=snapshot.child("name").getValue().toString();
                            String retrieveStatus=snapshot.child("status").getValue().toString();
                            String retrieveProfileImage=snapshot.child("image").getValue().toString();

                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveStatus);
                            Glide.with(SettingsActivity.this).load(retrieveProfileImage).into(userProfileImage);

                        }
                        else if ((snapshot.exists())&&(snapshot.hasChild("name")))
                        {
                            String retrieveUserName=snapshot.child("name").getValue().toString();
                            String retrieveStatus=snapshot.child("status").getValue().toString();

                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveStatus);

                        }
                        else
                        {
                            Toast.makeText(SettingsActivity.this, "Please set & Update your Profile information...", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {

                    }
                });
    }

    private void SendUserToMainActivity()
    {
        Intent mainintent =new Intent(SettingsActivity.this,MainActivity.class);
        mainintent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainintent);
        finish();
    }

}

