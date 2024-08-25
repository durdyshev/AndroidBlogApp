package com.example.komp.gurles;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.media.AudioManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.justblog.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.calling.CallListener;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SinchClass {
    private SinchClient sinchClient;
    private Call call;
    private Context context;
    private String user_id;

    private FirebaseFirestore firebaseFirestore;
    private Dialog dialog;

    public void setCall(Call call) {
        this.call = call;
        this.call.answer();
    }

    public Call getCall() {
        return call;
    }

    public SinchClass(SinchClient sinchClient, final Call call, final Context context, String user_id) {
        this.sinchClient = sinchClient;
        this.call = call;
        this.context = context;
        this.user_id = user_id;
        dialog = new Dialog(context);
        this.sinchClient = Sinch.getSinchClientBuilder().context(context)
                .applicationKey("9745356e-28c7-4fcc-a6bf-3808ca8edef3")
                .applicationSecret("CiiyXLMqQku3PfzKZkJv0g==")
                .environmentHost("clientapi.sinch.com")
                .userId(this.user_id)
                .build();
        this.sinchClient.setSupportCalling(true);
        this.sinchClient.startListeningOnActiveConnection();

        // sinchClient.setSupportManagedPush(true);
        this.sinchClient.start();

        this.sinchClient.getCallClient().addCallClientListener(new CallClientListener() {
            @Override
            public void onIncomingCall(CallClient callClient, final Call incomingcall) {
                firebaseFirestore = FirebaseFirestore.getInstance();


                final CircleImageView profil, answer_call, end_call;
                final TextView ady, wagt;
                final LinearLayout linearLayout;
                dialog.setContentView(com.example.justblog.R.layout.layout_caller);
                int width = ViewGroup.LayoutParams.MATCH_PARENT;
                int height = ViewGroup.LayoutParams.WRAP_CONTENT;
                Objects.requireNonNull(dialog.getWindow()).setLayout(width, height);
                dialog.setCancelable(false);
                linearLayout = (LinearLayout) dialog.findViewById(R.id.layout_caller_linearlayout);
                profil = (CircleImageView) dialog.findViewById(R.id.layout_caller_image);
                answer_call = (CircleImageView) dialog.findViewById(R.id.layout_caller_answer);
                end_call = (CircleImageView) dialog.findViewById(R.id.layout_caller_end);
                ady = (TextView) dialog.findViewById(R.id.layout_caller_name);

                firebaseFirestore.collection("ulanyjylar").document(incomingcall.getRemoteUserId()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                        String dost_ady = documentSnapshot.getString("ady");
                        String dost_profil = documentSnapshot.getString("surat");

                        ady.setText(dost_ady);
                        RequestOptions requestOptions = new RequestOptions();
                        requestOptions.centerInside();
                        Glide.with(context).load(dost_profil).apply(requestOptions).into(profil);


                    }
                });
                end_call.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        incomingcall.hangup();

                    }
                });
                answer_call.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setCall(incomingcall);
                        linearLayout.setVisibility(View.GONE);

                        //  call=incomingcall;

//                        call.answer();
                        getCall().addCallListener(new SinchCallListener(context, dialog, call));
                        Toast.makeText(context, "Jan baslady", Toast.LENGTH_LONG).show();
                    }
                });


                dialog.show();











             /*   final AlertDialog alertDialog=new AlertDialog.Builder(context).create();
               Toast.makeText(context,incomingcall.getRemoteUserId(),Toast.LENGTH_LONG).show();
                //  alertDialog.setTitle("Jan gelyar");
                alertDialog.setButton(android.app.AlertDialog.BUTTON_NEUTRAL, "Ocur", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        alertDialog.dismiss();
                       incomingcall.hangup();
                    }
                });
                alertDialog.setButton(android.app.AlertDialog.BUTTON_POSITIVE, "Ac", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        setCall(incomingcall);
                      //  call=incomingcall;

//                        call.answer();
                       getCall().addCallListener(new SinchCallListener(context,alertDialog,call));
                        Toast.makeText(context,"Jan baslady",Toast.LENGTH_LONG).show();
                    }
                });

                alertDialog.show();*/
            }
        });
    }
}

class SinchCallListener implements CallListener {
    private Context context1;
    private Dialog dialog;
    private Call call;

    public SinchCallListener(Context context, Dialog dialog, Call call) {
        this.context1 = context;
        this.dialog = dialog;
        this.call = call;
    }

    @Override
    public void onCallProgressing(Call call) {
        Toast.makeText(this.context1, "Jan baslady", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onCallEstablished(Call call) {
        Toast.makeText(context1, "Ulasildi", Toast.LENGTH_LONG).show();
        ((Activity) this.context1).setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);

    }

    @Override
    public void onCallEnded(Call endedcall) {
        Toast.makeText(context1, "Jan gutardy", Toast.LENGTH_LONG).show();
        dialog.dismiss();


        call = null;
        SinchError a = endedcall.getDetails().getError();
        ((Activity) this.context1).setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);

    }

    @Override
    public void onShouldSendPushNotification(Call call, List<PushPair> list) {

    }

}
