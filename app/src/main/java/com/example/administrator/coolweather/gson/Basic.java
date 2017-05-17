package com.example.administrator.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2017/4/30.
 */

public class Basic {


    /**
     * city : 深圳
     * cnty : 中国
     * id : CN101280601
     * lat : 22.547
     * lon : 114.085947
     * update : {"loc":"2017-04-29 23:53","utc":"2017-04-29 15:53"}
     */

    @SerializedName("city")
    public String cityName;
    public String cnty;
    @SerializedName("id")
    public String weatherId;
    public String lat;
    public String lon;
    public UpdateBean update;

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getCnty() {
        return cnty;
    }

    public void setCnty(String cnty) {
        this.cnty = cnty;
    }

    public String getWeatherId() {
        return weatherId;
    }

    public void setId(String weatherId) {
        this.weatherId = weatherId;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public UpdateBean getUpdate() {
        return update;
    }

    public void setUpdate(UpdateBean update) {
        this.update = update;
    }

    public static class UpdateBean {
        /**
         * loc : 2017-04-29 23:53
         * utc : 2017-04-29 15:53
         */

        public String loc;
        public String utc;

        public String getLoc() {
            return loc;
        }

        public void setLoc(String loc) {
            this.loc = loc;
        }

        public String getUtc() {
            return utc;
        }

        public void setUtc(String utc) {
            this.utc = utc;
        }
    }
}
