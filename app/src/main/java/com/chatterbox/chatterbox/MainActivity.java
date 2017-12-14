package com.chatterbox.chatterbox;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.*;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;

import java.text.DateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    String uid;
    Fragment fragment = null;
    private Firebase rootRef = new Firebase("https://chatterbox-b475f.firebaseio.com/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        uid = firebaseAuth.getInstance().getCurrentUser().getUid();
        if(firebaseAuth.getCurrentUser() == null){
            //Start login activity
            finish();
            startActivity(new Intent(this, LogBeforeMainActivity.class));
            return;
        }
        setContentView(R.layout.activity_main);
        try {
            getSupportActionBar().show();
        }catch (Exception e){}


        displayView();

    }


    private void displayView() {

        fragment = new chatsFragment();

        if (fragment != null) {
            String backStateName =  fragment.getClass().getName();

            FragmentManager manager = getSupportFragmentManager();
            boolean fragmentPopped = manager.popBackStackImmediate (backStateName, 0);

            if (!fragmentPopped && manager.findFragmentByTag(backStateName) == null){ //fragment not in back stack, create it.
                FragmentTransaction ft = manager.beginTransaction();
                ft.replace(R.id.container_body, fragment, backStateName);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.addToBackStack(backStateName);
                ft.commit();
            }
            // set the toolbar title
            //getSupportActionBar().setTitle(title);
        }
    }

    @Override
    public void onBackPressed(){
        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            finish();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                FirebaseAuth.getInstance().signOut();
                finish();
                startActivity(new Intent(this, LogBeforeMainActivity.class));
                break;
            default:
                break;
        }
        return true;
    }

}
