package com.example.komp.gurles;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;



public class Sms_paylas_adapterclass extends RecyclerView.Adapter<Sms_paylas_adapterclass.ViewHolder>{
    public Context context;
    public List<Obshydostlar_adapter> dostlar;
    public List<String> idler;
    public Dialog dialog;


    public Sms_paylas_adapterclass(List<Obshydostlar_adapter>dostlar){
        this.dostlar=dostlar;

    }
    @NonNull
    @Override
    public Sms_paylas_adapterclass.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.sms_paylas_yeke_haly,parent,false);
        context=parent.getContext();
        dialog=new Dialog(context);
        idler=new ArrayList<>();
        return new Sms_paylas_adapterclass.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final Sms_paylas_adapterclass.ViewHolder holder, final int position) {
        final String ady_data=dostlar.get(position).getAdy();
        holder.tekstyap(ady_data);
        final String suraturl=dostlar.get(position).getProfil_surat();
        holder.suratindir(suraturl);
        final String id=dostlar.get(position).getUser_id();

        /*holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.checkBox.isChecked()){
                    idler.add(id);
                }
            }
        });*/

        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(((CompoundButton) view).isChecked()){
                    Toast.makeText(context,"Basyldy",Toast.LENGTH_LONG).show();
                    idler.add(id);
                } else {
                    Toast.makeText(context,"Ayryldy",Toast.LENGTH_LONG).show();
                }
            }
        });
        //holder.tekstyap(idler.get(position));




    }

    @Override
    public int getItemCount() {
        return dostlar.size();
    }

    public class ViewHolder extends  RecyclerView.ViewHolder{
        private TextView AdyView;
        private View mView;
        private ImageView imageView;
        private CheckBox checkBox;



        public ViewHolder(View itemView) {
            super(itemView);
            mView=itemView;
            checkBox=(CheckBox)mView.findViewById(R.id.sms_paylas_yeke_haly_checkbox);

        }
        public void  tekstyap(String Text){
            AdyView=mView.findViewById(R.id.sms_paylas_yeke_haly_ady);
            AdyView.setText(Text);

        }
        public void suratindir(String suraturl){
            imageView= mView.findViewById(R.id.sms_paylas_yeke_haly_profil);
            Glide.with(context).load(suraturl).into(imageView);
        }

    }

}
