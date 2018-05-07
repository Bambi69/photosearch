package com.gyd.photosearch.util;

import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {

    private static String ES_DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";

    /**
     * convert org.joda.time.DateTime to java.util.Date
     * @param dateTime org.joda.time.DateTime instance
     * @return date
     */
    public static Date getDateFromDateTime(DateTime dateTime) {
        return ((DateTime) dateTime).toDate();
    }

    /**
     * retrieve year from date
     * @param date
     * @return year in string format
     */
    public static String getYearFromDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return String.valueOf(cal.get(Calendar.YEAR));
    }

    /**
     * retrieve month from date
     * @param date
     * @return month in string format like "xx"
     */
    public static String getMonthFromDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return String.format("%02d", cal.get(Calendar.MONTH)+1);
    }

    /**
     * convert date to elasticsearch string format
     * @param d date to convert
     * @return string date to index
     */
    public static String convertDateToEsFormat(Date d){
        return new SimpleDateFormat(ES_DATE_FORMAT).format(d);
    }
}
