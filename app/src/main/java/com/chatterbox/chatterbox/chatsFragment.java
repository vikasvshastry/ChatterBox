package com.chatterbox.chatterbox;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.util.Date;

public class chatsFragment extends Fragment {

    RecyclerView recyclerView;
    LinearLayoutManager mLayoutManager;
    private FirebaseAuth firebaseAuth;
    String uid,userName;
    private Firebase rootRef = new Firebase("https://chatterbox-b475f.firebaseio.com/");
    private final int REQUEST_CODE=99;
    FirebaseRecyclerAdapter<chatHead, ChatsViewHolder> adapter;
    Boolean isGroup;
    String groupName;

    public chatsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uid = firebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chats, container, false);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setStackFromEnd(true);
        recyclerView = (RecyclerView)rootView.findViewById(R.id.recyclerViewForChats);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(mLayoutManager);

        adapter = new FirebaseRecyclerAdapter<chatHead,ChatsViewHolder>(
                chatHead.class,
                R.layout.chat_head_layout,
                chatsFragment.ChatsViewHolder.class,
                rootRef.child("user-chats").child(uid)
        ) {
            @Override
            protected void populateViewHolder(ChatsViewHolder chatsViewHolder, final chatHead c, int i) {
                chatsViewHolder.name.setText(c.getName());
                chatsViewHolder.phoneNo.setText(c.getPhno());
                String temp = c.getName().charAt(0)+"";
                chatsViewHolder.letter.setText(temp.toUpperCase());
                if(c.getType().equals("group"))
                    chatsViewHolder.triangle.setBackgroundColor(Color.parseColor("#ef5350"));

                chatsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putString("chatId",c.getChatId());
                        bundle.putString("name",c.getName());
                        bundle.putString("phno",c.getPhno());
                        bundle.putString("type",c.getType());

                        Fragment fragment = new messagesFragment();
                        fragment.setArguments(bundle);
                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.add(R.id.container_body, fragment);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();
                    }
                });
            }
        };

        recyclerView.setAdapter(adapter);

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.add_user_or_group);
        final Button group = (Button)dialog.findViewById(R.id.group);
        final Button person = (Button)dialog.findViewById(R.id.addNumber);

        FloatingActionButton fab = (FloatingActionButton)rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isGroup=false;
                dialog.show();
                person.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                        startActivityForResult(intent, REQUEST_CODE);
                    }
                });
                group.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        Toast.makeText(getActivity(), "Please choose one number to start with", Toast.LENGTH_SHORT).show();
                        final Dialog dialog1 = new Dialog(getActivity());
                        dialog1.setContentView(R.layout.create_group_dialog);
                        final EditText groupname = (EditText)dialog1.findViewById(R.id.groupname);
                        final Button select = (Button)dialog1.findViewById(R.id.select);
                        dialog1.show();
                        select.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog1.dismiss();
                                isGroup=true;
                                groupName = groupname.getText().toString().trim();
                                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                                startActivityForResult(intent, REQUEST_CODE);
                            }
                        });

                    }
                });
            }
        });

        return rootView;
    }

    private void hasApp(final String PhNumber, final String name){
        rootRef.child("registered").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    String key = ds.getKey();
                    String value = ds.getValue().toString().trim();
                    if(value.equals(PhNumber)){
                        addNewConversation(value,key,name);
                        return;
                    }
                }
                Toast.makeText(getActivity(), "This number does not have ChatterBox installed.", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        switch (reqCode) {
            case (REQUEST_CODE):
                if (resultCode == Activity.RESULT_OK) {
                    Uri contactData = data.getData();
                    Cursor c = getActivity().getContentResolver().query(contactData, null, null, null, null);
                    if (c.moveToFirst()) {
                        String contactId = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
                        String hasNumber = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                        String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        String num = "";
                        if (Integer.valueOf(hasNumber) == 1) {
                            Cursor numbers = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
                            while (numbers.moveToNext()) {
                                num = numbers.getString(numbers.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            }
                            hasApp(num,name);
                        }
                    }
                    break;
                }
        }
    }

    private void addNewConversation(final String phno, final String uidFriend, final String name){
        rootRef.child("user-chats").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(uidFriend) && (!isGroup)){
                    Toast.makeText(getActivity(), "This number has already been added.", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    final Firebase chatId = rootRef.child("allChats").push();

                    final chatHead ch = new chatHead();

                    ch.setChatId(chatId.getKey());
                    ch.setType("p2p");
                    ch.setName(name);
                    ch.setPhno(phno);

                    if(isGroup){
                        ch.setName(groupName);
                        ch.setType("group");
                        rootRef.child("participants").child(chatId.getKey()).child(uidFriend).setValue(name);
                        rootRef.child("user-chats").child(uid).push().setValue(ch);
                    }
                    else
                    {
                        rootRef.child("user-chats").child(uid).child(uidFriend).setValue(ch);
                    }

                    rootRef.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            userName = dataSnapshot.child("name").getValue(String.class);
                            String phnum = dataSnapshot.child("phno").getValue(String.class);
                            ch.setName(userName);
                            ch.setPhno(phnum);
                            if(isGroup) {
                                ch.setName(groupName);
                                rootRef.child("user-chats").child(uidFriend).push().setValue(ch);
                                rootRef.child("participants").child(chatId.getKey()).child(uid).setValue(userName);
                            }
                            else{
                                rootRef.child("user-chats").child(uidFriend).child(uid).setValue(ch);
                            }
                        }
                        @Override
                        public void onCancelled(FirebaseError firebaseError) {
                        }
                    });

                }
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder{

        TextView name,phoneNo,letter;
        View mView;
        ImageView triangle;

        public ChatsViewHolder(View v){
            super(v);
            this.mView = v;
            name = (TextView)v.findViewById(R.id.headingText);
            phoneNo = (TextView)v.findViewById(R.id.bodyText);
            triangle = (ImageView)v.findViewById(R.id.sideBar);
            letter = (TextView)v.findViewById(R.id.letter);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}