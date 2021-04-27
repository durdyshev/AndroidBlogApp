package com.example.komp.gurles;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AlertDialogLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class Dost_gelen_adapterclass extends RecyclerView.Adapter<Dost_gelen_adapterclass.ViewHolder>{
    public Context context;
    public List<Dost_gelen_adapter>gelenlist;
    public FirebaseFirestore firebaseFirestore;
    public String user_id;
    public FirebaseAuth mAuth;

    public Dost_gelen_adapterclass(List<Dost_gelen_adapter>gelenlist){
        this.gelenlist=gelenlist;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View mview= LayoutInflater.from(parent.getContext()).inflate(R.layout.dost_gelen_yeke_haly,parent,false);
        context=parent.getContext();
        firebaseFirestore=FirebaseFirestore.getInstance();
        mAuth= FirebaseAuth.getInstance();
        user_id=mAuth.getCurrentUser().getUid();
        return new ViewHolder(mview);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final String ady_data=gelenlist.get(position).getAdy();
        holder.tekstyap(ady_data);
        final String suraturl=gelenlist.get(position).getProfil_surat();
        holder.suratindir(suraturl);
        final String licnyid=gelenlist.get(position).getUser_id();
        final String id=gelenlist.get(position).getId();
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent=new Intent(context,Dost_yeke_haly.class);
                intent.putExtra("idi",id);
                context.startActivity(intent);
            }
        });
        holder.tik.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String,String> userMap=new HashMap<>();
                userMap.put("ady",ady_data);
                userMap.put("id",licnyid);
                userMap.put("profil_surat",suraturl);
                userMap.put("user_id",id);

                firebaseFirestore.collection("/ulanyjylar/"+user_id+"/dostlar").document(id).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(context,"Gosuldy",Toast.LENGTH_LONG).show();
                            firebaseFirestore.collection("ulanyjylar").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if(task.isSuccessful()){
                                        if (task.getResult().exists()) {
                                            String ad=task.getResult().getString("ady");
                                            String ozid=task.getResult().getString("id");
                                            String surat=task.getResult().getString("surat");
                                            String user_id=task.getResult().getString("user_id");
                                            Map<String,String> map=new HashMap<>();
                                            map.put("ady",ad);
                                            map.put("id",ozid);
                                            map.put("profil_surat",surat);
                                            map.put("user_id",user_id);

                                            firebaseFirestore.collection("/ulanyjylar/"+id+"/dostlar").document(user_id).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                }
                                            });

                                        }
                                    }
                                }

                            });

                            firebaseFirestore.collection("/ulanyjylar/"+user_id+"/dost_iberen").document(id).delete();
                            firebaseFirestore.collection("/ulanyjylar/"+id+"/dost_ugradylan").document(user_id).delete();

                        }

                    }
                });



            }
        });
        holder.close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseFirestore.collection("/ulanyjylar/"+user_id+"/dost_iberen").document(id).delete();
                firebaseFirestore.collection("/ulanyjylar/"+id+"/dost_ugradylan").document(user_id).delete();

            }
        });

    }





    @Override
    public int getItemCount() {
        return gelenlist.size();
    }

    public class ViewHolder extends  RecyclerView.ViewHolder{
        private TextView AdyView;
        private View mView;
        private ImageView imageView;
        private ImageButton tik;
        private CircleImageView close;
        private CardView cardView;





        public ViewHolder(View itemView) {
            super(itemView);
            mView=itemView;

            tik=mView.findViewById(R.id.dostlar_gelen_yeke_tick);
            cardView=mView.findViewById(R.id.dostlar_gelen_yeke_haly_card);
            close=mView.findViewById(R.id.dostlar_gelen_close);
        }
        public void  tekstyap(String Text){
            AdyView=mView.findViewById(R.id.dostlar_gelen_yeke_ady);
            AdyView.setText(Text);

        }
        public void suratindir(String suraturl){
            imageView= mView.findViewById(R.id.dostlar_gelen_yeke_profil);
            Glide.with(context).load(suraturl).into(imageView);
        }

    }

}

