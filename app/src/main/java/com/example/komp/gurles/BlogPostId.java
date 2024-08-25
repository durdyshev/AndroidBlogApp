package com.example.komp.gurles;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.Exclude;


public class BlogPostId {
    @Exclude
    public String BlogPostId;

    public <T extends BlogPostId> T within(@NonNull final String id) {
        this.BlogPostId = id;
        return (T) this;
    }
}
