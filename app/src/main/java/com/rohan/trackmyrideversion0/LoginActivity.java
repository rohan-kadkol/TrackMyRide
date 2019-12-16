package com.rohan.trackmyrideversion0;

import androidx.annotation.NonNull;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();

    @BindView(R.id.et_email)
    EditText etEmail;
    @BindView(R.id.et_password)
    EditText etPassword;
    @BindView(R.id.btn_login)
    Button btnLogin;
    @BindView(R.id.pb_loading)
    ProgressBar pbLoading;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

        btnLogin.setOnClickListener(view -> {
            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();
            if (TextUtils.isEmpty(email) && TextUtils.isEmpty(password)) {
                Toast.makeText(LoginActivity.this, getString(R.string.error_enter_email_password), Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(email)) {
                Toast.makeText(LoginActivity.this, getString(R.string.error_enter_email), Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(password)) {
                Toast.makeText(LoginActivity.this, getString(R.string.error_enter_password), Toast.LENGTH_SHORT).show();
            }
            pbLoading.setVisibility(View.VISIBLE);
            loginToFirebase(email, password);
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

    private void registerUser(String email, String password) {
        Task<AuthResult> task = FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password);
        task.addOnSuccessListener(authResult -> {
            pbLoading.setVisibility(View.GONE);
            Toast.makeText(LoginActivity.this, getString(R.string.registered), Toast.LENGTH_SHORT).show();
            goToHomeScreen();
        });
        task.addOnFailureListener(e -> {
            pbLoading.setVisibility(View.GONE);
            if (e instanceof FirebaseAuthWeakPasswordException) {
                Toast.makeText(LoginActivity.this, getString(R.string.error_enter_stronger_password), Toast.LENGTH_SHORT).show();
            } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                Toast.makeText(LoginActivity.this, getString(R.string.error_enter_valid_email), Toast.LENGTH_SHORT).show();
            } else if (e instanceof FirebaseAuthUserCollisionException) {
                Toast.makeText(LoginActivity.this, getString(R.string.error_user_already_exists), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(LoginActivity.this, getString(R.string.error_registration), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToHomeScreen() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        finish();
        startActivity(intent);
    }
}
