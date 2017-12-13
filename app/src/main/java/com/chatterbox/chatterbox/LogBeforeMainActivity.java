package com.chatterbox.chatterbox;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LogBeforeMainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button buttonLogin;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonsignup;
    TextView textView;

    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();

        if(firebaseAuth.getCurrentUser() != null){
            //Start main activity
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }

        setContentView(R.layout.activity_log_before_main);

        progressDialog = new ProgressDialog(this);
        buttonLogin = (Button) findViewById(R.id.btn_login);
        editTextEmail = (EditText) findViewById(R.id.input_email);
        editTextPassword = (EditText) findViewById(R.id.input_password);
        buttonsignup = (Button) findViewById(R.id.btn_signup);
        textView = (TextView) findViewById(R.id.link_register);

        buttonLogin.setOnClickListener(this);
        buttonsignup.setOnClickListener(this);
        textView.setOnClickListener(this);

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.password_reset_dialog);
        final TextView select = (TextView) dialog.findViewById(R.id.select);
        final EditText editText = (EditText) dialog.findViewById(R.id.mail);
        TextView forgot = (TextView)findViewById(R.id.forgot);
        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
                select.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String string = editText.getText().toString().trim();
                        dialog.dismiss();
                        FirebaseAuth auth = FirebaseAuth.getInstance();
                        auth.sendPasswordResetEmail(string).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(LogBeforeMainActivity.this, "Email sent", Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    Toast.makeText(LogBeforeMainActivity.this, "Currently unable to process request", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
            }
        });

    }

    private void userLogin(){
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();


        if(TextUtils.isEmpty(email)){
            Toast.makeText(LogBeforeMainActivity.this, "Please enter Email", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(LogBeforeMainActivity.this, "Please enter Password", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Logging In...");
        progressDialog.show();

        try {
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressDialog.dismiss();
                            if (task.isSuccessful()) {
                                //start main activity
                                finish();
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            } else {
                                Toast.makeText(LogBeforeMainActivity.this, "Failed to login", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
        }catch (Exception e){
            Toast.makeText(this, "Unknown error. Contact Dev", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View view){
        if(view == buttonsignup || view == textView){
            //intent to start register activity
            finish();
            startActivity(new Intent(this, RegBeforeMainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY));
        }
        if(view == buttonLogin){
            userLogin();
        }
    }
}