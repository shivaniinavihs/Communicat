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

public class LoginActivity extends AppCompatActivity {


    private FirebaseAuth mauth;

    private Button LoginButton ,  PhoneLoginButton;
    private EditText UserEmail ,UserPassword;
    private TextView NeedNewAccountLink , ForgetPasswordLink;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);
        mauth=FirebaseAuth.getInstance() ;

        IntializeFields();


        NeedNewAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                SendUserToRegisterActivity();

            }
        });
        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                AllowUserToLogin();
            }
        });
        PhoneLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent phoneLoginIntent=new Intent(LoginActivity.this,PhoneLoginActivity.class);
                startActivity(phoneLoginIntent);
            }
        });
    }
    private void AllowUserToLogin()
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
            loadingBar.setTitle("Sign In");
            loadingBar.setMessage("Please Wait...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();
            mauth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if(task.isSuccessful())
                            {
                                SendUserToMainActivity();
                                Toast.makeText(LoginActivity.this, "Logged in Successful...", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                            else
                            {
                                String message= task.getException().toString();
                                Toast.makeText(LoginActivity.this, "ERROR:"+message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();

                            }

                        }
                    });
        }
    }


    private void IntializeFields()
    {
        LoginButton=(Button)findViewById(R.id.login_button);
        PhoneLoginButton=(Button)findViewById(R.id.phone_login_button);
        UserEmail =(EditText)findViewById(R.id.login_email);
        UserPassword =(EditText)findViewById(R.id.login_password);
        NeedNewAccountLink =( TextView)findViewById(R.id.need_new_account_link);
        ForgetPasswordLink =( TextView)findViewById(R.id.forget_password_link);
        loadingBar= new ProgressDialog(this);



    }


    private void SendUserToMainActivity()
    {
        Intent mainintent=new Intent(LoginActivity.this,MainActivity.class);
        mainintent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainintent);
        finish();
    }
    private void SendUserToRegisterActivity()
    {
        Intent registerintent=new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(registerintent);
    }

}