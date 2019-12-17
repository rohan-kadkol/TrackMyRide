package com.rohan.trackmyrideversion0;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

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

    private void loginToFirebase(final String email, final String password) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(
                email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                pbLoading.setVisibility(View.GONE);
                Log.d(TAG, "onComplete: Firebase auth success!");
                Toast.makeText(LoginActivity.this, getString(R.string.signed_in), Toast.LENGTH_SHORT).show();
                goToHomeScreen();
            } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                pbLoading.setVisibility(View.GONE);
                Log.d(TAG, "onComplete: Firebase auth failed-Invalid credentials");
                Toast.makeText(this, getString(R.string.error_incorrect_password), Toast.LENGTH_SHORT).show();
            } else if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                Log.d(TAG, "onComplete: Firebase auth failed-User does not exist, registering user");
                registerUser(email, password);
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
