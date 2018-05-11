package com.gyd.photosearch.util;

import org.joda.time.DateTime;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtil {

    // DateFormatSymbols in default Locale
    private static final DateFormatSymbols dfs = new DateFormatSymbols(Locale.FRENCH);

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
     * retrieve complete month from date (ex: Janvier, Février, etc)
     *
     * @param date
     * @return month in string format like "xx"
     */
    public static String getMonthFromDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        // we retrieve complete month from date format symbols (ex : janvier)
        String month = dfs.getMonths()[cal.get(Calendar.MONTH)];

        // then, upper first letter
        month = month.substring(0, 1).toUpperCase() + month.substring(1);

        return month;
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
