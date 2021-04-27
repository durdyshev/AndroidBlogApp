package com.example.komp.gurles;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class dost_yeke_haly_hemmesi_adapterclass2 extends RecyclerView.Adapter<dost_yeke_haly_hemmesi_adapterclass2.ViewHolder>{
    public Context context;
    private List<Obshydostlar_adapter> dostlar;
    public Dialog dialog;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    private String user_id;

    public dost_yeke_haly_hemmesi_adapterclass2(List<Obshydostlar_adapter>dostlar){
        this.dostlar=dostlar;
    }
    @NonNull
    @Override
    public dost_yeke_haly_hemmesi_adapterclass2.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.dostlar_hemmesi_fragment,parent,false);
        context=parent.getContext();
        dialog=new Dialog(context);
        firebaseFirestore=FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();
        user_id=mAuth.getUid();
        return new dost_yeke_haly_hemmesi_adapterclass2.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final dost_yeke_haly_hemmesi_adapterclass2.ViewHolder holder, final int position) {


        final String id=dostlar.get(position).getUser_id();

       // final String dost_size= String.valueOf(dostlar.size());

        firebaseFirestore.collection("ulanyjylar").document(id).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
           String profil=documentSnapshot.getString("surat");
           String ady=documentSnapshot.getString("ady");
           holder.tekstyap(ady,profil,id);
            }
        });




holder.cardView.setOnClickListener(new View.OnClickListener() {
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
      private CircleImageView profil;
      private TextView ady;
      private Button gos,ayyr;
      private CardView cardView;
        private View mView;





        public ViewHolder(View itemView) {
            super(itemView);
            mView=itemView;
            profil=mView.findViewById(R.id.dostlar_hemmesi_fragment_profil);
            ady=mView.findViewById(R.id.dostlar_hemmesi_fragment_ady);
            gos=mView.findViewById(R.id.dostlar_hemmesi_fragment_gos);
            ayyr=mView.findViewById(R.id.dostlar_hemmesi_fragment_cykar);
            cardView=mView.findViewById(R.id.dostlar_hemmesi_fragment_cardview);
        }
        public void tekstyap(String adyy, String profilo, final String ido)
        {

            firebaseFirestore.collection("ulanyjylar").document(user_id).collection("dostlar")
                    .document(ido).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                    if(documentSnapshot.exists()){
                        ayyr.setVisibility(View.VISIBLE);
                    }
                    else if (ido.equals(user_id)) {
                        gos.setVisibility(View.INVISIBLE);
                        ayyr.setVisibility(View.INVISIBLE);
                    }
                    else{
                        gos.setVisibility(View.VISIBLE);
                    }

                }
            });
            ady.setText(adyy);
            Glide.with(context).load(profilo).into(profil);
        }
    }

}
