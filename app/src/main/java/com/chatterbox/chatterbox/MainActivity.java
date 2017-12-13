package com.chatterbox.chatterbox;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private int fragno;
    Fragment fragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() == null){
            //Start login activity
            finish();
            startActivity(new Intent(this, LogBeforeMainActivity.class));
            return;
        }

        setContentView(R.layout.activity_main);

        displayView(0);

    }

    private void displayView(int position) {
        String title = getString(R.string.app_name);
        fragno=position;
        switch (position) {
            case 0:
                fragment = new chatsFragment();
                title = "ChatterBox";
                break;
            case 1:
                fragment = new messagesFragment();
                title = "messages";
                break;
        }

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
            getSupportActionBar().setTitle(title);
        }
    }

}
