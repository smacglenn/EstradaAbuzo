package com.example.androiddrinkzlyboozeclient.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androiddrinkzlyboozeclient.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatPictureHolder extends RecyclerView.ViewHolder {
    private Unbinder unbinder;
    @BindView(R.id.txt_time1)
    public TextView txt_time;
    @BindView(R.id.txt_email1)
    public TextView txt_email;
    @BindView(R.id.txt_chat_message1)
    public TextView txt_chat_message;
    @BindView(R.id.profile_image1)
    public CircleImageView profile_image;
    @BindView(R.id.image_preview)
    public ImageView img_preview;

    public ChatPictureHolder(@NonNull View itemView) {
        super(itemView);
        unbinder = ButterKnife.bind(this,itemView);
    }
}
