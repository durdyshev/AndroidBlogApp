package com.example.komp.gurles;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Post_adapter_class extends RecyclerView.Adapter<Post_adapter_class.ViewHolder> {
    public Context context;
    public List<Postadapter>blog_list;
    public List<String>list;
    public FirebaseFirestore firebaseFirestore;
    public String user_id,profil_surat,adyy;
    public FirebaseAuth mauth;
    private String san,komment_san;
   private FirebaseFirestoreSettings settings;
    Post_adapter_class(List<Postadapter>blog_list){
        this.blog_list=blog_list;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_yeke_haly4,parent,false);
        context=parent.getContext();
        firebaseFirestore=FirebaseFirestore.getInstance();
        settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        firebaseFirestore.setFirestoreSettings(settings);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        mauth = FirebaseAuth.getInstance();
        user_id=mauth.getCurrentUser().getUid();


        final String blogpostid = blog_list.get(position).BlogPostId;
        final String info_data = blog_list.get(position).getInformasiya();
        final String tipi=blog_list.get(position).getTipi();
        holder.setinfotext(info_data);
        final List<String> suraturl = blog_list.get(position).getSurat_url();
        String kiceldilen = blog_list.get(position).getKici_surat();

        final String id = blog_list.get(position).getUser_id();
        final long millisecond = blog_list.get(position).getWagt().getTime();
     holder.tazewagt(millisecond);
       final String dateString = android.text.format.DateFormat.format("dd/MM/yyyy hh:mm", new Date(millisecond)).toString();
      //  holder.setwagt(dateString);
holder.likebtn.setImageResource(R.mipmap.heart);

if(tipi.equals("post")){
    holder.ugrat(suraturl);
    holder.profil_calsylan.setVisibility(View.INVISIBLE);
    holder.viewPager.setVisibility(View.VISIBLE);
    holder.arkafon.setVisibility(View.INVISIBLE);
    holder.linearLayout.setVisibility(View.VISIBLE);
    holder.surat_sanaw.setVisibility(View.VISIBLE);
    holder.videoview_relative.setVisibility(View.INVISIBLE);
}
else if(tipi.equals("video")){

    holder.video_oynat(suraturl.get(0));
    holder.profil_calsylan.setVisibility(View.INVISIBLE);
    holder.viewPager.setVisibility(View.INVISIBLE);
    holder.arkafon.setVisibility(View.INVISIBLE);
    holder.linearLayout.setVisibility(View.INVISIBLE);
    holder.surat_sanaw.setVisibility(View.INVISIBLE);
    holder.videoview_relative.setVisibility(View.VISIBLE);

}
else if(tipi.equals("profil")){
    firebaseFirestore.collection("ulanyjylar").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
        @Override
        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
            if (task.isSuccessful()) {


                String arkafon= task.getResult().getString("arkafon");
                holder.calsylan_profil_degistir_arkafon(arkafon);
            }





        }
    });
    String foto =suraturl.get(0);


    holder.profil_calsylan.setVisibility(View.VISIBLE);
    holder.viewPager.setVisibility(View.INVISIBLE);
    holder.arkafon.setVisibility(View.VISIBLE);
    holder.linearLayout.setVisibility(View.INVISIBLE);
    holder.surat_sanaw.setVisibility(View.INVISIBLE);
    holder.videoview_relative.setVisibility(View.INVISIBLE);
    holder.calsylan_profil_degistir(foto);

}





        firebaseFirestore.collection("ulanyjylar").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {

                    adyy = task.getResult().getString("ady");
                    profil_surat = task.getResult().getString("surat");

                }


                holder.atgetir(adyy,tipi);
                holder.profilsurat(profil_surat);


            }
        });
        firebaseFirestore.collection("/ulanyjylar/" + id + "/postlar/" + blogpostid + "/Like").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                san = String.valueOf(documentSnapshots.size());
                holder.updatelikescount(san);
            }

        });
        firebaseFirestore.collection("/ulanyjylar/" + id + "/postlar/" + blogpostid + "/Komment").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                komment_san = String.valueOf(documentSnapshots.size());
                holder.updatecommentscount(komment_san);
            }

        });

if(isOnline()){
    firebaseFirestore.collection("/ulanyjylar/" + id + "/postlar/" + blogpostid + "/Like").document(mauth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
        @Override
        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.getResult().exists()) {

                    holder.likebtn.setImageResource(R.mipmap.heart_gyzyl);
                }
                else{
                    holder.likebtn.setImageResource(R.mipmap.heart);
                }
        }
    });
}



holder.videoplayknopka.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        if(holder.isPlaying){
            holder.videoView.pause();
            holder.isPlaying=false;
            holder.videoplayknopka.setImageResource(R.mipmap.play);
        }
        else{
            holder.videoView.start();
            holder.isPlaying=true;
            holder.videoplayknopka.setImageResource(R.mipmap.pause);
        }
    }
});

        holder.komment_ugrat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(TextUtils.isEmpty(holder.komment_tekst.getText())){
                    Toast.makeText(context,"Yaz bir zatlar mat tvoyu",Toast.LENGTH_LONG).show();
                }
                if(!isOnline()){
                    Toast.makeText(context,"Internetinizi açyň",Toast.LENGTH_LONG).show();


                }

                else{
                    final String randomname=FieldValue.serverTimestamp().toString();
                    Map<String, Object> commentmap = new HashMap<>();
                    commentmap.put("sms", holder.komment_tekst.getText().toString());
                    commentmap.put("userid", id);
                    commentmap.put("wagt", FieldValue.serverTimestamp());
                    firebaseFirestore.collection("/ulanyjylar/" + id + "/postlar").document(blogpostid).collection("Komment").document(user_id+randomname).set(commentmap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(!task.isSuccessful()){
                                Toast.makeText(context,"Problema cykdy:"+ task.getException().getMessage(),Toast.LENGTH_LONG).show();
                            }
                            else{
                                holder.komment_tekst.setText("");
                            }
                        }
                    });
                }
            }
        });
        holder.popupmenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PopupMenu popupMenu = new PopupMenu(context, holder.popupmenu);
               if(isOnline()){
                   if (id.equals(mauth.getCurrentUser().getUid())) {
                       popupMenu.inflate(R.menu.popupoz);
                       popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                           @Override
                           public boolean onMenuItemClick(MenuItem item) {
                               if (item.getItemId() == R.id.menu_poz) {
                                   firebaseFirestore.collection("/ulanyjylar/" + mauth.getCurrentUser().getUid() + "/postlar").document(blogpostid).delete();
                                   notifyItemRemoved(position);


                               }
                               return false;
                           }
                       });
                       popupMenu.show();
                   } else {
                       popupMenu.inflate(R.menu.popupuser);
                       popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                           @Override
                           public boolean onMenuItemClick(MenuItem item) {
                               if(item.getItemId()==R.id.popupuser_sakla){
                                   Map<String,Object> postMap=new HashMap<>();
                                   postMap.put("surat_url",suraturl);
                                   postMap.put("informasiya",info_data);
                                   postMap.put("user_id",id);
                                   postMap.put("wagt", dateString);

                                   firebaseFirestore.collection("/ulanyjylar/" + mauth.getCurrentUser().getUid() + "/saklanan").document(blogpostid).set(postMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                       @Override
                                       public void onComplete(@NonNull Task<Void> task) {
                                           Toast.makeText(context,"Post saklandy",Toast.LENGTH_LONG).show();
                                       }
                                   });

                               }
                               return false;
                           }
                       });
                       popupMenu.show();


                   }
               }
               else{
                   Toast.makeText(context,"Internetiniz acyn",Toast.LENGTH_LONG).show();
               }




            }
        });

        holder.komment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(context,Comment.class);
                intent.putExtra("post_id",blogpostid);
                intent.putExtra("idi",id);
                intent.putExtra("ady",adyy);
                intent.putExtra("profil",profil_surat);
                intent.putExtra("post_surat", (Serializable) suraturl);
                intent.putExtra("taryh",millisecond);
                intent.putExtra("info",info_data);
                intent.putExtra("like_sany",san);
                intent.putExtra("komment_sany",komment_san);
                context.startActivity(intent);
            }
        });
        holder.komment_sany.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(context,Comment.class);
                intent.putExtra("post_id",blogpostid);
                intent.putExtra("idi",id);
                intent.putExtra("ady",adyy);
                intent.putExtra("profil",profil_surat);
                intent.putExtra("post_surat", (Serializable) suraturl);
                intent.putExtra("taryh",millisecond);
                intent.putExtra("info",info_data);
                intent.putExtra("like_sany",holder.like_sany.getText());
                intent.putExtra("komment_sany",holder.komment_sany.getText());

                context.startActivity(intent);
            }
        });


        holder.likebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {// holder.like_sany.setVisibility(View.VISIBLE);
                if(isOnline()){
                firebaseFirestore.collection("/ulanyjylar/" + id + "/postlar/" + blogpostid + "/Like").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (!task.getResult().exists()) {
                            Map<String, Object> likemap = new HashMap<>();
                            likemap.put("wagt", FieldValue.serverTimestamp());
                            firebaseFirestore.collection("/ulanyjylar/" + id + "/postlar").document(blogpostid).collection("Like").document(user_id).set(likemap);
                            holder.likebtn.setImageResource(R.mipmap.heart_gyzyl);
                        } else {
                            firebaseFirestore.collection("/ulanyjylar/" + id + "/postlar").document(blogpostid).collection("Like").document(user_id).delete();
                            holder.likebtn.setImageResource(R.mipmap.heart);

                        }
                    }
                });}
                else {
                    Toast.makeText(context,"Internetiniz yapyk",Toast.LENGTH_LONG).show();
                }

            }
        });


      holder.sms_paylas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(context,Sms_paylas.class);
                intent.putExtra("surat",  suraturl.get(0));
                intent.putExtra("blogpost", blogpostid);
                intent.putExtra("id",id);
                context.startActivity(intent);
            }
        });
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context,Comment.class);
                intent.putExtra("post_id",blogpostid);
                intent.putExtra("idi",id);
                intent.putExtra("ady",adyy);
                intent.putExtra("profil",profil_surat);
                intent.putExtra("post_surat", (Serializable) suraturl);
                intent.putExtra("taryh",millisecond);
                intent.putExtra("info",info_data);
                intent.putExtra("like_sany",holder.like_sany.getText());
                intent.putExtra("komment_sany",holder.komment_sany.getText());
                context.startActivity(intent);
            }
        });
        holder.like_sany.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context,Like.class);
                intent.putExtra("post_id",blogpostid);
                intent.putExtra("idi",id);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return blog_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private View mView;
        private TextView info,ady,like_sany,komment_sany,BlogTime,surat_sanaw;
        private ImageView imageView,arkafon;

        private CircleImageView profil,likebtn,komment,profil_calsylan,sms_paylas;

        private ImageButton popupmenu,komment_ugrat;
        private EditText komment_tekst;
       private CardView cardView;
        private ViewpagerAdapter adapter;
        private ViewPager viewPager;
        private int dotscount=0;
        private ImageView[] dots;
        private LinearLayout linearLayout;
        private RelativeLayout videoview_relative;

        // video play
        private CircleImageView videoplayknopka;
        private VideoView videoView;
        private TextView shuwagt,sonwagt;
        private ProgressBar videoprogress,videoskacat;
        private boolean isPlaying=false;
        private int current=0;
        private int duration=0;


        public ViewHolder(View itemView) {
            super(itemView);
            mView=itemView;
            videoview_relative=
                    (RelativeLayout)mView.findViewById(R.id.recycle_yeke_haly_video_relative);
            surat_sanaw=(TextView)mView.findViewById(R.id.recycle_yeke_haly_surat_sanawy);
            linearLayout=(LinearLayout)mView.findViewById(R.id.recycle_yeke_haly_linear);
            likebtn=(CircleImageView)mView.findViewById(R.id.recycle_yeke_haly_like);
            popupmenu=(ImageButton) mView.findViewById(R.id.popup);
            komment=(CircleImageView)mView.findViewById(R.id.recycle_yeke_haly_comment);
            komment_sany=(TextView)mView.findViewById(R.id.recycle_yeke_haly_komment_san);
            komment_tekst=(EditText)mView.findViewById(R.id.recycle_yeke_haly_komment_edit);
            komment_ugrat=(ImageButton)mView.findViewById(R.id.recycle_yeke_haly_komment_ugrat);
            sms_paylas=(CircleImageView)mView.findViewById(R.id.recycle_yeke_haly_paylas);
            cardView=(CardView)mView.findViewById(R.id.main_blog_post);
            viewPager=(ViewPager)mView.findViewById(R.id.recycle_yeke_haly_viewpager);
            like_sany=(TextView)mView.findViewById(R.id.recycle_yeke_haly_like_san);
        profil_calsylan=(CircleImageView)mView.findViewById(R.id.recycle_yeke_haly_profil_surat);
       arkafon=(ImageView)mView.findViewById(R.id.recycle_yeke_haly_arkafon);
       videoView=(VideoView)mView.findViewById(R.id.recycle_yeke_haly_videoview);
       shuwagt=(TextView)mView.findViewById(R.id.recycle_yeke_haly_video_shuwagt);
       sonwagt=(TextView)mView.findViewById(R.id.recycle_yeke_haly_video_obshywagt);
       videoprogress=(ProgressBar)mView.findViewById(R.id.recycle_yeke_haly_video_progress);
        videoskacat=(ProgressBar)mView.findViewById(R.id.recycle_yeke_haly_video_skacatprogress);
        videoplayknopka=(CircleImageView)mView.findViewById(R.id.recycle_yeke_haly_video_play);
        }








        public  void video_oynat(String video){
            videoprogress.setMax(100);
            Uri uri=Uri.parse(video);
            videoView.setVideoURI(uri);
            videoView.requestFocus();
            videoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mp, int what, int extra) {
                    if(what == mp.MEDIA_INFO_BUFFERING_START){
                        videoskacat.setVisibility(View.VISIBLE);
                    }
                    else if(what==mp.MEDIA_INFO_BUFFERING_END){
                        videoskacat.setVisibility(View.INVISIBLE);
                    }
                    return false;
                }
            });

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                duration=mp.getDuration()/1000;
                String durationstring=String.format("%02d:%02d", duration/60 ,duration % 60);
                sonwagt.setText(durationstring);
            }
        });
            videoView.start();
            isPlaying=true;
            videoplayknopka.setImageResource(R.mipmap.pause);
         //   new Videoprogress().execute();
        }

        @SuppressLint("StaticFieldLeak")
        public class Videoprogress extends AsyncTask<Void,Integer,Void> {

            @SuppressLint("WrongThread")
            @Override
            protected Void doInBackground(Void... voids) {
                do{
                    current=videoView.getCurrentPosition()/1000;
                    publishProgress(current);



                }
                while (videoprogress.getProgress()<=100);
                return null;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);

                videoprogress.setProgress(values[0]);
                try{
                    int currentpercent= values[0] *100/duration;
                     videoprogress.setProgress(currentpercent);

                     String currentstring=String.format("%02d:%02d", values[0]/60 ,values[0]%60);
                        shuwagt.setText(currentstring);


                }catch (Exception e){

                }

            }
        }




        public  void calsylan_profil_degistir(String foto){
            Glide.with(context).load(foto).into(profil_calsylan);
        }
        public  void calsylan_profil_degistir_arkafon(String fon){
            Glide.with(context).load(fon).into(arkafon);
        }
        public void setinfotext(String tekst){
            info=mView.findViewById(R.id.recycle_yeke_haly_info);
            info.setText(tekst);
        }


        public void suratindir(String suraturl,String kici_surat) {
            imageView= mView.findViewById(R.id.recycle_yeke_haly_surat);
            RequestOptions requestOptions=new RequestOptions();
            requestOptions.placeholder(R.drawable.image_placeholder);
            Glide.with(context).applyDefaultRequestOptions(requestOptions).load(suraturl).thumbnail(Glide.with(context).load(kici_surat)).into(imageView);
        }
public void pager(){

}
        public void atgetir(String at,String type) {
            ady=mView.findViewById(R.id.recycle_yeke_haly_ulanyjy);
            if(type.equals("post")){
                ady.setText(at+"  >> post paýlaşdy.");
            }
            else if(type.equals("profil")){

                ady.setText(at+"  >> profil surat üýtgetdi.");
            }

        }

        public void profilsurat(String profil_surat) {
            profil=mView.findViewById(R.id.recycle_yeke_haly_profil);
            Glide.with(context).load(profil_surat).into(profil);

        }
        public void setwagt(String Date){
            BlogTime=mView.findViewById(R.id.recycle_yeke_haly_tarih);
            BlogTime.setText(Date);
        }

        public void updatelikescount(String i) {
            if(i.equals("0")){
                like_sany.setVisibility(View.GONE);
            }
            else {
                    like_sany.setVisibility(View.VISIBLE);
                like_sany.setText(i+" adam halady");
            }
        }

        public void updatecommentscount(String komment_san) {
            komment_sany=(TextView)mView.findViewById(R.id.recycle_yeke_haly_komment_san);
            if(komment_san.equals("0")){komment_sany.setVisibility(View.GONE);}
            else{
                komment_sany.setVisibility(View.VISIBLE);
            komment_sany.setText("teswirleri gör"+ "("+komment_san+")");
            }
        }

        public void ugrat(List<String> suraturl) {
            adapter = new ViewpagerAdapter(context, suraturl);
            viewPager.setAdapter(adapter);
            dotscount=0;
            dotscount = adapter.getCount();
            dots = new ImageView[dotscount];
            linearLayout.removeAllViews();

                for (int i = 0; i < dotscount; i++) {

                    dots[i] = new ImageView(context);
                    dots[i].setImageDrawable(ContextCompat.getDrawable(context, R.drawable.non_active_dot));

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);

                    params.setMargins(8, 0, 8, 0);

                    linearLayout.addView(dots[i], params);

                }

                dots[0].setImageDrawable(ContextCompat.getDrawable(context, R.drawable.active_dot));
                surat_sanaw.setText("1/"+dotscount);
                viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                    }

                    @Override
                    public void onPageSelected(int position) {
                       int i;
                        for( i = 0; i< dotscount; i++){
                            dots[i].setImageDrawable(ContextCompat.getDrawable(context, R.drawable.non_active_dot));
                        surat_sanaw.setText((position+1)+"/"+dotscount);
                        }

                        dots[position].setImageDrawable(ContextCompat.getDrawable(context, R.drawable.active_dot));
                        surat_sanaw.setText((position+1)+"/"+dotscount);
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {

                    }
                });





        }

        private void noktalar(ViewPager viewPager) {

        }

        public void tazewagt(long millisecond) {
            BlogTime=mView.findViewById(R.id.recycle_yeke_haly_tarih);
            GetTimeAgo getTimeAgo = new GetTimeAgo();

            String lastSeenTime = getTimeAgo.getTimeAgo(millisecond, context);
            BlogTime.setText(lastSeenTime);
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}

