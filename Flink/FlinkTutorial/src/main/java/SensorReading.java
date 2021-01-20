public class SensorReading {
    public String area;
    public String uid;
    public String os;
    public String ch;
    public String appid;
    public String mid;
    public String type2;
    public String vs;
    public String ts;

    public SensorReading(String area, String uid, String os, String ch, String appid, String mid, String type2, String vs, String ts) {
        this.area = area;
        this.uid = uid;
        this.os = os;
        this.ch = ch;
        this.appid = appid;
        this.mid = mid;
        this.type2 = type2;
        this.vs = vs;
        this.ts = ts;
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

    public String getType2() {
        return type2;
    }

    public void setType2(String type2) {
        this.type2 = type2;
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
}
