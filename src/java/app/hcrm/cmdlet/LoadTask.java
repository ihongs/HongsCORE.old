package app.hcrm.cmdlet;

import app.hongs.HongsError;
import app.hongs.HongsException;
import app.hongs.cmdlet.CmdletHelper;
import app.hongs.db.DB;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 加载任务
 * @author Hong
 */
public class LoadTask {
    public static void cmdlet(Map<String, String[]> args) throws HongsException {
        Map<String, Object> opts = CmdletHelper.getOpts(args,
                "task*s", "time:s", "process:i");
        List<String> taskIds = (List<String>) opts.get("task");
        String timeStr = (String) opts.get("time");
        Date time; int hour, wday, mday;
        Calendar cal;

        if (timeStr == null ) {
            time = new Date();
        }
        else {
            time = parseTime(timeStr);
        }
        cal = Calendar.getInstance();
        cal.setTime(time);

        hour = cal.get(Calendar.HOUR_OF_DAY);
        wday = cal.get(Calendar.DAY_OF_WEEK);
        mday = cal.get(Calendar.DAY_OF_MONTH);

        String sql = "SELECT * FROM a_hcrm_dataset WHERE dflag = 0";
        List params = new ArrayList();
        if (taskIds != null) {
            sql += " AND id IN (?)";
            params.add(taskIds);
        }
        else {
            sql += " AND ((exec_type = 2)";
            sql += " OR (exec_type = 3 AND exec_hour = ?)";
            params.add(hour);
            sql += " OR (exec_type = 4 AND exec_hour = ? AND exec_wday = ?)";
            params.add(hour); params.add(wday);
            sql += " OR (exec_type = 5 AND exec_hour = ? AND exec_mday = ?))";
            params.add(hour); params.add(mday);
        }

        DB db = DB.getInstance("hcrm");
        List<Map<String, Object>> rows = db.fetchAll(sql, params.toArray());
    }

    private static Date parseTime(String timeStr) {
        SimpleDateFormat sdf;
        Date time;

        do {
            sdf = new SimpleDateFormat("yyyy/M/d_H:i:s");
            try {
                time = sdf.parse(timeStr);
                break;
            }
            catch (ParseException ex) { }

            sdf = new SimpleDateFormat("yyyy/M/d_H");
            try {
                time = sdf.parse(timeStr);
                break;
            }
            catch (ParseException ex) { }

            sdf = new SimpleDateFormat("yyyy/M/d");
            try {
                time = sdf.parse(timeStr);
                break;
            }
            catch (ParseException ex) { }

            HongsError er = new HongsError(0x42, "Can not parse time format");
                                er.setTranslate( "Can not parse time format");
            throw er;
        }
        while (false);

        return time;
    }
}
