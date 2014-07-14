package app.hongs.db.sync;

import app.hongs.HongsException;
import app.hongs.db.DB;
import app.hongs.db.Table;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

/**
 * 库结构同步器(DB structure synchronizer)
 *
 * <b>注意: 参考MySQL编写, 可能不适用于其他数据库</b>
 *
 * @author Hongs
 */
public class DBSync
{

  private DB db;

  /**
   * 通过库对象构造
   * @param db
   * @throws app.hongs.HongsException
   */
  public DBSync(DB db)
  throws HongsException
  {
    this.db = db;
  }

  /**
   * 同步从数据库(多个)
   * @param slavers
   * @param tablePrefix 从库表前缀
   * @param tableSuffix 从库表后缀
   * @param delExtraTables 删除多余的表
   * @param delExtraFields 删除多余的字段
   * @throws app.hongs.HongsException
   */
  public void syncSlavers(List<DB> slavers, String tablePrefix, String tableSuffix, boolean delExtraTables, boolean delExtraFields)
  throws HongsException
  {
    Iterator it = slavers.iterator();
    while (it.hasNext())
    {
      DB slaver = (DB)it.next();
      this.syncSlaver(slaver, tablePrefix, tableSuffix, delExtraTables, delExtraFields);
    }
  }

  /**
   * 同步从数据库
   * @param slaver
   * @param tablePrefix 从库表前缀
   * @param tableSuffix 从库表后缀
   * @param delExtraTables 删除多余的表
   * @param delExtraFields 删除多余的字段
   * @throws app.hongs.HongsException
   */
  public void syncSlaver(DB slaver, String tablePrefix, String tableSuffix, boolean delExtraTables, boolean delExtraFields)
  throws HongsException
  {
    Set tables = new HashSet();

    if (delExtraTables)
    {
      List rows = slaver.fetchAll("SHOW TABLES");
      Iterator it = rows.iterator();
      while (it.hasNext())
      {
        Map.Entry et = (Map.Entry)((Map)it.next()).entrySet().iterator().next();
        String table = (String)et.getValue();
        tables.add(table);
      }
    }

    Set <String> tns = this.db.getTableNames();
    for (String  tab : tns)
    {
      Table table = this.db.getTable(tab);

      Map config = new HashMap();
      config.put("name", table.tableName);
      Table table2 = new Table(slaver, config);

      TableSync sst = new TableSync( table );
      sst.syncSlaver(table2, delExtraFields);

      if (delExtraTables)
      {
        tables.remove(table2.tableName);
      }
    }

    if (delExtraTables)
    {
      Iterator it2 = tables.iterator();
      while (it2.hasNext())
      {
        String table = (String)it2.next();
        slaver.execute("DROP TABLE '"+table+"'");
      }
    }
  }

}
