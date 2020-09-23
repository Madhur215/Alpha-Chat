package com.example.alphachat.Util;

import java.util.Calendar;
import java.util.HashMap;

public class DateAndTime {

    private HashMap<Integer, String> months = new HashMap<>();
    private Calendar calendar;

    public DateAndTime() {

        months.put(1, "Jan");
        months.put(2, "Feb");
        months.put(3, "Mar");
        months.put(4, "Apr");
        months.put(5, "May");
        months.put(6, "Jun");
        months.put(7, "Jul");
        months.put(8, "Aug");
        months.put(9, "Sep");
        months.put(10, "Oct");
        months.put(11, "Nov");
        months.put(12, "Dec");
        calendar = Calendar.getInstance();
    }

    public String getTime(){
        String hour = Integer.toString(calendar.get(Calendar.HOUR_OF_DAY));
        String minute = Integer.toString(calendar.get(Calendar.MINUTE));
        if(minute.length() == 1){
            minute = "0" + minute;
        }
        if(hour.length() == 1){
            hour = "0" + hour;
        }
        return hour + ":" + minute;
    }

    public String getDATE(){
        String day = Integer.toString(calendar.get(Calendar.DATE));
        String month = months.get(calendar.get(Calendar.MONTH) + 1);
        String year = Integer.toString(calendar.get(Calendar.YEAR));
        year = String.valueOf(year.charAt(2)) + year.charAt(3);
        if(day.length() == 1)
            day = "0" + day;
        return day + " " + month + " " + year;
    }

}
