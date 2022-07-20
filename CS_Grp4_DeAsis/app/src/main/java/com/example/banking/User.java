package com.example.banking;

import com.google.firebase.firestore.DocumentSnapshot;

public class User {

    private String name = "",address = "",email = "",password = "",number = "",id = "",profile = "default", funds = "";

    public User(String name, String address, String email, String password, String number, String id, String date, String profile, String funds){
        this.name = name;
        this.address = address;
        this.email = email;
        this.password = password;
        this.number = number;
        this.id = id;
        this.profile = profile;
        this.funds = funds;
    }

    public User() {

    }

    public void parseData(DocumentSnapshot dataSnapshot) {
        id = dataSnapshot.getId();
        if (dataSnapshot.get("name") != null) {
            name = dataSnapshot.get("name").toString();
        }
        if (dataSnapshot.get("cellphone") != null) {
            number = dataSnapshot.get("cellphone").toString();
        }
        if (dataSnapshot.get("email") != null) {
            email = dataSnapshot.get("email").toString();
        }
        if (dataSnapshot.get("address") != null) {
            address = dataSnapshot.get("address").toString();
        }
        if (dataSnapshot.get("profile") != null) {
            profile = dataSnapshot.get("profile").toString();
        }
    }

    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
    }

    public String getEmail()
    {
        return email;
    }
    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getPassword()
    {
        return password;
    }
    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getAddress()
    {
        return address;
    }
    public void setAddress(String address)
    {
        this.address = address;
    }

    public String getCellphone()
    {
        return number;
    }
    public void setCellphone(String number)
    {
        this.number = number;
    }

    public String getID()
    {
        return id;
    }
    public void setID(String id)
    {
        this.id = id;
    }

    public String getProfile()
    {
        return profile;
    }
    public void setProfile(String profile)
    {
        this.profile = profile;
    }

    public String getFunds()
    {
        return funds;
    }
    public void setFunds(String funds)
    {
        this.funds = funds;
    }
}
