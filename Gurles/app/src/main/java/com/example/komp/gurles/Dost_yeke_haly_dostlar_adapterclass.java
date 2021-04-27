package com.example.komp.gurles;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class Dost_yeke_haly_dostlar_adapterclass extends RecyclerView.Adapter<Dost_yeke_haly_dostlar_adapterclass.ViewHolder>{
    public Context context;
    public List<Dost_yeke_haly_dostlar_adapter> dostlar;
    public Dialog dialog;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;

    public Dost_yeke_haly_dostlar_adapterclass(List<Dost_yeke_haly_dostlar_adapter>dostlar){
        this.dostlar=dostlar;
    }
    @NonNull
    @Override
    public Dost_yeke_haly_dostlar_adapterclass.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.dost_yeke_haly_cardview,parent,false);
        context=parent.getContext();
        dialog=new Dialog(context);
        firebaseFirestore=FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();
        return new Dost_yeke_haly_dostlar_adapterclass.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final Dost_yeke_haly_dostlar_adapterclass.ViewHolder holder, final int position) {

        final String id=dostlar.get(position).getUser_id();


firebaseFirestore.collection("ulanyjylar").document(id).addSnapshotListener(new EventListener<DocumentSnapshot>() {
    @Override
    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {


       String ady=documentSnapshot.getString("ady");
       String profil=documentSnapshot.getString("surat");

       holder.tekstyap(ady,profil);

    }
});



holder.profil.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        Intent intent=new Intent(context,Dost_yeke_haly.class);
        intent.putExtra("idi",id);
        context.startActivity(intent);
    }
});





    }

    @Override
    public int getItemCount() {
        return dostlar.size();
    }

    public class ViewHolder extends  RecyclerView.ViewHolder{
        private TextView AdyView;
        private View mView;
        private CircleImageView profil;




        public ViewHolder(View itemView) {
            super(itemView);
            mView=itemView;

            AdyView=mView.findViewById(R.id.dost_yeke_haly_cardview_ady);
            profil= mView.findViewById(R.id.dost_yeke_haly_cardview_profil);

        }
        public void  tekstyap(String Text,String suraturl){

            AdyView.setText(Text);

            Glide.with(context.getApplicationContext()).load(suraturl).into(profil);

        }

    }

}
