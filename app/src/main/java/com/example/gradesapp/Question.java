package com.example.gradesapp;



import java.util.ArrayList;
import java.util.List;


public class Question {
    List<OMRdata> OMRdata;

    public Question() {
        OMRdata = new ArrayList<>();
    }

    public List<OMRdata> getOMRdata() {
        return OMRdata;
    }

    public void setOMRdata(List<OMRdata> OMRdata) {
        this.OMRdata = OMRdata;
    }

    public void addOption(OMRdata OMRdata) {
        this.OMRdata.add(OMRdata);
    }

    @Override
    public String toString() {
        String string = "";
        for (int o = 0; o < OMRdata.size(); o++) {
            string = string + "[" + OMRdata.get(o).toString() + "]";
        }
        return string;
    }

    public int getNumeroOptions() {
        return OMRdata.size();
    }

    public OMRdata getOption(int o) {
        return OMRdata.get(o);
    }

}
