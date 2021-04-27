package com.example.komp.gurles;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Pikir_uytget extends AppCompatActivity {
    private ListView listView;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    private String user_id;
    private TextView tekst_at;
    private CircleImageView galam;
    String [] pikirler={"Aý parçasy","Bet","Ýabby","Baý ogul","Jalaý","Dünýäden habarsyz","Duzsyz","Kitap ýapýan"};
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pikir_uytget);
        listView=(ListView)findViewById(R.id.pikir_uytget_list);
        firebaseFirestore=FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();
        user_id=mAuth.getCurrentUser().getUid();
        dialog=new Dialog(Pikir_uytget.this);
tekst_at=(TextView)findViewById(R.id.pikir_uytget_tekst);
galam=(CircleImageView)findViewById(R.id.pikir_uytget_galam);


        firebaseFirestore.collection("ulanyjylar").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().exists()){
                          String pikir=task.getResult().getString("pikir");

                        if(pikir.equals(null)){
                            tekst_at.setText("Boş");
                        }
                        else{
                            tekst_at.setText(pikir);
                        }




                    }
                }
                else{

                }

            }
        });

        galam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText ad_edit;CircleImageView goybolsun,uytget;TextView tekst;
                dialog.setContentView(R.layout.uytget_layout);
                tekst=(TextView)dialog.findViewById(R.id.uytget_layout_tekst);
                ad_edit=(EditText)dialog.findViewById(R.id.uytget_layout_edittext);
                uytget=(CircleImageView) dialog.findViewById(R.id.uytget_layout_uytget_knopka);
                goybolsun=(CircleImageView) dialog.findViewById(R.id.uytget_layout_goybolsun);

                tekst.setText("Pikir üýtget");
                ad_edit.setText(tekst_at.getText().toString());
                dialog.show();

                uytget.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String pikir=ad_edit.getText().toString();
                        if(!TextUtils.isEmpty(pikir)){

                            Map<String,Object>usermap=new HashMap<>();
                            usermap.put("pikir",pikir);
                            firebaseFirestore.collection("ulanyjylar").document(user_id).update(usermap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(Pikir_uytget.this,"Status üýtgedildi",Toast.LENGTH_LONG).show();
                                    dialog.dismiss();
                                }
                            });
                        }
                    }
                });
                goybolsun.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });



            }
        });
MyAdapter adapter=new MyAdapter(this,pikirler);
listView.setAdapter(adapter);
listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
       listView.setEnabled(false);

        if(position==0){
            pikir_uytget("Aý parçasy");
        }
        if(position==1){
            pikir_uytget("Bet");
        }
        if(position==2){
            pikir_uytget("Ýabby");

        }if(position==3){
            pikir_uytget("Baý ogul");

        }if(position==4){
            pikir_uytget("Jalaý");

        }if(position==5){
            pikir_uytget("Dünýäden habarsyz");

        }if(position==6){
            pikir_uytget("Duzsyz");

        }
        if(position==7){
            pikir_uytget("Kitap ýapýan");

        }
    }
});



    }

    private void pikir_uytget(final String s) {
        Map<String, Object> usermap=new HashMap<>();
        usermap.put("pikir",s);
        firebaseFirestore.collection("ulanyjylar").document(user_id).update(usermap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(Pikir_uytget.this,"Status üýtgedildi "+s,Toast.LENGTH_LONG).show();
                tekst_at.setText(s);
                listView.setEnabled(true);
                Intent intent=new Intent(Pikir_uytget.this,Profil.class);
                startActivity(intent);
                finish();
            }
        });
    }

    class MyAdapter extends ArrayAdapter<String>{
        Context context;
        String pikirler[];
        MyAdapter(Context c,String pikir[]){
            super(c,R.layout.pikir_listview,R.id.textView,pikir);
            this.context=c;
            this.pikirler=pikir;

        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater=(LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view=layoutInflater.inflate(R.layout.pikir_listview,parent,false);
            TextView tekst=view.findViewById(R.id.listview_tekst);
            CircleImageView tick=view.findViewById(R.id.listview_tick);

            tekst.setText(pikirler[position]);

            return view;
        }
    }
}
