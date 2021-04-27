package com.example.komp.gurles;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Chatadapterclass extends RecyclerView.Adapter<Chatadapterclass.ViewHolder>{
    public Context context;
    public List<Chatadapter> gelenlist;
    private FirebaseFirestore firebaseFirestore;
    private String adyy,profil_surat,user_id;
    private String name,profile_picture;
    private FirebaseAuth mAuth;
    private int bas=0;



    public Chatadapterclass(List<Chatadapter>gelenlist){
        this.gelenlist=gelenlist;
    }
    @NonNull
    @Override
    public Chatadapterclass.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View mview= LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_yeke_haly,parent,false);
        context=parent.getContext();
        firebaseFirestore=FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();
        user_id=mAuth.getCurrentUser().getUid();

        return new Chatadapterclass.ViewHolder(mview);
    }

    @Override
    public void onBindViewHolder(@NonNull final Chatadapterclass.ViewHolder holder, int position) {
        final String id=gelenlist.get(position).getFrom();
        long millisecond = gelenlist.get(position).getTime().getTime();
        String dateString = android.text.format.DateFormat.format("dd/MM hh:mm", new Date(millisecond)).toString();
        holder.setwagt(dateString,user_id,id);



   //    holder.sanamak(user_id,id);




        firebaseFirestore.collection("ulanyjylar").document(user_id).collection("dostlar").whereEqualTo("user_id",id).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (!task.getResult().isEmpty()){
                    for (DocumentSnapshot document : task.getResult()) {

                         adyy = document.getString("ady");

                        holder.atgetir(adyy);

                        firebaseFirestore.collection("ulanyjylar").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {


                                    profil_surat = task.getResult().getString("surat");
                                    holder.profilsurat(profil_surat);
                                }







                            }
                        });

                    }
                }
                else {
                    firebaseFirestore.collection("/ulanyjylar/").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {

                                adyy = task.getResult().getString("ady");
                                profil_surat = task.getResult().getString("surat");
                            }


                            holder.atgetir(adyy);
                            holder.profilsurat(profil_surat);



                        }
                    });

                }
               // name = adyy;
                //profile_picture=profil_surat;
            }
        });

        Query sirala = firebaseFirestore.collection("/ulanyjylar/"+user_id+"/hatlar/"+id+"/hat").orderBy("time", Query.Direction.DESCENDING).limit(1);
        sirala.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                for(DocumentSnapshot ds:documentSnapshots){
                    String sms=ds.getString("message");
                    String tip=ds.getString("type");
                    String post="post paylasyldy";
                    String surat="surat paylasyldy";
                    String audio="ses paýlaşyldy";

                    if(tip.equals("paylas")){
                        holder.message(post);
                    }
                    if(tip.equals("surat")){
                        holder.message(surat);
                    }
                    if(tip.equals("text")) {
                        holder.message(sms);
                    }
                    if(tip.equals("audio")){
                        holder.message(audio);
                    }
                }
            }
        });



        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bas=0;
                holder.sanaw.setVisibility(View.INVISIBLE);
                Intent intent=new Intent(context,Sms_ugrat.class);
                intent.putExtra("id",id);
                intent.putExtra("ady",holder.ady.getText().toString());

                context.startActivity(intent);

            }
        });

        holder.card.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                PopupMenu popupMenu = new PopupMenu(context,holder.card);
                popupMenu.inflate(R.menu.chatadaptermenu);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        if(item.getItemId()==R.id.chat_menu_poz){
                            firebaseFirestore.collection("ulanyjylar").document(id).collection("hatlar").document(user_id).delete();
                            firebaseFirestore.collection("ulanyjylar").document(user_id).collection("chat").document(id).delete();
                            firebaseFirestore.collection("ulanyjylar").document(id).collection("chat").document(user_id).delete();

                            firebaseFirestore.collection("ulanyjylar").document(user_id).collection("hatlar").document(id).delete();



                        }
                        return false;
                    }
                });
                popupMenu.show();

                return false;
            }

        });
    }





    @Override
    public int getItemCount() {
        return gelenlist.size();
    }

    public class ViewHolder extends  RecyclerView.ViewHolder{

        private View mView;
        private CircleImageView profil;
        private TextView wagt,ady,message,sanaw;
        private CardView card;
        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            profil=mView.findViewById(R.id.chat_yeke_haly_profil);
            ady=mView.findViewById(R.id.chat_yeke_haly_ady);
            wagt=mView.findViewById(R.id.chat_yeke_haly_son);
            message=mView.findViewById(R.id.chat_yeke_haly_message);
            card=mView.findViewById(R.id.chat_yeke_haly_card);
            sanaw=mView.findViewById(R.id.chat_yeke_mesaj_sany);
        }

        public void setwagt(String dateString, final String user_id, final String id) {
            wagt.setText(dateString);
            firebaseFirestore.collection("/ulanyjylar/"+id+"/hatlar/"+user_id+"/hat").addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                   bas=0;
                    for(DocumentSnapshot ds:documentSnapshots){
                        String kim=ds.getString("from");
                        Boolean san=ds.getBoolean("seen");
                        if(san.equals(false) && !kim.equals(user_id)){
                            bas+=1;
                        }

                    }
                    if(String.valueOf(bas).equals("0") || String.valueOf(bas).equals(null)){
                        sanaw.setVisibility(View.GONE);
                    }

                    else{
                        sanaw.setVisibility(View.VISIBLE);
                        sanaw.setText(String.valueOf(bas));
                    }

                }
            });

            firebaseFirestore.collection("/ulanyjylar/"+user_id+"/hatlar/"+id+"/hat").addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
               if (documentSnapshots.isEmpty()){
                   firebaseFirestore.collection("/ulanyjylar/").document(user_id).collection("chat").document(id).delete();
               }
                }
            });

        }

        public void atgetir(String adyy) {
            ady.setText(adyy);
        }

        public void profilsurat(String profil_surat) {

            Glide.with(context).load(profil_surat).into(profil);
        }
        public void message(String sms){
            message.setText(sms);
        }



    }

}

