package app.hcim.model;

import app.hongs.HongsException;
import app.hongs.db.AbstractBaseModel;
import app.hongs.db.FetchMore;
import java.util.List;
import java.util.Map;

/**
 * 通用信息模型处理类
 * @author Hongs
 */
public class Demo extends AbstractBaseModel {

    public Demo() throws HongsException {
        super("hcim", "a_demo");
    }

    public String getAffectedNames() throws HongsException {
        StringBuilder sb = new StringBuilder();
        FetchMore     fm = new FetchMore();
        fm.setOption("FETCH_DFLAG", true );
        fm.select("product").where("id IN (?)", this.affectedIds);
        List<Map> rows = this.table.fetchMore(fm);
        for (Map  row  : rows) {
            sb.append(",").append(row.get("product").toString( ));
        }
        return sb.length()>0 ? sb.substring(1) : sb.toString();
    }

}
