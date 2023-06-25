package com.example.mysmartplugdemo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.tuya.smart.android.user.api.IRegisterCallback;
import com.tuya.smart.android.user.bean.User;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.IResultCallback;

public class RegistrationActivity extends AppCompatActivity {

    private EditText etRegEmail, etRegPassword, etRegCountryCode, etVerificationCode;
    private Button btnRegister, btnVerificationCode;

    private static final String TAG = "MySmartPlug";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        initViews();

        etVerificationCode.setVisibility(View.INVISIBLE);
        btnRegister.setVisibility(View.INVISIBLE);

        btnVerificationCode.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String registeredEmail = etRegEmail.getText().toString();
                String registeredCountryCode = etRegCountryCode.getText().toString();
                getValidationCode(registeredCountryCode, registeredEmail);

            }
        });
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String registeredEmail = etRegEmail.getText().toString();
                String registeredCountryCode = etRegCountryCode.getText().toString();
                String registeredPassword = etRegPassword.getText().toString();
                String inputVerificationCode = etVerificationCode.getText().toString();

                TuyaHomeSdk.getUserInstance().registerAccountWithEmail(registeredCountryCode, registeredEmail, registeredPassword, inputVerificationCode, registerCallback);
            }
        });
    }

    IRegisterCallback registerCallback = new IRegisterCallback() {
        @Override
        public void onSuccess(User user) {
            Log.d(TAG, "Registration Successful ");
            Toast.makeText(RegistrationActivity.this, "Registration Successful.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(RegistrationActivity.this, MainActivity.class));
        }

        @Override
        public void onError(String code, String error) {
            Log.d(TAG, "Registration Failed with Error. " + error);
            Toast.makeText(RegistrationActivity.this, "Registration failed.", Toast.LENGTH_LONG).show();
        }
    };

    IResultCallback validateCallback = new IResultCallback() {
        @Override
        public void onError(String code, String error) {
            Log.d(TAG, "Verification code failed with error: " + error);
            Toast.makeText(RegistrationActivity.this, "Failed to send verification code.", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onSuccess() {
            Toast.makeText(RegistrationActivity.this, "Successfully sent verification code.", Toast.LENGTH_LONG).show();
            etVerificationCode.setVisibility(View.VISIBLE);
            btnRegister.setVisibility(View.VISIBLE);
        }
    };

    private void getValidationCode(String countryCode, String email){
        //TuyaHomeSdk.getUserInstance().getRegisterEmailValidateCode(countryCode, email, validateCallback); //Comando obsoleto pero no quiere deci que no funcione
        TuyaHomeSdk.getUserInstance().sendVerifyCodeWithUserName(email,"",countryCode,1,validateCallback);
        //Tipo 1 es registro de cuenta con email; tipo 2 es login en la app con un email; tipo 3 es resetear la contraseña de una cuenta registrada con el email

    }

    private void initViews(){
        etRegEmail = findViewById(R.id.etRegEmail);
        etRegPassword = findViewById(R.id.etRegPassword);
        etRegCountryCode = findViewById(R.id.etRegCountryCode);
        etVerificationCode = findViewById(R.id.etVerificationCode);
        btnRegister = findViewById(R.id.btnUserRegister);
        btnVerificationCode = findViewById(R.id.btnValidate);
    }


}