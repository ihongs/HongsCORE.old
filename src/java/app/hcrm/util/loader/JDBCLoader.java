package app.hcrm.util.loader;

import app.hcrm.util.LoadUtil;
import app.hcrm.util.Loader;
import app.hcrm.util.Writer;
import app.hongs.HongsException;
import app.hongs.db.DB;
import app.hongs.db.DBFetch;
import java.util.Date;
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
        String drv = (String) conf.get("drv");
        String url = (String) conf.get("url");
        String user = (String) conf.get("user");
        String password = (String) conf.get("password");
        Properties prop = new Properties();
        if (null != user && !"".equals(user)) {
            prop.put("user", user);
        }
        if (null != password && !"".equals(password)) {
            prop.put("password", password);
        }

        db = DB.getInstanceByDriver(drv, url, prop);
    }

    @Override
    public void load(Writer writer) throws HongsException {
        String type = (String) conf.get("type");
        String sql  = (String) conf.get("sql" );
        if ("Table".equals(type)) {
            sql = "SELECT * FROM `"+conf.get("tablename")
              +"` WHERE "+conf.get("condition");
            
        }
        sql = LoadUtil.injectParams(sql , time);

        Map row;
        DBFetch rs = db.query(sql);
        while ( (row = rs.fetch( )) != null ) {
            writer.write(row);
        }
    }

    @Override
    public void close() throws HongsException {
        db.close();
    }

}
