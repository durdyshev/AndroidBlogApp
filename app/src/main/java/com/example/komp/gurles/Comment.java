package com.example.komp.gurles;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Comment extends AppCompatActivity {

    private Toolbar mToolbar;
    private String blogpostid,user_id,gelen_id,ady,profil,info;//,like_sany,komment_sany;
    private TextView ulanyjy_ady,ulanyjy_wagt,ulanyjy_info,ulanyjy_like_sany,ulanyjy_komment_sany,surat_sanaw;
    private List<String>post_surat;
    private long wagt;
    private EditText komment_sms;
    private ImageButton komment_ugrat;
    private FirebaseAuth mAuth;
    private RecyclerView komment_recycle,komment_like;
    private FirebaseFirestore firebaseFirestore;
    private CircleImageView profil_surat,likebtn;
    private ViewPager viewPager;
    private ViewpagerAdapter adapter;
    private List<LikeAdapter> like;
    private List<LikeAdapter> like_kese;
    private LikeHorizontalClass likeHorizontalClass;

    private CommentAdapterClass commentAdapterClass;
    private List<CommentAdapter> gelenlist;
    private int dotscount=0;
    private ImageView[] dots;
    private LinearLayout linearLayout;
    private LinearLayoutManager linearLayoutManager1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        mAuth=FirebaseAuth.getInstance();
        user_id=mAuth.getCurrentUser().getUid();
        firebaseFirestore=FirebaseFirestore.getInstance();
//mToolbar=(Toolbar)findViewById(R.id.komment_toolbar);
        likebtn=(CircleImageView)findViewById(R.id.comment_like);
        surat_sanaw=(TextView)findViewById(R.id.comment_dots_sanaw);
        linearLayout=(LinearLayout)findViewById(R.id.comment_dots_linear);
        viewPager=(ViewPager)findViewById(R.id.viewpager);

        post_surat=getIntent().getStringArrayListExtra("post_surat");
        blogpostid=getIntent().getStringExtra("post_id");
        gelen_id=getIntent().getStringExtra("idi");
       ady=getIntent().getStringExtra("ady");
        profil=getIntent().getStringExtra("profil");
        wagt=getIntent().getLongExtra("taryh",0);
        info=getIntent().getStringExtra("info");
     //   like_sany=getIntent().getStringExtra("like_sany");
       // komment_sany=getIntent().getStringExtra("komment_sany");

        komment_sms=(EditText)findViewById(R.id.comment_sms);
        komment_ugrat=(ImageButton)findViewById(R.id.comment_ugrat);
profil_surat=(CircleImageView)findViewById(R.id.comment_profil);
ulanyjy_ady=(TextView)findViewById(R.id.comment_ulanyjy);
ulanyjy_wagt=(TextView)findViewById(R.id.comment_tarih);

ulanyjy_like_sany=(TextView)findViewById(R.id.comment_like_san);
ulanyjy_komment_sany=(TextView)findViewById(R.id.comment_komment_san);
ulanyjy_info=(TextView)findViewById(R.id.comment_info);
komment_like=(RecyclerView)findViewById(R.id.komment_like_recycler);
         linearLayoutManager1
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

ulanyjy_ady.setText(ady);



ulanyjy_info.setText(info);
tazewagt(wagt);
        Glide.with(Comment.this).load(profil).into(profil_surat);
    ugrat(post_surat);


  //      setSupportActionBar(mToolbar);
//getSupportActionBar().setTitle("Kommentler");
//getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        like_kese=new ArrayList<>();
        likeHorizontalClass=new LikeHorizontalClass(like_kese);
        komment_like.setLayoutManager(linearLayoutManager1);
        komment_like.setAdapter(likeHorizontalClass);
        firebaseFirestore.collection("/ulanyjylar/" + gelen_id + "/postlar/" + blogpostid + "/Like").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

              String  san = String.valueOf(documentSnapshots.size());
                if(san.equals("0")){
                    komment_like.setVisibility(View.GONE);
                    ulanyjy_like_sany.setVisibility(View.GONE);
                }
                else {
                    ulanyjy_like_sany.setText("Like:"+san);
                    komment_like.setVisibility(View.VISIBLE);
                    ulanyjy_like_sany.setVisibility(View.VISIBLE);
                }

            }

        });
        firebaseFirestore.collection("/ulanyjylar/" + gelen_id + "/postlar/" + blogpostid + "/Komment").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                String komment_san = String.valueOf(documentSnapshots.size());

                if(komment_san.equals("0"))   {
                    komment_recycle.setVisibility(View.GONE);
                    ulanyjy_komment_sany.setVisibility(View.GONE);
                }
                else{
                    ulanyjy_komment_sany.setText("Teswirler:"+komment_san);
                    komment_recycle.setVisibility(View.VISIBLE);
                    ulanyjy_komment_sany.setVisibility(View.VISIBLE);
                }

            }

        });












likebtn.setImageResource(R.mipmap.heart);
        if(isOnline()){

            firebaseFirestore.collection("/ulanyjylar/" + gelen_id + "/postlar/" + blogpostid + "/Like").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if (task.getResult().exists()) {

                        likebtn.setImageResource(R.mipmap.heart_gyzyl);
                    }
                    else{
                        likebtn.setImageResource(R.mipmap.heart);
                    }
                }
            });
        }
    likebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isOnline()){
                firebaseFirestore.collection("/ulanyjylar/" + gelen_id + "/postlar/" + blogpostid + "/Like").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (!task.getResult().exists()) {
                            Map<String, Object> likemap = new HashMap<>();
                            likemap.put("wagt", FieldValue.serverTimestamp());
                            firebaseFirestore.collection("/ulanyjylar/" + gelen_id + "/postlar").document(blogpostid).collection("Like").document(user_id).set(likemap);
                           likebtn.setImageResource(R.mipmap.heart_gyzyl);
                        } else {
                            firebaseFirestore.collection("/ulanyjylar/" + gelen_id + "/postlar").document(blogpostid).collection("Like").document(user_id).delete();
                           likebtn.setImageResource(R.mipmap.heart);

                        }
                    }
                });}
                else{
                    Toast.makeText(Comment.this,"Internetiniz yapyk",Toast.LENGTH_LONG).show();
                }

            }
        });
        Query sirala=firebaseFirestore.collection("/ulanyjylar/"+gelen_id+"/postlar/"+blogpostid+"/Like").orderBy("wagt",Query.Direction.ASCENDING).limit(7);
        sirala.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                for(DocumentChange doc: documentSnapshots.getDocumentChanges()){
                    if(doc.getType()==DocumentChange.Type.ADDED){
                        String BlogPostId = doc.getDocument().getId();
                        LikeAdapter likeAdapter=doc.getDocument().toObject(LikeAdapter.class).within(BlogPostId);
                        like_kese.add(likeAdapter);
                        likeHorizontalClass.notifyDataSetChanged();


                    }
                }
            }
        });


        komment_recycle=findViewById(R.id.komment_recycler);
        gelenlist=new ArrayList<>();
        commentAdapterClass = new CommentAdapterClass(gelenlist);

        komment_recycle.setAdapter(commentAdapterClass);
        komment_recycle.setLayoutManager(new LinearLayoutManager(this));
        firebaseFirestore.collection("/ulanyjylar/"+gelen_id+"/postlar").document(blogpostid).collection("Komment").addSnapshotListener(Comment.this,new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                    if (doc.getType() == DocumentChange.Type.ADDED) {
                        CommentAdapter commentAdapter = doc.getDocument().toObject(CommentAdapter.class);
                        gelenlist.add(commentAdapter);
                        commentAdapterClass.notifyDataSetChanged();
                    }
                }
            }
        });

        komment_ugrat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String comment_sms=komment_sms.getText().toString();
                if(!comment_sms.isEmpty()){
                    final String randomname=FieldValue.serverTimestamp().toString();
                    Map<String, Object> commentmap = new HashMap<>();
                    commentmap.put("sms", comment_sms);
                    commentmap.put("userid", user_id);
                    commentmap.put("wagt", FieldValue.serverTimestamp());
                    firebaseFirestore.collection("/ulanyjylar/" + gelen_id + "/postlar").document(blogpostid).collection("Komment").add(commentmap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if(!task.isSuccessful()){
                                Toast.makeText(Comment.this,"Problema cykdy:"+ task.getException().getMessage(),Toast.LENGTH_LONG).show();
                            }
                            else{
                                komment_sms.setText("");
                            }
                        }
                    });

                }
            }
        });
        viewPager.
                setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Comment.this,surat.class);
                intent.putExtra("surat", (Serializable) post_surat);
                startActivity(intent);
            }
        });

    }
    public void ugrat(List<String> suraturl) {
        adapter = new ViewpagerAdapter(this, suraturl);
        viewPager.setAdapter(adapter);
        dotscount=0;
        dotscount = adapter.getCount();
        dots = new ImageView[dotscount];
        linearLayout.removeAllViews();

        for (int i = 0; i < dotscount; i++) {

            dots[i] = new ImageView(this);
            dots[i].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.non_active_dot));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);

            params.setMargins(8, 0, 8, 0);

            linearLayout.addView(dots[i], params);

        }

        dots[0].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.active_dot));
        surat_sanaw.setText("1/"+dotscount);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
              /*  Intent intent=new Intent(Comment.this,surat.class);
                intent.putExtra("surat", (Serializable) post_surat);
                startActivity(intent);*/
            }

            @Override
            public void onPageSelected(int position) {
                int i;
                for( i = 0; i< dotscount; i++){
                    dots[i].setImageDrawable(ContextCompat.getDrawable(Comment.this, R.drawable.non_active_dot));
                    surat_sanaw.setText((position+1)+"/"+dotscount);
                }

                dots[position].setImageDrawable(ContextCompat.getDrawable(Comment.this, R.drawable.active_dot));
                surat_sanaw.setText((position+1)+"/"+dotscount);

            }


            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });





    }
    public void tazewagt(long millisecond) {

        GetTimeAgo getTimeAgo = new GetTimeAgo();

        String lastSeenTime = getTimeAgo.getTimeAgo(millisecond, this);
        ulanyjy_wagt.setText(lastSeenTime);
    }
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    protected void onPause() {


        super.onPause();


    }
}
