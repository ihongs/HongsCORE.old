package app.hcrm.util;

import app.hongs.HongsException;
import app.hongs.db.AbstractBaseModel;
import app.hongs.db.FetchBean;
import java.util.List;
import java.util.Map;

/**
 * 通用报表模块抽象类
 * @author Hong
 */
public abstract class AbstractModel
extends AbstractBaseModel {

    public AbstractModel(String dbName, String tableName)
    throws HongsException {
        super(dbName, tableName);
    }

    public String getAffectedNames() throws HongsException {
        StringBuilder sb = new StringBuilder();
        FetchBean     fs = new FetchBean();
        fs.setOption("FETCH_DFLAG", true );
        fs.select("name").where("id IN (?)", this.affectedIds);
        List<Map> rows = this.table.fetchMore(fs);
        for (Map  row  : rows) {
            sb.append(",").append(row.get("name").toString( ));
        }
        return sb.length()>0 ? sb.substring(1) : sb.toString();
    }

}
