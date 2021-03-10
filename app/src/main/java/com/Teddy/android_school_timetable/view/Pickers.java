package com.Teddy.android_school_timetable.view;

import java.io.Serializable;

public class Pickers implements Serializable {

    private static final long serialVersionUID = 1L;

    private String showConetnt;
    private String showId;

    String getShowConetnt() {
        return showConetnt;
    }

    public String getShowId() {
        return showId;
    }

    public Pickers(String showConetnt, String showId) {
        super();
        this.showConetnt = showConetnt;
        this.showId = showId;
    }
}