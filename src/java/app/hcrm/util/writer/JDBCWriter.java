package app.hcrm.util.writer;

import app.hcrm.util.Writer;
import app.hongs.Core;
import app.hongs.HongsException;
import app.hongs.db.DB;
import app.hongs.db.Table;
import app.hongs.db.sync.TableSync;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 数据库写入器
 * @author Hong
 */
public class JDBCWriter implements Writer {

    private Map <String, String> cols;
    private List<String> dims;
    private Set <String> keys;
    private String name;

    private Table realTable;
    private Table tempTable;

    @Override
    public void setCols(Map<String, String> cols) {
        this.cols = cols;
    }

    @Override
    public void setDims(List<String> dims) {
        this.dims = dims;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public Set<String> getKeys() {
        return this.keys;
    }

    @Override
    public void open() throws HongsException {
        DB db = DB.getInstance("hcrm_base");
        realTable = new Table(db, name);
        tempTable = new Table(db, name+"_"+Core.getUniqueId());
        (new TableSync(realTable)).syncSlaver(tempTable, true); // 同步临时表结构
    }

    @Override
    public void write(Map data) throws HongsException {
        Map  info = new HashMap();
        for (Map.Entry et : cols.entrySet()) {
            String dstCol = et.getKey  ().toString();
            String srcCol = et.getValue().toString();
            info.put(dstCol, data.get(srcCol));
        }
        tempTable.insert(info);

        StringBuilder sb = new StringBuilder();
        for (String dim : dims) {
            sb.append(new byte[] {0x01})
              .append( info.get( dim ) );
        }
        keys.add(sb.substring(1));
    }

    @Override
    public void apply() throws HongsException {
        /***
         * 先按照keys清理掉旧的数据
         * 然后再从临时表把数据插入真实表
         * 最后删除掉临时表, 就完成了写入
         */

        StringBuilder sb = new StringBuilder();
        for (String dim : dims) {
            sb.append( ",'")
              .append(new byte[] {0x01})
              .append("',`")
              .append( dim )
              .append(  "`");
        }
        realTable.delete("CONCAT("+sb.substring(5)+") IN (?)", keys);

        String sql;

        sql = "INSERT INTO `"+realTable.tableName+"` SELECT * FROM `"+realTable.tableName+"`";
        this.realTable.db.execute(sql);

        sql = "DROP TABLE `"+tempTable.tableName+"`";
        this.tempTable.db.execute(sql);
    }

    @Override
    public void close() throws HongsException {
        // Nothing todo
    }
}
