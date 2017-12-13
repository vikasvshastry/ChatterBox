package com.chatterbox.chatterbox;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegBeforeMainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button buttonCreateAccount;
    private Button buttonLinkLogin;
    private EditText editTextName;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextPhno;
    private TextView textLoginLink;
    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;
    private Firebase rootRef = new Firebase("https://chatterbox-b475f.firebaseio.com/");
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_before_main);

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);

        //remove this after verification since its not necessary.
        if(firebaseAuth.getCurrentUser() != null){
            //Start main activity
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }

        buttonCreateAccount = (Button) findViewById(R.id.btn_signup);
        editTextName = (EditText) findViewById(R.id.input_name);
        editTextEmail = (EditText) findViewById(R.id.input_email);
        editTextPassword = (EditText) findViewById(R.id.input_password);
        buttonLinkLogin = (Button) findViewById(R.id.link_login);
        editTextPhno = (EditText) findViewById(R.id.input_phno);
        textLoginLink = (TextView) findViewById(R.id.login_link_text);

        textLoginLink.setOnClickListener(this);

        buttonCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        buttonLinkLogin.setOnClickListener(this);
    }

    private void registerUser(){
        final String email = editTextEmail.getText().toString().trim();
        final String name = editTextName.getText().toString().trim();
        final String password = editTextPassword.getText().toString().trim();
        final String Phno = editTextPhno.getText().toString().trim();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(RegBeforeMainActivity.this, "Please enter Email", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(name)){
            Toast.makeText(RegBeforeMainActivity.this, "Please enter Name", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(password) || !(password.length()>=6)){
            Toast.makeText(RegBeforeMainActivity.this, "Enter a Password that's at least 6 characters long", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(Phno) || Phno.length()!=10){
            Toast.makeText(RegBeforeMainActivity.this, "Phno field is either empty or incorrect", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Registering...");
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            //intent to login the user and take him to mainActivity
                            //Adding the user data to the database
                            uid = firebaseAuth.getCurrentUser().getUid();
                            final users newUser = new users(name,email,uid,password,Phno);

                            rootRef.child("users").child(uid).setValue(newUser);
                            rootRef.child("registered").push().setValue(Phno);

                            progressDialog.dismiss();
                            Toast.makeText(RegBeforeMainActivity.this,"Successfully Registered", Toast.LENGTH_SHORT).show();
                            finish();
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            user.sendEmailVerification()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(RegBeforeMainActivity.this, "Verification email sent to your account", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        }
                        else{
                            progressDialog.dismiss();
                            Toast.makeText(RegBeforeMainActivity.this,"Registration Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onClick(View view){
        if(view == buttonLinkLogin || view == textLoginLink){
            //write intent to start login activity
            finish();
            startActivity(new Intent(this, LogBeforeMainActivity.class));
        }
    }
}
