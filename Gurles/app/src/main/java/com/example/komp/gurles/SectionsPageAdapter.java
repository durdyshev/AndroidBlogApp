package com.example.komp.gurles;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by komp on 12.04.2018.
 */

class SectionsPageAdapter extends FragmentPagerAdapter{
    public SectionsPageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public android.support.v4.app.Fragment getItem(int position) {

        switch (position){
            case 0:
                Dost_gelen_fragment dost_gelen_fragment=new Dost_gelen_fragment();
                return dost_gelen_fragment;

            case 1: Dost_gozle_fragment dost_gozle_fragment=new Dost_gozle_fragment();
                return dost_gozle_fragment;

            default:return null;
        }

    }

    @Override
    public int getCount() {
        return 2;
    }
    public CharSequence getPageTitle(int position){
        switch (position){
            case 0:
                return "Dost gos";

            case 1:
                return "Gozle";

            default:
                return null;
        }


    }
}

