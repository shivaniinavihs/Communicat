package com.example.messenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.database.CursorIndexOutOfBoundsException;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserId ,senderUserID, Current_State;
    private CircleImageView userProfileImage;
    private TextView userProfileName,userProfileStatus;
    private Button SendMessageRequestButton,DeclineMessageRequestButton;
    private DatabaseReference UserRef ,ChatRequestRef,ContactsRef;
    private FirebaseAuth mauth;
    



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        UserRef= FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestRef= FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactsRef= FirebaseDatabase.getInstance().getReference().child("Contacts");

        mauth=FirebaseAuth.getInstance();

        receiverUserId=getIntent().getExtras().get("Visit_user_id").toString();
        senderUserID=mauth.getCurrentUser().getUid();

        userProfileImage=(CircleImageView)findViewById(R.id.visit_profile_image);
        userProfileName=(TextView)findViewById(R.id.visit_user_name);
        userProfileStatus=(TextView)findViewById(R.id.visit_profile_status);
        SendMessageRequestButton=(Button)findViewById(R.id.send_message_request_button);
        DeclineMessageRequestButton=(Button)findViewById(R.id.decline_message_request_button);

        Current_State="new";
         RetrieveUserInfo();
    }

    private void RetrieveUserInfo()
    {
        UserRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if((snapshot.exists()) && (snapshot.hasChild("image")))
                {

                    String userImage =snapshot.child("image").getValue().toString();
                    String userName =snapshot.child("name").getValue().toString();
                    String userStatus =snapshot.child("status").getValue().toString();
                    Glide.with(ProfileActivity.this).load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    ManageChatRequests();
                }
                else
                {
                    String userName =snapshot.child("name").getValue().toString();
                    String userStatus =snapshot.child("status").getValue().toString();
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);
                    ManageChatRequests();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void ManageChatRequests()
    {
        ChatRequestRef.child(senderUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if(snapshot.hasChild(receiverUserId))
                        {
                            String request_type=snapshot.child(receiverUserId).child("request_type").getValue().toString();
                            if(request_type.equals("sent"))
                            {
                                Current_State="request_sent";
                                SendMessageRequestButton.setText("Cancel Chat Request");
                            }
                            else if(request_type.equals("received"))
                            {
                                Current_State="request_received";
                                SendMessageRequestButton.setText("Accept Chat Request");
                                DeclineMessageRequestButton.setVisibility(View.VISIBLE);
                                DeclineMessageRequestButton.setEnabled(true);
                                DeclineMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view)
                                    {
                                        CancelChatRequest();
                                    }
                                });
                            }
                        }
                        else
                        {
                            ContactsRef.child(senderUserID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot)
                                        {
                                            if(snapshot.hasChild(receiverUserId))
                                            {
                                                Current_State="friends";
                                                SendMessageRequestButton.setText("Remove this contact");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error)
                                        {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {

                    }
                });
        if(!senderUserID.equals(receiverUserId))
        {
            SendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    SendMessageRequestButton.setEnabled(false);
                    if(Current_State.equals("new"))
                    {
                        SendChatRequests();
                    }
                    if(Current_State.equals("request_sent"))
                    {
                        CancelChatRequest();

                    }
                    if(Current_State.equals("request_received"))
                    {
                        AcceptChatRequest();

                    }
                    if(Current_State.equals("friends"))
                    {
                        RemoveSpecificContact();

                    }
                }
            });
        }
        else
        {
            SendMessageRequestButton.setVisibility(View.INVISIBLE);
        }
    }

    private void RemoveSpecificContact()
    {
        ContactsRef.child(senderUserID).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            ContactsRef.child(receiverUserId).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            SendMessageRequestButton.setEnabled(true);
                                            Current_State ="new";
                                            SendMessageRequestButton.setText("Send Message");

                                            DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                            DeclineMessageRequestButton.setEnabled(false);
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptChatRequest()
    {
        ContactsRef.child(senderUserID).child(receiverUserId)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            ContactsRef.child(receiverUserId).child(senderUserID)
                                    .child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                ChatRequestRef.child(senderUserID).child(receiverUserId)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if(task.isSuccessful())
                                                                {
                                                                    ChatRequestRef.child(receiverUserId).child(senderUserID)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                {
                                                                                    SendMessageRequestButton.setEnabled(true);
                                                                                    Current_State="friends";
                                                                                    SendMessageRequestButton.setText("Remove this Contact");
                                                                                    DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                                                    DeclineMessageRequestButton.setEnabled(false);

                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void CancelChatRequest()
    {
        ChatRequestRef.child(senderUserID).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            ChatRequestRef.child(receiverUserId).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                           SendMessageRequestButton.setEnabled(true);
                                           Current_State ="new";
                                           SendMessageRequestButton.setText("Send Message");

                                           DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                           DeclineMessageRequestButton.setEnabled(false);
                                        }
                                    });
                        }
                    }
                });
    }

    private void SendChatRequests()
    {
        ChatRequestRef.child(senderUserID).child(receiverUserId)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            ChatRequestRef.child(receiverUserId).child(senderUserID)
                                    .child("request_type").setValue("recieved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                SendMessageRequestButton.setEnabled(true);
                                                Current_State="request_sent";
                                                SendMessageRequestButton.setText("Cancel Chat Request");

                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}