package app.hongs.db.sync;

import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.Iterator;

import app.hongs.HongsException;
import app.hongs.db.Table;

/**
 * <h1>表结构同步器(Table structure synchronizer)</h1>
 *
 * <b>注意: 参考MySQL编写, 可能不适用于其他数据库(使用了SHOW语句)</b>
 *
 * @author Hongs
 */
public class TableSync
{

  private Table table;
  private TableDesc td;
  private Table slaver;
  private TableDesc sd;
  private String field;

  /**
   * 通过表对象构造
   * @param table
   * @throws app.hongs.HongsException
   */
  public TableSync(Table table)
  throws HongsException
  {
    this.table = table;
    this.td = TableDesc.getInstance(table);
    this.field = this.td.fields.keySet().toArray()[0].toString();
  }

  /**
   * 同步从表结构
   * @param slaver
   * @param delExtraFields 删除多余的字段
   * @throws app.hongs.HongsException
   */
  public void syncSlaver(Table slaver, boolean delExtraFields)
  throws HongsException
  {
    // 没有表则创建表
    String sql = "SHOW TABLES LIKE '"+slaver.tableName+"'" ;
    Map row = slaver.db.fetchOne(sql);
    if (row.isEmpty())
    {
        sql = "SHOW CRAETE TABLE `"+table.tableName+"`";
        sql = ((Map) table.db.fetchAll(sql).get( 0 ))
                    .get("Create Table").toString( );
        sql = sql.replaceFirst("^CREATE TABLE `.*?`",
              "CREATE TABLE `"+slaver.tableName+"`");

        slaver.db.execute(sql);

        return;
    }

    // 注册从表及结构
    this.slaver = slaver;
    this.sd = TableDesc.getInstance(slaver);

    Iterator  it;
    Map.Entry et;

    /**
     * 第一步:
     * 根据子表对比主表结构
     * 找出缺失或改变了的键并删除
     * 找出缺失的字段并删除
     */

    if (delExtraFields)
    {
      // 主键
      if (!this.sd.primaryKey.isEmpty()
      &&  !this.sd.primaryKey.equals(this.td.primaryKey))
      {
        this.alterKey(TableDesc.DROP, TableDesc.PRIMARY);
      }

      // 唯一键
      it = this.sd.uniqueKeys.entrySet().iterator();
      while (it.hasNext())
      {
        et = (Map.Entry)it.next();
        String key = (String)et.getKey();
        Set keys = (Set)et.getValue();

        if (!this.td.uniqueKeys.containsKey(key)
        ||  !this.td.uniqueKeys.get(key).equals(keys))
        {
          this.alterKey(TableDesc.DROP, key);
        }
      }

      // 索引键
      it = this.sd.indexKeys.entrySet().iterator();
      while (it.hasNext())
      {
        et = (Map.Entry)it.next();
        String key = (String)et.getKey();
        Set keys = (Set)et.getValue();

        if (!this.td.indexKeys.containsKey(key)
        ||  !this.td.indexKeys.get(key).equals(keys))
        {
          this.alterKey(TableDesc.DROP, key);
        }
      }

      // 字段
      it = this.sd.fields.entrySet().iterator();
      while (it.hasNext())
      {
        et = (Map.Entry)it.next();
        String fieldName = (String)et.getKey();

        if (!this.td.fields.containsKey(fieldName))
        {
          this.alterField(TableDesc.DROP, fieldName);
        }
      }
    }

    /**
     * 第二步:
     * 根据主表对比子表结构
     * 找出新增的字段并添加
     * 找出不同的字段并更新
     */

    it = this.td.fields.entrySet().iterator();
    while (it.hasNext())
    {
      et = (Map.Entry)it.next();
      String fieldName = (String)et.getKey();
      String fieldSql = (String)et.getValue();

      if (! this.sd.fields.containsKey(fieldName))
      {
        this.alterField(TableDesc.ADD, fieldName);
      }
      else
      {
        String fieldSql2 = (String)this.sd.fields.get(fieldName);

        if (! fieldSql2.equals(fieldSql))
        {
          this.alterField(TableDesc.CHANGE, fieldName);
        }
      }
    }

    /**
     * 第三步:
     * 根据主表对比
     * 找出新增的键并添加
     */

    // 主键
    if (!this.td.primaryKey.isEmpty()
    &&  !this.td.primaryKey.equals(this.sd.primaryKey))
    {
      this.alterKey(TableDesc.ADD , TableDesc.PRIMARY);
    }

    // 唯一键
    it = this.td.uniqueKeys.entrySet().iterator();
    while (it.hasNext())
    {
      et = (Map.Entry)it.next();
      String key = (String)et.getKey();
      Set keys = (Set)et.getValue();

      if (!this.sd.uniqueKeys.containsKey(key)
      ||  !this.sd.uniqueKeys.get(key).equals(keys))
      {
        this.alterKey(TableDesc.ADD, key);
      }
    }

    // 索引键
    it = this.td.indexKeys.entrySet().iterator();
    while (it.hasNext())
    {
      et = (Map.Entry)it.next();
      String key = (String)et.getKey();
      Set keys = (Set)et.getValue();

      if (!this.sd.indexKeys.containsKey(key)
      ||  !this.sd.indexKeys.get(key).equals(keys))
      {
        this.alterKey(TableDesc.ADD, key);
      }
    }
  }

  /**
   * 同步从表结构(多个)
   * @param slavers
   * @param delExtraFields 删除多余的字段
   * @throws app.hongs.HongsException
   */
  public void syncSlavers(List<Table> slavers, boolean delExtraFields)
  throws HongsException
  {
    Iterator it = slavers.iterator();
    while (it.hasNext())
    {
      Table slave = (Table)it.next();
      this.syncSlaver(slave, delExtraFields);
    }
  }

  private void alterField(int alterType, String fieldName)
  throws HongsException
  {
    TableDesc.alterField(alterType, this.slaver, this.table, fieldName);
  }

  private void alterKey(int alterType, String keyName)
  throws HongsException
  {
    TableDesc.alterKey(alterType, this.slaver, this.table, keyName);
  }

}
