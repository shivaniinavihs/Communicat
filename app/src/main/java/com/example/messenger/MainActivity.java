package com.example.messenger;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity
{

    private Toolbar mToolbar;
    private ViewPager myviewPager;
    private TabLayout myTabLayout;
    private TabAccessorAdapter myTabAccessorAdaptor;

    private FirebaseUser currentUser;
    private FirebaseAuth mauth;
    private DatabaseReference RootRef;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mauth=FirebaseAuth.getInstance() ;
        currentUser=mauth.getCurrentUser();
        RootRef= FirebaseDatabase.getInstance().getReference();


        mauth= FirebaseAuth.getInstance();
        mToolbar= (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("CommuniCat");

        myviewPager=(ViewPager)findViewById(R.id.main_tabs_pager);
        myTabAccessorAdaptor=new TabAccessorAdapter(getSupportFragmentManager());
        myviewPager.setAdapter(myTabAccessorAdaptor);

        myTabLayout= (TabLayout)findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myviewPager);



    }

    @Override
    protected void onStart() {
        super.onStart();

        if(currentUser==null)
        {
            SendUserToLoginActivity();
        }
        else
        {
            VerifyUserExistance();
        }

    }

    private void VerifyUserExistance()
    {
        String currentUserID=mauth.getCurrentUser().getUid();
        RootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if((snapshot.child("name").exists()))
                {
                    Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    SendUserToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });

    }

    private void SendUserToLoginActivity()
    {
        Intent loginintent=new Intent(MainActivity.this,LoginActivity.class);
        loginintent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginintent);
        finish();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        super.onOptionsItemSelected(item);
        if(item.getItemId()==R.id.main_find_friends_option)
        {
            SendUserToFindFriendsActivity();
        }
        if(item.getItemId()==R.id.main_settings_option)
        {
            SendUserToSettingsActivity();
        }
        if(item.getItemId()==R.id.main_create_group_option)
        {
            RequestNewGroup();
        }
        if(item.getItemId()==R.id.main_logout_option)
        {
            mauth.signOut();
            SendUserToLoginActivity();
        }
        return true;

    }

    private void RequestNewGroup()
    {
        AlertDialog.Builder builder= new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog);
        builder.setTitle("Enter Group Name : ");
        final EditText groupNameField= new EditText(MainActivity.this);
        groupNameField.setHint("e.g VitFriends");
        builder.setView(groupNameField);
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                String groupName = groupNameField.getText().toString();
                if(TextUtils.isEmpty(groupName))
                {
                    Toast.makeText(MainActivity.this, "Please Write Group Name.... ", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    CreateNewGroup(groupName);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                dialogInterface.cancel();
            }
        });
        builder.show();
    }

    private void CreateNewGroup(final String groupName)
    {
        RootRef.child("Groups").child(groupName).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(MainActivity.this, groupName + "is created successfully...", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void SendUserToSettingsActivity()
    {
        Intent settingsintent=new Intent(MainActivity.this, SettingsActivity.class);

        startActivity(settingsintent);


    }
    private void SendUserToFindFriendsActivity()
    {
        Intent FindFriendsintent=new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(FindFriendsintent);
        finish();

    }


    public TabLayout getMyTabLayout() {
        return myTabLayout;
    }

    public void setMyTabLayout(TabLayout myTabLayout) {
        this.myTabLayout = myTabLayout;
    }
}