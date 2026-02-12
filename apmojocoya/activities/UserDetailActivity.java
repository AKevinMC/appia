package com.example.apmojocoya.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class UserDetailActivity extends AppCompatActivity {

    private LinearLayout layoutFields;
    private Button btnSave;

    private FirebaseFirestore db;
    private String userId;
    private Map<String, EditText> userEditTextMap;
    private Map<String, Map<String, EditText>> medidorEditTextMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView scrollView = new ScrollView(this);
        layoutFields = new LinearLayout(this);
        layoutFields.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(layoutFields);

        btnSave = new Button(this);
        btnSave.setText("Guardar");
        layoutFields.addView(btnSave);

        setContentView(scrollView);

        db = FirebaseFirestore.getInstance();

        userId = getIntent().getStringExtra("userId");
        userEditTextMap = new HashMap<>();
        medidorEditTextMap = new HashMap<>();

        loadUserData(userId);

        btnSave.setOnClickListener(v -> saveUserData());
    }

    private void loadUserData(String userId) {
        DocumentReference userDocRef = db.collection("users").document(userId);
        userDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                for (String key : documentSnapshot.getData().keySet()) {
                    String value = documentSnapshot.getString(key);
                    createUserEditText(key, value);
                }
                loadUserMedidores(userId);
            } else {
                Toast.makeText(UserDetailActivity.this, "User not found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(UserDetailActivity.this, "Error loading user data", Toast.LENGTH_SHORT).show());
    }

    private void loadUserMedidores(String userId) {
        db.collection("users").document(userId).collection("medidores").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot medidores = task.getResult();
                        for (DocumentSnapshot medidorDoc : medidores) {
                            String medidorName = medidorDoc.getId();
                            createMedidorTitle(medidorName);
                            Map<String, EditText> medidorFieldsMap = new HashMap<>();
                            for (String key : medidorDoc.getData().keySet()) {
                                String value = medidorDoc.getString(key);
                                EditText editText = createMedidorEditText(key, value);
                                medidorFieldsMap.put(key, editText);
                            }
                            medidorEditTextMap.put(medidorName, medidorFieldsMap);
                        }
                    } else {
                        Toast.makeText(UserDetailActivity.this, "Error loading medidores data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createMedidorTitle(String medidorName) {
        TextView textView = new TextView(this);
        textView.setText(medidorName);
        textView.setTextSize(18);
        layoutFields.addView(textView);
    }

    private EditText createUserEditText(String key, String value) {
        EditText editText = new EditText(this);
        editText.setHint(key);
        editText.setText(value);
        layoutFields.addView(editText);
        userEditTextMap.put(key, editText);
        return editText;
    }

    private EditText createMedidorEditText(String key, String value) {
        EditText editText = new EditText(this);
        editText.setHint(key);
        editText.setText(value);
        layoutFields.addView(editText);
        return editText;
    }

    private void saveUserData() {
        Map<String, Object> user = new HashMap<>();
        for (Map.Entry<String, EditText> entry : userEditTextMap.entrySet()) {
            user.put(entry.getKey(), entry.getValue().getText().toString());
        }

        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    saveUserMedidoresData();
                    Toast.makeText(UserDetailActivity.this, "User data saved", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(UserDetailActivity.this, "Error saving user data", Toast.LENGTH_SHORT).show());
    }

    private void saveUserMedidoresData() {
        for (Map.Entry<String, Map<String, EditText>> medidorEntry : medidorEditTextMap.entrySet()) {
            String medidorName = medidorEntry.getKey();
            Map<String, EditText> medidorFieldsMap = medidorEntry.getValue();
            Map<String, Object> medidorData = new HashMap<>();
            for (Map.Entry<String, EditText> fieldEntry : medidorFieldsMap.entrySet()) {
                medidorData.put(fieldEntry.getKey(), fieldEntry.getValue().getText().toString());
            }
            db.collection("users").document(userId).collection("medidores").document(medidorName)
                    .set(medidorData)
                    .addOnSuccessListener(aVoid -> Toast.makeText(UserDetailActivity.this, medidorName + " data saved", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(UserDetailActivity.this, "Error saving " + medidorName + " data", Toast.LENGTH_SHORT).show());
        }
    }
}
