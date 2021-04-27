package com.example.komp.gurles;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

public class ViewpagerAdapter extends PagerAdapter {
   Activity activity;
   String [] images;
   LayoutInflater inflater;
   Context context;
    public ViewpagerAdapter(Context activity, List<String> images) {
        this.activity = (Activity) activity;
        this.images = images.toArray(new String[0]);
    }

    @Override
    public int getCount() {

        return images.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view==object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        inflater=(LayoutInflater)activity.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View itemview=inflater.inflate(R.layout.viewpager_item,container,false);
        ImageView image;
        image=(ImageView)itemview.findViewById(R.id.viewpager_surat);

        DisplayMetrics dis=new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dis);
        int height=dis.heightPixels;
        int width=dis.widthPixels;
        image.setMinimumHeight(height);
       image.setMinimumWidth(width);
        try{
            RequestOptions requestOptions=new RequestOptions();
            requestOptions.centerInside();
            Glide.with(activity.getApplicationContext()).load(images[position]).apply(requestOptions).into(image);

        }
        catch (Exception ex){

        }
        container.addView(itemview);
        return itemview;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        ((ViewPager)container).removeView((View)object);
    }
}

