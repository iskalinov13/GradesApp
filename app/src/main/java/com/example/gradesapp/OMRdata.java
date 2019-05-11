package com.example.gradesapp;
import org.opencv.core.Rect;

public class OMRdata {
    boolean value;
    Rect box;

    public OMRdata(boolean value, Rect box) {
        this.value = value;
        this.box = box;
    }

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    public Rect getBox() {
        return box;
    }

    public void setBox(Rect box) {
        this.box = box;
    }

    public boolean sameLine(OMRdata OMRdata) {
        return (Math.abs(box.y - OMRdata.getBox().y) < box.height);
    }

    public boolean sameGroup(OMRdata OMRdata, int distanceBetweenBoxes) {
        return (Math.abs(box.x - OMRdata.getBox().x) < (distanceBetweenBoxes * 2));
    }

    public int distance(OMRdata OMRdata){
        return (Math.abs(box.x - OMRdata.getBox().x));
    }

    @Override
    public String toString() {
        String string = "" + value;
        return string;
    }
}
