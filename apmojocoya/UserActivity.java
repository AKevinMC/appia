package com.example.apmojocoya;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class UserActivity extends AppCompatActivity {

    private Button btn_add_user;
    private RecyclerView recyclerViewUsers;
    private UserAdapter userAdapter;
    private List<Usuario> userList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        db = FirebaseFirestore.getInstance();

        btn_add_user = findViewById(R.id.btnadduser);
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));

        userList = new ArrayList<>();
        userAdapter = new UserAdapter(userList, this);
        recyclerViewUsers.setAdapter(userAdapter);

        btn_add_user.setOnClickListener(v -> {
            Intent intent = new Intent(UserActivity.this, NewUserActivity.class);
            startActivity(intent);
        });

        loadUsersFromFirestore();
    }

    private void loadUsersFromFirestore() {
        db.collection("users")
                .orderBy("apellidos")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            String name = document.getString("apellidos") + " " + document.getString("nombre");
                            String id = document.getId();

                            db.collection("users").document(id).collection("medidores")
                                    .get()
                                    .addOnCompleteListener(subTask -> {
                                        if (subTask.isSuccessful()) {
                                            int medidoresCount = subTask.getResult().size();
                                            userList.add(new Usuario(id, name, medidoresCount));
                                            userAdapter.notifyDataSetChanged();
                                        } else {
                                            // Manejar el error
                                        }
                                    });
                        }
                    } else {
                        // Manejar el error
                    }
                });
    }
}
