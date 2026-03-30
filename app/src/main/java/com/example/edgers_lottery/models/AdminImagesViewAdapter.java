package com.example.edgers_lottery.models;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.edgers_lottery.R;
import com.example.edgers_lottery.views.AdminImageFullscreenActivity;
import com.google.firebase.storage.StorageReference;

import java.util.List;

/*
 * Adapter for AdminImagesViewActivity, adds functionality for viewing and deleting images
 */
public class AdminImagesViewAdapter extends RecyclerView.Adapter<AdminImagesViewAdapter.ViewHolder> {

    private List<String> imageUrls;
    private List<StorageReference> imageRefs;
    private Context context;

    public AdminImagesViewAdapter(Context context, List<String> imageUrls, List<StorageReference> imageRefs) {
        this.context = context;
        this.imageUrls = imageUrls;
        this.imageRefs = imageRefs;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String url = imageUrls.get(position);

        Glide.with(context)
                .load(url)
                .centerCrop()
                .into(holder.imageView);

        // click image to enlarge
        holder.imageView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AdminImageFullscreenActivity.class);
            intent.putExtra("image_url", url);
            context.startActivity(intent);
        });

        // long click image to delete
        holder.imageView.setOnLongClickListener(v -> {
            StorageReference ref = imageRefs.get(position);
            new AlertDialog.Builder(context)
                    .setTitle("Delete Image")
                    .setMessage("Are you sure you want to delete this image?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        ref.delete().addOnSuccessListener(aVoid -> {
                            imageUrls.remove(position);
                            imageRefs.remove(position);
                            notifyItemRemoved(position);
                            Toast.makeText(context, "Image deleted", Toast.LENGTH_SHORT).show();
                        }).addOnFailureListener(e -> {
                            Toast.makeText(context, "Failed to delete image", Toast.LENGTH_SHORT).show();
                        });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        });

    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}