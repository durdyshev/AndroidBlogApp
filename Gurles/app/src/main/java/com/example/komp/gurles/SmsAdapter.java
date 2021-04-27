package com.example.komp.gurles;
import java.util.Date;

public class SmsAdapter extends BlogPostId {
    private String message;
    private String type;
    private String from;
    private Date time;
    private String blogpost;
    private Boolean seen;


    public Boolean getSeen() {
        return seen;
    }

    public void setSeen(Boolean seen) {
        this.seen = seen;
    }

    public Date getTime() {     return time; }
    public void setTime(Date time) {
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getBlogpost() {
        return blogpost;
    }

    public void setBlogpost(String blogpost) {
        this.blogpost = blogpost;
    }

    public SmsAdapter(String message, String type, String from, Date time, Boolean seen, String blogpost) {
        this.message = message;
        this.type = type;
        this.from = from;
        this.time = time;
        this.seen=seen;
        this.blogpost=blogpost;



    }



    public SmsAdapter() {
    }
}