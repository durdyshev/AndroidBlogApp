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

public class LikeAdapterClass extends RecyclerView.Adapter<LikeAdapterClass.ViewHolder>{
public Context context;
public List<LikeAdapter> like;
public List<String> idler;
public Dialog dialog;
private FirebaseFirestore firebaseFirestore;


public LikeAdapterClass(List<LikeAdapter>like){
        this.like=like;

        }
@NonNull
@Override
public LikeAdapterClass.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.like_item_list,parent,false);
        context=parent.getContext();
        dialog=new Dialog(context);
       firebaseFirestore=FirebaseFirestore.getInstance();
        return new LikeAdapterClass.ViewHolder(view);
        }

@Override
public void onBindViewHolder(@NonNull final LikeAdapterClass.ViewHolder holder, final int position) {
    final long millisecond = like.get(position).getWagt().getTime();
    holder.tazewagt(millisecond);


    final String id=like.get(position).BlogPostId;

    firebaseFirestore.collection("ulanyjylar").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
        @Override
        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
          String  name = task.getResult().getString("ady");
          String  picture= task.getResult().getString("surat");
            holder.profile(name,picture);
        }

    });






        }

@Override
public int getItemCount() {
        return like.size();
        }

public class ViewHolder extends  RecyclerView.ViewHolder{
    private TextView ady,time;
    private View mView;
    private CircleImageView imageView;




    public ViewHolder(View itemView) {
        super(itemView);
        mView=itemView;
      ady=(TextView)mView.findViewById(R.id.like_ulanyjy_ady);
      time=(TextView)mView.findViewById(R.id.like_ulanyjy_wagt);
      imageView=(CircleImageView)mView.findViewById(R.id.like_ulanyjy_profil);

    }
    public void tazewagt(long millisecond) {

        GetTimeAgo getTimeAgo = new GetTimeAgo();

        String lastSeenTime = getTimeAgo.getTimeAgo(millisecond, context);
        time.setText(lastSeenTime);
    }
    public void profile(String adyy,String surat){
        Glide.with(context).load(surat).into(imageView);
       ady.setText(adyy);

    }


}

}
