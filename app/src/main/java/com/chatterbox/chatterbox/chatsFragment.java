package com.chatterbox.chatterbox;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
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
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;

import java.text.DateFormat;
import java.util.Date;

public class chatsFragment extends Fragment {

    RecyclerView recyclerView;
    LinearLayoutManager mLayoutManager;
    private FirebaseAuth firebaseAuth;
    String uid;
    private Firebase rootRef = new Firebase("https://chatterbox-b475f.firebaseio.com/");
    private final int REQUEST_CODE=99;

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
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);

        recyclerView = (RecyclerView)rootView.findViewById(R.id.recyclerViewForChats);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(mLayoutManager);

        final FirebaseRecyclerAdapter<chatHead, ChatsViewHolder> adapter = new FirebaseRecyclerAdapter<chatHead,ChatsViewHolder>(
                chatHead.class,
                R.layout.chat_head_layout,
                chatsFragment.ChatsViewHolder.class,
                rootRef.child("user-chats").child(uid)
        ) {
            @Override
            protected void populateViewHolder(ChatsViewHolder chatsViewHolder, final chatHead c, int i) {
                chatsViewHolder.name.setText(c.getName());
                chatsViewHolder.phoneNo.setText(c.getPhno());

                chatsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putString("chatId",c.getChatId());
                        bundle.putString("name",c.getName());

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

        FloatingActionButton fab = (FloatingActionButton)rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, REQUEST_CODE);
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
                    }
                }
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
                if(dataSnapshot.hasChild(uidFriend)){
                    Toast.makeText(getActivity(), "This number has already been added.", Toast.LENGTH_SHORT).show();
                    return;
                }
                else
                {
                    Firebase chatId = rootRef.child("allChats").push();
                    Firebase msgId = chatId.push();

                    String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

                    CBMessage msg = new CBMessage();
                    msg.setMessageBody("Hi. You can now text me on ChatterBox.");
                    msg.setMsgId(msgId.getKey());
                    msg.setDate(currentDateTimeString.substring(0, 11));
                    msg.setSenderId(uidFriend);
                    msg.setTime(currentDateTimeString.substring(12, currentDateTimeString.length()));

                    msgId.setValue(msg);
                    rootRef.child("user-chats").child(uid).child(uidFriend).child("chatId").setValue(chatId.getKey());
                    rootRef.child("user-chats").child(uid).child(uidFriend).child("name").setValue(name);
                    rootRef.child("user-chats").child(uid).child(uidFriend).child("phno").setValue(phno);
                }
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder{

        TextView name,phoneNo;
        View mView;

        public ChatsViewHolder(View v){
            super(v);
            this.mView = v;
            name = (TextView)v.findViewById(R.id.headingText);
            phoneNo = (TextView)v.findViewById(R.id.bodyText);
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