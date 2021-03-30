package com.flink.bean;

import java.io.Serializable;

public class SensorReading implements Serializable{
    public String area;
    public String uid;
    public String os;
    public String ch;
    public String appid;
    public String mid;
    public String type;
    public String vs;
    public String ts;

    public SensorReading(String area, String uid, String os, String ch, String appid, String mid, String type, String vs, String ts) {
        this.area = area;
        this.uid = uid;
        this.os = os;
        this.ch = ch;
        this.appid = appid;
        this.mid = mid;
        this.type = type;
        this.vs = vs;
        this.ts = ts;
    }

    public SensorReading() {
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getCh() {
        return ch;
    }

    public void setCh(String ch) {
        this.ch = ch;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVs() {
        return vs;
    }

    public void setVs(String vs) {
        this.vs = vs;
    }

    public String getTs() {
        return ts;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }

    @Override
    public String toString() {
        return "sensorReading{" +
                "area='" + area + '\'' +
                ", uid='" + uid + '\'' +
                ", os='" + os + '\'' +
                ", ch='" + ch + '\'' +
                ", appid='" + appid + '\'' +
                ", mid='" + mid + '\'' +
                ", type='" + type + '\'' +
                ", vs='" + vs + '\'' +
                ", ts='" + ts + '\'' +
                '}';
    }
}
