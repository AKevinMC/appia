package com.example.apmojocoya;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<Usuario> userList;
    private Context context;

    public UserAdapter(List<Usuario> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        Usuario user = userList.get(position);

        // DIFERENCIAR INSTITUCIÓN
        if ("Institucion".equals(user.getTipo())) {
            holder.userName.setText(user.getName() + " (INST)");
            holder.userName.setTextColor(Color.parseColor("#3F51B5")); // Azul Indigo
        } else {
            holder.userName.setText(user.getName());
            holder.userName.setTextColor(Color.BLACK);
        }

        holder.userMedidores.setText("Medidores: " + user.getMedidoresCount());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, UserDetailActivity.class);
            intent.putExtra("userId", user.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userMedidores;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userName);
            userMedidores = itemView.findViewById(R.id.userMedidores);
        }
    }
}