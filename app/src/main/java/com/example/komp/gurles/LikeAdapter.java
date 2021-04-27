package com.example.komp.gurles;

import java.util.Date;

public class LikeAdapter extends BlogPostId {
    private Date wagt;

    public Date getWagt() {
        return wagt;
    }

    public void setWagt(Date wagt) {
        this.wagt = wagt;
    }

    public LikeAdapter(Date wagt) {
        this.wagt = wagt;
    }

    public LikeAdapter() {
    }
}
