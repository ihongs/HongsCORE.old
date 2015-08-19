package app.hongs.db;

import app.hongs.HongsException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 数据库查询结果
 * @author Hong
 */
public class FetchNext {
    private DB db;
    private ResultSet rs;
    private Statement ps;
    private Map<String, Class> fs = null;

    protected FetchNext(DB db, Statement ps, ResultSet rs) throws HongsException {
        this.db = db;
        this.ps = ps;
        this.rs = rs;
        this.fs = new LinkedHashMap();

        try {
            ResultSetMetaData md = rs.getMetaData( );
            for (int i = 1; i <= md.getColumnCount(); i ++) {
                fs.put(md.getColumnLabel(i), Class.forName(md.getColumnClassName(i)));
            }
        } catch (SQLException ex) {
            throw new HongsException(0x10a0, ex);
        } catch (ClassNotFoundException ex) {
            throw new HongsException(0x10a2, ex);
        }
    }

    public Statement getStatement() {
        return ps;
    }

    public ResultSet getReusltSet() {
        return rs;
    }

    public Map<String,Object> fetch() throws HongsException {
        try {
            if (rs.next()) {
                try
                {
                  int i = 0;
                  Map<String, Object> row = new LinkedHashMap( );
                  if (db.IN_OBJECT_MODE) {
                    for(Map.Entry<String, Class> et : fs.entrySet()) {
                        row.put(et.getKey(), rs.getObject(++ i, et.getValue()));
                    }
                  } else {
                    for(Map.Entry<String, Class> et : fs.entrySet()) {
                        row.put(et.getKey(), rs.getString(++ i));
                    }
                  }
                  return row;
                } catch (SQLException ex) {
                  throw new app.hongs.HongsException(0x10a4, ex);
                }
            }
            this.close();
        } catch (SQLException ex) {
            this.close();
            throw new app.hongs.HongsException(0x10a6, ex);
        }
        return null;
    }

    public void close() throws HongsException {
        if (fs == null) return;
        db.closeResultSet(rs);
        db.closeStatement(ps);
        fs = null;
    }
}
