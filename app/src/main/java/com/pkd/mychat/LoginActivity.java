package com.pkd.mychat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rengwuxian.materialedittext.MaterialEditText;

public class LoginActivity extends AppCompatActivity {

    MaterialEditText username, password;
    Button login;
    TextView signup;

    FirebaseAuth auth;
    FirebaseUser firebaseUser;

    @Override
    protected void onStart() {
        super.onStart();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null){
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lgoin);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> finish());

        auth = FirebaseAuth.getInstance();

        username = findViewById(R.id.txt_log_username);
        password = findViewById(R.id.txt_log_password);
        login = findViewById(R.id.btn_login);
        signup = findViewById(R.id.signup);

        signup.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
            startActivity(intent);
        });

        login.setOnClickListener(view -> {
            String user = username.getText().toString();
            String pass = password.getText().toString();

            if (user.isEmpty() || pass.isEmpty()){
                Toast.makeText(getApplicationContext(),"All field are Required.",Toast.LENGTH_SHORT).show();

            }else{
                login.setBackgroundColor(getResources().getColor(R.color.common_google_signin_btn_text_dark_disabled));

                auth.signInWithEmailAndPassword(user,pass).addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        Intent intent = new Intent(LoginActivity.this,HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();

                    }else{
                        login.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                        Toast.makeText(getApplicationContext(),"Authentication Error.",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
