package com.chatterbox.chatterbox;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.firebase.messaging.FirebaseMessaging;


public class messagesFragment extends Fragment {

    String chatId,name;
    RecyclerView recyclerView;
    LinearLayoutManager mLayoutManager;

    public messagesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_messages, container, false);

        Bundle b = getArguments();
        chatId = b.getString("chatId");
        name = b.getString("name");

        recyclerView = (RecyclerView)rootView.findViewById(R.id.recyclerViewForComments);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(mLayoutManager);

        final ImageView sendButton = (ImageView)rootView.findViewById(R.id.sendComment);
        final EditText editTextCmt = (EditText)rootView.findViewById(R.id.commentText);



        // Inflate the layout for this fragment
        return rootView;
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
