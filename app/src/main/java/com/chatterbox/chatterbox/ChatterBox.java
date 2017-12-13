package com.chatterbox.chatterbox;

import com.firebase.client.Firebase;

/**
 * Created by vikas on 13-Dec-17.
 */

public class ChatterBox extends android.app.Application{
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
        Firebase.getDefaultConfig().setPersistenceEnabled(true);
    }
}
