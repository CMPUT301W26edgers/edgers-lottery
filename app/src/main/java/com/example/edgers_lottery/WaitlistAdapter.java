package com.example.edgers_lottery;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class WaitlistAdapter extends RecyclerView.Adapter<WaitlistAdapter.ViewHolder> {

    public interface OnRemoveClickListener {
        void onRemove(WaitlistUser user, int position);
    }

    private List<WaitlistUser> users;
    private OnRemoveClickListener removeListener;

    public WaitlistAdapter(List<WaitlistUser> users, OnRemoveClickListener removeListener) {
        this.users = users;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_waitlist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WaitlistUser user = users.get(position);
        holder.tvName.setText(user.getName());
        holder.btnRemove.setOnClickListener(v -> removeListener.onRemove(user, holder.getAdapterPosition()));

        // load profile image if available
        if (user.getProfileImage() != null) {
            byte[] imageBytes = android.util.Base64.decode(user.getProfileImage(), android.util.Base64.DEFAULT);
            android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            holder.ivProfile.setImageBitmap(bitmap);
        }
    }

    @Override
    public int getItemCount() { return users.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProfile;
        TextView tvName;
        ImageButton btnRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvName = itemView.findViewById(R.id.tvName);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}