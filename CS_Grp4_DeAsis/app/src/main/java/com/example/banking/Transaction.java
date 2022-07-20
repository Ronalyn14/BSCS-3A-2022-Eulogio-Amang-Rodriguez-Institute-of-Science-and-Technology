package com.example.banking;

import com.google.firebase.firestore.DocumentSnapshot;

public class Transaction {

    private String reference = "", type = "", user = "", remark = "", amount = "", recepientid = "", recepientname = "", date = "";

    public Transaction(String reference, String type, String user, String remark, String amount, String recepientid, String recepientname, String date){
        this.reference = reference;
        this.type = type;
        this.user = user;
        this.remark = remark;
        this.amount = amount;
        this.recepientid = recepientid;
        this.recepientname = recepientname;
        this.date = date;
    }

    public Transaction() {

    }

    public String getReference()
    {
        return reference;
    }
    public void setReference(String reference)
    {
        this.reference = reference;
    }

    public String getType()
    {
        return type;
    }
    public void setType(String type)
    {
        this.type = type;
    }

    public String getUser()
    {
        return user;
    }
    public void setUser(String user)
    {
        this.user = user;
    }

    public String getRemark()
    {
        return remark;
    }
    public void setRemark(String remark)
    {
        this.remark = remark;
    }

    public String getAmount()
    {
        return amount;
    }
    public void setAmount(String amount)
    {
        this.amount = amount;
    }

    public String getRecepientid()
    {
        return recepientid;
    }
    public void setRecepientid(String recepientid)
    {
        this.recepientid = recepientid;
    }

    public String getRecepientname()
    {
        return recepientname;
    }
    public void setRecepientname(String recepientname)
    {
        this.recepientname = recepientname;
    }

    public String getDate()
    {
        return date;
    }
    public void setDate(String date)
    {
        this.date = date;
    }


}
