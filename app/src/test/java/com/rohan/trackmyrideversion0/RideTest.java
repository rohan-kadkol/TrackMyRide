package com.rohan.trackmyrideversion0;

import android.content.IntentFilter;

import androidx.test.platform.app.InstrumentationRegistry;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RideTest {
    @Test
    public void isEmailUnique() {
        String email = "testing123@testing123.com";
        String password = "password";
        FirebaseApp.initializeApp(InstrumentationRegistry.getInstrumentation().getTargetContext());
        Task<AuthResult> task = FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password);
        task.addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                setupTemporaryPath();
                checkIfRiderCodeIsUnique();
            }
        });
    }

    private void setupTemporaryPath() {
        final String path = "rides/" + "testing" + "/temporary";
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path);
        HashMap<String, Integer> hashMap = new HashMap<>();
        hashMap.put("temporary", -1);
        ref.setValue(hashMap);
    }

    private void checkIfRiderCodeIsUnique() {
        final String path = "rides/" + "testing";
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path);
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                assertTrue(dataSnapshot.exists());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        ref.addListenerForSingleValueEvent(postListener);
    }
}
