package com.example.komp.gurles;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class SaklananAdapterClass extends RecyclerView.Adapter<SaklananAdapterClass.ViewHolder>{
private Context context;
private List<SaklananAdapter> postlar;

private FirebaseFirestore firebaseFirestore;
private FirebaseAuth mAuth;

public SaklananAdapterClass(List<SaklananAdapter>postlar){
        this.postlar=postlar;
        }
@NonNull
@Override
public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.sakla_cardview,parent,false);
        context=parent.getContext();

        firebaseFirestore=FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();
        return new ViewHolder(view);
        }

@Override
public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
    final List<String> suraturl = postlar.get(position).getSurat_url();
    for(int i=0;i<1;i++){

        holder.suratyap(suraturl.get(i));
    }


        }

@Override
public int getItemCount() {
        return postlar.size();
        }

public class ViewHolder extends  RecyclerView.ViewHolder{
    private TextView AdyView;
    private View mView;
    private ImageView imageView;





    public ViewHolder(View itemView) {
        super(itemView);
        mView=itemView;
  imageView=(ImageView)mView.findViewById(R.id.sakla_surat);
    }

    public void suratyap(String s) {
        Glide.with(context).load(s).into(imageView);

    }
}

}
