package com.pkd.mychat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    MaterialEditText txtUsername, txtEmail, txtMobile, txtPassword;
    Button btnRegister;

    FirebaseAuth auth;
    DatabaseReference userReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Register");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> finish());

        txtUsername = (MaterialEditText)findViewById(R.id.txt_username);
        txtEmail = (MaterialEditText)findViewById(R.id.txt_email);
        txtMobile = (MaterialEditText)findViewById(R.id.txt_mobile);
        txtPassword = (MaterialEditText)findViewById(R.id.txt_password);
        btnRegister = (Button)findViewById(R.id.btn_register);

        auth = FirebaseAuth.getInstance();

        btnRegister.setOnClickListener(view -> {
            String username = txtUsername.getText().toString();
            String email = txtEmail.getText().toString();
            String mobile = txtMobile.getText().toString();
            String pass = txtPassword.getText().toString();

            if (username.isEmpty() || email.isEmpty() || pass.isEmpty()){
                Toast.makeText(getApplicationContext(),"All Fields are Required.",Toast.LENGTH_SHORT).show();
            }else if(pass.length()<6){
                Toast.makeText(getApplicationContext(),"Password must be at least 6 Characters",Toast.LENGTH_SHORT).show();
            }else{
                btnRegister.setBackgroundColor(getResources().getColor(R.color.common_google_signin_btn_text_dark_disabled));
                registerWithEmailAndPassword(username, email, mobile, pass);
            }
        });
    }

    public void registerWithEmailAndPassword(final String username, String email, String mobile, String pass){

        auth.createUserWithEmailAndPassword(email,pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser authCurrentUser = auth.getCurrentUser();

                        if (authCurrentUser != null) {
                            String userid = authCurrentUser.getUid();
                            userReference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("id", userid);
                            hashMap.put("username", username);
                            hashMap.put("mobile", mobile);
                            hashMap.put("imageURL", "default");
                            hashMap.put("status", "offline");
                            hashMap.put("search", username.toLowerCase());

                            userReference.setValue(hashMap).addOnCompleteListener(taskUserSaved -> {
                                if (taskUserSaved.isSuccessful()){
                                    Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                }else{
                                    Log.e("register", "USER DATA SAVING FAILED!");
                                    Toast.makeText(getApplicationContext(), "USER DATA SAVING FAILED!", Toast.LENGTH_LONG).show();
                                }
                            });

                        } else {
                            Log.e("register", "USER AUTHENTICATING FAILED!");
                            Toast.makeText(getApplicationContext(), "USER AUTHENTICATING FAILED!", Toast.LENGTH_LONG).show();
                        }
                    }
                    else {
                        btnRegister.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                        Toast.makeText(getApplicationContext(), "USER REGISTERING FAILED!", Toast.LENGTH_LONG).show();
                    }
                });
    }

}
