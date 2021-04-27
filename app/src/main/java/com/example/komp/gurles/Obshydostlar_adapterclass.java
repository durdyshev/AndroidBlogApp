package com.example.komp.gurles;

import android.annotation.SuppressLint;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.komp.gurles.R.*;

public class Obshydostlar_adapterclass extends RecyclerView.Adapter<Obshydostlar_adapterclass.ViewHolder>{
    public Context context;
    public List<Obshydostlar_adapter>dostlar;
    public Dialog dialog;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;

    public Obshydostlar_adapterclass(List<Obshydostlar_adapter>dostlar){
        this.dostlar=dostlar;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(layout.obshy_dostlar_yeke_haly,parent,false);
        context=parent.getContext();
        dialog=new Dialog(context);
        firebaseFirestore=FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        final String ady_data=dostlar.get(position).getAdy();
        holder.tekstyap(ady_data);
       // final String suraturl=dostlar.get(position).getProfil_surat();
        //holder.suratindir(suraturl);
        final String id=dostlar.get(position).getUser_id();
       // holder.useridgetir(id);
       // final String licnyid=dostlar.get(position).getId();
        final String dost_size= String.valueOf(dostlar.size());
     //   final String nomer=dostlar.get(position).getNumber();



firebaseFirestore.collection("ulanyjylar").document(id).addSnapshotListener(new EventListener<DocumentSnapshot>() {
    @Override
    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {


        String pikir= documentSnapshot.getString("pikir");
        String profil= documentSnapshot.getString("surat");
      //  holder.tekstyap(ady);
        holder.suratindir(profil);
        holder.useridgetir(pikir);

    }
});




       holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              final  CircleImageView profil;final ImageView arkafon;
                final TextView ady,ido,sany; Button glawny,yaz,blok;
                dialog.setContentView(layout.layout);
                arkafon=(ImageView) dialog.findViewById(R.id.dost_onizleme_arkafon);
                profil=(CircleImageView) dialog.findViewById(R.id.dost_onizleme_profil);
                ady=(TextView)dialog.findViewById(R.id.dost_onizleme_ady);
                ido=(TextView)dialog.findViewById(R.id.dost_onizleme_id);
                sany=(TextView)dialog.findViewById(R.id.dost_onizleme_dostlar);

                firebaseFirestore.collection("ulanyjylar").document(id).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                        String dost_id= documentSnapshot.getString("id");
                        String dost_pikir= documentSnapshot.getString("pikir");
                        String dost_profil= documentSnapshot.getString("surat");
                        String arka= documentSnapshot.getString("arkafon");
                        ady.setText(ady_data);
                        ido.setText(dost_id);
                        sany.setText(dost_size);
                        RequestOptions requestOptions=new RequestOptions();
                        requestOptions.centerInside();
     Glide.with(context).load(dost_profil).apply(requestOptions).into(profil);

     if(arka.equals("default")){
         Glide.with(context).load(drawable.background).apply(requestOptions).into(arkafon);

     }
     else {
         Glide.with(context).load(arka).apply(requestOptions).into(arkafon);
     }

                    }
                });




                dialog.show();
                glawny=dialog.findViewById(R.id.dost_onizleme_esasy_knopka);
                yaz=dialog.findViewById(R.id.dost_onizleme_yaz);
                blok=dialog.findViewById(R.id.dost_onizleme_blok);

                blok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Map<String,String> userMap=new HashMap<>();

                        userMap.put("user_id",id);

                        firebaseFirestore.collection("ulanyjylar").document(mAuth.getCurrentUser().getUid()).collection("blok").document(id).set(userMap);
                        firebaseFirestore.collection("ulanyjylar").document(mAuth.getCurrentUser().getUid()).collection("dostlar").document(id).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(context,"Bloklandy",Toast.LENGTH_LONG).show();
                                notifyItemRemoved(position);
                                notifyItemRemoved(dostlar.size()-1 );
                                dialog.dismiss();
                            }
                        });

                    }
                });
                glawny.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        Intent intent=new Intent(context,Dost_yeke_haly.class);
                        intent.putExtra("idi",id);
                        context.startActivity(intent);
                    }
                });
                yaz.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        Intent intent=new Intent(context,Sms_ugrat.class);
                        intent.putExtra("id",id);
                        intent.putExtra("ady",ady_data);
                       // intent.putExtra("surat",suraturl);
                        context.startActivity(intent);
                    }
                });

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
        private ImageView imageView;
        private TextView user_id;
        private CardView card;




        public ViewHolder(View itemView) {
            super(itemView);
            mView=itemView;
            card=mView.findViewById(id.obshy_dostlar_yeke_haly_card);
            AdyView=mView.findViewById(id.obshy_dostlar_yeke_haly_ady);
            imageView= mView.findViewById(id.obshy_dostlar_yeke_haly_profil);
            user_id=mView.findViewById(id.obshy_dostlar_yeke_haly_id);
        }
        public void  tekstyap(String Text){

            AdyView.setText(Text);

        }
        public void suratindir(String suraturl){

            Glide.with(context).load(suraturl).into(imageView);
        }
        public void useridgetir(String text){

            user_id.setText(text);
        }
    }

}
