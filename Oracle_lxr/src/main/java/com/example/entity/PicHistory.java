package com.example.entity;



public class PicHistory {


    @Override
    public String toString() {
        return "PicHistory{" +
                "id=" + id +
                ", picID=" + picID +
                ", picUrl='" + picUrl + '\'' +
                ", stars=" + stars +
                ", time='" + time + '\'' +
                '}';
    }

    private Integer id;
    private Integer picID;
    private String picUrl;
    private Integer stars;
    private String time;

    // Getters and Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPicID() {
        return picID;
    }

    public void setPicID(Integer picID) {
        this.picID = picID;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public Integer getStars() {
        return stars;
    }

    public void setStars(Integer stars) {
        this.stars = stars;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
