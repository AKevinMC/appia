package com.example.apmojocoya;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.example.apmojocoya.LoginActivity;
import com.example.apmojocoya.MainActivity;

public class StartingScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starting_screen);

        final Handler handler = new Handler();

        handler.postDelayed(() -> {

            //Vemos si hay una cuenta registrada
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(StartingScreenActivity.this);
            if(account != null){
                Intent intent = new Intent(StartingScreenActivity.this, MainActivity.class);
                startActivity(intent);
                //overridePendingTransition(R.anim.fade_in, R.anim.fade_out); //animacion de transicion
                finish();
            }else{
                Intent intent = new Intent(StartingScreenActivity.this, LoginActivity.class);
                startActivity(intent);
                //overridePendingTransition(R.anim.fade_in, R.anim.fade_out); //animacion de transicion
                finish();
            }

        }, 2000);

    }
}