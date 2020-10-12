package com.example.messenger;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.sql.SQLInvalidAuthorizationSpecException;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RequestsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RequestsFragment extends Fragment {
    private View RequestFragmentView;
    private RecyclerView myRequestsList;
    private DatabaseReference ChatRequestRef,UsersRef,ContactsRef;
    private FirebaseAuth mauth;
    private String currentUserID;





    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public RequestsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RequestsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RequestsFragment newInstance(String param1, String param2) {
        RequestsFragment fragment = new RequestsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RequestFragmentView= inflater.inflate(R.layout.fragment_requests, container, false);
        ChatRequestRef= FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        UsersRef=FirebaseDatabase.getInstance().getReference().child("Users");
        ContactsRef=FirebaseDatabase.getInstance().getReference().child("Contacts");
        mauth= FirebaseAuth.getInstance();
        currentUserID=mauth.getCurrentUser().getUid();
        myRequestsList=(RecyclerView)RequestFragmentView.findViewById(R.id.chat_requests_list);
        myRequestsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return RequestFragmentView;

    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options=
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ChatRequestRef.child(currentUserID),Contacts.class)
                .build();
        FirebaseRecyclerAdapter<Contacts,RequestViewHolder> adapter=
                new FirebaseRecyclerAdapter<Contacts, RequestViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final RequestViewHolder holder, int position, @NonNull Contacts model)
                    {
                        holder.itemView.findViewById(R.id.request_accept_btn).setVisibility(View.  VISIBLE);
                        holder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.VISIBLE);

                        final String list_user_id=getRef(position).getKey();
                        final DatabaseReference getTypeRef=getRef(position).child("request_type").getRef();
                        getTypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot)
                            {
                                if(snapshot.exists())
                                {
                                    String type =snapshot.getValue().toString();
                                    if(type.equals("received"))
                                    {
                                        UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot)
                                            {
                                                if(snapshot.hasChild("image"))
                                                {

                                                    final String requestProfileImage=snapshot.child("image").getValue().toString();


                                                    Picasso.get().load(requestProfileImage).into(holder.profileImage);



                                                }

                                                    final String requestUserName=snapshot.child("name").getValue().toString();
                                                    final String requestStatus =snapshot.child("status").getValue().toString();
                                                    holder.userName.setText(requestUserName);
                                                    holder.userStatus.setText("Wants to connect with you");

                                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        CharSequence options[]=new CharSequence[]
                                                                {
                                                                        "Accept",
                                                                        "Cancel"
                                                                };
                                                        AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                                                        builder.setTitle(requestUserName+ "  Chat Request");

                                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i)
                                                            {
                                                                if(i==0)
                                                                {
                                                                    ContactsRef.child(currentUserID).child(list_user_id).child("Contacts")
                                                                            .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task)
                                                                        {
                                                                            if(task.isSuccessful())
                                                                            {
                                                                                ContactsRef.child(list_user_id).child(currentUserID).child("Contacts")
                                                                                        .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task)
                                                                                    {
                                                                                        if(task.isSuccessful())
                                                                                        {
                                                                                            ChatRequestRef.child(currentUserID).child(list_user_id)
                                                                                                    .removeValue()
                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task)
                                                                                                        {
                                                                                                          if(task.isSuccessful())
                                                                                                          {
                                                                                                              ChatRequestRef.child(list_user_id).child(currentUserID)
                                                                                                                      .removeValue()
                                                                                                                      .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                          @Override
                                                                                                                          public void onComplete(@NonNull Task<Void> task)
                                                                                                                          {
                                                                                                                              if(task.isSuccessful())
                                                                                                                              {
                                                                                                                                  Toast.makeText(getContext(), "New Contacts Saved", Toast.LENGTH_SHORT).show();
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
                                                                        }
                                                                    });
                                                                }
                                                                if(i==1)
                                                                {
                                                                    ChatRequestRef.child(currentUserID).child(list_user_id)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                {
                                                                                    if(task.isSuccessful())
                                                                                    {
                                                                                        ChatRequestRef.child(list_user_id).child(currentUserID)
                                                                                                .removeValue()
                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task)
                                                                                                    {
                                                                                                        if(task.isSuccessful())
                                                                                                        {
                                                                                                            Toast.makeText(getContext(), "Contact Deleted", Toast.LENGTH_SHORT).show();
                                                                                                        }
                                                                                                    }
                                                                                                });
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                                        builder.show();


                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error)
                                            {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error)
                            {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
                    {
                        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                        RequestViewHolder Holder=new RequestViewHolder(view);
                        return Holder;

                    }
                };
        myRequestsList.setAdapter(adapter);
        adapter.startListening();
    }
    public static class RequestViewHolder extends  RecyclerView.ViewHolder
    {
        TextView userName,userStatus;
        CircleImageView profileImage;
        Button AcceptButton , CancelButton;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            userName=itemView.findViewById(R.id.user_profile_name);
            userStatus=itemView.findViewById(R.id.user_status);
            profileImage=itemView.findViewById(R.id.users_profile_image);
            AcceptButton=itemView.findViewById(R.id.request_accept_btn);
            userName=itemView.findViewById(R.id.request_cancel_btn);



        }
    }
}