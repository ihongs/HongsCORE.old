package app.hcrm.util.loader;

import app.hcrm.util.LoadUtil;
import app.hcrm.util.Loader;
import app.hcrm.util.Writer;
import app.hongs.HongsException;
import app.hongs.db.DB;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * JDBC加载器
 * @author Hong
 */
public class JDBCLoader implements Loader {

    private DB db;
    private Map conf;
    private Date time;

    @Override
    public void setConf(Map conf) {
        this.conf = conf;
    }

    @Override
    public void setTime(Date time) {
        this.time = time;
    }

    @Override
    public void open() throws HongsException {
        String drv = conf.get("drv").toString();
        String url = conf.get("url").toString();
        String username = conf.get("username").toString();
        String password = conf.get("password").toString();
        Properties prop = new Properties();
        if (null != username && !"".equals(username)) {
            prop.put("username", username);
        }
        if (null != password && !"".equals(password)) {
            prop.put("password", password);
        }

        Map config = new HashMap();
        Map driver = new HashMap();
        driver.put("drv", drv);
        driver.put("url", url);
        driver.put("info", "prop");
        config.put("driver", driver);
        db = new DB(config);
    }

    @Override
    public void load(Writer writer) throws HongsException {
        String sql = (String) conf.get("sql");
        if (sql == null || sql.length() == 0) {
            sql = "SELECT * FROM `"+conf.get("tablename")
            +"` WHERE "+conf.get("condition");
        }
        sql = LoadUtil.injectParams(sql,time);

        Map row;
        ResultSet rs = db.query(sql);
        while ( (row = db.fetch(rs)) != null) {
            writer.write(row);
        }
    }

    @Override
    public void close() throws HongsException {
        db.close();
    }

}
