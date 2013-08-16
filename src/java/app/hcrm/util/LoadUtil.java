package app.hcrm.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 加载工具
 * @author Hong
 */
public class LoadUtil {

    public static String injectParams(String str, Date time) {
        Pattern pattern = Pattern.compile("((?:[\\\\][\\\\])*)\\$(?:(\\w+)|\\{(.*?)\\})");
        Matcher matcher = pattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        String       st;

        Pattern timePattern = Pattern.compile("(time|date)(([+\\-])(\\d+[HDWM])+)?(\\|(.+))?");
        Pattern secsPattern = Pattern.compile("(\\d)([HDWM])");
        Matcher timeMatcher;
        SimpleDateFormat df;

        while (matcher.find()) {
            st = matcher.group(2);
            if (st == null) {
                st = matcher.group(3);
            }

            switch (st) {
                case "time":
                    df = new SimpleDateFormat("yyyy-MM-dd HH:00:00");
                    st = df.format(time);
                    break;
                case "date":
                    df = new SimpleDateFormat("yyyy-MM-dd");
                    st = df.format(time);
                    break;
                default:
                    timeMatcher = timePattern.matcher(st);
                    if (timeMatcher.matches()) {
                        st = getTimeParam(st, time, timeMatcher, secsPattern);
                    }
                    else {
                        st = "";
                    }
            }

            st = matcher.group(1) + st;
            st = Matcher.quoteReplacement(st);
            matcher.appendReplacement(sb, st);
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    private static String getTimeParam(String st, Date time,
            Matcher timeMatcher, Pattern secsPattern) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(time);

        if (timeMatcher.group(2) != null) {
            Matcher secsMatcher = secsPattern.matcher(timeMatcher.group(4));
            while ( secsMatcher.find( ) ) {
                int i = Integer.parseInt(secsMatcher.group(1));
                switch (secsMatcher.group(2)) {
                    case "H":
                        cal.add(Calendar.HOUR, i);
                        break;
                    case "D":
                        cal.add(Calendar.HOUR, i * 24);
                        break;
                    case "W":
                        cal.add(Calendar.HOUR, i * 24 * 7);
                        break;
                    case "M":
                        cal.add(Calendar.MONTH, i);
                        break;
                }
            }
        }

        SimpleDateFormat df;
        if (timeMatcher.group(6) != null) {
            df = new SimpleDateFormat(timeMatcher.group(6));
            st = df.format(cal.getTime());
        }
        else {
            st = timeMatcher.group(1);
            switch (st) {
                case "time":
                    df = new SimpleDateFormat("yyyy-MM-dd HH:00:00");
                    st = df.format(cal.getTime());
                    break;
                case "date":
                    df = new SimpleDateFormat("yyyy-MM-dd");
                    st = df.format(cal.getTime());
                    break;
                default:
                    st = "";
            }
        }

        return st;
    }

}
