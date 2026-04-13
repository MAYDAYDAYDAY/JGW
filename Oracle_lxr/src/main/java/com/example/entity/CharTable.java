package com.example.entity;

public class CharTable {
    String sWord;  // 字形 或 位置

    Integer id ;
    
    String BI;// 金文链接

    String OBC; //甲骨字链接


    public String getsWord() {
        return sWord;
    }

    public void setsWord(String sWord) {
        this.sWord = sWord;
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getBI() {
        return BI;
    }

    public void setBI(String BI) {
        this.BI = BI;
    }

    public String getOBC() {
        if(this.OBC != null)
            return OBC;
        else
            return  null;
    }

    public void setOBC(String OBC) {
        this.OBC = OBC;
    }
}
