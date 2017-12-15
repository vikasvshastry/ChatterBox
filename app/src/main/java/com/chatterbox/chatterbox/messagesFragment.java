package com.chatterbox.chatterbox;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.Deflater;

import static com.google.android.gms.plus.PlusOneDummyView.TAG;


public class messagesFragment extends Fragment{

    String chatId,name,uid,phno,type;
    RecyclerView recyclerView;
    LinearLayoutManager mLayoutManager;
    private Firebase rootRef = new Firebase("https://chatterbox-b475f.firebaseio.com/");
    FirebaseAuth firebaseAuth;
    private final int REQUEST_CODE=99;

    public messagesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uid = firebaseAuth.getInstance().getCurrentUser().getUid();
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_messages, container, false);

        Bundle b = getArguments();
        chatId = b.getString("chatId");
        name = b.getString("name");
        phno = b.getString("phno");
        type = b.getString("type");

        if(name.isEmpty()) {
            name = phno;
        }
        ((MainActivity) getActivity()).setActionBarTitle(name);

        recyclerView = (RecyclerView)rootView.findViewById(R.id.recyclerViewForComments);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(mLayoutManager);

        final ImageView sendButton = (ImageView)rootView.findViewById(R.id.sendComment);
        final EditText editTextCmt = (EditText)rootView.findViewById(R.id.commentText);

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cmtText = editTextCmt.getText().toString().trim();
                if(TextUtils.isEmpty(cmtText))
                {
                    Toast.makeText(getActivity(), "Nothing to send", Toast.LENGTH_SHORT).show();
                }
                else{
                    try {
                        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

                        Firebase tempRefForKey = rootRef.child("allChats").child(chatId).push();

                        CBMessage msg = new CBMessage();
                        msg.setMessageBody(cmtText);
                        msg.setMsgId(tempRefForKey.getKey());
                        msg.setDate(currentDateTimeString.substring(0, 11));
                        msg.setSenderId(uid);
                        msg.setTime(currentDateTimeString.substring(12, currentDateTimeString.length()));


                        tempRefForKey.setValue(msg, new Firebase.CompletionListener() {
                            @Override
                            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                if (firebaseError == null) {
                                    editTextCmt.setText("");
                                } else {
                                    Toast.makeText(getActivity(), "Unable to send", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }catch(Exception e){
                        Toast.makeText(getActivity(),"Unknown error. Report to dev", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        final FirebaseRecyclerAdapter<CBMessage, CommentViewHolder> adapter = new FirebaseRecyclerAdapter<CBMessage,CommentViewHolder>(
                CBMessage.class,
                R.layout.chat_sent_layout,
                CommentViewHolder.class,
                rootRef.child("allChats").child(chatId)
        ) {
            @Override
            protected void populateViewHolder(CommentViewHolder commentViewHolder, CBMessage c,int i) {
                String msg = c.getMessageBody()+ " " +Html.fromHtml(" &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;");
                commentViewHolder.commentBody.setText(msg);
                commentViewHolder.sentTime.setText(c.getTime());
            }

            @Override
            public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                switch (viewType) {
                    case 1:
                        View userType1 = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.chat_sent_layout, parent, false);
                        return new CommentViewHolder(userType1);
                    case 2:
                        View userType2 = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.chat_receive_layout, parent, false);
                        return new CommentViewHolder(userType2);
                }
                return super.onCreateViewHolder(parent, viewType);
            }

            @Override
            public int getItemViewType(int position) {
                CBMessage c = getItem(position);
                if(TextUtils.equals(c.getSenderId(),uid)){
                    return 1;
                }
                else
                    return 2;
            }
        };

        //scroll down when new comment posted
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                recyclerView.scrollToPosition(positionStart);
            }
        });

        recyclerView.setAdapter(adapter);

        editTextCmt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                    }
                }, 200);
            }
        });

        // Inflate the layout for this fragment
        return rootView;
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder{

        TextView commentBody,sentTime;
        View mView;

        public CommentViewHolder(View v){
            super(v);
            this.mView = v;
            commentBody = (TextView)v.findViewById(R.id.commentBody);
            sentTime = (TextView)v.findViewById(R.id.sentTime);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        ((MainActivity) getActivity()).setActionBarTitle("ChatterBox");
        super.onDetach();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if(type.equals("group"))
        inflater.inflate(R.menu.fragment_menu, menu);
        else
            inflater.inflate(R.menu.fragment_menu_two, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_part:
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, REQUEST_CODE);
                break;
            case R.id.action_view_part:
                final Dialog dialog = new Dialog(getActivity());
                dialog.setContentView(R.layout.view_part_dialog);
                final TextView textView = (TextView)dialog.findViewById(R.id.text);
                final Button button = (Button)dialog.findViewById(R.id.button);
                rootRef.child("participants").child(chatId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String parts = "";
                        for(DataSnapshot snap : dataSnapshot.getChildren()){
                            parts = snap.getValue().toString().trim() + "\n\n" + parts;
                        }
                        textView.setText(parts);
                        dialog.show();
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                    }
                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
                break;
            case R.id.action_location:

                String link = getLoc();
                link = "Find me here : \n"+ Html.fromHtml(link)+"";
                try {
                    String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

                    Firebase tempRefForKey = rootRef.child("allChats").child(chatId).push();

                    CBMessage msg = new CBMessage();
                    msg.setMessageBody(link);
                    msg.setMsgId(tempRefForKey.getKey());
                    msg.setDate(currentDateTimeString.substring(0, 11));
                    msg.setSenderId(uid);
                    msg.setTime(currentDateTimeString.substring(12, currentDateTimeString.length()));


                    tempRefForKey.setValue(msg, new Firebase.CompletionListener() {
                        @Override
                        public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                            if (firebaseError != null) {
                                Toast.makeText(getActivity(), "Unable to send", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }catch(Exception e){
                    Toast.makeText(getActivity(),"Unknown error. Report to dev", Toast.LENGTH_SHORT).show();
                }

                break;

            case R.id.action_upload_file:

                Intent intent1 = new Intent(Intent.ACTION_GET_CONTENT);
                intent1.setType("file/*");
                startActivityForResult(intent1, 200);

                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    String path;
    Uri uri;
    StorageReference rootStoreRef = FirebaseStorage.getInstance().getReference();

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
                        String name1 = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        String num = "";
                        if (Integer.valueOf(hasNumber) == 1) {
                            Cursor numbers = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
                            while (numbers.moveToNext()) {
                                num = numbers.getString(numbers.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            }
                            hasApp(num,name1);
                        }
                    }
                    break;
                }
            case 200:
                if (resultCode == Activity.RESULT_OK) {
                    uri = data.getData();
                    path = uri.getPath();

                    ConnectivityManager connectivityManager = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                    if (!(activeNetworkInfo != null && activeNetworkInfo.isConnected())) {
                        Toast.makeText(getActivity(), "Not connected to the internet.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    uploadAttachment();
                }
                break;
        }
    }

    private void uploadAttachment(){
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Uploading File...");
        progressDialog.show();
        String typeOfFile;
        InputStream stream;
        String key = path;
        try {
            byte[] inputBytes = IOUtils.toByteArray(new FileInputStream(new File(key)));
            Deflater compressor = new Deflater();
            compressor.setLevel(Deflater.BEST_COMPRESSION);
            compressor.setInput(inputBytes);
            compressor.finish();
            ByteArrayOutputStream bos = new ByteArrayOutputStream(inputBytes.length);
            byte[] buf = new byte[1024];
            while (!compressor.finished()) {
                int count = compressor.deflate(buf);
                bos.write(buf, 0, count);
            }
            stream = new ByteArrayInputStream(bos.toByteArray());
            typeOfFile = "doc";

            rootStoreRef.child("all uploads").child(getFileName(uri)).putStream(stream).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(getActivity(), "Done uploading", Toast.LENGTH_SHORT).show();
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    progressDialog.dismiss();

                    String link = downloadUrl.toString();
                    link = "Find file here : \n"+ Html.fromHtml(link)+"";
                    try {
                        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

                        Firebase tempRefForKey = rootRef.child("allChats").child(chatId).push();

                        CBMessage msg = new CBMessage();
                        msg.setMessageBody(link);
                        msg.setMsgId(tempRefForKey.getKey());
                        msg.setDate(currentDateTimeString.substring(0, 11));
                        msg.setSenderId(uid);
                        msg.setTime(currentDateTimeString.substring(12, currentDateTimeString.length()));


                        tempRefForKey.setValue(msg, new Firebase.CompletionListener() {
                            @Override
                            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                if (firebaseError != null) {
                                    Toast.makeText(getActivity(), "Unable to send", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }catch(Exception e){
                        Toast.makeText(getActivity(),"Unknown error. Report to dev", Toast.LENGTH_SHORT).show();
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getActivity(), "Failed", Toast.LENGTH_SHORT).show();
            }});

        }catch(Exception e){
            Toast.makeText(getActivity(), "Network error", Toast.LENGTH_SHORT).show();
        }
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void hasApp(final String PhNumber, final String name1){
        rootRef.child("registered").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    String key = ds.getKey();
                    String value = ds.getValue().toString().trim();
                    if(value.equals(PhNumber)){
                        addNewConversation(value,key,name1);
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

    private void addNewConversation(final String phno, final String uidFriend, final String name1){
        rootRef.child("participants").child(chatId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(uidFriend)){
                    Toast.makeText(getActivity(), "This number has already been added.", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    final chatHead ch = new chatHead();

                    ch.setChatId(chatId);
                    ch.setType("group");
                    ch.setName(name);
                    ch.setPhno(phno);

                    rootRef.child("user-chats").child(uidFriend).push().setValue(ch);
                    rootRef.child("participants").child(chatId).child(uidFriend).setValue(name1);
                    Toast.makeText(getActivity(), "Successfully added", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    final String gpsLocationProvider = LocationManager.GPS_PROVIDER;
    final String networkLocationProvider = LocationManager.NETWORK_PROVIDER;
    String wantPermission = android.Manifest.permission.ACCESS_FINE_LOCATION;
    String longitude,lattitude;

    private boolean checkPermission(String permission){
        if (Build.VERSION.SDK_INT >= 23) {
            int result = ContextCompat.checkSelfPermission(getActivity(), permission);
            if (result == PackageManager.PERMISSION_GRANTED){
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    private String getLoc(){
        LocationManager locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);

        String link="Corrupt link";

        if (checkPermission(wantPermission)) {
            Location lastKnownLocationGps = locationManager.getLastKnownLocation(gpsLocationProvider);
            Location lastKnownLocationNetwork = locationManager.getLastKnownLocation(networkLocationProvider);

            if (lastKnownLocationGps == null) {
                Log.d(TAG, "NO GPS");

                if (lastKnownLocationNetwork == null) {
                    Log.d(TAG, "NO Network");
                    Log.d(TAG, "NO Location!");
                } else {
                    Log.d(TAG, "Location (Network)" + lastKnownLocationNetwork.getLatitude() + ", " + lastKnownLocationNetwork.getLongitude());
                    longitude=lastKnownLocationNetwork.getLongitude()+"";
                    lattitude=lastKnownLocationNetwork.getLatitude()+"";
                }
            } else {
                Log.d(TAG, "GPS " + lastKnownLocationGps.toString());

                if (lastKnownLocationNetwork == null) {
                    Log.d(TAG, "NO Network");
                    Log.d(TAG, "Location (GPS) " + lastKnownLocationGps.getLatitude() + ", " + lastKnownLocationGps.getLongitude());
                    longitude=lastKnownLocationGps.getLongitude()+"";
                    lattitude=lastKnownLocationGps.getLatitude()+"";
                } else {
                    Log.d(TAG, "Network " + lastKnownLocationNetwork.toString());

                    //Both Location provider have last location decide location base on accuracy
                    if (lastKnownLocationGps.getAccuracy() <= lastKnownLocationNetwork.getAccuracy()) {
                        Log.d(TAG, "Location (GPS) " + lastKnownLocationGps.getLatitude() + ", " + lastKnownLocationGps.getLongitude());
                        longitude=lastKnownLocationGps.getLongitude()+"";
                        lattitude=lastKnownLocationGps.getLatitude()+"";
                    } else {
                        Log.d(TAG, "Location (Network) " + lastKnownLocationNetwork.getLatitude() + ", " + lastKnownLocationNetwork.getLongitude());
                        longitude=lastKnownLocationNetwork.getLongitude()+"";
                        lattitude=lastKnownLocationNetwork.getLatitude()+"";
                    }
                }
            }

            lattitude.trim();
            longitude.trim();
            link = "http://www.google.com/maps/place/"+lattitude+","+longitude;
            Toast.makeText(getActivity(),link, Toast.LENGTH_SHORT).show();
            return link;
        }
        return link;
    }
}
