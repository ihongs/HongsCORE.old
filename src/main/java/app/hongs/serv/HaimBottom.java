package app.hongs.serv;

import app.hongs.HongsException;
import app.hongs.db.DB;
import app.hongs.db.FetchCase;
import app.hongs.db.Model;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 自动打底模型
 * @author Hongs
 */
public class HaimBottom extends Model {
    public HaimBottom(String dbName, String tableName) throws HongsException {
        super(DB.getInstance(dbName).getTable(tableName));
    }

    public Map<String, String> getFieldLabels() throws HongsException {
        this.db.prepareStatement("SELECT * FROM `"+this.table.tableName+"`");
        return null;
    }
}
