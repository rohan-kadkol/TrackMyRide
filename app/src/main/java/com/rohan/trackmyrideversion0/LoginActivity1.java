package com.rohan.trackmyrideversion0;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity1 extends AppCompatActivity {
    TextInputLayout mTilEmail;
    TextInputLayout mTilPassword;

    Button mBtnLogin;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_1);

        mTilEmail = findViewById(R.id.text_input_email);
        mTilPassword = findViewById(R.id.text_input_password);
        mBtnLogin = findViewById(R.id.btn_login);

        mBtnLogin.setOnClickListener(v -> {
            if (validateEmail() | validatePassword()) {

            }
        });
    }

    public boolean validateEmail() {
        String email = mTilEmail.getEditText().getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mTilEmail.setError(getString(R.string.error_enter_valid_email));
            return false;
        } else {
            mTilEmail.setError(null);
            return true;
        }
    }

    public boolean validatePassword() {
        String password = mTilPassword.getEditText().getText().toString().trim();

        if (TextUtils.isEmpty(password)) {
            mTilPassword.setError(getString(R.string.error_enter_password));
            return false;
        } else {
            mTilPassword.setError(null);
            return true;
        }
    }
}
