package com.example.komp.gurles;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlokAdapterClass extends RecyclerView.Adapter<BlokAdapterClass.ViewHolder>{
    public Context context;
    public List<BlokAdapter> gelenlist;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;


    public BlokAdapterClass(List<BlokAdapter>gelenlist){
        this.gelenlist=gelenlist;
    }
    @NonNull
    @Override
    public BlokAdapterClass.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View mview= LayoutInflater.from(parent.getContext()).inflate(R.layout.blok_yeke_haly,parent,false);
        context=parent.getContext();
        mAuth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();

        return new BlokAdapterClass.ViewHolder(mview);
    }

    @Override
    public void onBindViewHolder(@NonNull final BlokAdapterClass.ViewHolder holder, final int position) {
final String ady=gelenlist.get(position).getAdy();
final String user_id=gelenlist.get(position).getUser_id();
final String profil=gelenlist.get(position).getProfil_surat();
final String licnyid=gelenlist.get(position).getId();
final String nomer=gelenlist.get(position).getNumber();
holder.adyyap(ady);
holder.suratyap(profil);

holder.cykar.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        Map<String,Object>userMap=new HashMap<>();
        userMap.put("ady",ady);
        userMap.put("id",licnyid);
        userMap.put("user_id",user_id);
        userMap.put("profil_surat",profil);
        userMap.put("number",nomer);
        firebaseFirestore.collection("ulanyjylar").document(mAuth.getCurrentUser().getUid()).collection("dostlar").document(user_id).set(userMap);
firebaseFirestore.collection("ulanyjylar").document(mAuth.getCurrentUser().getUid()).collection("blok").document(user_id).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
    @Override
    public void onComplete(@NonNull Task<Void> task) {
        Toast.makeText(context,ady+" blokdan acyldy",Toast.LENGTH_LONG).show();
        notifyItemRemoved(position);
    }
});
    }
});





    }




    @Override
    public int getItemCount() {
        return gelenlist.size();
    }

    public class ViewHolder extends  RecyclerView.ViewHolder{

        private TextView Ady;
        private CircleImageView imageView;
        private Button cykar;
        private View mView;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            Ady=(TextView)mView.findViewById(R.id.blok_yeke_haly_ady);
            imageView=(CircleImageView)mView.findViewById(R.id.blok_yeke_haly_profil);
            cykar=(Button)mView.findViewById(R.id.blok_yeke_cykar);

        }

        public void adyyap(String ady) {
        Ady.setText(ady);
        }
        public void suratyap(String surat){
            RequestOptions requestOptions=new RequestOptions();
            requestOptions.placeholder(R.drawable.profile_placeholder);
            Glide.with(context).applyDefaultRequestOptions(requestOptions).load(surat).into(imageView);

        }
    }

}

