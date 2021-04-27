package com.example.komp.gurles;

import android.content.Context;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapterClass extends RecyclerView.Adapter<CommentAdapterClass.ViewHolder>{
    public Context context;
    public List<CommentAdapter> gelenlist;
    private FirebaseFirestore firebaseFirestore;
    private String ady,profil;
    private String adyy,profil_surat;


    public CommentAdapterClass(List<CommentAdapter>gelenlist){
        this.gelenlist=gelenlist;
    }
    @NonNull
    @Override
    public CommentAdapterClass.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View mview= LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item_list,parent,false);
        context=parent.getContext();
        firebaseFirestore=FirebaseFirestore.getInstance();

        return new CommentAdapterClass.ViewHolder(mview);
    }

    @Override
    public void onBindViewHolder(@NonNull final CommentAdapterClass.ViewHolder holder, int position) {
        final String sms=gelenlist.get(position).getSms();
        final long millisecond = gelenlist.get(position).getWagt().getTime();
        holder.tazewagt(millisecond);

        holder.commentgetir(sms);
        final String id=gelenlist.get(position).getUser_id();
        firebaseFirestore.collection("/ulanyjylar/").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    adyy = task.getResult().getString("ady");
                    profil_surat = task.getResult().getString("surat");
                }
                holder.ady(adyy);
                holder.suratindir(profil_surat);
            }
        });


    }





    @Override
    public int getItemCount() {
        return gelenlist.size();
    }

    public class ViewHolder extends  RecyclerView.ViewHolder{
        private TextView kom;
        private TextView Ady,BlogTime;
        private CircleImageView imageView;
        private View mView;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

        }
        public void commentgetir(String komment) {
            kom=mView.findViewById(R.id.comment_item_list_komment);
            kom.setText(komment);
        }

        public void ady(String ady) {
            Ady=mView.findViewById(R.id.comment_item_list_ulanyjy);
            Ady.setText(ady);

        }


        public void suratindir(String suraturl) {
            imageView= mView.findViewById(R.id.comment_item_list_surat);
            RequestOptions requestOptions=new RequestOptions();
            requestOptions.placeholder(R.drawable.profile_placeholder);
            Glide.with(context.getApplicationContext()).applyDefaultRequestOptions(requestOptions).load(suraturl).into(imageView);

        }
        public void tazewagt(long millisecond) {
            BlogTime=mView.findViewById(R.id.comment_item_list_wagt);
            GetTimeAgo getTimeAgo = new GetTimeAgo();

            String lastSeenTime = getTimeAgo.getTimeAgo(millisecond, context);
            BlogTime.setText(lastSeenTime);
        }
    }

}

