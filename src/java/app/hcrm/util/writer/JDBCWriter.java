package app.hcrm.util.writer;

import app.hcrm.util.Writer;
import app.hongs.HongsException;
import app.hongs.db.DB;
import app.hongs.db.Table;
import java.util.Map;

/**
 * 数据库写入器
 * @author Hong
 */
public class JDBCWriter implements Writer {

    String name;
    String flag;
    int pos;
    int len;
    DB db;
    
    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setFlag(String flag) {
        this.flag = flag;
    }

    @Override
    public int getPos() {
        return this.pos;
    }

    @Override
    public int getLen() {
        return this.len;
    }

    @Override
    public void open() throws HongsException {
        db = (DB)DB.getInstance("hcrm");
    }

    @Override
    public void clean() {
        
    }

    @Override
    public void write(Map data) {
        
    }

    @Override
    public void close() {
        // It wiil auto close on application exit.
    }

    @Override
    public void setCols(Map cols) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
