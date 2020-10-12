package com.example.messenger;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private Button CreateAccountButton;
    private EditText UserEmail ,UserPassword;
    private TextView AlreadyHaveAccountLink;
    private FirebaseAuth mauth;
    private DatabaseReference RootRef;
    private ProgressDialog loadingBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register2);

        mauth=FirebaseAuth.getInstance();
        RootRef= FirebaseDatabase.getInstance().getReference();
        IntializeFields();
        AlreadyHaveAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                SendUserToLoginActivity();

            }
        });
        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                CreateNewAccount();

            }
        });
    }

    private void CreateNewAccount()
    {
        String email= UserEmail.getText().toString();
        String password= UserPassword.getText().toString();

        if (TextUtils.isEmpty(email))
        {
            Toast.makeText(this, "Please enter email..", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(password))
        {
            Toast.makeText(this, "Please enter Password..", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Creating NEW Account");
            loadingBar.setMessage("Please Wait , While we are creating new account for you...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();
            mauth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if(task.isSuccessful())
                            {
                                String currentID=mauth.getCurrentUser().getUid();
                                RootRef.child("Users").child(currentID).setValue(" ");

                                SendUserToMainActivity();
                                Toast.makeText(RegisterActivity.this, "Account Created Successfully...", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                                mauth.signOut();
                            }
                            else
                            {
                                String message= task.getException().toString();
                                Toast.makeText(RegisterActivity.this, "ERROR:"+message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                                mauth.signOut();
                            }
                        }
                    });
        }

    }

    private void IntializeFields()
    {

        CreateAccountButton=(Button)findViewById(R.id.register_button);
        UserEmail =(EditText)findViewById(R.id.register_email);
        UserPassword =(EditText)findViewById(R.id.register_password);
        AlreadyHaveAccountLink =( TextView)findViewById(R.id.Already_have_Account_link);
        loadingBar=new ProgressDialog(this);

    }
    private void SendUserToLoginActivity()
    {
        Intent loginintent=new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(loginintent);
    }
    private void SendUserToMainActivity()
    {
        Intent mainintent=new Intent(RegisterActivity.this,MainActivity.class);
        mainintent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainintent);
        finish();
    }

}