package kore.botssdk.utils;

import android.annotation.SuppressLint;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Copyright (c) 2014 Kore Inc. All rights reserved.
 */

@SuppressLint("UnknownNullness")
public class DateUtils {
    public static final SimpleDateFormat isoFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
    public static final SimpleDateFormat fileFormatter = new SimpleDateFormat("yy_MM_dd_HH_mm_ss", Locale.ENGLISH);
    public static final Format dateFormat4 = new SimpleDateFormat("d MMM yyyy 'at' h:mm a", Locale.ENGLISH);
    public static final Format dateTime = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
    public static final Format yearFormat = new SimpleDateFormat("yyyy", Locale.ENGLISH);
    public static final Format dateFormatDay = new SimpleDateFormat("EEE, d MMM yyyy", Locale.ENGLISH);
    public static final SimpleDateFormat dateWeekMsg = new SimpleDateFormat("EE, MMM dd", Locale.ENGLISH);
    public static final SimpleDateFormat dateWeekMsgBubble = new SimpleDateFormat("EE MMM dd", Locale.ENGLISH);
    public static final SimpleDateFormat dateWeekDay = new SimpleDateFormat("EE, MMM dd, yyyy", Locale.ENGLISH);
    public static final SimpleDateFormat dateWeekDayBubble = new SimpleDateFormat("EE MMM dd yyyy", Locale.ENGLISH);
    public static final SimpleDateFormat dateTime1 = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
    public static final Format calendar_list_format = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.ENGLISH);
    public static final Format calendar_list_format_2 = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
    public static final SimpleDateFormat dateWeekDayTime = new SimpleDateFormat("EE, MMM dd yyyy 'at' hh:mm a", Locale.ENGLISH);
    public static final SimpleDateFormat dateWeekDayTime4 = new SimpleDateFormat("dd MMM, yyyy, hh:mm a", Locale.ENGLISH);
    public static final SimpleDateFormat dateWeekDayTime5 = new SimpleDateFormat("EEE, MMM dd, yyyy, ", Locale.ENGLISH);
    public static final Format calendar_event_list_format1 = new SimpleDateFormat("EEE, d MMM", Locale.ENGLISH);
    private static final Format dateMonthDay = new SimpleDateFormat("MMM dd", Locale.ENGLISH);
    public static final SimpleDateFormat dateWeekMsgTime = new SimpleDateFormat("EE, MMM dd, hh:mm a", Locale.ENGLISH);
    private static final Format dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
    public static final Format calendar_allday_format = new SimpleDateFormat("EE, MMM dd", Locale.ENGLISH);
    public static final Format calendar_allday_time_format = new SimpleDateFormat("EE, MMM dd, hh:mm a", Locale.ENGLISH);

    public static String getTimeStamp(String timeStamp, boolean timezoneModifiedRequired) throws ParseException {
        if (timeStamp == null || timeStamp.isEmpty()) return "";
        long timeStampMillis = Objects.requireNonNull(isoFormatter.parse(timeStamp)).getTime() + ((timezoneModifiedRequired) ? TimeZone.getDefault().getRawOffset() : 0);
        return getTimeStamp(timeStampMillis);
    }

    public static String getCurrentDateTime() {
        return fileFormatter.format(new Date(System.currentTimeMillis()));
    }

    public static double getOneDayMiliseconds(long diffvalue) {
        double seconds = (double) (diffvalue / 1000);
        return seconds / (60 * 60);

    }

    public static String getTimeStamp(long timeStampMillis) {
        return dateFormat4.format(new Date(timeStampMillis));
    }

    public static String getTimeInAmPm(long dateInMs) {
        return dateTime.format(new Date(dateInMs));
    }

    /**
     * Just now
     * Today, JUN 08
     */
    public static String getSlotsDate(long lastModified) {
        // CREATE DateFormatSymbols WITH ALL SYMBOLS FROM (DEFAULT) Locale
        DateFormatSymbols symbols = new DateFormatSymbols(Locale.getDefault());

        // OVERRIDE SOME symbols WHILE RETAINING OTHERS
        symbols.setAmPmStrings(new String[]{"am", "pm"});
        int messageYear = Integer.parseInt(yearFormat.format(new Date(lastModified)));
        int currentYear = Integer.parseInt(yearFormat.format(new Date()));
        String time = "";

        if (android.text.format.DateUtils.isToday(lastModified)) {
            time = "Today";
        } else if (isYesterday(lastModified)) {
            time = "Yesterday";
        } else if (isTomorrow(lastModified)) {
            time = "Tomorrow";
        } else {

            time = currentYear == messageYear ? dateWeekMsg.format(new Date(lastModified)) : dateWeekDayBubble.format(new Date(lastModified));
        }
        return time;
    }


    public static int getDays(long diff) {
        return (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
    }


    @SuppressLint("UnknownNullness")
    public static String formattedSentDateV6(long lastModified) {
        // CREATE DateFormatSymbols WITH ALL SYMBOLS FROM (DEFAULT) Locale
        DateFormatSymbols symbols = new DateFormatSymbols(Locale.getDefault());

        // OVERRIDE SOME symbols WHILE RETAINING OTHERS
        symbols.setAmPmStrings(new String[]{"am", "pm"});
        dateWeekDay.setDateFormatSymbols(symbols);
        int messageYear = Integer.parseInt(yearFormat.format(new Date(lastModified)));
        int currentYear = Integer.parseInt(yearFormat.format(new Date()));

        String time = "";
        if (android.text.format.DateUtils.isToday(lastModified)) {
            time = "Today, " + dateMonthDay.format(new Date(lastModified));
        } else if (isYesterday(lastModified)) {
            time = "Yesterday, " + dateMonthDay.format(new Date(lastModified));
        } else if (isTomorrow(lastModified)) {
            time = "Tomorrow, " + dateMonthDay.format(new Date(lastModified));
        } else {
            time = currentYear == messageYear ? dateWeekMsg.format(new Date(lastModified)) : dateWeekDay.format(new Date(lastModified));
        }


        return time;
    }
    public static String formattedSentDate(long lastModified) {
        // CREATE DateFormatSymbols WITH ALL SYMBOLS FROM (DEFAULT) Locale
        DateFormatSymbols symbols = new DateFormatSymbols(Locale.getDefault());

        // OVERRIDE SOME symbols WHILE RETAINING OTHERS
        symbols.setAmPmStrings(new String[]{"am", "pm"});
        dateWeekDay.setDateFormatSymbols(symbols);
        int messageYear = Integer.parseInt(yearFormat.format(new Date(lastModified)));
        int currentYear = Integer.parseInt(yearFormat.format(new Date()));

        String time = "";
        if (android.text.format.DateUtils.isToday(lastModified)) {
            time = "Today";
        } else if (isYesterday(lastModified)) {
            time = "Yesterday";
        } else if (isTomorrow(lastModified)) {
            time = "Tomorrow";
        }
        else {
            time = currentYear == messageYear ? dateWeekMsgBubble.format(new Date(lastModified)) : dateWeekDay.format(new Date(lastModified));
        }


        return time;
    }

    public static String getDate(long lastModified) {
        return dateWeekMsg.format(new Date(lastModified));
    }

    public static String getDayDate(double startdate) {
        return calendar_allday_format.format(startdate);
    }

    public static String getMorethanDayDate(double startdate, double enddate) {
        return calendar_allday_format.format(startdate) + " - " + calendar_allday_format.format(enddate);
    }

    public static String getMorethanDayDateTime(double startdate, double enddate) {
        return calendar_allday_time_format.format(startdate) + " - " + calendar_allday_time_format.format(enddate);
    }

    public static String getDateMMMDDYYYY(double startdate, double enddate) {
        return dateWeekDayTime5.format(startdate) + calendar_list_format_2.format(startdate).toLowerCase() + " to " + calendar_list_format_2.format(enddate).toLowerCase();
    }

    public static String getDateWithTime(long lastModified) {

        String date = dateWeekMsgTime.format(lastModified);

        if (android.text.format.DateUtils.isToday(lastModified)) {
            date = "Today " + getTimeInAmPm(lastModified);
        } else if (isYesterday(lastModified)) {
            date = "Yesterday " + getTimeInAmPm(lastModified);
        } else if (isTomorrow(lastModified)) {
            date = "Tomorrow " + getTimeInAmPm(lastModified);
        }
        return date;
    }

    public static String getCalDay(long mdate) {
        String date = DateUtils.calendar_event_list_format1.format(mdate);

        if (android.text.format.DateUtils.isToday(mdate)) {
            date = "Today";
        } else if (isYesterday(mdate)) {
            date = "Yesterday";
        } else if (isTomorrow(mdate)) {
            date = "Tomorrow";
        }
        return date;
    }

    public static String getFilesDateSturcture(String lastModified) {
        try {
            Date dateCal = isoFormatter.parse(lastModified);
            if(dateCal != null)
            {
                long date = dateCal.getTime();

                if (android.text.format.DateUtils.isToday(date)) {
                    return "Last Edited Today";
                } else if (isYesterday(date)) {
                    return "Last Edited Yesterday";
                }
            }
            return "Last Edited " + getDateFromString(lastModified);
        } catch (Exception e) {
            e.printStackTrace();
            return lastModified;
        }
    }

    public static boolean isYesterday(long millis) {
        return android.text.format.DateUtils.isToday(millis + android.text.format.DateUtils.DAY_IN_MILLIS);
    }

    public static boolean isTomorrow(long millis) {
        return android.text.format.DateUtils.isToday(millis - android.text.format.DateUtils.DAY_IN_MILLIS);
    }

    public static String getDateFromString(String time) {
        if (time == null || time.isEmpty()) return "";
        try {
            Date date = DateUtils.isoFormatter.parse(time);
            return calendar_list_format.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return time;
    }

    public static String getDateFromStringByDate(String time) {
        if (time == null || time.isEmpty()) return "";
        try {
            long lastModified = new Date(time).getTime();
            if (android.text.format.DateUtils.isToday(lastModified)) {
                return "Today";
            } else if (isYesterday(lastModified)) {
                return "Yesterday";
            }
            return dateFormat.format(lastModified);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    public static Date getDDMMYYYY(long date) {
        SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);

        try {
            return DATE_FORMAT.parse(dateFormat.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getDateinDayFormat(long dateInMs) {
        return dateFormatDay.format(new Date(dateInMs));
    }


    public static String formattedSentDateV8(long lastModified, boolean isDetailView) {
        // CREATE DateFormatSymbols WITH ALL SYMBOLS FROM (DEFAULT) Locale
        DateFormatSymbols symbols = new DateFormatSymbols(Locale.getDefault());

        // OVERRIDE SOME symbols WHILE RETAINING OTHERS
        symbols.setAmPmStrings(new String[]{"am", "pm"});
        dateWeekDay.setDateFormatSymbols(symbols);
        int messageYear = Integer.parseInt(yearFormat.format(new Date(lastModified)));
        int currentYear = Integer.parseInt(yearFormat.format(new Date()));

        String time = "";

        if (isDetailView) {
            if (android.text.format.DateUtils.isToday(lastModified)) {
                time = "Today at " + dateTime1.format(new Date(lastModified));
            } else if (isYesterday(lastModified)) {
                time = "Yesterday, " + dateTime1.format(new Date(lastModified));
            } else if (isTomorrow(lastModified)) {
                time = "Tomorrow, " + dateTime1.format(new Date(lastModified));
            } else {
                time = dateWeekDayTime.format(new Date(lastModified));
            }
        } else {

            if (android.text.format.DateUtils.isToday(lastModified)) {
                time = "Today, " + dateMonthDay.format(new Date(lastModified));
            } else if (isYesterday(lastModified)) {
                time = "Yesterday, " + dateMonthDay.format(new Date(lastModified));
            } else if (isTomorrow(lastModified)) {
                time = "Tomorrow, " + dateMonthDay.format(new Date(lastModified));
            } else {
                time = currentYear == messageYear ? dateWeekMsg.format(new Date(lastModified)) : dateWeekDay.format(new Date(lastModified));
            }
        }
        return time;
    }

    public static String getCorrectedTimeZone(String timeZone) {
        if (StringUtils.isNullOrEmptyWithTrim(timeZone)) return "";
        timeZone = timeZone.toLowerCase();
        if (timeZone.contains("calcutta")) {
            timeZone = timeZone.replace("calcutta", "kolkata");
        }
        return timeZone;
    }


    public static boolean formattedSentDateV8_InAnnouncement2(long createdOn) {
        boolean isDateLabelShouldVisble = true;
        if (android.text.format.DateUtils.isToday(createdOn)) {
            isDateLabelShouldVisble = false;
        } else if (isYesterday(createdOn)) {
            isDateLabelShouldVisble = false;
        }

        return isDateLabelShouldVisble;

    }

    public static String formattedSentDateV8_InAnnouncement(long createdOn) {
        String time = "";
        if (android.text.format.DateUtils.isToday(createdOn)) {
            time = "Today at " + dateTime1.format(new Date(createdOn));
        } else if (isYesterday(createdOn)) {
            time = "Yesterday, " + dateTime1.format(new Date(createdOn));
        } else {
            time = dateWeekDayTime4.format(new Date(createdOn));
        }

        return time;
    }

    public static String getDay(long mdate) {
        String date = DateUtils.calendar_event_list_format1.format(mdate);

        if (isTodayOrBefore(mdate)) {
            //date = "Today";
            date = "Later today";
        } /*else if (isYesterday(mdate)) {
            date = "Yesterday";
        }*/ else if (isTomorrow(mdate)) {
            date = "Tomorrow";
        }
        return date;
    }

    /**
     * @return true if the supplied when is today else false
     */
    public static boolean isTodayOrBefore(long when) {
        if (android.text.format.DateUtils.isToday(when)) {
            return true;
        } else {
            Date current = new Date();
            Date other = new Date(when);
            return other.before(current);
        }
    }

    public static String getMonthName(int month)
    {
        switch (month)
        {
            case 0:
                return "January";
            case 1:
                return "February";
            case 2:
                return "March";
            case 3:
                return "April";
            case 4:
                return "May";
            case 5:
                return "June";
            case 6:
                return "July";
            case 7:
                return "August";
            case 8:
                return "September";
            case 9:
                return "October";
            case 10:
                return "November";
            case 11:
                return "December";
            default:
                return "";
        }
    }

    public static String getDayOfMonthSuffix(final int n) {
        if (n >= 11 && n <= 13) {
            return "th";
        }
        switch (n % 10) {
            case 1:  return "st";
            case 2:  return "nd";
            case 3:  return "rd";
            default: return "th";
        }
    }
}