package com.example.komp.gurles;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.renderscript.Sampler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SmsAdapterClass extends RecyclerView.Adapter<SmsAdapterClass.ViewHolder>{
    public static final int MSG_TYPE_LEFT=0;
    public static final int MSG_TYPE_RIGHT=1;
    public Context context;
    public List<SmsAdapter> gelenlist;
    public FirebaseFirestore firebaseFirestore;
    public String user_id;
    public String surat;
    public FirebaseAuth mAuth;
    private  List<String> group;
    private  String adyy,profile_picture,informasiya,san,komment_san;
    private Date wagto;

    private long millisecond;

    public MediaPlayer mediaPlayer;
    private List<String>paylassurat;
    public SmsAdapterClass(List<SmsAdapter>gelenlist){
        this.gelenlist=gelenlist;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        firebaseFirestore=FirebaseFirestore.getInstance();
        mAuth= FirebaseAuth.getInstance();
        context=parent.getContext();
        user_id=mAuth.getCurrentUser().getUid();
        mediaPlayer=new MediaPlayer();
        if(viewType==MSG_TYPE_RIGHT){
            View mview= LayoutInflater.from(parent.getContext()).inflate(R.layout.sms_item_right,parent,false);
            return new ViewHolder(mview);
        }
        else{
            View mview= LayoutInflater.from(parent.getContext()).inflate(R.layout.sms_item_left,parent,false);
            return new ViewHolder(mview);
        }




    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
      final String postid=gelenlist.get(position).getBlogpost();
        final String sms_tekst=gelenlist.get(position).getMessage();
        final String smsid=gelenlist.get(position).BlogPostId;
        final String id=gelenlist.get(position).getFrom();
        final String tipi=gelenlist.get(position).getType();
       // final long millisecond = gelenlist.get(position).getTime().getTime();
       // holder.tazewagt(millisecond);

        final Boolean okalma=gelenlist.get(position).getSeen();


//
//



        if(okalma.equals(true)){
            holder.okalma("okaldy");

        }
        if(okalma.equals(false)){
            holder.okalma("ugradyldy");
        }

        if(tipi.equals("surat")){

            holder.smssurat(sms_tekst);
            holder.sms_teksto.setVisibility(View.INVISIBLE);
            holder.sms_surat.setVisibility(View.VISIBLE);
            holder.card.setVisibility(View.GONE);
            holder.audio_linear.setVisibility(View.GONE);


        }
        if(tipi.equals("text")) {
            holder.sms_surat.setVisibility(View.GONE);
            holder.sms_teksto.setVisibility(View.VISIBLE);
            holder.tekstyap(sms_tekst);
            holder.card.setVisibility(View.GONE);
            holder.audio_linear.setVisibility(View.GONE);
        }
        if(tipi.equals("paylas")){
            holder.sms_surat.setVisibility(View.GONE);
            holder.sms_teksto.setVisibility(View.GONE);
            holder.card.setVisibility(View.VISIBLE);
            holder.paylassurat(sms_tekst,postid);
            holder.audio_linear.setVisibility(View.GONE);

        }
        if(tipi.equals("audio")){

            holder.sms_surat.setVisibility(View.GONE);
            holder.sms_teksto.setVisibility(View.GONE);
            holder.card.setVisibility(View.GONE);
            holder.audio_linear.setVisibility(View.VISIBLE);
       //   holder.audio_oynat(sms_tekst);


        }


        holder.audio_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.audio_oynat(sms_tekst);
                if(!mediaPlayer.isPlaying()){
                    mediaPlayer.start();
                    holder.audio_play.setImageResource(R.mipmap.pause);
                }
                else{
                    mediaPlayer.pause();
                    holder.audio_play.setImageResource(R.mipmap.play);
                }

            }
        });


        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                firebaseFirestore.collection("ulanyjylar").document(sms_tekst).collection("postlar").document(gelenlist.get(position).getBlogpost())
                        .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(final DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                        List<String>array= (List<String>) documentSnapshot.get("suraturl");
                        informasiya=documentSnapshot.getString("informasiya");

                        wagto=documentSnapshot.getDate("wagt");
                        millisecond=wagto.getTime();
                        firebaseFirestore.collection("ulanyjylar").document(sms_tekst).collection("postlar").document(gelenlist.get(position).getBlogpost()).collection("Like").addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(QuerySnapshot sansnapshot, FirebaseFirestoreException e) {
                                san=String.valueOf(sansnapshot.size());
                            }
                        });
                        firebaseFirestore.collection("ulanyjylar").document(sms_tekst).collection("postlar").document(gelenlist.get(position).getBlogpost()).collection("Komment").addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(QuerySnapshot kommentsnapshots, FirebaseFirestoreException e) {
                                komment_san="Teswirleri "+"gör("+String.valueOf(kommentsnapshots.size())+")";
                            }
                        });


                        firebaseFirestore.collection("ulanyjylar").document(sms_tekst).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful()){
                                    DocumentSnapshot document=task.getResult();
                                    adyy=document.getString("ady");
                                    profile_picture=document.getString("surat");
                                    Intent intent=new Intent(context,Comment.class);
                                    intent.putExtra("post_id",postid);
                                    intent.putExtra("idi",sms_tekst);
                                    intent.putExtra("ady",adyy);
                                    intent.putExtra("profil",profile_picture);
                                    intent.putExtra("post_surat", (Serializable) group);
                                     intent.putExtra("taryh",millisecond);
                                    intent.putExtra("info",informasiya);
                                    intent.putExtra("like_sany", san +" adam halady");
                                    intent.putExtra("komment_sany",komment_san);
                                    context.startActivity(intent);
                                }

                            }
                        });
                    }
                });


            }
        });
holder.sms_surat.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {

         paylassurat=new ArrayList<>();
         paylassurat.add(sms_tekst);
         Intent intent=new Intent(context, com.example.komp.gurles.surat.class);
         intent.putExtra("surat",(Serializable)paylassurat);
         context.startActivity(intent);
    }
});

        holder.sms_teksto.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                PopupMenu popupMenu = new PopupMenu(context,holder.sms_teksto);
                    popupMenu.inflate(R.menu.sms_menu_edit);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            if (item.getItemId() == R.id.sms_menu_edit_kopyala) {

                                ClipboardManager clipboardManager=(ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData data=ClipData.newPlainText("copy",holder.sms_teksto.getText().toString());
                                if(clipboardManager!=null){
                                    clipboardManager.setPrimaryClip(data);
                                }

                            }
                            if(item.getItemId()==R.id.sms_menu_edit_poz){
                                final Map<String,Object> map=new HashMap<>();
                                map.put("message","Sms pozuldy");
                                map.put("type","text");
                                firebaseFirestore.collection("ulanyjylar").document(user_id).collection("hatlar").document(id).collection("hat").document(smsid).delete();
                                firebaseFirestore.collection("ulanyjylar").document(id).collection("hatlar").document(user_id).collection("hat")
                                        .whereEqualTo("message",holder.sms_teksto.getText().toString()).addSnapshotListener(new EventListener<QuerySnapshot>() {
                                    @Override
                                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                                        if(!documentSnapshots.isEmpty()){


                                            String postid=documentSnapshots.getDocuments().get(position).getId();
                                            firebaseFirestore.collection("ulanyjylar").document(id).collection("hatlar").document(user_id).collection("hat")
                                                    .document(postid).update(map);

                                        }
                                    }
                                });


                            }
                            return false;
                        }
                    });
                    popupMenu.show();

                return false;
            }

        });

        firebaseFirestore.collection("ulanyjylar").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {



                    surat = task.getResult().getString("surat");
                }



                holder.profilsurat(surat);


            }
        });

        holder.sms_profil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context,Dost_yeke_haly.class);
                intent.putExtra("idi",id);
                context.startActivity(intent);
            }
        });
        //  holder.postid(smsid);

    }


    @Override
    public int getItemCount() {
        return gelenlist.size();
    }
    public class ViewHolder extends  RecyclerView.ViewHolder{


        private TextView sms_teksto,sms_wagt,sms_paylas_tekst,sms_gorulme,audio_wagt;
        private CircleImageView sms_profil;
        private ImageView sms_surat,sms_paylas_surat,audio_play,audio_pause;
        private CardView card;
        private View mView;
        private LinearLayout audio_linear;





        public ViewHolder(View itemView) {
            super(itemView);
            mView=itemView;
            sms_gorulme=mView.findViewById(R.id.sms_item_gorulme);
            sms_teksto=mView.findViewById(R.id.sms_item_tekst);
            sms_surat=mView.findViewById(R.id.sms_item_surat);
            sms_wagt=mView.findViewById(R.id.sms_item_wagt);
            sms_profil=mView.findViewById(R.id.sms_item_profil);
            card=mView.findViewById(R.id.sms_item_card);
            sms_paylas_tekst=mView.findViewById(R.id.sms_item_paylas_tekst);
            sms_paylas_surat=mView.findViewById(R.id.sms_item_paylas_surat);
            audio_linear=mView.findViewById(R.id.sms_item_audio_linear);
            audio_play=mView.findViewById(R.id.sms_item_audio_play);
            audio_pause=mView.findViewById(R.id.sms_item_audio_pause);
            audio_wagt=mView.findViewById(R.id.sms_item_audio_wagt);

        }

        private void audio_oynat(String sms_tekst) {
            mediaPlayer=new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);


            try {
                mediaPlayer.setDataSource(sms_tekst);
                mediaPlayer.prepareAsync();

                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.start();

                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

            audio_wagt.setText(String.valueOf(mediaPlayer.getDuration()));

        }

        public void  paylassurat(String message,String paylasanid){

           // Toast.makeText(context,message+" "+paylasanid,Toast.LENGTH_LONG).show();
            //RequestOptions requestOptions=new RequestOptions();
            // requestOptions.placeholder(R.drawable.image_placeholder);
          //  Glide.with(context).load(Text).into(sms_paylas_surat);
            firebaseFirestore.collection("ulanyjylar").document(message).collection("postlar").document(paylasanid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        if(task.getResult().exists()) {
                            DocumentSnapshot document = task.getResult();
                            String ady = document.getString("informasiya");
                            group = new ArrayList<>();
                            group = (List<String>) document.get("surat_url");


                            sms_paylas_tekst.setText(ady);
                            Glide.with(context).load(group.get(0)).into(sms_paylas_surat);
                        }
                    else {
                        card.setVisibility(View.GONE);
                        sms_teksto.setText("Post pozuldy");
                        sms_teksto.setVisibility(View.VISIBLE);


                        }}
                }
            });



        }
        public void  tekstyap(String Text){
            sms_teksto=mView.findViewById(R.id.sms_item_tekst);
            sms_teksto.setText(Text);

        }
        public void profilsurat(String profil_surat) {
            sms_profil=mView.findViewById(R.id.sms_item_profil);
            Glide.with(context).load(profil_surat).into(sms_profil);
        }
        public void smssurat(String sms) {
            sms_surat=mView.findViewById(R.id.sms_item_surat);
            RequestOptions requestOptions=new RequestOptions();
            requestOptions.placeholder(R.drawable.image_placeholder);
            Glide.with(context).applyDefaultRequestOptions(requestOptions).load(sms).into(sms_surat);
        }
        public void tazewagt(long millisecond) {

            GetTimeAgo getTimeAgo= new GetTimeAgo();

            String lastSeenTime = getTimeAgo.getTimeAgo(millisecond, context);
            sms_wagt.setText(lastSeenTime);
        }
        public void postid(String id){
            sms_wagt.setText(id);
        }
        public void setwagt(String Date){
            sms_wagt.setText(Date);
        }


        public void okalma(String tekst) {
        sms_gorulme.setText(tekst);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(gelenlist.get(position).getFrom().equals(user_id)){
            return MSG_TYPE_RIGHT;
        }
        else{
            return MSG_TYPE_LEFT;
        }

    }

}


