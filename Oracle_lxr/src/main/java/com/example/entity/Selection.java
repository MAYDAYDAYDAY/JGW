package com.example.entity;

import java.util.List;

public class Selection {
    Integer ans;

    public Integer getAns() {
        return ans;
    }

    public void setAns(Integer ans) {
        this.ans = ans;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    String imgUrl;

    List<String> options;
}
