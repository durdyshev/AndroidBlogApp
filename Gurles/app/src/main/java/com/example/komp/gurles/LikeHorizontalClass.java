package com.example.komp.gurles;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class LikeHorizontalClass extends RecyclerView.Adapter<LikeHorizontalClass.ViewHolder>{
    public Context context;
    public List<LikeAdapter> like;
    public List<String> idler;
    public Dialog dialog;
    private FirebaseFirestore firebaseFirestore;


    public LikeHorizontalClass(List<LikeAdapter>like){
        this.like=like;

    }
    @NonNull
    @Override
    public LikeHorizontalClass.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_like_yeke_haly,parent,false);
        context=parent.getContext();
        dialog=new Dialog(context);
        firebaseFirestore=FirebaseFirestore.getInstance();
        return new LikeHorizontalClass.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final LikeHorizontalClass.ViewHolder holder, final int position) {


final long millisecond=like.get(position).getWagt().getTime();
        final String id=like.get(position).BlogPostId;

        firebaseFirestore.collection("ulanyjylar").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                String  picture= task.getResult().getString("surat");
                holder.profile(picture);
            }

        });






    }

    @Override
    public int getItemCount() {
        return like.size();
    }

    public class ViewHolder extends  RecyclerView.ViewHolder{

        private View mView;
        private CircleImageView imageView;




        public ViewHolder(View itemView) {
            super(itemView);
            mView=itemView;

            imageView=(CircleImageView)mView.findViewById(R.id.comment_like_yeke_haly_profil);

        }

        public void profile(String surat){
            Glide.with(context).load(surat).into(imageView);


        }


    }

}
