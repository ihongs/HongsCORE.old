package app.hcrm.cmdlet;

import app.hcrm.util.Loader;
import app.hcrm.util.Writer;
import app.hcrm.util.writer.JDBCWriter;
import app.hongs.HongsError;
import app.hongs.HongsException;
import app.hongs.cmdlet.CmdletHelper;
import app.hongs.db.DB;
import app.hongs.db.FetchBean;
import app.hongs.db.Table;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        Integer process = (Integer) opts.get("process");
        if (process == null) process = 1;
        Date time; int hour, wday, mday;
        Calendar cal;

        if (timeStr == null ) {
            cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_MONTH,-1);
            time = cal.getTime();
        }
        else {
            cal = Calendar.getInstance();
            cal.setTime(parseTime( timeStr ));
            time = cal.getTime();
        }

        hour = cal.get(Calendar.HOUR_OF_DAY);
        wday = cal.get(Calendar.DAY_OF_WEEK);
        mday = cal.get(Calendar.DAY_OF_MONTH);
        
        Set assocTypes = new HashSet();
        assocTypes.add("BLS_TO");
        assocTypes.add("HAS_ONE");
        assocTypes.add("HAS_MANY");

        FetchBean bean = new FetchBean();
        bean.setOption("ASSOC_TYPES", assocTypes);
        bean.where("dflag=0");
        if (taskIds != null) {
            bean.where(" AND id IN (?)", taskIds);
        }
        else {
            String sql;
            sql  = "((exec_type = 2)";
            sql += " OR (exec_type = 3 AND exec_hour = ?)";
            sql += " OR (exec_type = 4 AND exec_hour = ? AND exec_wday = ?)";
            sql += " OR (exec_type = 5 AND exec_hour = ? AND exec_mday = ?))";
            bean.where(sql, hour, hour, wday, hour, mday);
        }

        Table table = DB.getInstance("hcrm").getTable("a_hcrm_dataset");
        List<Map<String, Object>> list = table.fetchMore(bean);

        // 构建线程池
        ThreadPoolExecutor pool = new ThreadPoolExecutor(
                0, process, 1, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(3),
                new ThreadPoolExecutor.DiscardOldestPolicy());

        // 执行任务
        for (Map<String, Object> info : list) {
            String clas = (String) info.get("class");
            String name = "m_hcrm_data_"+info.get("id");

            Map conf = new HashMap();
            List<Map> confList;
            confList = (List) info.get("a_hcrm_datasrc_conf");
            if (confList != null) for (Map confInfo : confList) {
                conf.put(confInfo.get("name"), confInfo.get("value"));
            }
            confList = (List) info.get("a_hcrm_dataset_conf");
            if (confList != null) for (Map confInfo : confList) {
                conf.put(confInfo.get("name"), confInfo.get("value"));
            }

            Map<String,String> cols = new HashMap();
            List<String> dims = new ArrayList();
            List<Map> colsList;
            colsList = (List) info.get("a_hcrm_dataset_cols");
            if (colsList != null) for (Map colsInfo : colsList) {
                cols.put("C"+colsInfo.get( "id" ).toString(),
                             colsInfo.get("name").toString());
                if ("1".equals(colsInfo.get("type"))) {
                dims.add("C"+colsInfo.get( "id" ).toString());
                }
            }

            pool.execute(new Task(time, clas, conf, name, cols, dims));
        }
        
        pool.shutdown();
        try {
            pool.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            throw new HongsException(0x1000, ex);
        }
    }

    private static class Task implements Runnable, Serializable {

        private Date    time;
        private String  clas;
        private Map     conf;
        private String  name;
        private Map <String,String> cols;
        private List<String>        dims;

        public Task(Date time, String clas, Map conf, String name,
                Map<String,String> cols, List<String> dims) {
            this.time = time;
            this.clas = clas;
            this.conf = conf;
            this.name = name;
            this.cols = cols;
            this.dims = dims;
        }

        @Override
        public void run() {
            Class  cls;
            Object obj;
            try {
                cls = Class.forName("app.hcrm.util.loader."+clas);
                obj = cls.newInstance();
            }
            catch (ClassNotFoundException |
                   InstantiationException |
                   IllegalAccessException ex) {
                throw new Error(ex);
            }
            if ( !( obj instanceof Loader ) ) {
                throw new Error("");
            }

            Writer writer = new JDBCWriter();
            writer.setName(name);
            writer.setCols(cols);
            writer.setDims(dims);

            Loader loader = (Loader) obj;
            loader.setTime(time);
            loader.setConf(conf);

            try {
                writer.open();
                loader.open();
                loader.load(writer);
                loader.close();
                writer.apply();
                writer.close();
            } catch (HongsException ex) {
                throw new Error(ex);
            }
        }

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
