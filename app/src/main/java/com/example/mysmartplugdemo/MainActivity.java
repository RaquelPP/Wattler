package com.example.mysmartplugdemo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.tuya.smart.android.user.api.ILoginCallback;
import com.tuya.smart.android.user.bean.User;
import com.tuya.smart.home.sdk.TuyaHomeSdk;

public class MainActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etCountryCode;
    private Button btnLogin, btnRegister;
    private CheckBox btnCheckBox;

    private static final String TAG = "MySmartPlug";

    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    String key = "sesion";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        //Si ya se ha iniciado sesion con un usuario, lo reconoce e inicia sesión automáticamente
        if (checkSession()){
            startActivity(new Intent(MainActivity.this,HomeActivity.class));
        }else{
            Toast.makeText(MainActivity.this, "Please Login", Toast.LENGTH_LONG).show();
        }

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSession(btnCheckBox.isChecked());

                String countryCode = etCountryCode.getText().toString();
                String email = etEmail.getText().toString();
                String password = etPassword.getText().toString();

                TuyaHomeSdk.getUserInstance().loginWithEmail(countryCode, email, password, loginCallback);

            }
        });
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RegistrationActivity.class));
            }
        });
    }

    private ILoginCallback loginCallback = new ILoginCallback() {
        @Override
        public void onSuccess(User user) {
            Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_LONG).show();
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
        }

        @Override
        public void onError(String code, String error) {
            Log.d(TAG, "Login Failed with Error: " + error);
            Toast.makeText(MainActivity.this, "Login Failed with Error: " + error, Toast.LENGTH_LONG).show();
        }
    };

    private void initViews(){
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etCountryCode = findViewById(R.id.etCountryCode);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        btnCheckBox = findViewById(R.id.btnCheckBox);
        preferences = this.getPreferences(Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    /**
     * saveSession
     * Guardar el inicio de sesión
     */
    public void saveSession(boolean checked){
        editor.putBoolean(key, checked);
        editor.apply();//para guardar el dato
    }

    /**
     * Revisa si la sesión se va a guardar o no
     * @return
     */
    public boolean checkSession(){
        return this.preferences.getBoolean(key,false);
    }
}