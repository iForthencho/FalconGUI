package com.example.falcongui;

public class Robot {

    public String ID;
    public String Location = "";
    public String Ob_Loc_x = "";
    public String Ob_Loc_y = "";
    public int Take_pic = 0;
    public String Time = "Offline";
    public int pose_w = 0;
    public int pose_x = 0;
    public int pose_y = 0;

    public Robot () {}

    public Robot (String ID) {
        this.ID = ID;
    }
}
