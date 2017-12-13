package com.chatterbox.chatterbox;

/**
 * Created by vikas on 8/24/2016.
 */
public class users {
    private String name;
    private String email;
    private String password;
    private String phno;
    private String uid;

    public users() {}

    public users(String name,String email, String uid,String password,String phno) {

        this.name = name;
        this.email = email;
        this.uid = uid;
        this.password = password;
        this.phno = phno;
    }

    public String getName() {return name;}
    public String getEmail() {return email;}
    public String getUid() {return uid;}
    public String getPassword() {return  password;}
    public String getPhno() {return  phno;}
}
