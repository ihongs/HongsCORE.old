package app.hongs.db;

import app.hongs.HongsException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

/**
 * 数据库查询结果
 * @author Hong
 */
public class DBFetch {
    private DB db;
    private ResultSet rs;
    private Statement ps;
    private String[ ] ls;

    protected DBFetch(DB db, Statement ps, ResultSet rs) {
        this.db = db;
        this.ps = ps;
        this.rs = rs;
    }

    public ResultSet getReusltSet() {
        return rs;
    }

    public Statement getStatement() {
        return ps;
    }

    public String[ ] getLabels() throws HongsException {
        if (this.ls == null) {
            this.ls = db.getColLabels(rs);
        }
        return ls;
    }

    public Map<String,Object> fetch() throws HongsException {
        try {
            if (rs.next()) {
                return db.getRowValues(rs, this.getLabels());
            }
            this.close();
        } catch (SQLException ex) {
            this.close();
            throw new app.hongs.HongsException( 0x1042, ex );
        }
        return null;
    }

    public void close() throws HongsException {
        db.closeResultSet(rs);
        db.closeStatement(ps);
        ls = null;
    }
}
