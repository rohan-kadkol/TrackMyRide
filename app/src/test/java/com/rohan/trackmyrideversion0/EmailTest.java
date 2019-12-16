package com.rohan.trackmyrideversion0;

import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EmailTest {

    @Test
    public void isEmailUnique() {
        String email = "testing123@testing123.com";
        String password = "password";
//        FirebaseApp.initializeApp(InstrumentationRegistry.getInstrumentation().getTargetContext());
        FirebaseApp.initializeApp(InstrumentationRegistry.getInstrumentation().getTargetContext());
        Task<AuthResult> task = FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password);
        task.addOnCompleteListener(task1 -> assertTrue(task1.isSuccessful()));
    }
}
